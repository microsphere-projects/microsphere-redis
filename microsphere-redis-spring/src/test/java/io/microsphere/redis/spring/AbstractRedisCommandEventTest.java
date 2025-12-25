/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.redis.spring;

import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_INTERFACE_NAME;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.System.nanoTime;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Abstract {@link RedisCommandEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Disabled
public abstract class AbstractRedisCommandEventTest extends AbstractRedisTest {

    private Map<Object, Object> data = new HashMap<>();

    @Autowired
    protected RedisContext redisContext;

    protected RedisSerializer keySerializer;

    protected RedisSerializer valueSerializer;

    @BeforeEach
    void setUp() {
        this.redisTemplate.setKeySerializer(STRING_SERIALIZER);
        this.redisTemplate.setValueSerializer(STRING_SERIALIZER);
        this.keySerializer = this.stringRedisTemplate.getKeySerializer();
        this.valueSerializer = this.stringRedisTemplate.getValueSerializer();
    }

    @Test
    void test() {

        context.addApplicationListener((ApplicationListener<RedisCommandEvent>) this::onRedisCommandEvent);

        stringRedisTemplate.execute(c -> {
            assertEquals(c, c);
            assertNotEquals(c, this);
            assertEquals(c.hashCode(), c.hashCode());

            assertThrows(RuntimeException.class, () -> {
                c.execute("Not-Found-Command");
            });

            return c;
        }, true);

        String suffix = "-" + nanoTime();

        String key1 = "Key-1" + suffix;
        String value1 = "Value-1";
        assertSet(this.redisTemplate, key1, value1);
        if (!data.isEmpty()) { // Enable RedisCommandEvent
            assertEquals(value1, data.get(key1));
        }

        String key2 = "Key-2" + suffix;
        String value2 = "Value-2";
        assertSet(this.stringRedisTemplate, key2, value2);
        if (!data.isEmpty()) {
            assertEquals(value2, data.get(key2));
        }

        String key3 = "Key-3" + suffix;
        String value3 = "Value-3";
        this.stringRedisTemplate.execute(c -> {
            c.execute("SET", this.keySerializer.serialize(key3), this.valueSerializer.serialize(value3));
            return c;
        }, true);

        if (!data.isEmpty()) {
            assertEquals(value3, data.get(key3));
        }
    }

    void onRedisCommandEvent(RedisCommandEvent event) {
        String interfaceName = event.getInterfaceName();
        String methodName = event.getMethodName();
        Object[] args = event.getArgs();
        int parameterCount = event.getParameterCount();

        // assert interface name
        assertNotNull(interfaceName);

        // assert source application
        assertEquals(DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE, event.getApplicationName());

        byte[] keyBytes;
        byte[] valueBytes;

        if (REDIS_COMMANDS_INTERFACE_NAME.equalsIgnoreCase(interfaceName)) {
            // assert method name
            assertEquals("execute", methodName);

            // assert command
            assertEquals("SET", args[0]);

            byte[][] bytesArray = (byte[][]) args[1];
            keyBytes = bytesArray[0];
            valueBytes = bytesArray[1];

            // assert parameter count
            assertEquals(2, parameterCount);
        } else {

            keyBytes = (byte[]) event.getArg(0);
            valueBytes = (byte[]) event.getArg(1);

            // assert method name
            assertEquals("set", methodName);

            // assert parameters
            assertArrayEquals(ofArray(keyBytes, valueBytes), args);

            // assert parameter count
            assertEquals(2, parameterCount);

            // assert parameter types
            assertArrayEquals(new Class[]{byte[].class, byte[].class}, event.getParameterTypes());
        }

        Object key = this.keySerializer.deserialize(keyBytes);
        Object value = this.valueSerializer.deserialize(valueBytes);

        this.data.put(key, value);
    }

    void assertSet(RedisTemplate redisTemplate, String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
        assertEquals(value, valueOperations.get(key));
    }
}
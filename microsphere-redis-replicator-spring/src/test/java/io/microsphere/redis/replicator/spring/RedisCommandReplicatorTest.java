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

package io.microsphere.redis.replicator.spring;


import io.microsphere.redis.replicator.spring.config.DefaultRedisConfig;
import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;

import static io.microsphere.redis.replicator.spring.RedisCommandReplicator.BEAN_NAME;
import static io.microsphere.redis.spring.event.RedisCommandEvent.Builder.source;
import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link RedisCommandReplicator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandReplicator
 * @since 1.0.0
 */
@SpringJUnitConfig(classes = {
        DefaultRedisConfig.class,
        RedisCommandReplicatorTest.class
})
class RedisCommandReplicatorTest {

    private static final Method SET_METHOD = findMethod(RedisConnection.class, "set", byte[].class, byte[].class);

    @Bean(BEAN_NAME)
    public RedisCommandReplicator redisCommandReplicator(RedisConnectionFactory redisConnectionFactory) {
        return new RedisCommandReplicator(redisConnectionFactory);
    }

    private String key;

    private String value;

    private byte[] keyBytes;

    private byte[] valueBytes;

    private RedisCommandEvent.Builder builder;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private RedisCommandReplicator redisCommandReplicator;

    @BeforeEach
    void setUp() {
        long time = currentTimeMillis();
        this.key = "test-key-" + time;
        this.value = "test-value-" + time;
        this.keyBytes = STRING_SERIALIZER.serialize(key);
        this.valueBytes = STRING_SERIALIZER.serialize(value);
        this.builder = source(this)
                .applicationName("test-service")
                .sourceBeanName("redisTemplate")
                .method(SET_METHOD);
    }

    @Test
    void testApplicationEvent() {
        testApplicationEvent(true);
    }

    @Test
    void testApplicationEventOnFailed() {
        testApplicationEvent(false);
    }

    void testApplicationEvent(boolean setValue) {
        RedisCommandEvent redisCommandEvent = setValue ? builder
                .args(keyBytes, valueBytes)
                .build() : builder.build();

        context.publishEvent(new RedisCommandReplicatedEvent(redisCommandEvent, "default"));

        byte[] valueBytesFromCache = getValueAsBytes();
        if (setValue) {
            assertArrayEquals(valueBytes, valueBytesFromCache);
        } else {
            assertNull(valueBytesFromCache);
        }
    }

    byte[] getValueAsBytes() {
        RedisConnection redisConnection = this.redisCommandReplicator.getRedisConnection();
        return redisConnection.get(keyBytes);
    }
}
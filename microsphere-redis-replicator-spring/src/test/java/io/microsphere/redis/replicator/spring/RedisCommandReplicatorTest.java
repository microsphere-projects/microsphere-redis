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
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.lang.reflect.Method;

import static io.microsphere.redis.replicator.spring.RedisCommandReplicator.BEAN_NAME;
import static io.microsphere.redis.spring.event.RedisCommandEvent.Builder.source;
import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
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
class RedisCommandReplicatorTest extends DefaultRedisConfig {

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
        testInSpringContainer(context -> {
            RedisCommandEvent redisCommandEvent = builder
                    .args(keyBytes, valueBytes)
                    .build();

            context.publishEvent(new RedisCommandReplicatedEvent(redisCommandEvent, "default"));

            byte[] valueBytesFromCache = getValueAsBytes(context);
            assertArrayEquals(valueBytes, valueBytesFromCache);

        }, RedisCommandReplicatorTest.class);
    }

    @Test
    void testApplicationEventOnFailed() {
        testInSpringContainer(context -> {
            RedisCommandEvent redisCommandEvent = builder.build();

            context.publishEvent(new RedisCommandReplicatedEvent(redisCommandEvent, "default"));

            byte[] valueBytesFromCache = getValueAsBytes(context);
            assertNull(valueBytesFromCache);

        }, RedisCommandReplicatorTest.class);
    }

    byte[] getValueAsBytes(ConfigurableApplicationContext context) {
        RedisCommandReplicator redisCommandReplicator = context.getBean(RedisCommandReplicator.class);
        RedisConnection redisConnection = redisCommandReplicator.getRedisConnection();
        return redisConnection.get(keyBytes);
    }
}
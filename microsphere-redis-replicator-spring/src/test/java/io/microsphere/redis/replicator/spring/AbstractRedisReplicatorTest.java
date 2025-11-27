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

import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract Redis Replicator Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@DirtiesContext
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=127.0.0.1:9092"
})
@Disabled
public abstract class AbstractRedisReplicatorTest {

    @Autowired
    protected ConfigurableApplicationContext context;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Test
    public void test() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Map<Object, Object> data = new HashMap<>();

        context.addApplicationListener((ApplicationListener<RedisCommandReplicatedEvent>) event -> {
            RedisCommandEvent redisCommandEvent = event.getSourceEvent();

            RedisSerializer keySerializer = stringRedisTemplate.getKeySerializer();
            RedisSerializer valueSerializer = stringRedisTemplate.getValueSerializer();
            Object key = keySerializer.deserialize((byte[]) redisCommandEvent.getArg(0));
            Object value = valueSerializer.deserialize((byte[]) redisCommandEvent.getArg(1));
            data.put(key, value);

            assertEquals("org.springframework.data.redis.connection.RedisStringCommands", redisCommandEvent.getInterfaceName());
            assertEquals("set", redisCommandEvent.getMethodName());
            assertArrayEquals(ofArray(byte[].class, byte[].class), redisCommandEvent.getParameterTypes());
            assertEquals("default", redisCommandEvent.getApplicationName());
            latch.countDown();
        });

        stringRedisTemplate.opsForValue().set("Key-1", "Value-1");

        if (latch.await(10, SECONDS)) {
            assertEquals("Value-1", data.get("Key-1"));
        }
    }

    @Bean
    public static RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public static StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
        stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringRedisTemplate;
    }

    @Bean
    public static RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory("127.0.0.1", 6379);
        redisConnectionFactory.afterPropertiesSet();
        redisConnectionFactory.validateConnection();
        return redisConnectionFactory;
    }
}
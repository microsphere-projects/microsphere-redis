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

package io.microsphere.redis.spring.context;


import io.microsphere.redis.spring.config.RedisConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;
import static io.microsphere.redis.spring.context.RedisContext.get;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RedisContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisContext
 * @since 1.0.0
 */
class RedisContextTest {

    @Bean(BEAN_NAME)
    public RedisContext redisContext() {
        return new RedisContext();
    }

    @Bean(RedisConfiguration.BEAN_NAME)
    public RedisConfiguration redisConfiguration() {
        return new RedisConfiguration();
    }

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory("127.0.0.1", 6379);
        redisConnectionFactory.afterPropertiesSet();
        redisConnectionFactory.validateConnection();
        return redisConnectionFactory;
    }

    @Test
    void test() {
        testInSpringContainer((context, environment) -> {
            RedisContext redisContext = get(context);
            RedisConfiguration redisConfiguration = redisContext.getRedisConfiguration();
            assertSame(RedisConfiguration.get(context), redisConfiguration);
            assertSame(context.getBeanFactory(), redisContext.getBeanFactory());
            assertSame(context, redisContext.getApplicationContext());
            assertSame(context.getClassLoader(), redisContext.getClassLoader());
            assertSame(environment, redisContext.getEnvironment());
            assertFalse(redisContext.isEnabled());
            assertNotNull(redisContext.getRedisTemplate("redisTemplate"));
            assertNull(redisContext.getRedisTemplate("not-found"));
            assertEquals(ofSet("redisTemplate"), redisContext.getRedisTemplateBeanNames());
            assertEquals(ofSet("redisConnectionFactory"), redisContext.getRedisConnectionFactoryBeanNames());
            assertEquals(redisConfiguration.isCommandEventExposed(), redisContext.isCommandEventExposed());
            assertEquals(redisConfiguration.getApplicationName(), redisContext.getApplicationName());
            assertEquals(emptyList(), redisContext.getRedisConnectionInterceptors());
            assertEquals(emptyList(), redisContext.getRedisCommandInterceptors());
        }, RedisContextTest.class);
    }
}
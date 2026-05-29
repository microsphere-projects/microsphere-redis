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
package io.microsphere.redis.spring.annotation;

import io.microsphere.redis.spring.AbstractRedisTest;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.context.RedisContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.util.HashSet;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnableRedisContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ContextConfiguration(classes = EnableRedisContextTest.class)
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true"
})
@EnableRedisContext
class EnableRedisContextTest extends AbstractRedisTest {

    @Autowired
    private RedisContext redisContext;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private RedisConfiguration redisConfiguration;

    @Autowired
    @Qualifier("redisTemplate")
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    @Qualifier("stringRedisTemplate")
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void test() throws Throwable {
        assertSame(beanFactory, redisContext.getBeanFactory());
        assertSame(context, redisContext.getApplicationContext());
        assertSame(redisConfiguration, redisContext.getRedisConfiguration());
        assertSame(environment, redisConfiguration.getEnvironment());
        assertSame(redisTemplate, redisContext.getRedisTemplate("redisTemplate"));
        assertSame(stringRedisTemplate, redisContext.getRedisTemplate("stringRedisTemplate"));

        assertEquals("application", redisConfiguration.getApplicationName());
        assertEquals(new HashSet<>(asList("redisTemplate", "stringRedisTemplate")), redisContext.getRedisTemplateBeanNames());
        assertTrue(redisContext.isEnabled());
        assertTrue(redisContext.isCommandEventExposed());
    }
}

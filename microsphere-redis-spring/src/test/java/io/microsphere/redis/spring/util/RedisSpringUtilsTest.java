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

package io.microsphere.redis.spring.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getApplicationName;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getWrappedRedisTemplateBeanNames;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisCommandEventExposed;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisEnabled;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * {@link RedisSpringUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisSpringUtils
 * @since 1.0.0
 */
class RedisSpringUtilsTest {

    private static final String[] REDIS_TEMPLATE_BEAN_NAMES = ofArray(
            "redisTemplate",
            "stringRedisTemplate",
            " redisTemplate",
            "stringRedisTemplate ",
            " redisTemplate , stringRedisTemplate ",
            " ",
            ""
    );

    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        this.environment = new MockEnvironment();
    }

    @Test
    void testGetApplicationName() {
        String applicationName = getApplicationName(this.environment);
        assertEquals("application", applicationName);

        this.environment.setProperty("spring.application.name", "test");
        applicationName = getApplicationName(this.environment);
        assertEquals("test", applicationName);
    }

    @Test
    void testIsMicrosphereRedisEnabled() {
        assertFalse(isMicrosphereRedisEnabled(this.environment));

        this.environment.setProperty("microsphere.redis.enabled", "true");
        assertTrue(isMicrosphereRedisEnabled(this.environment));
    }

    @Test
    void testIsMicrosphereRedisCommandEventExposed() {
        assertTrue(isMicrosphereRedisCommandEventExposed(this.environment));

        this.environment.setProperty("microsphere.redis.command-event.exposed", "false");
        assertFalse(isMicrosphereRedisCommandEventExposed(this.environment));
    }

    @Test
    void testGetWrappedRedisTemplateBeanNames() {
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        Set<String> beanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment);
        assertSame(emptySet(), beanNames);

        this.environment.setProperty("microsphere.redis.wrapped-redis-templates", "*");
        beanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment);
        assertEquals(emptySet(), beanNames);

        this.environment.setProperty("microsphere.redis.wrapped-redis-templates", arrayToCommaDelimitedString(REDIS_TEMPLATE_BEAN_NAMES));
        beanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment);
        assertEquals(ofSet("redisTemplate", "stringRedisTemplate"), beanNames);
    }
}
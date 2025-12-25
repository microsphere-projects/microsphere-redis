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


import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.config.RedisConfig;
import io.microsphere.redis.spring.config.RedisContextConfig;
import io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.LoggingRedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import io.microsphere.redis.spring.interceptor.StopWatchRedisConnectionInterceptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisCommandInterceptors;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisConnectionFactoryBeanNames;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisConnectionInterceptors;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisTemplate;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisTemplateBeanNames;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getApplicationName;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getRawRedisConnection;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getWrappedRedisTemplateBeanNames;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisCommandEventExposed;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisEnabled;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
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
@PropertySource(
        value = "classpath:/META-INF/test-redis.properties"
)
@EnableRedisInterceptor
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

        beanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment, "redisTemplate", "stringRedisTemplate");
        assertEquals(ofSet("redisTemplate", "stringRedisTemplate"), beanNames);

        beanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment, REDIS_TEMPLATE_BEAN_NAMES);
        assertEquals(ofSet("redisTemplate", "stringRedisTemplate"), beanNames);
    }

    @Test
    void testFindRedisTemplate() {
        testInSpringContainer(context -> {
            assertNotNull(findRedisTemplate(context, "redisTemplate"));
            assertNotNull(findRedisTemplate(context, "stringRedisTemplate"));
            assertNull(findRedisTemplate(context, "notFoundRedisTemplate"));
        }, RedisConfig.class);
    }

    @Test
    void testFindRedisTemplateBeanNames() {
        testInSpringContainer(context -> {
            Set<String> redisTemplateBeanNames = findRedisTemplateBeanNames(context.getBeanFactory());
            assertEquals(emptySet(), redisTemplateBeanNames);
        });

        testInSpringContainer(context -> {
            Set<String> redisTemplateBeanNames = findRedisTemplateBeanNames(context.getBeanFactory());
            assertEquals(ofSet("redisTemplate", "stringRedisTemplate"), redisTemplateBeanNames);
        }, RedisConfig.class);
    }

    @Test
    void testFindRedisConnectionFactoryBeanNames() {
        testInSpringContainer(context -> {
            Set<String> redisConnectionFactoryBeanNames = findRedisConnectionFactoryBeanNames(context.getBeanFactory());
            assertEquals(emptySet(), redisConnectionFactoryBeanNames);
        });

        testInSpringContainer(context -> {
            Set<String> redisConnectionFactoryBeanNames = findRedisConnectionFactoryBeanNames(context.getBeanFactory());
            assertEquals(ofSet("redisConnectionFactory"), redisConnectionFactoryBeanNames);
        }, RedisConfig.class);
    }

    @Test
    void testFindRedisCommandInterceptors() {
        testInSpringContainer(context -> {
            List<RedisCommandInterceptor> redisCommandInterceptors = findRedisCommandInterceptors(context);
            assertEquals(0, redisCommandInterceptors.size());
        });

        testInSpringContainer(context -> {
            List<RedisCommandInterceptor> redisCommandInterceptors = findRedisCommandInterceptors(context);
            assertEquals(2, redisCommandInterceptors.size());
        }, RedisContextConfig.class, EventPublishingRedisCommandInterceptor.class, LoggingRedisCommandInterceptor.class);
    }

    @Test
    void testFindRedisConnectionInterceptors() {
        testInSpringContainer(context -> {
            List<RedisConnectionInterceptor> redisConnectionInterceptors = findRedisConnectionInterceptors(context);
            assertEquals(0, redisConnectionInterceptors.size());
        });

        testInSpringContainer(context -> {
            List<RedisConnectionInterceptor> redisConnectionInterceptors = findRedisConnectionInterceptors(context);
            assertEquals(1, redisConnectionInterceptors.size());
        }, StopWatchRedisConnectionInterceptor.class);
    }

    @Test
    void testGetRawRedisConnection() {
        testInSpringContainer(context -> {
            RedisConnectionFactory redisConnectionFactory = context.getBean(RedisConnectionFactory.class);
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            RedisConnection rawRedisConnection = getRawRedisConnection(redisConnection);
            assertSame(redisConnection, rawRedisConnection);
        }, RedisConfig.class);

        testInSpringContainer(context -> {
            RedisConnectionFactory redisConnectionFactory = context.getBean(RedisConnectionFactory.class);
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            RedisConnection rawRedisConnection = getRawRedisConnection(redisConnection);
            assertNotSame(redisConnection, rawRedisConnection);
        }, RedisContextConfig.class, RedisSpringUtilsTest.class);
    }
}
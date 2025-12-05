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

package io.microsphere.redis.spring.interceptor;


import io.microsphere.redis.spring.AbstractRedisTest;
import io.microsphere.redis.spring.config.RedisContextConfig;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link EventPublishingRedisCommandInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingRedisCommandInterceptor
 * @since 1.0.0
 */
@ContextConfiguration(
        classes = {
                RedisContextConfig.class,
                EventPublishingRedisCommandInterceptor.class,
                EventPublishingRedisCommandInterceptorTest.class
        }
)
class EventPublishingRedisCommandInterceptorTest extends AbstractRedisTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private RedisConnection redisConnection;

    @Autowired
    @Qualifier(BEAN_NAME)
    private RedisContext redisContext;

    private RedisMethodContext redisMethodContext;

    @Autowired
    private EventPublishingRedisCommandInterceptor interceptor;

    @Autowired
    private ConfigurableApplicationContext context;

    private MockPropertySource mockPropertySource;

    private Throwable failure;

    @BeforeEach
    void setUp() {
        this.redisConnection = redisConnectionFactory.getConnection();
        this.redisMethodContext = new RedisMethodContext(this.redisConnection, SET_METHOD, SET_METHOD_ARGS, this.redisContext, SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE);
        this.mockPropertySource = new MockPropertySource();
        this.context.getEnvironment().getPropertySources().addFirst(this.mockPropertySource);
        publishRedisConfigurationPropertyChangedEvent();
    }

    @Test
    void testAfterExecute() {
        ApplicationListener<RedisCommandEvent> listener = event -> {
            assertSame(SET_METHOD, event.getMethod());
            assertEquals("set", event.getMethodName());
            assertSame(SET_METHOD_ARGS, event.getArgs());
            assertArrayEquals((byte[]) SET_METHOD_ARGS[0], (byte[]) event.getArg(0));
            assertArrayEquals((byte[]) SET_METHOD_ARGS[1], (byte[]) event.getArg(1));
            assertEquals(2, event.getParameterCount());
            assertArrayEquals(ofArray(byte[].class, byte[].class), event.getParameterTypes());
            assertEquals(this.redisContext.getApplicationName(), event.getApplicationName());
            assertEquals(SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE, event.getSourceBeanName());
        };

        this.context.addApplicationListener(listener);

        assertInterceptor();

        Method method = findMethod(RedisConnection.class, "randomKey");
        this.redisMethodContext = new RedisMethodContext(this.redisConnection, method, EMPTY_OBJECT_ARRAY, this.redisContext);
        assertInterceptor();
    }

    @Test
    void testAfterExecuteOnDisabled() {
        disable();
        assertInterceptor();
    }

    @Test
    void testAfterExecuteOnFailed() {
        this.failure = new RuntimeException("For testing...");
        assertInterceptor();

        disable();
        assertInterceptor();
    }

    private void disable() {
        this.mockPropertySource.setProperty(MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME, "false");
        publishRedisConfigurationPropertyChangedEvent();
    }

    void assertInterceptor() {
        this.interceptor.afterExecute(this.redisMethodContext, null, failure);
        assertEquals(0, this.interceptor.getOrder());
    }

    private void publishRedisConfigurationPropertyChangedEvent() {
        this.context.publishEvent(new RedisConfigurationPropertyChangedEvent(this.context, ofSet(MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME)));
    }
}
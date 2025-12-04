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
import io.microsphere.redis.spring.metadata.Parameter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;
import static io.microsphere.redis.spring.interceptor.RedisMethodContext.clear;
import static io.microsphere.redis.spring.interceptor.RedisMethodContext.get;
import static io.microsphere.redis.spring.interceptor.RedisMethodContext.set;
import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.isWriteCommandMethod;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisMethodContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMethodContext
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        RedisContextConfig.class,
        RedisMethodContextTest.class
})
class RedisMethodContextTest extends AbstractRedisTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private RedisConnection redisConnection;

    private Method setMethod = findMethod(RedisConnection.class, "set", byte[].class, byte[].class);

    private Object[] args = ofArray("key".getBytes(UTF_8), "value".getBytes(UTF_8));

    @Autowired
    @Qualifier(BEAN_NAME)
    private RedisContext redisContext;

    private String sourceBeanName = "redisTemplate";

    private RedisMethodContext context;

    @BeforeEach
    void setUp() {
        this.redisConnection = redisConnectionFactory.getConnection();
        this.context = new RedisMethodContext(this.redisConnection, this.setMethod, this.args, this.redisContext, this.sourceBeanName);
    }

    @AfterEach
    void tearDown() {
        this.redisConnection.close();
    }

    @Test
    void test() {
        assertRedisMethodContextCommons(this.context, this.sourceBeanName, setMethod, args);
    }

    @Test
    void testWithNullBeanName() {
        Method randomKeyMethod = findMethod(RedisKeyCommands.class, "randomKey");
        RedisMethodContext context = new RedisMethodContext(this.redisConnection, randomKeyMethod, EMPTY_OBJECT_ARRAY, this.redisContext);
        assertRedisMethodContextCommons(context, null, randomKeyMethod, EMPTY_OBJECT_ARRAY);
    }

    @Test
    void testInThreadLocal() {
        RedisMethodContext context = get();
        assertNull(context);

        set(this.context);
        context = get();
        assertSame(context, this.context);

        clear();
        context = get();
        assertNull(context);
    }

    void assertRedisMethodContextCommons(RedisMethodContext context, String sourceBeanName, Method method, Object... args) {
        assertSame(this.redisConnection, context.getTarget());
        assertSame(method, context.getMethod());
        assertArrayEquals(args, context.getArgs());
        assertEquals(sourceBeanName, context.getSourceBeanName());
        assertSame(this.redisContext, context.getRedisContext());

        assertThrows(IllegalArgumentException.class, context::stop);
        context.start();
        assertTrue(context.getStartTimeNanos() < System.nanoTime());

        context.stop();
        assertTrue(context.getDurationNanos() > 0);
        assertTrue(context.getDuration(MICROSECONDS) > 0);

        assertNull(context.getAttribute("name"));
        assertFalse(context.hasAttribute("name"));
        assertNull(context.removeAttribute("name"));
        assertTrue(context.getAttributes().isEmpty());

        context.setAttribute("name", "value");
        assertEquals("value", context.getAttribute("name"));
        assertTrue(context.hasAttribute("name"));
        assertFalse(context.getAttributes().isEmpty());
        assertEquals("value", context.removeAttribute("name"));
        assertFalse(context.hasAttribute("name"));
        assertTrue(context.getAttributes().isEmpty());

        assertEquals(isWriteCommandMethod(method), context.isWriteMethod());
        assertEquals(isWriteCommandMethod(method), context.isWriteMethod());

        int parameterCount = method.getParameterCount();

        Parameter[] parameters = context.getParameters();
        assertEquals(parameterCount, parameters.length);

        parameters = context.getParameters();
        assertEquals(parameterCount, parameters.length);

        assertEquals(parameterCount, context.getParameterCount());
        assertEquals(parameterCount, context.getParameterCount());

        java.lang.reflect.Parameter[] params = method.getParameters();
        for (int i = 0; i < parameterCount; i++) {
            java.lang.reflect.Parameter param = params[i];
            String parameterName = param.getName();
            Parameter parameter = context.getParameter(i);
            assertEquals(parameterName, parameter.getMetadata().getParameterName());
            assertEquals(param.getType().getName(), parameter.getParameterType());
            assertEquals(context.getParameter(parameterName), parameter);
        }

        assertNull(context.getParameter("not-found"));

        assertSame(this.redisContext.getRedisConfiguration(), context.getRedisConfiguration());
        assertSame(this.redisContext.getBeanFactory(), context.getBeanFactory());
        assertSame(this.redisContext.getApplicationContext(), context.getApplicationContext());
        assertSame(this.redisContext.getClassLoader(), context.getClassLoader());
        assertSame(this.redisContext.getRedisTemplateBeanNames(), context.getRedisTemplateBeanNames());
        assertSame(this.redisContext.getRedisConnectionFactoryBeanNames(), context.getRedisConnectionFactoryBeanNames());
        assertSame(this.redisContext.isEnabled(), context.isEnabled());
        assertSame(this.redisContext.getEnvironment(), context.getEnvironment());
        assertSame(this.redisContext.isCommandEventExposed(), context.isCommandEventExposed());
        assertSame(this.redisContext.getApplicationName(), context.getApplicationName());
        assertEquals(sourceBeanName != null, context.isSourceFromRedisTemplate());
        assertEquals(sourceBeanName != null, context.isSourceFromRedisTemplate());
        assertFalse(context.isSourceFromRedisConnectionFactory());
        assertFalse(context.isSourceFromRedisConnectionFactory());

        assertEquals(isWriteCommandMethod(method), context.isWriteMethod(true));

        assertNotNull(context.toString());
    }
}
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;

/**
 * {@link RedisMethodInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMethodInterceptor
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        RedisContextConfig.class,
        RedisMethodInterceptorTest.class
})
class RedisMethodInterceptorTest extends AbstractRedisTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private RedisConnection redisConnection;

    @Autowired
    @Qualifier(BEAN_NAME)
    private RedisContext redisContext;

    private RedisMethodContext context;

    private RedisMethodInterceptor interceptor;

    @BeforeEach
    void setUp() {
        this.redisConnection = redisConnectionFactory.getConnection();
        this.interceptor = new RedisMethodInterceptorImpl();
        this.context = new RedisMethodContext(this.redisConnection, SET_METHOD, SET_METHOD_ARGS, this.redisContext, SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE);
    }

    @AfterEach
    void tearDown() {
        this.redisConnection.close();
    }

    @Test
    void testBeforeExecute() throws Throwable {
        this.interceptor.beforeExecute(this.context);
    }

    @Test
    void testAfterExecute() throws Throwable {
        this.interceptor.afterExecute(this.context, null, null);
    }

    @Test
    void testHandleError() {
        this.interceptor.handleError(this.context, true, null, null, null);
    }

    static class RedisMethodInterceptorImpl implements RedisMethodInterceptor {

        @Override
        public void beforeExecute(RedisMethodContext context) throws Throwable {
            RedisMethodInterceptor.super.beforeExecute(context);
        }

        @Override
        public void beforeExecute(Object target, Method method, Object[] args, String sourceBeanName) throws Throwable {
            RedisMethodInterceptor.super.beforeExecute(target, method, args, sourceBeanName);
        }

        @Override
        public void afterExecute(RedisMethodContext context, Object result, Throwable failure) throws Throwable {
            RedisMethodInterceptor.super.afterExecute(context, result, failure);
        }

        @Override
        public void afterExecute(Object target, Method method, Object[] args, String sourceBeanName, Object result, Throwable failure) throws Throwable {
            RedisMethodInterceptor.super.afterExecute(target, method, args, sourceBeanName, result, failure);
        }

        @Override
        public void handleError(RedisMethodContext context, boolean before, Object result, Throwable failure, Throwable error) {
            RedisMethodInterceptor.super.handleError(context, before, result, failure, error);
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }
}
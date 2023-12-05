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
package io.microsphere.redis.spring.beans;

import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.interceptor.InterceptingRedisConnectionInvocationHandler;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * {@link RedisConnectionFactory} Proxy {@link BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConnectionFactory
 * @see ReactiveRedisConnectionFactory
 * @since 1.0.0
 */
public class RedisConnectionFactoryProxyBeanPostProcessor extends GenericBeanPostProcessorAdapter<RedisConnectionFactory> {

    private static final Class[] REDIS_CONNECTION_TYPES = new Class[]{RedisConnection.class};

    private final ConfigurableBeanFactory beanFactory;

    public RedisConnectionFactoryProxyBeanPostProcessor(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    protected RedisConnectionFactory doPostProcessAfterInitialization(RedisConnectionFactory bean, String beanName) throws BeansException {
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvice(new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
                Method method = invocation.getMethod();
                method.setAccessible(true);
                String methodName = method.getName();
                Object result = invocation.proceed();
                if ("getConnection".equals(methodName) && result instanceof RedisConnection) {
                    RedisContext redisContext = RedisContext.get(beanFactory);
                    if (redisContext.isEnabled()) {
                        result = newProxyRedisConnection((RedisConnection) result, redisContext, beanName);
                    }
                }
                return result;
            }
        });
        return (RedisConnectionFactory) proxyFactory.getProxy();
    }

    private static RedisConnection newProxyRedisConnection(RedisConnection connection, RedisContext redisContext, String sourceBeanName) {
        ClassLoader classLoader = redisContext.getClassLoader();
        InvocationHandler invocationHandler = newInvocationHandler(connection, redisContext, sourceBeanName);
        return (RedisConnection) newProxyInstance(classLoader, REDIS_CONNECTION_TYPES, invocationHandler);
    }

    private static InvocationHandler newInvocationHandler(RedisConnection connection, RedisContext redisContext, String sourceBeanName) {
        return new InterceptingRedisConnectionInvocationHandler(connection, redisContext, sourceBeanName);
    }
}

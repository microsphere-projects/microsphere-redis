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

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.interceptor.InterceptingRedisConnectionInvocationHandler;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import static io.microsphere.redis.spring.context.RedisContext.get;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static java.lang.reflect.Proxy.newProxyInstance;

/**
 * {@link RedisConnectionFactory} Proxy {@link BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConnectionFactory
 * @see ReactiveRedisConnectionFactory
 * @see RedisConnection
 * @see DelegatingWrapper
 * @since 1.0.0
 */
public class RedisConnectionFactoryProxyBeanPostProcessor extends GenericBeanPostProcessorAdapter<RedisConnectionFactory> {

    private static final Class[] REDIS_CONNECTION_TYPES = new Class[]{RedisConnection.class, DelegatingWrapper.class};

    private static final String SOURCE_BEAN_ATTRIBUTE_NAME = "_sourceBean";

    private static final Method GET_CONNECTION_METHOD = findMethod(RedisConnectionFactory.class, "getConnection");

    private final ConfigurableBeanFactory beanFactory;

    public RedisConnectionFactoryProxyBeanPostProcessor(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    protected RedisConnectionFactory doPostProcessAfterInitialization(RedisConnectionFactory bean, String beanName) throws BeansException {
        setRawRedisConnectionFactory(this.beanFactory, beanName, bean);
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.addAdvice(new MethodInterceptor() {
            @Nullable
            @Override
            public Object invoke(@Nonnull MethodInvocation invocation) throws Throwable {
                Method method = invocation.getMethod();
                trySetAccessible(method);
                Object result = invocation.proceed();
                if (GET_CONNECTION_METHOD.equals(method)) {
                    RedisContext redisContext = get(beanFactory);
                    if (redisContext.isEnabled()) {
                        result = newProxyRedisConnection((RedisConnection) result, redisContext, bean, beanName);
                    }
                }
                return result;
            }
        });
        return (RedisConnectionFactory) proxyFactory.getProxy();
    }

    public static RedisConnection newProxyRedisConnection(RedisConnection connection, RedisContext redisContext,
                                                          Object sourceBean, String sourceBeanName) {
        ClassLoader classLoader = redisContext.getClassLoader();
        InvocationHandler invocationHandler = newInvocationHandler(connection, redisContext, sourceBean, sourceBeanName);
        return (RedisConnection) newProxyInstance(classLoader, REDIS_CONNECTION_TYPES, invocationHandler);
    }

    static void setRawRedisConnectionFactory(ConfigurableBeanFactory beanFactory, String beanName, RedisConnectionFactory redisConnectionFactory) {
        BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
        beanDefinition.setAttribute(SOURCE_BEAN_ATTRIBUTE_NAME, redisConnectionFactory);
    }

    public static RedisConnectionFactory getRawRedisConnectionFactory(ConfigurableBeanFactory beanFactory, String beanName) {
        BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
        RedisConnectionFactory redisConnectionFactory = (RedisConnectionFactory) beanDefinition.getAttribute(SOURCE_BEAN_ATTRIBUTE_NAME);
        return redisConnectionFactory == null ? (RedisConnectionFactory) beanFactory.getBean(beanName) : redisConnectionFactory;
    }

    private static InvocationHandler newInvocationHandler(RedisConnection connection, RedisContext redisContext,
                                                          Object sourceBean, String sourceBeanName) {
        return new InterceptingRedisConnectionInvocationHandler(connection, redisContext, sourceBean, sourceBeanName);
    }
}
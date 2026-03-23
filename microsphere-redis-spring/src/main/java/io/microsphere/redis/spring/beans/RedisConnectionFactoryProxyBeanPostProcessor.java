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
 * {@link BeanPostProcessor} that wraps every {@link RedisConnectionFactory} bean in an AOP proxy
 * so that each call to {@link RedisConnectionFactory#getConnection()} returns a JDK dynamic proxy
 * that implements both {@link RedisConnection} and {@link DelegatingWrapper}.  The proxy delegates
 * all calls to an {@link InterceptingRedisConnectionInvocationHandler}, enabling transparent
 * interception of Redis commands.
 *
 * <p>Registered by {@link io.microsphere.redis.spring.annotation.RedisInterceptorBeanDefinitionRegistrar}
 * when no explicit {@link org.springframework.data.redis.core.RedisTemplate} bean names are specified
 * via {@link io.microsphere.redis.spring.annotation.EnableRedisInterceptor#wrapRedisTemplates()}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Registered automatically by @EnableRedisInterceptor when wrapRedisTemplates is empty.
 *   // Manual registration in a BeanFactory:
 *   ConfigurableBeanFactory beanFactory = ...;
 *   beanFactory.addBeanPostProcessor(new RedisConnectionFactoryProxyBeanPostProcessor(beanFactory));
 *
 *   // Retrieve the raw (pre-proxy) RedisConnectionFactory for a bean named "redisConnectionFactory"
 *   RedisConnectionFactory raw =
 *       RedisConnectionFactoryProxyBeanPostProcessor.getRawRedisConnectionFactory(beanFactory, "redisConnectionFactory");
 * }</pre>
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

    /**
     * Creates a new {@link RedisConnectionFactoryProxyBeanPostProcessor} bound to the given bean factory.
     *
     * @param beanFactory the Spring {@link ConfigurableBeanFactory} used to look up the
     *                    {@link RedisContext} and to store raw factory references
     */
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

    /**
     * Creates a JDK dynamic proxy that wraps {@code connection} and implements both
     * {@link RedisConnection} and {@link DelegatingWrapper}. The proxy intercepts every
     * method call through an {@link InterceptingRedisConnectionInvocationHandler}.
     *
     * @param connection     the real {@link RedisConnection} to wrap
     * @param redisContext   the {@link RedisContext} providing interceptors and configuration
     * @param sourceBean     the source bean (e.g. the {@link RedisConnectionFactory} or template)
     * @param sourceBeanName the Spring bean name of the source bean
     * @return a proxy {@link RedisConnection} that intercepts all commands
     */
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

    /**
     * Retrieves the original (pre-proxy) {@link RedisConnectionFactory} for the given bean name.
     * Falls back to resolving the bean from the factory if the attribute was never stored.
     *
     * @param beanFactory the Spring bean factory
     * @param beanName    the name of the proxied {@link RedisConnectionFactory} bean
     * @return the original {@link RedisConnectionFactory}; never {@code null}
     */
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
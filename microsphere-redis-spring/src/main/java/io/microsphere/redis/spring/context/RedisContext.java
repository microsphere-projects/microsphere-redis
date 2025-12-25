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

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisCommandInterceptors;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisConnectionFactoryBeanNames;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisConnectionInterceptors;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisTemplate;
import static io.microsphere.redis.spring.util.RedisSpringUtils.findRedisTemplateBeanNames;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;

/**
 * Redis Context
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisContext implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryAware, BeanClassLoaderAware {

    private static final Logger logger = getLogger(RedisConfiguration.class);

    public static final String BEAN_NAME = "microsphere:redisContext";

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurableApplicationContext context;

    private ClassLoader classLoader;

    private RedisConfiguration redisConfiguration;

    private Set<String> redisTemplateBeanNames;

    private Set<String> redisConnectionFactoryBeanNames;

    private List<RedisConnectionInterceptor> redisConnectionInterceptors;

    private List<RedisCommandInterceptor> redisCommandInterceptors;

    @Override
    public void afterSingletonsInstantiated() {
        this.redisConfiguration = getRedisConfiguration();
        this.redisTemplateBeanNames = findRedisTemplateBeanNames(beanFactory);
        this.redisConnectionFactoryBeanNames = findRedisConnectionFactoryBeanNames(beanFactory);
        this.redisConnectionInterceptors = findRedisConnectionInterceptors(beanFactory);
        this.redisCommandInterceptors = findRedisCommandInterceptors(beanFactory);
    }

    @NonNull
    public RedisConfiguration getRedisConfiguration() {
        RedisConfiguration redisConfiguration = this.redisConfiguration;
        if (redisConfiguration == null) {
            logger.trace("RedisConfiguration is not initialized, it will be gotten from BeanFactory[{}]", beanFactory);
            redisConfiguration = RedisConfiguration.get(beanFactory);
            this.redisConfiguration = redisConfiguration;
        }
        return redisConfiguration;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asConfigurableListableBeanFactory(beanFactory);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = asConfigurableApplicationContext(applicationContext);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return context;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public ConfigurableEnvironment getEnvironment() {
        return getRedisConfiguration().getEnvironment();
    }

    public boolean isEnabled() {
        return getRedisConfiguration().isEnabled();
    }

    public RedisTemplate<?, ?> getRedisTemplate(String redisTemplateBeanName) {
        return findRedisTemplate(context, redisTemplateBeanName);
    }

    public Set<String> getRedisTemplateBeanNames() {
        return redisTemplateBeanNames;
    }

    public Set<String> getRedisConnectionFactoryBeanNames() {
        return redisConnectionFactoryBeanNames;
    }

    public boolean isCommandEventExposed() {
        return getRedisConfiguration().isCommandEventExposed();
    }

    public String getApplicationName() {
        return getRedisConfiguration().getApplicationName();
    }

    public List<RedisConnectionInterceptor> getRedisConnectionInterceptors() {
        return redisConnectionInterceptors;
    }

    public List<RedisCommandInterceptor> getRedisCommandInterceptors() {
        return redisCommandInterceptors;
    }

    public static RedisContext get(BeanFactory beanFactory) {
        return beanFactory.getBean(RedisContext.class);
    }
}
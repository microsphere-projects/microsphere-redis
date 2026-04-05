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
 * Central Spring bean that aggregates all Redis infrastructure components for the Microsphere
 * Redis extension, including:
 * <ul>
 *   <li>{@link RedisConfiguration} – environment-backed Redis settings</li>
 *   <li>Discovered {@link RedisConnectionInterceptor} and {@link RedisCommandInterceptor} lists</li>
 *   <li>Resolved bean names for all {@link RedisTemplate} and
 *       {@link org.springframework.data.redis.connection.RedisConnectionFactory} beans</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Retrieve the RedisContext from a BeanFactory / ApplicationContext
 *   RedisContext redisContext = RedisContext.get(applicationContext);
 *
 *   // Check whether interception is currently active
 *   boolean enabled = redisContext.isEnabled();
 *
 *   // Get the RedisConfiguration
 *   RedisConfiguration config = redisContext.getRedisConfiguration();
 *
 *   // Find all interceptors
 *   List<RedisCommandInterceptor> commandInterceptors = redisContext.getRedisCommandInterceptors();
 * }</pre>
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

    /**
     * Returns the {@link RedisConfiguration} bean, resolving it lazily from the
     * {@link org.springframework.beans.factory.BeanFactory} on first access.
     *
     * @return non-null {@link RedisConfiguration}
     */
    @Nullable
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

    /**
     * Returns the {@link ConfigurableListableBeanFactory} associated with this context.
     *
     * @return the bean factory; never {@code null}
     */
    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Returns the {@link ConfigurableApplicationContext} associated with this context.
     *
     * @return the application context; never {@code null}
     */
    public ConfigurableApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Returns the {@link ClassLoader} injected via {@link BeanClassLoaderAware}.
     *
     * @return the class loader used for dynamic proxy creation; never {@code null}
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Returns the {@link ConfigurableEnvironment} from the underlying {@link RedisConfiguration}.
     *
     * @return the environment; never {@code null}
     */
    public ConfigurableEnvironment getEnvironment() {
        return getRedisConfiguration().getEnvironment();
    }

    /**
     * Returns {@code true} when the Redis interceptor infrastructure is globally enabled
     * (controlled by the property {@code microsphere.redis.enabled}).
     *
     * @return {@code true} if interception is currently enabled
     */
    public boolean isEnabled() {
        return getRedisConfiguration().isEnabled();
    }

    /**
     * Finds and returns the {@link RedisTemplate} bean with the given name from the application context.
     *
     * @param redisTemplateBeanName the Spring bean name of the desired {@link RedisTemplate}
     * @return the {@link RedisTemplate}, or {@code null} if not found
     */
    public RedisTemplate<?, ?> getRedisTemplate(String redisTemplateBeanName) {
        return findRedisTemplate(context, redisTemplateBeanName);
    }

    /**
     * Returns the set of Spring bean names for all {@link RedisTemplate} beans discovered in the context.
     *
     * @return bean names; may be empty but never {@code null}
     */
    public Set<String> getRedisTemplateBeanNames() {
        return redisTemplateBeanNames;
    }

    /**
     * Returns the set of Spring bean names for all
     * {@link org.springframework.data.redis.connection.RedisConnectionFactory} beans discovered in the context.
     *
     * @return bean names; may be empty but never {@code null}
     */
    public Set<String> getRedisConnectionFactoryBeanNames() {
        return redisConnectionFactoryBeanNames;
    }

    /**
     * Returns {@code true} if {@link io.microsphere.redis.spring.event.RedisCommandEvent} publishing
     * is enabled (controlled by {@link RedisConfiguration#isCommandEventExposed()}).
     *
     * @return {@code true} if Redis command events should be published to the application context
     */
    public boolean isCommandEventExposed() {
        return getRedisConfiguration().isCommandEventExposed();
    }

    /**
     * Returns the application name from the underlying {@link RedisConfiguration}.
     *
     * @return the application name; never {@code null}
     */
    public String getApplicationName() {
        return getRedisConfiguration().getApplicationName();
    }

    /**
     * Returns the ordered list of {@link RedisConnectionInterceptor} beans registered in the context.
     *
     * @return list of connection interceptors; may be empty but never {@code null}
     */
    public List<RedisConnectionInterceptor> getRedisConnectionInterceptors() {
        return redisConnectionInterceptors;
    }

    /**
     * Returns the ordered list of {@link RedisCommandInterceptor} beans registered in the context.
     *
     * @return list of command interceptors; may be empty but never {@code null}
     */
    public List<RedisCommandInterceptor> getRedisCommandInterceptors() {
        return redisCommandInterceptors;
    }

    /**
     * Retrieves the {@link RedisContext} bean from the given {@link BeanFactory}.
     *
     * @param beanFactory the Spring bean factory
     * @return the {@link RedisContext} singleton; never {@code null}
     */
    public static RedisContext get(BeanFactory beanFactory) {
        return beanFactory.getBean(RedisContext.class);
    }
}
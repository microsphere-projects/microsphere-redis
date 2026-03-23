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
package io.microsphere.redis.spring.annotation;

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor;
import io.microsphere.redis.spring.beans.RedisTemplateWrapperBeanPostProcessor;
import io.microsphere.redis.spring.beans.WrapperProcessors;
import io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor.BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getWrappedRedisTemplateBeanNames;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * {@link ImportBeanDefinitionRegistrar} implementation that processes the attributes of
 * {@link EnableRedisInterceptor} and registers the appropriate interceptor infrastructure beans:
 * <ul>
 *   <li>{@link io.microsphere.redis.spring.beans.RedisTemplateWrapperBeanPostProcessor} when
 *       specific {@link org.springframework.data.redis.core.RedisTemplate} bean names are specified</li>
 *   <li>{@link io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor} when
 *       no template bean names are provided (intercepts all connections)</li>
 *   <li>{@link io.microsphere.redis.spring.beans.WrapperProcessors} for wrapper chain management</li>
 *   <li>{@link io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor} when
 *       {@code exposeCommandEvent = true}</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Triggered automatically by @EnableRedisInterceptor.
 *   // Direct usage in tests:
 *   RedisInterceptorBeanDefinitionRegistrar registrar = new RedisInterceptorBeanDefinitionRegistrar();
 *   registrar.setEnvironment(environment);
 *   registrar.registerBeanDefinitions(
 *       Set.of("redisTemplate"), // wrapRedisTemplateBeanNames
 *       true,                    // exposedCommandEvent
 *       registry
 *   );
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableRedisInterceptor
 * @since 1.0.0
 */
class RedisInterceptorBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static Class<EnableRedisInterceptor> ENABLE_REDIS_INTERCEPTOR_CLASS = EnableRedisInterceptor.class;

    private static final Logger logger = getLogger(RedisInterceptorBeanDefinitionRegistrar.class);

    private ConfigurableEnvironment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(importingClassMetadata, ENABLE_REDIS_INTERCEPTOR_CLASS);
        String[] wrapRedisTemplates = annotationAttributes.getStringArray("wrapRedisTemplates");
        boolean exposeCommandEvent = annotationAttributes.getBoolean("exposeCommandEvent");

        logger.trace("@EnableRedisInterceptor({}} annotated on the '{}'", annotationAttributes, importingClassMetadata);

        ConfigurableListableBeanFactory beanFactory = asConfigurableListableBeanFactory(registry);

        Set<String> wrapRedisTemplateBeanNames = getWrappedRedisTemplateBeanNames(beanFactory, this.environment, wrapRedisTemplates);

        registerBeanDefinitions(wrapRedisTemplateBeanNames, exposeCommandEvent, registry);
    }

    /**
     * Registers the interceptor infrastructure beans based on resolved template bean names and the
     * command-event exposure flag.
     *
     * @param wrappedRedisTemplateBeanNames the resolved set of {@link org.springframework.data.redis.core.RedisTemplate}
     *                                      bean names to wrap; may be empty
     * @param exposedCommandEvent           {@code true} to register the
     *                                      {@link io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor}
     * @param registry                      the Spring bean-definition registry to register beans into
     */
    public void registerBeanDefinitions(Set<String> wrappedRedisTemplateBeanNames, boolean exposedCommandEvent, BeanDefinitionRegistry registry) {

        if (isEmpty(wrappedRedisTemplateBeanNames)) {
            addRedisConnectionFactoryProxyBeanPostProcessor(registry);
        } else {
            registerRedisTemplateWrapperBeanPostProcessor(wrappedRedisTemplateBeanNames, registry);
        }

        registerWrapperProcessors(registry);

        if (exposedCommandEvent) {
            registerEventPublishingRedisCommendInterceptor(registry);
        }
    }

    private void registerRedisTemplateWrapperBeanPostProcessor(Set<String> wrappedRedisTemplateBeanNames, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisTemplateWrapperBeanPostProcessor.BEAN_NAME, RedisTemplateWrapperBeanPostProcessor.class, wrappedRedisTemplateBeanNames);
    }

    private void addRedisConnectionFactoryProxyBeanPostProcessor(BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = asConfigurableBeanFactory(registry);
        beanFactory.addBeanPostProcessor(new RedisConnectionFactoryProxyBeanPostProcessor(beanFactory));
    }

    private void registerEventPublishingRedisCommendInterceptor(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, EventPublishingRedisCommandInterceptor.class);
    }

    private void registerWrapperProcessors(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WrapperProcessors.BEAN_NAME, WrapperProcessors.class);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = asConfigurableEnvironment(environment);
    }
}
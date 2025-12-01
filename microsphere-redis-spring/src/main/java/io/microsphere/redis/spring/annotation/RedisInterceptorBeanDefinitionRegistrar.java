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
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor.BEAN_NAME;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimWhitespace;

/**
 * Redis Interceptor {@link ImportBeanDefinitionRegistrar}
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

        Set<String> wrapRedisTemplateBeanNames = resolveWrappedRedisTemplateBeanNames(wrapRedisTemplates);

        registerBeanDefinitions(wrapRedisTemplateBeanNames, exposeCommandEvent, registry);
    }

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

    private Set<String> resolveWrappedRedisTemplateBeanNames(String[] wrapRedisTemplates) {
        Set<String> wrappedRedisTemplateBeanNames = new LinkedHashSet<>();
        for (String wrapRedisTemplate : wrapRedisTemplates) {
            String wrappedRedisTemplateBeanName = environment.resolveRequiredPlaceholders(wrapRedisTemplate);
            Set<String> beanNames = commaDelimitedListToSet(wrappedRedisTemplateBeanName);
            for (String beanName : beanNames) {
                wrappedRedisTemplateBeanName = trimWhitespace(beanName);
                if (hasText(wrappedRedisTemplateBeanName)) {
                    wrappedRedisTemplateBeanNames.add(wrappedRedisTemplateBeanName);
                }
            }
        }
        return wrappedRedisTemplateBeanNames;
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
        this.environment = (ConfigurableEnvironment) environment;
    }
}
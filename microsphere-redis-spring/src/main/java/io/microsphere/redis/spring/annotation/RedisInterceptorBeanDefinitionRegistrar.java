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
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import io.microsphere.redis.spring.interceptor.RedisMethodInterceptor;
import io.microsphere.spring.beans.BeanSource;
import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor.BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getWrappedRedisTemplateBeanNames;
import static io.microsphere.spring.beans.BeanSource.registerBeans;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
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
class RedisInterceptorBeanDefinitionRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = getLogger(RedisInterceptorBeanDefinitionRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ResolvablePlaceholderAnnotationAttributes<EnableRedisInterceptor> attributes = getAnnotationAttributes(metadata, EnableRedisInterceptor.class);
        String[] wrapRedisTemplates = attributes.getStringArray("wrapRedisTemplates");
        boolean exposeCommandEvent = attributes.getBoolean("exposeCommandEvent");
        BeanSource[] sources = (BeanSource[]) attributes.get("sources");

        logger.trace("@EnableRedisInterceptor({}} annotated on the '{}'", attributes, metadata);

        Set<String> wrapRedisTemplateBeanNames = getWrappedRedisTemplateBeanNames(this.beanFactory, this.environment, wrapRedisTemplates);

        registerBeanDefinitions(wrapRedisTemplateBeanNames, exposeCommandEvent, sources, registry);
    }

    /**
     * Registers the interceptor infrastructure beans based on resolved template bean names and the
     * command-event exposure flag.
     *
     * @param wrappedRedisTemplateBeanNames the resolved set of {@link org.springframework.data.redis.core.RedisTemplate}
     *                                      bean names to wrap; may be empty
     * @param exposedCommandEvent           {@code true} to register the
     *                                      {@link EventPublishingRedisCommandInterceptor}
     * @param sources                       the sources that will be used to register the beans of Interceptor
     * @param registry                      the Spring bean-definition registry to register beans into
     */
    public void registerBeanDefinitions(Set<String> wrappedRedisTemplateBeanNames, boolean exposedCommandEvent,
                                        BeanSource[] sources, BeanDefinitionRegistry registry) {

        if (isEmpty(wrappedRedisTemplateBeanNames)) {
            addRedisConnectionFactoryProxyBeanPostProcessor(registry);
        } else {
            registerRedisTemplateWrapperBeanPostProcessor(wrappedRedisTemplateBeanNames, registry);
        }

        registerWrapperProcessors(registry);

        if (exposedCommandEvent) {
            registerEventPublishingRedisCommendInterceptor(registry);
        }

        registerInterceptors(sources);
    }

    private void registerRedisTemplateWrapperBeanPostProcessor(Set<String> wrappedRedisTemplateBeanNames, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisTemplateWrapperBeanPostProcessor.BEAN_NAME, RedisTemplateWrapperBeanPostProcessor.class, wrappedRedisTemplateBeanNames);
    }

    private void addRedisConnectionFactoryProxyBeanPostProcessor(BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = asConfigurableBeanFactory(registry);
        beanFactory.addBeanPostProcessor(new RedisConnectionFactoryProxyBeanPostProcessor(beanFactory));
    }

    private void registerWrapperProcessors(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WrapperProcessors.BEAN_NAME, WrapperProcessors.class);
    }

    private void registerEventPublishingRedisCommendInterceptor(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, EventPublishingRedisCommandInterceptor.class);
    }

    private void registerInterceptors(BeanSource[] sources) {
        registerBeans(this.beanFactory, sources, RedisMethodInterceptor.class, RedisCommandInterceptor.class,
                RedisConnectionInterceptor.class);
    }
}
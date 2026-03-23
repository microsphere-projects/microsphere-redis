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

import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;

/**
 * {@link ImportBeanDefinitionRegistrar} implementation that programmatically registers the
 * {@link RedisContext} bean under its canonical bean name.
 * Triggered by the {@link EnableRedisContext} annotation.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Applied automatically when @EnableRedisContext is present on a @Configuration class.
 *   // Manual registration (e.g. in tests):
 *   BeanDefinitionRegistry registry = ...; // e.g. GenericApplicationContext
 *   RedisContextBeanDefinitionRegistrar registrar = new RedisContextBeanDefinitionRegistrar();
 *   registrar.registerBeanDefinitions(registry);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class RedisContextBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(registry);
    }

    /**
     * Registers a {@link BeanDefinition} for the {@link RedisContext} bean into the given registry.
     *
     * @param registry the Spring bean-definition registry to register beans into
     */
    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, RedisContext.class);
    }
}
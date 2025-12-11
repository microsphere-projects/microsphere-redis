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

import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.redis.spring.config.RedisConfiguration.BEAN_NAME;
import static io.microsphere.redis.spring.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener.supports;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.init;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;

/**
 * {@link RedisConfiguration} {@link BeanDefinition} Registrar
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class RedisConfigurationBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, BeanClassLoaderAware {

    static {
        init();
    }

    private ClassLoader classLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        registerBeanDefinitions(registry);
    }

    public void registerBeanDefinitions(BeanDefinitionRegistry registry) {
        registerRedisConfiguration(registry);
        registerApplicationListeners(registry);
    }

    private void registerRedisConfiguration(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, BEAN_NAME, RedisConfiguration.class);
    }

    void registerApplicationListeners(BeanDefinitionRegistry registry) {
        if (supports(this.classLoader)) {
            registerBeanDefinition(registry, PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class);
        }
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
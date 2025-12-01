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


import io.microsphere.redis.spring.beans.RedisTemplateWrapperBeanPostProcessor;
import io.microsphere.redis.spring.beans.WrapperProcessors;
import io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisInterceptorBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisInterceptorBeanDefinitionRegistrar
 * @since 1.0.0
 */
@SpringJUnitConfig(classes =
        RedisInterceptorBeanDefinitionRegistrarTest.class
)
@EnableRedisInterceptor(
        wrapRedisTemplates = {"", " "},
        exposeCommandEvent = false
)
class RedisInterceptorBeanDefinitionRegistrarTest {

    @Autowired
    private ConfigurableBeanFactory beanFactory;

    @Test
    void test() {
        assertFalse(isBeanPresent(this.beanFactory, RedisTemplateWrapperBeanPostProcessor.BEAN_NAME, RedisTemplateWrapperBeanPostProcessor.class));
        assertTrue(isBeanPresent(this.beanFactory, WrapperProcessors.BEAN_NAME, WrapperProcessors.class));
        assertFalse(isBeanPresent(this.beanFactory, EventPublishingRedisCommandInterceptor.BEAN_NAME, EventPublishingRedisCommandInterceptor.class));
    }
}
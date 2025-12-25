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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.microsphere.redis.spring.config.RedisConfiguration.BEAN_NAME;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisConfigurationBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConfigurationBeanDefinitionRegistrar
 * @since 1.0.0
 */
@EnableRedisConfiguration
class RedisConfigurationBeanDefinitionRegistrarTest {

    @Test
    void test() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, RedisConfiguration.class));
            assertTrue(isBeanPresent(context, BEAN_NAME, RedisConfiguration.class));
            assertTrue(isBeanPresent(context, PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class));
        }, RedisConfigurationBeanDefinitionRegistrarTest.class);
    }

    @Test
    void testRegisterApplicationListeners() {
        RedisConfigurationBeanDefinitionRegistrar registrar = new RedisConfigurationBeanDefinitionRegistrar();
        ClassLoader classLoader = ClassLoader.getSystemClassLoader().getParent();
        registrar.setBeanClassLoader(classLoader);
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        registrar.registerApplicationListeners(beanFactory);

        beanFactory.freezeConfiguration();
        beanFactory.preInstantiateSingletons();
        assertFalse(isBeanPresent(beanFactory, PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class));
    }
}
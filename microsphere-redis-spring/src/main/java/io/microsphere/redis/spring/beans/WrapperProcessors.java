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

import io.microsphere.lang.Wrapper;
import io.microsphere.lang.WrapperProcessor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static java.util.Collections.emptyList;

/**
 * The composite class of {@link WrapperProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class WrapperProcessors implements InitializingBean, BeanFactoryAware {

    public static final String BEAN_NAME = "wrapperProcessors";

    private ConfigurableListableBeanFactory beanFactory;

    private Map<Class<?>, List<WrapperProcessor>> wrapperProcessorsMap;

    public <W extends Wrapper> W process(W wrapper) {
        Class<?> wrapperType = wrapper.getClass();
        List<WrapperProcessor> wrapperProcessors = wrapperProcessorsMap.getOrDefault(wrapperType, emptyList());
        for (WrapperProcessor<W> wrapperProcessor : wrapperProcessors) {
            wrapper = wrapperProcessor.process(wrapper);
        }
        return wrapper;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asConfigurableListableBeanFactory(beanFactory);
    }

    @Override
    public void afterPropertiesSet() {
        this.wrapperProcessorsMap = initWrapperProcessorsMap();
    }

    private Map<Class<?>, List<WrapperProcessor>> initWrapperProcessorsMap() {
        Map<Class<?>, List<WrapperProcessor>> wrapperHandlersMap = new HashMap<>(2);
        ConfigurableListableBeanFactory beanFactory = this.beanFactory;
        String[] beanNames = beanFactory.getBeanNamesForType(WrapperProcessor.class);
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            Class<?> wrapperClass = resolvableType.as(WrapperProcessor.class).getGeneric(0).resolve();
            wrapperHandlersMap.computeIfAbsent(wrapperClass, k -> {
                Class<WrapperProcessor> wrapperProcessorClass = (Class<WrapperProcessor>) resolvableType.getRawClass();
                return getSortedBeans(beanFactory, wrapperProcessorClass);
            });
        }
        return wrapperHandlersMap;
    }
}

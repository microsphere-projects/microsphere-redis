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


import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.beans.RedisTemplateWrapperBeanPostProcessor.BEAN_NAME;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RedisTemplateWrapperBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisTemplateWrapperBeanPostProcessor
 * @since 1.0.0
 */
class RedisTemplateWrapperBeanPostProcessorTest {

    private static final Set<String> TEST_BEAN_NAMES = ofSet("redisTemplate", "stringRedisTemplate", "redisConnectionFactory");

    @Test
    void test() {
        testInSpringContainer(context -> {
            RedisTemplateWrapperBeanPostProcessor postProcessor = context.getBean(RedisTemplateWrapperBeanPostProcessor.class);
            assertSame(emptySet(), postProcessor.getWrappedRedisTemplateBeanNames());
        }, EmptyConfig.class);

        testInSpringContainer(context -> {
            RedisTemplateWrapperBeanPostProcessor postProcessor = context.getBean(RedisTemplateWrapperBeanPostProcessor.class);
            assertEquals(TEST_BEAN_NAMES, postProcessor.getWrappedRedisTemplateBeanNames());
        }, Config.class);
    }

    static class Config extends BaseConfig {

        @Bean(name = BEAN_NAME)
        public RedisTemplateWrapperBeanPostProcessor redisTemplateWrapperBeanPostProcessor() {
            return new RedisTemplateWrapperBeanPostProcessor(TEST_BEAN_NAMES);
        }
    }

    static class EmptyConfig extends BaseConfig {

        @Bean(name = BEAN_NAME)
        public RedisTemplateWrapperBeanPostProcessor redisTemplateWrapperBeanPostProcessor() {
            return new RedisTemplateWrapperBeanPostProcessor(emptySet());
        }
    }

    static class BaseConfig extends RedisContextConfig {

        @Bean(name = WrapperProcessors.BEAN_NAME)
        public WrapperProcessors wrapperProcessors() {
            return new WrapperProcessors();
        }
    }
}
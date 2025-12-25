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


import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;

import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME;
import static io.microsphere.spring.core.env.PropertySourcesUtils.BOOTSTRAP_PROPERTY_SOURCE_NAME;
import static java.lang.String.valueOf;
import static java.util.Collections.emptyMap;

/**
 * {@link RedisInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisInitializer
 * @since 1.0.0
 */
class RedisInitializerTest {

    private RedisInitializer initializer;

    @BeforeEach
    void setUp() {
        this.initializer = new RedisInitializer();
    }

    @Test
    void testInitialize() {
        assertInitialize(false, true, false, false);
        assertInitialize(false, true, true, false);
        assertInitialize(false, true, true, true);
        assertInitialize(false, true, false, true);
    }

    @Test
    void testInitializeOnUnsupported() {
        assertInitialize(false, false, false, false);
        assertInitialize(true, false, false, false);
        assertInitialize(true, true, false, false);
    }

    void assertInitialize(boolean isBootstrap, boolean enabled, boolean interceptorEnabled, boolean isEventExposed) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        ConfigurableEnvironment environment = context.getEnvironment();
        MutablePropertySources propertySources = environment.getPropertySources();

        if (isBootstrap) {
            propertySources.addFirst(new MapPropertySource(BOOTSTRAP_PROPERTY_SOURCE_NAME, emptyMap()));
        } else {
            context.register(RedisContextConfig.class);
        }

        MockPropertySource mockPropertySource = new MockPropertySource();
        mockPropertySource.setProperty(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME, valueOf(enabled));
        mockPropertySource.setProperty(MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME, valueOf(interceptorEnabled));
        mockPropertySource.setProperty(MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME, valueOf(isEventExposed));
        propertySources.addFirst(mockPropertySource);

        this.initializer.initialize(context);
        context.refresh();
        context.close();
    }
}
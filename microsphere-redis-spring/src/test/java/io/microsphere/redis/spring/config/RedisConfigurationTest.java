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

package io.microsphere.redis.spring.config;


import io.microsphere.redis.spring.AbstractRedisTest;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.config.RedisConfiguration.BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConfiguration
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        RedisContextConfig.class,
        RedisConfiguration.class,
        RedisConfigurationTest.class
})
@TestPropertySource(
        properties = {
                "spring.application.name=test-service"
        }
)
class RedisConfigurationTest extends AbstractRedisTest {

    @Autowired
    @Qualifier(BEAN_NAME)
    private RedisConfiguration redisConfiguration;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void test() {
        assertSame(this.environment, this.redisConfiguration.getEnvironment());
        assertEquals("test-service", this.redisConfiguration.getApplicationName());
        assertFalse(this.redisConfiguration.isEnabled());
        assertTrue(this.redisConfiguration.isCommandEventExposed());

        RedisConfigurationPropertyChangedEvent event = new RedisConfigurationPropertyChangedEvent(this.context, ofSet("test"));
        this.context.publishEvent(event);
        assertFalse(this.redisConfiguration.isEnabled());

        MockPropertySource propertySource = new MockPropertySource();
        propertySource.setProperty(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME, "true");
        this.environment.getPropertySources().addFirst(propertySource);

        event = new RedisConfigurationPropertyChangedEvent(this.context, ofSet(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME));
        this.context.publishEvent(event);
        assertTrue(this.redisConfiguration.isEnabled());

        event = new RedisConfigurationPropertyChangedEvent(this.context, ofSet(MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME));
        this.context.publishEvent(event);
        assertTrue(this.redisConfiguration.isCommandEventExposed());
    }
}
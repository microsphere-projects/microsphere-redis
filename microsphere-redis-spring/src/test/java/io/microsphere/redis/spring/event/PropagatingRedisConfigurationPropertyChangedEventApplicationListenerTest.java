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

package io.microsphere.redis.spring.event;


import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener.supports;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropagatingRedisConfigurationPropertyChangedEventApplicationListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropagatingRedisConfigurationPropertyChangedEventApplicationListener
 * @since 1.0.0
 */
@SpringJUnitConfig(
        classes = {
                RedisContextConfig.class,
                PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class,
                PropagatingRedisConfigurationPropertyChangedEventApplicationListenerTest.class
        }
)
class PropagatingRedisConfigurationPropertyChangedEventApplicationListenerTest {

    @Autowired
    private PropagatingRedisConfigurationPropertyChangedEventApplicationListener listener;

    @Autowired
    private ConfigurableApplicationContext context;

    @Test
    void testSupports() {
        assertTrue(supports(getDefaultClassLoader()));
        assertFalse(supports(getSystemClassLoader().getParent()));
    }

    @Test
    void testSupportsEventType() {
        assertTrue(listener.supportsEventType(EnvironmentChangeEvent.class));
        assertFalse(listener.supportsEventType(RedisConfigurationPropertyChangedEvent.class));
    }

    @Test
    void testOnApplicationEvent() {
        assertRedisConfigurationPropertyChangedEvent(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME,
                MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME, "not-found-key");
    }

    void assertRedisConfigurationPropertyChangedEvent(String... keys) {
        Set<String> keysSet = ofSet(keys);
        EnvironmentChangeEvent environmentChangeEvent = new EnvironmentChangeEvent(keysSet);
        ApplicationListener<RedisConfigurationPropertyChangedEvent> listener = event -> {
            assertTrue(keysSet.containsAll(event.getPropertyNames()));
            assertSame(this.context, event.getSource());
            assertSame(this.context.getEnvironment(), event.getEnvironment());
            assertNotNull(event.getRedisConfiguration());
            for (String key : keys) {
                assertEquals(this.listener.isRedisPropertyName(key), event.hasProperty(key));
            }
        };
        this.context.addApplicationListener(listener);
        this.context.publishEvent(environmentChangeEvent);
        this.context.removeApplicationListener(listener);
    }
}
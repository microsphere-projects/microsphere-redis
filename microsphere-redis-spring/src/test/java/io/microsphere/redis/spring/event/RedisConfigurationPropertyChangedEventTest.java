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


import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.config.RedisConfiguration.BEAN_NAME;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RedisConfigurationPropertyChangedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConfigurationPropertyChangedEvent
 * @since 1.0.0
 */
class RedisConfigurationPropertyChangedEventTest {

    @Test
    void test() {
        testInSpringContainer((context, environment) -> {
            Set<String> propertyNames = ofSet("test-key");
            RedisConfigurationPropertyChangedEvent event = new RedisConfigurationPropertyChangedEvent(context, propertyNames);
            assertSame(context, event.getSource());
            assertSame(environment, event.getEnvironment());
            assertSame(propertyNames, event.getPropertyNames());
            assertSame(context.getBean(BEAN_NAME, RedisConfiguration.class), event.getRedisConfiguration());
            assertNotNull(event.toString());
        }, RedisContextConfig.class);
    }
}
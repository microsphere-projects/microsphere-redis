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

package io.microsphere.redis.spring.boot.autoconfigure.condition;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConditionalOnRedisEnabled} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConditionalOnRedisEnabled
 * @since 1.0.0
 */
class ConditionalOnRedisEnabledTest {

    @Nested
    @SpringJUnitConfig
    @TestPropertySource(
            properties = "microsphere.redis.enabled = false"
    )
    @Import(Config.class)
    class DisabledTest {

        @Autowired
        private ObjectProvider<Config> enabledConfigProvider;

        @Test
        void testDisabled() {
            assertNull(enabledConfigProvider.getIfAvailable());
        }
    }

    @Test
    void testEnabled() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, Config.class));
        }, Config.class);
    }

    @ConditionalOnRedisEnabled
    static class Config {
    }
}
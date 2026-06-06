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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ConditionalOnMyBatisEnabled} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConditionalOnMyBatisEnabled
 * @since 1.0.0
 */
@SpringBootTest(
        classes = ConditionalOnMyBatisEnabledTest.EnabledConfig.class,
        properties = "microsphere.mybatis.enabled = false"
)
class ConditionalOnMyBatisEnabledTest {

    @Autowired
    private ObjectProvider<EnabledConfig> enabledConfigProvider;

    @Test
    void testDisabled() {
        assertNull(enabledConfigProvider.getIfAvailable());
    }

    @Test
    void testEnabled() {
        testInSpringContainer(context -> {
            assertTrue(isBeanPresent(context, EnabledConfig.class));
        }, EnabledConfig.class);
    }

    @ConditionalOnMyBatisEnabled
    static class EnabledConfig {
    }
}

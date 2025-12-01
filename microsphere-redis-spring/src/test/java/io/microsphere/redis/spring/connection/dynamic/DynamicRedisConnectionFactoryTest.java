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

package io.microsphere.redis.spring.connection.dynamic;


import io.microsphere.redis.spring.AbstractRedisTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory.DEFAULT_REDIS_CONNECTION_FACTORY_BEAN_NAME;
import static io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory.clearTarget;
import static io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory.switchTarget;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DynamicRedisConnectionFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DynamicRedisConnectionFactory
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        DynamicRedisConnectionFactoryTest.class
})
class DynamicRedisConnectionFactoryTest extends AbstractRedisTest {

    private static final String REDIS_CONNECTION_FACTORY_BEAN_NAME = "redisConnectionFactory";

    @Bean
    @Primary
    public static DynamicRedisConnectionFactory dynamicRedisConnectionFactory() {
        return new DynamicRedisConnectionFactory();
    }

    @Autowired
    @Qualifier(REDIS_CONNECTION_FACTORY_BEAN_NAME)
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private DynamicRedisConnectionFactory dynamicRedisConnectionFactory;

    @BeforeEach
    void setUp() {
        clearTarget();
    }

    @AfterEach
    void tearDown() {
        clearTarget();
    }

    @Test
    void test() {
        assertRedisConnectionFactory();

        this.dynamicRedisConnectionFactory.setDefaultRedisConnectionFactoryBeanName(REDIS_CONNECTION_FACTORY_BEAN_NAME);
        assertEquals(REDIS_CONNECTION_FACTORY_BEAN_NAME, this.dynamicRedisConnectionFactory.getDefaultRedisConnectionFactoryBeanName());
        assertRedisConnectionFactory();

        switchTarget(REDIS_CONNECTION_FACTORY_BEAN_NAME);
        assertRedisConnectionFactory();
    }

    void assertRedisConnectionFactory() {
        assertEquals(REDIS_CONNECTION_FACTORY_BEAN_NAME, DEFAULT_REDIS_CONNECTION_FACTORY_BEAN_NAME);
        assertSame(this.redisConnectionFactory, this.dynamicRedisConnectionFactory.determineTargetRedisConnectionFactory());
        assertSame(this.redisConnectionFactory, this.dynamicRedisConnectionFactory.getDefaultRedisConnectionFactory());
        assertEquals(ofMap(REDIS_CONNECTION_FACTORY_BEAN_NAME, this.redisConnectionFactory), this.dynamicRedisConnectionFactory.getRedisConnectionFactories());
        assertNotNull(this.dynamicRedisConnectionFactory.getConnection());
        assertThrows(RuntimeException.class, this.dynamicRedisConnectionFactory::getClusterConnection);
        assertNotNull(this.dynamicRedisConnectionFactory.getSentinelConnection());
        assertTrue(this.dynamicRedisConnectionFactory.getConvertPipelineAndTxResults());
        assertNull(this.dynamicRedisConnectionFactory.translateExceptionIfPossible(new RuntimeException("For testing")));
    }
}
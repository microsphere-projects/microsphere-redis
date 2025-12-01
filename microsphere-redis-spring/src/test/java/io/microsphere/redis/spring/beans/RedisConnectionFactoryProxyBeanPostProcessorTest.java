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


import io.microsphere.redis.spring.AbstractRedisTest;
import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * {@link RedisConnectionFactoryProxyBeanPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConnectionFactoryProxyBeanPostProcessor
 * @since 1.0.0
 */
@ContextConfiguration(
        classes = {
                RedisContextConfig.class,
                RedisConnectionFactoryProxyBeanPostProcessor.class,
                RedisConnectionFactoryProxyBeanPostProcessorTest.class
        }
)
class RedisConnectionFactoryProxyBeanPostProcessorTest extends AbstractRedisTest {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Test
    void test() {
        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        assertFalse(redisConnection instanceof Proxy);
    }
}
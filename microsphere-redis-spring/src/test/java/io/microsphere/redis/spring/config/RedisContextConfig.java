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

import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.context.annotation.Bean;

import static io.microsphere.redis.spring.context.RedisContext.BEAN_NAME;

/**
 * The configuration class for testing RedisContext
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisContext
 * @see RedisConfiguration
 * @since 1.0.0
 */
public class RedisContextConfig extends RedisConfig {

    @Bean(BEAN_NAME)
    public static RedisContext redisContext() {
        return new RedisContext();
    }

    @Bean(RedisConfiguration.BEAN_NAME)
    public static RedisConfiguration redisConfiguration() {
        return new RedisConfiguration();
    }
}
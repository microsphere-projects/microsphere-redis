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
package io.microsphere.redis.spring;

import io.microsphere.redis.spring.config.RedisConfig;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Abstract Redis Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringJUnitConfig
@Disabled
@Import(RedisConfig.class)
public abstract class AbstractRedisTest {

    public static final Method SET_METHOD = findMethod(RedisConnection.class, "set", byte[].class, byte[].class);

    public static final Object[] SET_METHOD_ARGS = ofArray("key".getBytes(UTF_8), "value".getBytes(UTF_8));

    public static final String SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE = "redisTemplate";

    @Autowired
    protected RedisTemplate redisTemplate;

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected ConfigurableApplicationContext context;
}
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

package io.microsphere.redis.spring.util;


import org.junit.jupiter.api.Test;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.util.RedisConstants.ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_FAIL_FAST_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_INTERCEPTOR_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.spring.util.RedisConstants.REDIS_TEMPLATE_BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.SPRING_APPLICATION_NAME_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.STRING_REDIS_TEMPLATE_BEAN_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConstants
 * @since 1.0.0
 */
class RedisConstantsTest {

    @Test
    void testConstants() {
        assertEquals("redisTemplate", REDIS_TEMPLATE_BEAN_NAME);
        assertEquals("stringRedisTemplate", STRING_REDIS_TEMPLATE_BEAN_NAME);
        assertEquals("application", DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE);
        assertEquals("spring.application.name", SPRING_APPLICATION_NAME_PROPERTY_NAME);
        assertEquals("microsphere.redis.", MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.redis.enabled", MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME);
        assertFalse(DEFAULT_MICROSPHERE_REDIS_ENABLED);
        assertEquals("microsphere.redis.command-event.", MICROSPHERE_REDIS_COMMAND_EVENT_PROPERTY_NAME_PREFIX);
        assertEquals("true", DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_VALUE);
        assertEquals("microsphere.redis.command-event.exposed", MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME);
        assertTrue(DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED);
        assertEquals("true", DEFAULT_MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_VALUE);
        assertEquals("microsphere.redis.fail-fast", MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_NAME);
        assertTrue(MICROSPHERE_REDIS_FAIL_FAST_ENABLED);
        assertEquals("*", DEFAULT_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_VALUE);
        assertEquals("microsphere.redis.wrapped-redis-templates", WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME);
        assertEquals(ofSet("*"), ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES);
        assertEquals("microsphere.redis.interceptor.", MICROSPHERE_REDIS_INTERCEPTOR_PROPERTY_NAME_PREFIX);
        assertEquals("true", DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_VALUE);
        assertEquals("microsphere.redis.interceptor.enabled", MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME);
        assertTrue(DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED);
        assertEquals("${microsphere.redis.wrapped-redis-templates:}", DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER);
    }
}
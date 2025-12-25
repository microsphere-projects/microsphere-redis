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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.annotation.ConfigurationProperty.SYSTEM_PROPERTIES_SOURCE;
import static io.microsphere.collection.Sets.ofSet;
import static java.lang.Boolean.getBoolean;
import static java.lang.Boolean.parseBoolean;
import static java.lang.System.getProperty;

/**
 * The constants of Redis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface RedisConstants {

    /**
     * {@link RedisTemplate} Bean Name
     */
    String REDIS_TEMPLATE_BEAN_NAME = "redisTemplate";

    /**
     * {@link StringRedisTemplate} Bean Name
     */
    String STRING_REDIS_TEMPLATE_BEAN_NAME = "stringRedisTemplate";

    /**
     * The default property value of Spring Application Name.
     */
    String DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE = "application";

    /**
     * The property name of Spring Application Name.
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String SPRING_APPLICATION_NAME_PROPERTY_NAME = "spring.application.name";

    /**
     * The property name prefix of Microsphere Redis.
     */
    String MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX = "microsphere.redis.";

    /**
     * The property name of Microsphere Redis enabled in Spring.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = "false",
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + "enabled";

    /**
     * The default value of Microsphere Redis enabled.
     */
    boolean DEFAULT_MICROSPHERE_REDIS_ENABLED = getBoolean(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME);

    /**
     * The property name prefix of {@link RedisCommandEvent} exposed in Spring.
     */
    String MICROSPHERE_REDIS_COMMAND_EVENT_PROPERTY_NAME_PREFIX = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + "command-event.";

    /**
     * The default property value of {@link RedisCommandEvent} exposed in Spring.
     */
    String DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_VALUE = "true";

    /**
     * The property name of {@link RedisCommandEvent} exposed in Spring.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME = MICROSPHERE_REDIS_COMMAND_EVENT_PROPERTY_NAME_PREFIX + "exposed";

    /**
     * The default value of {@link RedisCommandEvent} exposed
     */
    boolean DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED = parseBoolean(DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_VALUE);

    /**
     * The default property value of Microsphere Redis fail-fast enabled.
     */
    String DEFAULT_MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_VALUE = "true";

    /**
     * The property name of Microsphere Redis fail-fast enabled.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_VALUE,
            source = SYSTEM_PROPERTIES_SOURCE
    )
    String MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_NAME = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + "fail-fast";

    /**
     * The fail-fast enabled
     */
    boolean MICROSPHERE_REDIS_FAIL_FAST_ENABLED = parseBoolean(getProperty(MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_NAME, DEFAULT_MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_VALUE));

    /**
     * The default property value of Wrapped {@link RedisTemplate} list of Spring Bean names.
     */
    String DEFAULT_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_VALUE = "*";

    /**
     * Wrapped {@link RedisTemplate} list of Spring Bean names.
     */
    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + "wrapped-redis-templates";

    /**
     * The all wrapped bean names of {@link RedisTemplate}: "*"
     */
    Set<String> ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES = ofSet(DEFAULT_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_VALUE);

    /**
     * The prefix of Redis Interceptors' property name
     */
    String MICROSPHERE_REDIS_INTERCEPTOR_PROPERTY_NAME_PREFIX = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + "interceptor.";

    /**
     * The default property value of Redis Interceptor enabled.
     */
    String DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_VALUE = "true";

    /**
     * The property name of Redis Interceptor enabled in Spring.
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    String MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME = MICROSPHERE_REDIS_INTERCEPTOR_PROPERTY_NAME_PREFIX + "enabled";

    /**
     * The default Redis Interceptor enabled
     */
    boolean DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED = parseBoolean(DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_VALUE);

    /**
     * The default placeholder of Wrapped {@link RedisTemplate} list of Spring Bean names.
     */
    String DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER = "${" + WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME + ":}";
}
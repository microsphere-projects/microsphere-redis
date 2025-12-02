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

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import io.microsphere.util.Utils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisConstants.ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.SPRING_APPLICATION_NAME_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME;
import static io.microsphere.spring.beans.BeanUtils.getBeanNames;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimAllWhitespace;

/**
 * The utils class for Microsphere Redis Spring
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Utils
 * @since 1.0.0
 */
public abstract class RedisSpringUtils implements Utils {

    private static final Logger logger = getLogger(RedisSpringUtils.class);

    @Nonnull
    public static String getApplicationName(Environment environment) {
        return environment.getProperty(SPRING_APPLICATION_NAME_PROPERTY_NAME, DEFAULT_SPRING_APPLICATION_NAME_PROPERTY_VALUE);
    }

    public static boolean isMicrosphereRedisEnabled(Environment environment) {
        return getBoolean(environment, MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME, DEFAULT_MICROSPHERE_REDIS_ENABLED, "Configuration", "enabled");
    }

    public static boolean isMicrosphereRedisCommandEventExposed(Environment environment) {
        return getBoolean(environment, MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME, DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED, "Command Event", "exposed");
    }

    @Nonnull
    @Immutable
    public static Set<String> getWrappedRedisTemplateBeanNames(ConfigurableListableBeanFactory beanFactory,
                                                               Environment environment, String... wrapRedisTemplates) {
        if (isEmpty(wrapRedisTemplates)) {
            return resolveWrappedRedisTemplateBeanNames(beanFactory, environment);
        } else {
            return resolveWrappedRedisTemplateBeanNames(environment, wrapRedisTemplates);
        }
    }

    public static RedisTemplate<?, ?> findRedisTemplate(BeanFactory beanFactory, String redisTemplateBeanName) {
        if (beanFactory.containsBean(redisTemplateBeanName)) {
            return beanFactory.getBean(redisTemplateBeanName, RedisTemplate.class);
        }
        return null;
    }

    public static Set<String> findRestTemplateBeanNames(ConfigurableListableBeanFactory beanFactory) {
        Set<String> redisTemplateBeanNames = ofSet(getBeanNames(beanFactory, RedisTemplate.class));
        logger.trace("The all bean names of RedisTemplate : {}", redisTemplateBeanNames);
        return redisTemplateBeanNames;
    }

    public static Set<String> findRedisConnectionFactoryBeanNames(ConfigurableListableBeanFactory beanFactory) {
        Set<String> redisConnectionFactoryBeanNames = ofSet(getBeanNames(beanFactory, RedisConnectionFactory.class));
        logger.trace("The all bean names of RedisConnectionFactory : {}", redisConnectionFactoryBeanNames);
        return redisConnectionFactoryBeanNames;
    }

    public static List<RedisCommandInterceptor> findRedisCommandInterceptors(ListableBeanFactory beanFactory) {
        return getSortedBeans(beanFactory, RedisCommandInterceptor.class);
    }

    public static List<RedisConnectionInterceptor> findRedisConnectionInterceptors(ListableBeanFactory beanFactory) {
        return getSortedBeans(beanFactory, RedisConnectionInterceptor.class);
    }

    /**
     * Resolve the wrapped {@link RedisTemplate} Bean Names
     *
     * @param environment        {@link Environment}
     * @param wrapRedisTemplates The wrapped {@link RedisTemplate} Bean Names
     * @return non-null
     */
    @Nonnull
    static Set<String> resolveWrappedRedisTemplateBeanNames(Environment environment, String... wrapRedisTemplates) {
        Set<String> wrappedRedisTemplateBeanNames = newLinkedHashSet(wrapRedisTemplates.length);
        for (String wrapRedisTemplate : wrapRedisTemplates) {
            String wrappedRedisTemplateBeanName = environment.resolveRequiredPlaceholders(wrapRedisTemplate);
            Set<String> beanNames = commaDelimitedListToSet(wrappedRedisTemplateBeanName);
            for (String beanName : beanNames) {
                String name = trimAllWhitespace(beanName);
                if (hasText(name)) {
                    wrappedRedisTemplateBeanNames.add(name);
                }
            }
        }
        return unmodifiableSet(wrappedRedisTemplateBeanNames);
    }

    /**
     * Resolve the wrapped {@link RedisTemplate} Bean Name list, the default value is from {@link Collections#emptySet()}
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @return If no configuration is found, {@link Collections#emptySet()} is returned
     */
    @Nonnull
    static Set<String> resolveWrappedRedisTemplateBeanNames(ConfigurableListableBeanFactory beanFactory, Environment environment) {
        Set<String> wrappedRedisTemplateBeanNames = environment.getProperty(WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME, Set.class);
        if (wrappedRedisTemplateBeanNames == null) {
            return emptySet();
        } else if (ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES.equals(wrappedRedisTemplateBeanNames)) {
            return findRestTemplateBeanNames(beanFactory);
        } else {
            String[] beanNames = wrappedRedisTemplateBeanNames.toArray(EMPTY_STRING_ARRAY);
            return resolveWrappedRedisTemplateBeanNames(environment, beanNames);
        }
    }

    public static boolean getBoolean(Environment environment, String propertyName, boolean defaultValue, String feature, String statusIfTrue) {
        Boolean propertyValue = environment.getProperty(propertyName, Boolean.class);
        boolean value = propertyValue == null ? defaultValue : propertyValue.booleanValue();
        logger.trace("Microsphere Redis {} is '{}' in the Spring Environment[property name: '{}' , property value: {} , default value: {}",
                feature, (value ? statusIfTrue : "not " + statusIfTrue), propertyName, propertyValue, defaultValue);
        return value;
    }

    private RedisSpringUtils() {
    }
}
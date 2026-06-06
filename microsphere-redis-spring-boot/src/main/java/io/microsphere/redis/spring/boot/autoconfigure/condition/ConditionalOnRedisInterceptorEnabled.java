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

import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.util.RedisConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link ConditionalOnProperty} that checks
 * if Redis Interceptor is enabled via the property {@value io.microsphere.redis.spring.util.RedisConstants#MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME}.
 * <p>
 * This annotation can be used on configuration classes or bean methods to conditionally enable
 * Redis Interceptor-related components based on the configuration property. By default, if the property is
 * not specified, Redis Interceptor is considered enabled ({@code matchIfMissing = true}).
 *
 * <h3>Examples</h3>
 *
 * <h4>1. Enable a Configuration Class when {@link EnableRedisInterceptor Redis Interceptor is Enabled}</h4>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnRedisInterceptorEnabled
 * public class RedisInterceptorConfiguration {
 * }
 * }</pre>
 *
 * <h4>2. Disable Redis Interceptor by Setting Property to False</h4>
 * In your {@code application.properties} or {@code application.yml}:
 * <pre>{@code
 * # application.properties
 * microsphere.redis.interceptor.enabled=false
 *
 * # OR application.yml
 * microsphere:
 *   redis:
 *     interceptor:
 *       enabled: false
 * }</pre>
 * With the above configuration, any component annotated with {@code @ConditionalOnRedisInterceptorEnabled}
 * will not be loaded.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConstants#MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME
 * @see EnableRedisInterceptor
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@ConditionalOnRedisEnabled
@ConditionalOnProperty(name = MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME, matchIfMissing = true)
public @interface ConditionalOnRedisInterceptorEnabled {
}
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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A composed condition annotation that checks whether Redis is available for use.
 * <p>
 * This annotation combines {@link ConditionalOnRedisEnabled} and
 * {@link org.springframework.boot.autoconfigure.condition.ConditionalOnClass} to ensure
 * that Redis is explicitly enabled and the required Redis classes are present on the classpath.
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * @Configuration
 * @ConditionalOnRedisAvailable
 * public class RedisAutoConfiguration {
 *
 *     @Bean
 *     public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
 *         RedisTemplate<String, Object> template = new RedisTemplate<>();
 *         template.setConnectionFactory(connectionFactory);
 *         return template;
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConditionalOnRedisEnabled
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@ConditionalOnRedisEnabled
@ConditionalOnClass(name = {
        "org.springframework.data.redis.connection.RedisConnection",      // Spring Data Redis API
        "io.microsphere.redis.metadata.RedisMetadataLoader",              // Microsphere Redis Core API
        "io.microsphere.redis.spring.interceptor.RedisMethodInterceptor", // Microsphere Redis Spring API
})
public @interface ConditionalOnRedisAvailable {
}
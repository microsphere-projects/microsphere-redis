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

package io.microsphere.redis.spring.boot.autoconfigure;

import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.boot.autoconfigure.condition.ConditionalOnRedisAvailable;
import io.microsphere.redis.spring.context.RedisInitializer;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;

/**
 * Microsphere Redis Auto Configuration.
 * <p>
 * This configuration class is activated when Redis is available in the application context.
 * It is configured to run after Spring Boot's default {@code RedisAutoConfiguration} to allow
 * for custom overrides or extensions of the default Redis setup.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // In your Spring Boot application, simply include this starter in your dependencies.
 * // The auto-configuration will automatically set up Redis beans if Redis is available.
 *
 * @SpringBootApplication
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisInitializer
 * @see EnableRedisInterceptor
 * @since 1.0.0
 */
@ConditionalOnRedisAvailable
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
public class RedisAutoConfiguration {
}
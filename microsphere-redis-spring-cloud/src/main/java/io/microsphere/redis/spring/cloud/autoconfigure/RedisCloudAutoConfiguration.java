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

package io.microsphere.redis.spring.cloud.autoconfigure;

import io.microsphere.redis.spring.boot.autoconfigure.condition.ConditionalOnRedisAvailable;
import io.microsphere.redis.spring.cloud.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * The Auto-{@link Configuration} for MyBatis Spring Cloud
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Automatically applied by Spring Cloud when the dependency is on the classpath
 *   // and microsphere.mybatis.enabled is true (default).
 *   // Exposes MyBatis feature information via Spring Cloud Actuator HasFeatures.
 *
 *   // application.properties:
 *   // microsphere.mybatis.enabled=true
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Configuration
 * @see org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnRedisAvailable
@ConditionalOnClass(name = {
        "org.springframework.cloud.context.environment.EnvironmentChangeEvent"         // Spring Cloud Context API
})
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"     // Spring Boot [2.0, 4.0)
})
@Import(value = {
        PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class
})
public class RedisCloudAutoConfiguration {
}

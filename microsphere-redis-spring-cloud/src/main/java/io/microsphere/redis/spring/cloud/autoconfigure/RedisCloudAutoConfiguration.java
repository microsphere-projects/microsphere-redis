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
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesAvailable;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static java.util.Collections.emptyList;

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
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
@Import(value = {
        RedisCloudAutoConfiguration.FeaturesConfiguration.class,
        PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class
})
public class RedisCloudAutoConfiguration {

    @ConditionalOnFeaturesAvailable
    public static class FeaturesConfiguration {

        /**
         * The bean name of {@link HasFeatures}
         *
         * @see #redisFeatures(ListableBeanFactory)
         */
        public final static String REDIS_FEATURES_BEAN_NAME = "redisFeatures";

        private static Set<Class<?>> typeFeatures = of(
                RedisTemplate.class,
                StringRedisTemplate.class,
                RedisConnectionFactory.class,
                RedisCommandInterceptor.class,
                RedisConnectionInterceptor.class
        );

        @Bean(name = REDIS_FEATURES_BEAN_NAME)
        public HasFeatures redisFeatures(ListableBeanFactory beanFactory) {
            List<NamedFeature> namedFeatures = newArrayList(typeFeatures.size());
            for (Class<?> type : typeFeatures) {

                if (isBeanPresent(beanFactory, type)) {
                    String name = MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX + type.getSimpleName();
                    namedFeatures.add(new NamedFeature(name, type));
                }
            }
            return new HasFeatures(emptyList(), namedFeatures);
        }
    }
}

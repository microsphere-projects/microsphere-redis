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

import io.microsphere.redis.executor.ExecutorFilter;
import io.microsphere.redis.executor.ExecutorInterceptor;
import io.microsphere.redis.spring.boot.autoconfigure.condition.ConditionalOnMyBatisEnabled;
import io.microsphere.spring.cloud.client.condition.ConditionalOnFeaturesEnabled;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.SetUtils.of;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.redis.constants.PropertyConstants.MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX;
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
 * @see org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration
 * @since 1.0.0
 */
@ConditionalOnMyBatisEnabled
@AutoConfigureAfter(name = {
        "org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration"
})
@Import(MyBatisCloudAutoConfiguration.FeaturesConfiguration.class)
public class RedisCloudAutoConfiguration {

    /**
     * Inner {@link Configuration} that registers the {@link HasFeatures} bean when
     * Spring Cloud features reporting is enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // When @ConditionalOnFeaturesEnabled is satisfied, the "myBatisFeatures" bean is
     *   // automatically created and exposed to the Spring Cloud actuator feature endpoint.
     * }</pre>
     */
    @ConditionalOnFeaturesEnabled
    public static class FeaturesConfiguration {

        /**
         * The bean name of {@link HasFeatures}
         *
         * @see #redisFeatures(ListableBeanFactory)
         */
        public final static String REDIS_FEATURES_BEAN_NAME = "redisFeatures";

        private static Set<Class<?>> typeFeatures = of(
                org.apache.ibatis.session.Configuration.class,
                SqlSessionFactory.class,
                SqlSessionFactoryBean.class,
                SqlSessionTemplate.class,
                ExecutorFilter.class,
                ExecutorInterceptor.class
        );

        /**
         * Create a {@link HasFeatures} bean that exposes the active MyBatis components as named features
         * for the Spring Cloud actuator features endpoint.
         *
         * <h3>Example Usage</h3>
         * <pre>{@code
         *   // Accessed via the Spring Cloud actuator features endpoint at /actuator/features.
         *   // Each present bean type is reported as a named feature:
         *   //   "microsphere.mybatis.SqlSessionFactory" -> SqlSessionFactory.class
         * }</pre>
         *
         * @param beanFactory the {@link ListableBeanFactory} used to check which beans are present
         * @return a {@link HasFeatures} with one {@link NamedFeature} per present MyBatis component type
         */
        @Bean(name = REDIS_FEATURES_BEAN_NAME)
        public HasFeatures redisFeatures(ListableBeanFactory beanFactory) {
            List<NamedFeature> namedFeatures = newArrayList(typeFeatures.size());
            for (Class<?> type : typeFeatures) {
                if (isBeanPresent(beanFactory, type)) {
                    String name = MICROSPHERE_MYBATIS_PROPERTY_NAME_PREFIX + DOT + type.getSimpleName();
                    namedFeatures.add(new NamedFeature(name, type));
                }
            }
            return new HasFeatures(emptyList(), namedFeatures);
        }
    }
}

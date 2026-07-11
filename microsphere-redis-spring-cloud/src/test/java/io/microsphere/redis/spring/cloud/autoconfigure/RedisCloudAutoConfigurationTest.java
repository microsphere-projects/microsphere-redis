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


import io.microsphere.redis.metadata.RedisMetadataLoader;
import io.microsphere.redis.spring.cloud.event.PropagatingRedisConfigurationPropertyChangedEventApplicationListener;
import io.microsphere.redis.spring.interceptor.RedisMethodInterceptor;
import io.microsphere.spring.boot.test.AutoConfigurationTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.data.redis.connection.RedisConnection;

import java.util.Set;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

/**
 * {@link RedisCloudAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(
        classes = {
                RedisCloudAutoConfigurationTest.class
        },
        webEnvironment = NONE
)
class RedisCloudAutoConfigurationTest extends AutoConfigurationTest<RedisCloudAutoConfiguration> {

    @Override
    protected void configureAutoConfiguredClasses(Set<Class<?>> autoConfiguredClasses) {
        autoConfiguredClasses.add(PropagatingRedisConfigurationPropertyChangedEventApplicationListener.class);
    }

    @Override
    protected void configureGlobalDisabledPropertyValues(Set<String> globalDisabledPropertyValues) {
        globalDisabledPropertyValues.add("microsphere.redis.enabled=false");
    }

    @Override
    protected void configureGlobalMissingClasses(Set<Class<?>> globalMissingClasses) {
        globalMissingClasses.add(RedisConnection.class);
        globalMissingClasses.add(RedisMetadataLoader.class);
        globalMissingClasses.add(RedisMethodInterceptor.class);
        globalMissingClasses.add(EnvironmentChangeEvent.class);
    }
}
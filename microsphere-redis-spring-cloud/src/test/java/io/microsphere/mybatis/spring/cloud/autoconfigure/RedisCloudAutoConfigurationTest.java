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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.actuator.HasFeatures;
import org.springframework.cloud.client.actuator.NamedFeature;

import java.util.List;

import static io.microsphere.redis.spring.cloud.autoconfigure.MyBatisCloudAutoConfiguration.FeaturesConfiguration.MYBATIS_FEATURES_BEAN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RedisCloudAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCloudAutoConfiguration
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        RedisCloudAutoConfigurationTest.class
})
@EnableAutoConfiguration
class RedisCloudAutoConfigurationTest {

    @Autowired
    @Qualifier(MYBATIS_FEATURES_BEAN_NAME)
    private HasFeatures hasFeatures;

    @Test
    void test() {
        List<NamedFeature> namedFeatures = hasFeatures.getNamedFeatures();
        assertEquals(2, namedFeatures.size());
    }
}

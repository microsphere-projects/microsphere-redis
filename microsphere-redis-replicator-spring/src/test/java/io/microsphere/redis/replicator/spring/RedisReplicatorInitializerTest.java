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

package io.microsphere.redis.replicator.spring;


import io.microsphere.redis.replicator.spring.config.FullRedisReplicationConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.REDIS_REPLICATOR_CONSUMER_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.REDIS_REPLICATOR_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.lang.String.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisReplicatorInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisReplicatorInitializer
 * @since 1.0.0
 */
class RedisReplicatorInitializerTest {

    private RedisReplicatorInitializer initializer;

    @BeforeEach
    void setUp() {
        this.initializer = new RedisReplicatorInitializer();
    }

    @Test
    void testSupports() {
        testInSpringContainer((context, environment) -> {
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
            assertTrue(this.initializer.supports(context, registry));

            setProperties(context, REDIS_REPLICATOR_ENABLED_PROPERTY_NAME, "false");
            assertFalse(this.initializer.supports(context, registry));
        });
    }

    @Test
    void testInitialize() {
        assertInitialize(false, null);
        assertInitialize(false, DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE);
    }

    @Test
    void testInitializeOnConsumerEnabled() {
        assertInitialize(true, DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE);
        assertInitialize(true, null);
    }

    void assertInitialize(boolean consumerEnabled, String bootstrapServers) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) context.getBeanFactory();
        Map<String, Object> properties = new HashMap<>();
        properties.put(REDIS_REPLICATOR_CONSUMER_ENABLED_PROPERTY_NAME, valueOf(consumerEnabled));
        if (bootstrapServers != null) {
            properties.put(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME, bootstrapServers);
        }
        setProperties(context, properties);

        this.initializer.initialize(context, registry);

        context.register(FullRedisReplicationConfig.class);
        context.refresh();
        context.close();
    }

    void setProperties(ConfigurableApplicationContext context, String... properties) {
        setProperties(context, ofMap(properties));
    }

    void setProperties(ConfigurableApplicationContext context, Map<String, Object> properties) {
        ConfigurableEnvironment environment = context.getEnvironment();
        MapPropertySource testPropertySource = new MapPropertySource("testPropertySource", properties);
        environment.getPropertySources().addFirst(testPropertySource);
    }

    @Test
    void testGetOrder() {
        assertEquals(0, this.initializer.getOrder());
    }
}
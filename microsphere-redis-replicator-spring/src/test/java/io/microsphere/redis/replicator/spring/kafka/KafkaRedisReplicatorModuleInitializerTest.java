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

package io.microsphere.redis.replicator.spring.kafka;


import io.microsphere.redis.replicator.spring.config.FullRedisReplicationConfig;
import io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration;
import io.microsphere.redis.replicator.spring.kafka.producer.KafkaProducerRedisCommandEventListener;
import io.microsphere.redis.replicator.spring.kafka.producer.KafkaProducerRedisReplicatorConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_CONSUMER_ENABLED_PROPERTY_NAME;
import static java.lang.ClassLoader.getSystemClassLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link KafkaRedisReplicatorModuleInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see KafkaRedisReplicatorModuleInitializer
 * @since 1.0.0
 */
class KafkaRedisReplicatorModuleInitializerTest {

    private MockEnvironment environment;

    private ConfigurableApplicationContext context;

    private ConfigurableListableBeanFactory beanFactory;

    private BeanDefinitionRegistry registry;

    private KafkaRedisReplicatorModuleInitializer initializer;

    @BeforeEach
    void setUp() {
        this.environment = new MockEnvironment();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(FullRedisReplicationConfig.class);
        context.setEnvironment(this.environment);
        this.context = context;
        this.beanFactory = context.getBeanFactory();
        this.registry = (BeanDefinitionRegistry) this.beanFactory;
        this.initializer = new KafkaRedisReplicatorModuleInitializer();
    }

    @AfterEach
    void tearDown() {
        this.context.close();
    }

    @Test
    void testSupports() {
        this.environment.setProperty(SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME, DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE);
        assertTrue(this.initializer.supports(this.context));

        this.environment.setProperty(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME, DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE);
        assertTrue(this.initializer.supports(this.context));
    }

    @Test
    void testSupportsOnUnsupported() {
        assertFalse(this.initializer.supports(this.context));

        if (this.context instanceof DefaultResourceLoader) {
            ((DefaultResourceLoader) this.context).setClassLoader(getSystemClassLoader().getParent());
        }

        assertFalse(this.initializer.supports(this.context));
    }

    @Test
    void testInitializeProducerModule() {
        this.initializer.initializeProducerModule(this.context, registry);

        this.context.refresh();
        assertEquals(1, this.beanFactory.getBeansOfType(KafkaProducerRedisReplicatorConfiguration.class).size());
        assertEquals(1, this.beanFactory.getBeansOfType(KafkaProducerRedisCommandEventListener.class).size());
    }

    @Test
    void testInitializeConsumerModule() {
        this.initializer.initializeConsumerModule(this.context, this.registry);

        this.context.refresh();
        assertEquals(1, this.beanFactory.getBeansOfType(KafkaConsumerRedisReplicatorConfiguration.class).size());
    }

    @Test
    void testInitializeConsumerModuleOnDisabled() {
        this.environment.setProperty(KAFKA_CONSUMER_ENABLED_PROPERTY_NAME, "false");
        this.initializer.initializeConsumerModule(this.context, this.registry);

        this.context.refresh();
        assertEquals(0, this.beanFactory.getBeansOfType(KafkaConsumerRedisReplicatorConfiguration.class).size());
    }
}
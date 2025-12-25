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

package io.microsphere.redis.replicator.spring.kafka.producer;


import io.microsphere.redis.replicator.spring.config.DefaultRedisReplicationConfig;
import io.microsphere.redis.replicator.spring.config.FullRedisReplicationConfig;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import org.junit.jupiter.api.Test;

import static io.microsphere.redis.replicator.spring.kafka.producer.KafkaProducerRedisReplicatorConfiguration.DEFAULT_KAFKA_PRODUCER_KEY_PREFIX;
import static io.microsphere.redis.replicator.spring.kafka.producer.KafkaProducerRedisReplicatorConfiguration.KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.producer.KafkaProducerRedisReplicatorConfiguration.KAFKA_PRODUCER_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link KafkaProducerRedisReplicatorConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see KafkaProducerRedisReplicatorConfiguration
 * @since 1.0.0
 */
class KafkaProducerRedisReplicatorConfigurationTest {

    @Test
    void testConstants() {
        assertEquals("microsphere.redis.replicator.kafka.producer.", KAFKA_PRODUCER_PROPERTY_NAME_PREFIX);
        assertEquals("RPE-", DEFAULT_KAFKA_PRODUCER_KEY_PREFIX);
        assertEquals("microsphere.redis.replicator.kafka.key-prefix", KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME);
    }

    @Test
    void testOnDefault() {
        testInSpringContainer(context -> {
            KafkaProducerRedisReplicatorConfiguration configuration = context.getBean(KafkaProducerRedisReplicatorConfiguration.class);
            assertNotNull(configuration.getRedisReplicatorKafkaTemplate());
            assertEquals(DEFAULT_KAFKA_PRODUCER_KEY_PREFIX, configuration.getKeyPrefix());
        }, KafkaProducerRedisReplicatorConfiguration.class, RedisReplicatorConfiguration.class, DefaultRedisReplicationConfig.class);
    }

    @Test
    void testOnCustomized() {
        testInSpringContainer(context -> {
            KafkaProducerRedisReplicatorConfiguration configuration = context.getBean(KafkaProducerRedisReplicatorConfiguration.class);
            assertNotNull(configuration.getRedisReplicatorKafkaTemplate());
            assertEquals("redis-replicator-event-", configuration.getKeyPrefix());
        }, KafkaProducerRedisReplicatorConfiguration.class, RedisReplicatorConfiguration.class, FullRedisReplicationConfig.class);
    }

    @Test
    void testDestroyOnNotInitialized() throws Exception {
        KafkaProducerRedisReplicatorConfiguration configuration = new KafkaProducerRedisReplicatorConfiguration();
        configuration.destroy();
    }
}
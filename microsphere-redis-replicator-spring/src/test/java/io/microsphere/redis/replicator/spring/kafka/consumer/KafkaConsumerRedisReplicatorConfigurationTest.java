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

package io.microsphere.redis.replicator.spring.kafka.consumer;


import io.microsphere.redis.replicator.spring.config.FullRedisReplicationConfig;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_CONSUMER_ENABLED;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_CONSUMER_ENABLED_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_LISTENER_CONCURRENCY;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_LISTENER_CONCURRENCY_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.GROUP_ID_CONFIG;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_CONSUMER_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_CONSUMER_GROUP_ID_PREFIX;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_CONSUMER_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.KAFKA_LISTENER_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.kafka.consumer.KafkaConsumerRedisReplicatorConfiguration.isKafkaConsumerEnabled;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link KafkaConsumerRedisReplicatorConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see KafkaConsumerRedisReplicatorConfiguration
 * @since 1.0.0
 */
@SpringJUnitConfig(
        classes = {
                RedisReplicatorConfiguration.class,
                KafkaRedisReplicatorConfiguration.class,
                KafkaConsumerRedisReplicatorConfiguration.class,
                FullRedisReplicationConfig.class
        }
)
@TestPropertySource(properties = {
        "microsphere.redis.replicator.kafka.consumer.enabled=true",
        "microsphere.redis.replicator.kafka.listener.poll-timeout=5000",
        "microsphere.redis.replicator.kafka.listener.concurrency=3"

})
class KafkaConsumerRedisReplicatorConfigurationTest {

    @Autowired
    private KafkaConsumerRedisReplicatorConfiguration kafkaConsumerRedisReplicatorConfiguration;

    @Autowired
    private ApplicationContext context;

    @Test
    void testConstants() {
        assertEquals("true", DEFAULT_KAFKA_CONSUMER_ENABLED_PROPERTY_VALUE);
        assertEquals("10000", DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_VALUE);
        assertEquals("1", DEFAULT_KAFKA_LISTENER_CONCURRENCY_PROPERTY_VALUE);
        assertEquals("group.id", GROUP_ID_CONFIG);
        assertEquals("microsphere.redis.replicator.kafka.consumer.", KAFKA_CONSUMER_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.redis.replicator.kafka.listener.", KAFKA_LISTENER_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.redis.replicator.kafka.consumer.enabled", KAFKA_CONSUMER_ENABLED_PROPERTY_NAME);
        assertEquals("microsphere.redis.replicator.kafka.listener.poll-timeout", KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME);
        assertEquals("microsphere.redis.replicator.kafka.listener.concurrency", KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME);
        assertEquals("Redis-Replicator-", KAFKA_CONSUMER_GROUP_ID_PREFIX);
        assertEquals(true, DEFAULT_KAFKA_CONSUMER_ENABLED);
        assertEquals(10000, DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT);
        assertEquals(1, DEFAULT_KAFKA_LISTENER_CONCURRENCY);
    }

    @Test
    void testProperties() {
        assertTrue(isKafkaConsumerEnabled(this.context));
        assertEquals(5000, kafkaConsumerRedisReplicatorConfiguration.listenerPollTimeOut);
        assertEquals(3, kafkaConsumerRedisReplicatorConfiguration.listenerConcurrency);

        testInSpringContainer((context, environment) -> {
            assertTrue(isKafkaConsumerEnabled(context));

            MutablePropertySources propertySources = environment.getPropertySources();
            MockPropertySource mockPropertySource = new MockPropertySource();
            mockPropertySource.setProperty(KAFKA_CONSUMER_ENABLED_PROPERTY_NAME, false);
            propertySources.addFirst(mockPropertySource);
            assertFalse(isKafkaConsumerEnabled(context));
        });
    }

    @Test
    void testConsumeRecordOnFailed() {
        ConsumerRecord<byte[], byte[]> consumerRecord = new ConsumerRecord<>("topic", 0, 0, null, null);
        kafkaConsumerRedisReplicatorConfiguration.consumeRecord(consumerRecord);
    }
}
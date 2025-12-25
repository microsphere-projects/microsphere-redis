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
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.KAFKA_TOPIC_PREFIX_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration.SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link KafkaRedisReplicatorConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see KafkaRedisReplicatorConfiguration
 * @since 1.0.0
 */
@SpringJUnitConfig(
        classes = {
                RedisReplicatorConfiguration.class,
                KafkaRedisReplicatorConfiguration.class,
                FullRedisReplicationConfig.class
        }
)
class KafkaRedisReplicatorConfigurationTest {

    @Autowired
    private KafkaRedisReplicatorConfiguration kafkaRedisReplicatorConfiguration;

    @AfterEach
    void tearDown() throws Exception {
        this.kafkaRedisReplicatorConfiguration.destroy();
    }

    @Test
    void testConstants() {
        assertEquals("127.0.0.1:9092", DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE);
        assertEquals("spring.kafka.bootstrap-servers", SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME);
        assertEquals("${spring.kafka.bootstrap-servers:127.0.0.1:9092}", SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER);
        assertEquals("microsphere.redis.replicator.kafka.", KAFKA_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.redis.replicator.kafka.bootstrap.servers", KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME);
        assertEquals("${microsphere.redis.replicator.kafka.bootstrap.servers:${spring.kafka.bootstrap-servers:127.0.0.1:9092}}", KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER);
        assertEquals("redis-replicator-event-topic-", DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE);
        assertEquals("microsphere.redis.replicator.kafka.topic-prefix", KAFKA_TOPIC_PREFIX_PROPERTY_NAME);

        MockEnvironment mockEnvironment = new MockEnvironment();
        assertEquals(DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE, mockEnvironment.resolvePlaceholders(SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER));
        assertEquals(DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE, mockEnvironment.resolvePlaceholders(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER));
    }

    @Test
    void testCreateTopic() {
        assertEquals("redis-replicator-event-topic-default", this.kafkaRedisReplicatorConfiguration.createTopic("default"));
    }

    @Test
    void testGetDomain() {
        assertEquals("default", this.kafkaRedisReplicatorConfiguration.getDomain("redis-replicator-event-topic-default"));
    }

    @Test
    void testGetTopics() {
        // default,test,fixed,duplicated
        String[] domains = ofArray("default", "test", "fixed", "duplicated");
        String[] topics = this.kafkaRedisReplicatorConfiguration.getTopics();
        for (int i = 0; i < domains.length; i++) {
            assertEquals(this.kafkaRedisReplicatorConfiguration.createTopic(domains[i]), topics[i]);
        }
    }
}
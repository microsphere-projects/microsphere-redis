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

import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.apache.kafka.clients.producer.Partitioner;

/**
 * The strategy class calculates the partition for the given {@link RedisCommandEvent}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandEvent
 * @see Partitioner
 * @since 1.0.0
 */
public interface RedisComandEventPartitioner {

    /**
     * Calculate the partition for the given {@link RedisCommandEvent}
     *
     * @param event {@link RedisCommandEvent}
     * @return <code>null</code> if the partition will be calculated by Kafka
     */
    Integer partition(RedisCommandEvent event);
}

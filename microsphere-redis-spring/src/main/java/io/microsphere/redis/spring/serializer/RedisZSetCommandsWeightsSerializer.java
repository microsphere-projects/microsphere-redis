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

package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.RedisZSetCommands.Weights;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

import static io.microsphere.redis.spring.serializer.Serializers.defaultSerialize;

/**
 * {@link RedisZSetCommands.Weights} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSerializer
 * @see RedisZSetCommands
 * @see Weights
 * @since 1.0.0
 */
public class RedisZSetCommandsWeightsSerializer extends AbstractSerializer<Weights> {

    public static final RedisZSetCommandsWeightsSerializer REDIS_ZSET_COMMANDS_WEIGHTS_SERIALIZER = new RedisZSetCommandsWeightsSerializer();

    @Override
    protected byte[] doSerialize(Weights weights) throws SerializationException {
        List<Double> doubles = weights.toList();
        return defaultSerialize(doubles);
    }

    @Override
    protected Weights doDeserialize(byte[] bytes) throws SerializationException {
        List<Double> doubles = Serializers.deserialize(bytes, List.class);
        int size = doubles.size();
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = doubles.get(i);
        }
        return Weights.of(weights);
    }
}
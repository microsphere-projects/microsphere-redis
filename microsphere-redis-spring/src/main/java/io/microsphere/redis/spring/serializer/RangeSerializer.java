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

import org.springframework.data.domain.Range;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static io.microsphere.redis.spring.serializer.RangeModel.from;
import static io.microsphere.redis.spring.serializer.Serializers.defaultDeserialize;
import static io.microsphere.redis.spring.serializer.Serializers.defaultSerialize;

/**
 * {@link RedisSerializer} class for {@link Range}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Range
 * @see RedisZSetCommandsRangeSerializer
 * @since 1.0.0
 */
public class RangeSerializer extends AbstractSerializer<Range> {

    public static final RangeSerializer RANGE_SERIALIZER = new RangeSerializer();

    @Override
    protected byte[] doSerialize(Range range) throws SerializationException {
        RangeModel rangeModel = from(range);
        return defaultSerialize(rangeModel);
    }

    public byte[] serialize(RangeModel rangeModel) throws SerializationException {
        return defaultSerialize(rangeModel);
    }

    @Override
    protected Range doDeserialize(byte[] bytes) throws SerializationException {
        RangeModel rangeModel = defaultDeserialize(bytes);
        return rangeModel.toRange();
    }
}
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

import io.microsphere.redis.serializer.Serializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Delegating {@link RedisSerializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisSerializer
 * @since 1.0.0
 */
final class DelegatingRedisSerializer<T> implements RedisSerializer<T> {

    private final Serializer<T> serializer;

    public DelegatingRedisSerializer(Serializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        return serializer.serialize(t);
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        return serializer.deserialize(bytes);
    }

    @Override
    public boolean canSerialize(Class<?> type) {
        return serializer.canSerialize(type);
    }

    @Override
    public Class<?> getTargetType() {
        return serializer.getTargetType();
    }

    public static <T> RedisSerializer<T> createDelegate(Serializer<T> serializer) {
        return new DelegatingRedisSerializer<>(serializer);
    }
}

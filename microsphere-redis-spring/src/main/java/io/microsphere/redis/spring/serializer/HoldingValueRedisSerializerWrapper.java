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

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.util.ValueHolder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.lang.Nullable;

import static io.microsphere.redis.util.ValueHolder.get;

/**
 * {@link RedisSerializer} decorator that transparently caches the bidirectional mapping
 * between Java objects and their serialized byte arrays in the thread-local
 * {@link ValueHolder}, so that the interceptor layer can later look up the original object
 * given the raw bytes and vice versa.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Wrap an existing serializer
 *   RedisSerializer<String> original = RedisSerializer.string();
 *   RedisSerializer<String> wrapped  = HoldingValueRedisSerializerWrapper.wrap(original);
 *
 *   // Serialization caches the mapping
 *   byte[] bytes = wrapped.serialize("hello");
 *   String back  = (String) ValueHolder.get().getValue(bytes); // "hello"
 *
 *   // Wrap all serializers on a RedisTemplate at once
 *   HoldingValueRedisSerializerWrapper.wrap(redisTemplate);
 * }</pre>
 *
 * @param <T> the type handled by the delegate serializer
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class HoldingValueRedisSerializerWrapper<T> implements RedisSerializer<T>, DelegatingWrapper {

    private final RedisSerializer<T> delegate;

    /**
     * Creates a wrapper around the given delegate serializer.
     *
     * @param delegate the actual {@link RedisSerializer} to delegate serialization work to
     */
    public HoldingValueRedisSerializerWrapper(RedisSerializer<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    @Nullable
    public byte[] serialize(T value) throws SerializationException {
        // Try to find the ThreadLocal cached result
        ValueHolder valueHolder = get();
        byte[] rawValue = valueHolder.getRawValue(value);
        if (rawValue == null) {
            rawValue = this.delegate.serialize(value);
            // Cache the first time serialization
            valueHolder.set(value, rawValue);
        }
        return rawValue;
    }

    @Override
    @Nullable
    public T deserialize(byte[] bytes) throws SerializationException {
        // Try to find the ThreadLocal cached result
        ValueHolder valueHolder = get();
        T value = (T) valueHolder.getValue(bytes);
        if (value == null) {
            value = this.delegate.deserialize(bytes);
            valueHolder.set(value, bytes);
        }
        return value;
    }

    public boolean canSerialize(Class type) {
        return Serializers.canSerialize(this.delegate, type);
    }

    public Class<?> getTargetType() {
        return Serializers.getTargetType(this.delegate);
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }

    /**
     * Wraps the given {@link RedisSerializer} with a {@link HoldingValueRedisSerializerWrapper},
     * unless it is already wrapped or is {@code null}.
     *
     * @param <T>             the serializer target type
     * @param redisSerializer the serializer to wrap; may be {@code null}
     * @return the wrapped serializer, or {@code redisSerializer} if already wrapped or {@code null}
     */
    public static <T> RedisSerializer<T> wrap(RedisSerializer<T> redisSerializer) {
        if (redisSerializer == null || redisSerializer instanceof HoldingValueRedisSerializerWrapper) {
            return redisSerializer;
        }
        return new HoldingValueRedisSerializerWrapper<>(redisSerializer);
    }

    /**
     * Wraps all serializers (default, key, value, hash-key, hash-value) on the given
     * {@link RedisTemplate} with {@link HoldingValueRedisSerializerWrapper} instances.
     *
     * @param redisTemplate the template whose serializers should be wrapped
     */
    public static void wrap(RedisTemplate redisTemplate) {
        redisTemplate.setDefaultSerializer(wrap(redisTemplate.getDefaultSerializer()));
        redisTemplate.setKeySerializer(wrap(redisTemplate.getKeySerializer()));
        redisTemplate.setValueSerializer(wrap(redisTemplate.getValueSerializer()));
        redisTemplate.setHashKeySerializer(wrap(redisTemplate.getHashKeySerializer()));
        redisTemplate.setHashValueSerializer(wrap(redisTemplate.getHashValueSerializer()));
    }
}
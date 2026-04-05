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
package io.microsphere.redis.util;

import io.microsphere.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.lang.ThreadLocal.withInitial;

/**
 * Thread-local holder that maintains a bidirectional cache between Java objects and their
 * raw byte-array ({@link RawValue}) representations.  During a Redis serialization cycle
 * (e.g. inside a {@link org.springframework.data.redis.serializer.RedisSerializer}), the
 * original value and its serialized bytes are stored so the interceptor layer can later
 * look up the original object given the raw bytes, and vice versa.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Inside a serializer wrapper – store value with its raw bytes
 *   Object value = "my-redis-value";
 *   byte[] rawBytes = redisSerializer.serialize(value);
 *   ValueHolder.get().set(value, rawBytes);
 *
 *   // Later, retrieve the original value from raw bytes
 *   Object resolved = ValueHolder.get().getValue(rawBytes); // "my-redis-value"
 *
 *   // Or retrieve the raw bytes from the original value
 *   byte[] raw = ValueHolder.get().getRawValue(value);
 *
 *   // Clean up the thread-local state after each request / command execution
 *   ValueHolder.clear();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ValueHolder {

    private static final Logger logger = getLogger(ValueHolder.class);

    private static final ThreadLocal<ValueHolder> holder = withInitial(() -> new ValueHolder(4));

    private final Map<Object, Object> cache;

    /**
     * Creates a {@link ValueHolder} with an internal cache sized to {@code initialCapacity * 2}
     * (to accommodate both directions of the bidirectional mapping).
     *
     * @param initialCapacity the initial number of value-pair mappings to preallocate
     */
    public ValueHolder(int initialCapacity) {
        this.cache = new HashMap<>(initialCapacity << 1);
    }

    /**
     * Stores a bidirectional mapping between {@code value} and its serialized {@code rawValue} bytes.
     * After this call, {@link #getValue(byte[])} will return {@code value}, and
     * {@link #getRawValue(Object)} will return {@code rawValue}.
     *
     * @param value    the deserialized Java object
     * @param rawValue the serialized byte array representation of {@code value}
     */
    public void set(Object value, byte[] rawValue) {
        RawValue rawValueObject = RawValue.of(rawValue);
        put(value, rawValueObject);
        put(rawValueObject, value);
    }

    private void put(Object key, Object value) {
        Object oldValue = cache.put(key, value);
        logger.trace("Put key[{}] and value[{}] into cache, old value : {}", key, value, oldValue);
    }

    /**
     * Retrieves the original Java object associated with the given raw byte array.
     *
     * @param rawValue the serialized byte array key
     * @return the previously stored Java object, or {@code null} if not found
     */
    public Object getValue(byte[] rawValue) {
        RawValue rawValueObject = RawValue.of(rawValue);
        return cache.get(rawValueObject);
    }

    /**
     * Retrieves the raw byte array associated with the given Java object.
     *
     * @param value the Java object key
     * @return the previously stored raw bytes, or {@code null} if not found
     */
    public byte[] getRawValue(Object value) {
        Object rawValue = cache.get(value);
        return rawValue instanceof RawValue ? ((RawValue) rawValue).getData() : null;
    }

    /**
     * Returns the {@link ValueHolder} bound to the current thread, creating a new instance
     * (with initial capacity 4) if one does not yet exist.
     *
     * @return the current thread's {@link ValueHolder}; never {@code null}
     */
    public static ValueHolder get() {
        return holder.get();
    }

    /**
     * Removes the {@link ValueHolder} from the current thread, releasing all cached mappings
     * and avoiding memory leaks in thread-pool environments.
     */
    public static void clear() {
        holder.remove();
    }

}

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
package io.microsphere.redis.serializer;

import io.microsphere.io.Deserializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static io.microsphere.util.ClassUtils.isAssignableFrom;

/**
 * Redis Serializer interface serialization and deserialization of Objects to byte arrays (binary data).
 *
 * @param <T> the type of the object to be serialized
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see io.microsphere.io.Serializer
 * @see Deserializer
 * @since 1.0.0
 */
public interface Serializer<T> {

    /**
     * Serialize the given object to binary data.
     *
     * @param t object to serialize. Can be {@literal null}.
     * @return the equivalent binary data. Can be {@literal null}.
     */
    @Nullable
    byte[] serialize(@Nullable T t) throws RuntimeException;

    /**
     * Deserialize an object from the given binary data.
     *
     * @param bytes object binary representation. Can be {@literal null}.
     * @return the equivalent object instance. Can be {@literal null}.
     */
    @Nullable
    T deserialize(@Nullable byte[] bytes) throws RuntimeException;

    /**
     * Determine whether the given type can be serialized by this serializer.
     *
     * @param type the type to check
     * @return {@literal true} if the type can be serialized by this serializer, {@literal false} otherwise
     */
    default boolean canSerialize(Class<?> type) {
        return isAssignableFrom(getTargetType(), type);
    }

    /**
     * Get the target type of the serializer
     *
     * @return the target type
     */
    @Nonnull
    default Class<?> getTargetType() {
        return Object.class;
    }

}

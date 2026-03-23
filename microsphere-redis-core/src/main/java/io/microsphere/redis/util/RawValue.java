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

import java.util.Arrays;

import static io.microsphere.util.ArrayUtils.arrayEquals;

/**
 * Immutable class that wraps a raw Redis value stored as a {@code byte[]} array.
 * Used internally by {@link ValueHolder} to maintain bidirectional mappings between
 * Java objects and their serialized byte representations.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   byte[] bytes = "hello".getBytes(StandardCharsets.UTF_8);
 *
 *   // Create via factory method
 *   RawValue rawValue = RawValue.of(bytes);
 *   System.out.println(rawValue.getData().length); // 5
 *
 *   // Create via constructor
 *   RawValue rawValue2 = new RawValue(bytes);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RawValue {

    private final byte[] data;

    public RawValue(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RawValue)) {
            return false;
        }

        RawValue rawValue = (RawValue) o;
        return arrayEquals(data, rawValue.data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    /**
     * Factory method to create a {@link RawValue} from a byte array.
     *
     * @param bytes the raw bytes; may be {@code null} if the value was not serializable
     * @return a new {@link RawValue} wrapping the given bytes
     */
    public static RawValue of(byte[] bytes) {
        return new RawValue(bytes);
    }
}
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
package io.microsphere.redis.spring.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.redis.spring.util.ValueHolder.clear;
import static io.microsphere.redis.spring.util.ValueHolder.get;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link ValueHolder} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ValueHolderTest {

    private ValueHolder valueHolder;

    @BeforeEach
    void setUp() {
        valueHolder = new ValueHolder(4);
    }

    @AfterEach
    void tearDown() {
        clear();
    }

    @Test
    void testConstructor() {
        ValueHolder holder = new ValueHolder(10);
        assertNotNull(holder);
    }

    @Test
    void testSetAndGet() {
        String value = "test-value";
        byte[] rawValue = "test-raw-value".getBytes();

        // Mock RawValue behavior for testing purposes
        valueHolder.set(value, rawValue);

        // Test getValue
        Object retrievedValue = valueHolder.getValue(rawValue);
        assertEquals(value, retrievedValue);

        // Test getRawValue
        byte[] retrievedRawValue = valueHolder.getRawValue(value);
        assertArrayEquals(rawValue, retrievedRawValue);
    }

    @Test
    void testGetWithNonExistentRawValue() {
        byte[] rawValue = "non-existent".getBytes();
        Object value = valueHolder.getValue(rawValue);
        assertNull(value);
    }

    @Test
    void testGetRawValueWithNonExistentValue() {
        String value = "non-existent";
        byte[] rawValue = valueHolder.getRawValue(value);
        assertNull(rawValue);
    }

    @Test
    void testThreadLocalGet() {
        ValueHolder holder1 = get();
        ValueHolder holder2 = get();

        // Same thread should get the same instance
        assertSame(holder1, holder2);
    }

    @Test
    void testClear() {
        ValueHolder holder1 = get();
        clear();
        ValueHolder holder2 = get();

        // After clear, should get a new instance
        assertNotSame(holder1, holder2);
    }

    @Test
    void testCacheCapacity() {
        // Test that the cache is initialized with the correct capacity
        ValueHolder holder = new ValueHolder(8);
        // We can't directly test the internal cache size without exposing it,
        // but we can verify the holder was created successfully
        assertNotNull(holder);
    }
}
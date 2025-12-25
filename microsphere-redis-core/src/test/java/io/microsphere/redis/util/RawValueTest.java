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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.redis.util.RawValue.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RawValue} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RawValue
 * @since 1.0.0
 */
class RawValueTest {

    private static final String TEST_DATA = "test-data";

    private static final byte[] TEST_DATA_BYTES = TEST_DATA.getBytes();

    private RawValue rawValue;

    @BeforeEach
    void setUp() {
        this.rawValue = of(TEST_DATA_BYTES);
    }

    @Test
    void testGetData() {
        assertArrayEquals(TEST_DATA_BYTES, this.rawValue.getData());
    }

    @Test
    void testEquals() {
        assertTrue(this.rawValue.equals(this.rawValue));
        assertTrue(this.rawValue.equals(of(TEST_DATA_BYTES)));
        assertFalse(this.rawValue.equals(null));
        assertFalse(this.rawValue.equals(TEST_DATA));
    }

    @Test
    void testHashCode() {
        assertEquals(this.rawValue.hashCode(), this.rawValue.hashCode());
        assertEquals(this.rawValue.hashCode(), of(TEST_DATA_BYTES).hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.rawValue.toString(), this.rawValue.toString());
        assertEquals(this.rawValue.toString(), of(TEST_DATA_BYTES).toString());
    }
}
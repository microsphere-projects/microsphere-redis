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

package io.microsphere.redis.spring.metadata;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link Parameter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Parameter
 * @since 1.0.0
 */
class ParameterTest {

    private static final String testValue = "test-value";

    private Parameter parameter;

    @BeforeEach
    void setUp() {
        this.parameter = createParameter();
    }

    @Test
    void testGetters() {
        assertEquals(testValue, this.parameter.getValue());
        assertNotNull(this.parameter.getMetadata());
        assertEquals(0, this.parameter.getParameterIndex());
        assertEquals("java.lang.String", this.parameter.getParameterType());
        assertArrayEquals(testValue.getBytes(UTF_8), this.parameter.getRawValue());
    }

    @Test
    void testEquals() {
        assertTrue(this.parameter.equals(this.parameter));
        assertTrue(this.parameter.equals(createParameter()));

        assertFalse(this.parameter.equals(null));
        assertFalse(this.parameter.equals(new Parameter("testValue", new ParameterMetadata(0, "java.lang.String"))));
        assertFalse(this.parameter.equals(new Parameter(testValue, new ParameterMetadata(0, "java.lang.String"))));
        assertFalse(this.parameter.equals(new Parameter(testValue, new ParameterMetadata(1, "java.lang.String"))));
    }

    @Test
    void testHashCode() {
        assertEquals(this.parameter.hashCode(), createParameter().hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter("testValue", new ParameterMetadata(0, "java.lang.String")).hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter(testValue, new ParameterMetadata(0, "java.lang.String")).hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter(testValue, new ParameterMetadata(1, "java.lang.String")).hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter(null, new ParameterMetadata(1, "java.lang.String")).hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter(testValue, null).hashCode());
        assertNotEquals(this.parameter.hashCode(), new Parameter(null, null).hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.parameter.toString(), createParameter().toString());
    }

    private Parameter createParameter() {
        Parameter parameter = new Parameter(testValue, new ParameterMetadata(0, "java.lang.String", "name"));
        parameter.setRawValue(testValue.getBytes(UTF_8));
        return parameter;
    }
}
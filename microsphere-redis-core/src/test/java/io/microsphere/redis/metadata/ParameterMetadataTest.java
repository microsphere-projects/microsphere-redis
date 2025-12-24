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

package io.microsphere.redis.metadata;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ParameterMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ParameterMetadata
 * @since 1.0.0
 */
class ParameterMetadataTest {

    private ParameterMetadata parameterMetadata;

    @BeforeEach
    void setUp() {
        this.parameterMetadata = new ParameterMetadata(0, "java.lang.String", "name");
    }

    @Test
    void testGetters() {
        assertEquals(0, parameterMetadata.getParameterIndex());
        assertEquals("java.lang.String", parameterMetadata.getParameterType());
        assertEquals("name", parameterMetadata.getParameterName());
    }

    @Test
    void testEquals() {
        assertTrue(this.parameterMetadata.equals(this.parameterMetadata));
        assertTrue(this.parameterMetadata.equals(new ParameterMetadata(0, "java.lang.String", "name")));
        assertTrue(this.parameterMetadata.equals(new ParameterMetadata(0, "java.lang.String")));

        assertFalse(this.parameterMetadata.equals(null));
        assertFalse(this.parameterMetadata.equals(new ParameterMetadata(0, "java.lang.Integer", "name")));
        assertFalse(this.parameterMetadata.equals(new ParameterMetadata(0, "java.lang.Integer")));
        assertFalse(this.parameterMetadata.equals(new ParameterMetadata(1, "java.lang.String", "name")));
        assertFalse(this.parameterMetadata.equals(new ParameterMetadata(1, "java.lang.String")));
    }

    @Test
    void testHashCode() {
        assertEquals(this.parameterMetadata.hashCode(), new ParameterMetadata(0, "java.lang.String", "name").hashCode());
        assertEquals(this.parameterMetadata.hashCode(), new ParameterMetadata(0, "java.lang.String").hashCode());
        assertFalse(this.parameterMetadata.hashCode()  == new ParameterMetadata(1, "java.lang.String").hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.parameterMetadata.toString(), new ParameterMetadata(0, "java.lang.String", "name").toString());
        assertNotEquals(this.parameterMetadata.toString(), new ParameterMetadata(0, "java.lang.String").toString());
        assertNotEquals(this.parameterMetadata.toString(), new ParameterMetadata(1, "java.lang.String").toString());
    }
}
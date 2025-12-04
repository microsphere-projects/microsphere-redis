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

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MethodMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodMetadata
 * @since 1.0.0
 */
class MethodMetadataTest {

    private MethodMetadata methodMetadata;

    @BeforeEach
    void setUp() {
        this.methodMetadata = new MethodMetadata();
    }

    @Test
    void test() {
        setAndAssert(this.methodMetadata);

        MethodMetadata methodMetadata1 = new MethodMetadata();
        setAndAssert(methodMetadata1);

        assertTrue(this.methodMetadata.equals(this.methodMetadata));

        assertMethodMetadata(this.methodMetadata, methodMetadata1);

        assertFalse(this.methodMetadata.equals("Hello"));

        methodMetadata1.setParameterTypes(ofArray("java.lang.String", "java.lang.Integer"));
        assertFalse(this.methodMetadata.equals(methodMetadata1));

        methodMetadata1.setMethodName("");
        assertFalse(this.methodMetadata.equals(methodMetadata1));

        methodMetadata1.setInterfaceName("");
        assertFalse(this.methodMetadata.equals(methodMetadata1));

        methodMetadata1.setWrite(false);
        assertFalse(this.methodMetadata.equals(methodMetadata1));

        methodMetadata1.setIndex((short) -1);
        assertFalse(this.methodMetadata.equals(methodMetadata1));
    }

    void assertMethodMetadata(MethodMetadata one, MethodMetadata another) {
        assertTrue(one.equals(another));
        assertTrue(another.equals(one));
        assertEquals(one.hashCode(), another.hashCode());
        assertEquals(one.toString(), another.toString());
    }

    void setAndAssert(MethodMetadata methodMetadata) {
        short index = 0;
        String interfaceName = "io.microsphere.redis.spring.metadata.MethodMetadataTest";
        String methodName = "test";
        String[] parameterTypes = new String[]{"java.lang.String"};
        boolean write = true;

        methodMetadata.setIndex(index);
        methodMetadata.setInterfaceName(interfaceName);
        methodMetadata.setMethodName(methodName);
        methodMetadata.setParameterTypes(parameterTypes);
        methodMetadata.setWrite(write);

        assertEquals(index, methodMetadata.getIndex());
        assertEquals(interfaceName, methodMetadata.getInterfaceName());
        assertEquals(methodName, methodMetadata.getMethodName());
        assertArrayEquals(parameterTypes, methodMetadata.getParameterTypes());
        assertEquals(write, methodMetadata.isWrite());
    }
}
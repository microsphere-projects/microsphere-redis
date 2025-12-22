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

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

        assertEquals(this.methodMetadata, this.methodMetadata);

        assertMethodMetadata(this.methodMetadata, methodMetadata1);

        assertNotEquals(this.methodMetadata, "Hello");

        methodMetadata1.setParameterTypes(ofArray("java.lang.String", "java.lang.Integer"));
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);

        methodMetadata1.setMethodName("");
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);

        methodMetadata1.setInterfaceName("");
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);

        methodMetadata1.setCommands(ofArray(""));
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);

        methodMetadata1.setWrite(false);
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);

        methodMetadata1.setIndex(-1);
        assertNotEquals(this.methodMetadata, methodMetadata1);
        assertNotEquals(methodMetadata1, this.methodMetadata);
    }

    void assertMethodMetadata(MethodMetadata one, MethodMetadata another) {
        assertEquals(one, another);
        assertEquals(another, one);
        assertEquals(one.hashCode(), another.hashCode());
        assertEquals(one.toString(), another.toString());
    }

    void setAndAssert(MethodMetadata methodMetadata) {
        short index = 0;
        String interfaceName = "io.microsphere.redis.metadata.MethodMetadataTest";
        String methodName = "test";
        String[] parameterTypes = new String[]{"java.lang.String"};
        String[] commands = ofArray("X");
        boolean write = true;

        methodMetadata.setIndex(index);
        methodMetadata.setInterfaceName(interfaceName);
        methodMetadata.setMethodName(methodName);
        methodMetadata.setParameterTypes(parameterTypes);
        methodMetadata.setCommands(commands);
        methodMetadata.setWrite(write);

        assertEquals(index, methodMetadata.getIndex());
        assertEquals(interfaceName, methodMetadata.getInterfaceName());
        assertEquals(methodName, methodMetadata.getMethodName());
        assertArrayEquals(parameterTypes, methodMetadata.getParameterTypes());
        assertArrayEquals(commands, methodMetadata.getCommands());
        assertEquals(write, methodMetadata.isWrite());
    }
}
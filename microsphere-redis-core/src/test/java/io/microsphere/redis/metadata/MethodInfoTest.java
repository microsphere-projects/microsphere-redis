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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.redis.util.RedisCommandUtils.buildParameterMetadataList;
import static io.microsphere.redis.util.RedisCommandUtils.getParameterClassNames;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.Math.abs;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link MethodInfo} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodInfo
 * @since 1.0.0
 */
class MethodInfoTest {

    private String methodName;

    private Class<?>[] parameterTypes;

    private Method method;

    private MethodInfo methodInfo;

    @BeforeEach
    void setUp() {
        this.methodName = "toUpperCase";
        this.parameterTypes = ofArray(Locale.class);
        this.method = findMethod(String.class, this.methodName, this.parameterTypes);
        this.methodInfo = buildMethodInfo(method);
    }

    @Test
    void test() {
        String buildId = buildMethodId(this.method);
        assertEquals(buildId, this.methodInfo.getId());
        assertEquals(abs(buildId.hashCode()), this.methodInfo.getIndex());
        assertEquals(this.methodName, this.methodInfo.getName());
        assertEquals(this.method, this.methodInfo.getMethod());
        assertEquals(buildMethodMetadata(this.method), this.methodInfo.getMethodMetadata());
        assertEquals(buildParameterMetadataList(this.method), this.methodInfo.getParameterMetadataList());
        assertEquals(this.methodInfo.hashCode(), this.methodInfo.getIndex());
        assertEquals(this.methodInfo.toString(), this.methodInfo.getId());

        MethodInfo methodInfo1 = buildMethodInfo(this.method);
        assertEquals(this.methodInfo, this.methodInfo);
        assertEquals(this.methodInfo, methodInfo1);
        assertNotEquals(this.methodInfo, buildId);

        Method method2 = findMethod(String.class, "toUpperCase");
        MethodInfo methodInfo2 = buildMethodInfo(method2);
        assertNotEquals(methodInfo1, methodInfo2);

        MethodInfo methodInfo3 = new MethodInfo(this.method, null, null);
        assertNotEquals(methodInfo1, methodInfo3);
        assertNotEquals(methodInfo2, methodInfo3);

        MethodInfo methodInfo4 = new MethodInfo(this.method, buildMethodMetadata(this.method), null);
        assertNotEquals(methodInfo1, methodInfo4);
        assertNotEquals(methodInfo2, methodInfo4);

        MethodInfo methodInfo5 = new MethodInfo(this.method, buildMethodMetadata(this.method), emptyList());
        assertNotEquals(methodInfo1, methodInfo5);
        assertNotEquals(methodInfo2, methodInfo5);
    }

    MethodInfo buildMethodInfo(Method method) {
        MethodMetadata methodMetadata = buildMethodMetadata(method);
        List<ParameterMetadata> parameterMetadataList = buildParameterMetadataList(method);
        return new MethodInfo(method, methodMetadata, parameterMetadataList);
    }

    private MethodMetadata buildMethodMetadata(Method method) {
        int index = buildMethodIndex(method);
        String[] parameterClassNames = getParameterClassNames(method.getParameterTypes());
        MethodMetadata methodMetadata = new MethodMetadata();
        methodMetadata.setIndex(index);
        methodMetadata.setMethodName(method.getName());
        methodMetadata.setParameterTypes(parameterClassNames);
        methodMetadata.setCommands(ofArray("UC"));
        methodMetadata.setWrite(true);
        return methodMetadata;
    }
}
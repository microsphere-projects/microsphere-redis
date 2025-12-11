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


import io.microsphere.redis.metadata.ParameterMetadata;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.redis.spring.AbstractRedisTest.SET_METHOD;
import static io.microsphere.redis.spring.metadata.MethodMetadataRepository.getWriteParameterMetadataList;
import static io.microsphere.redis.spring.metadata.MethodMetadataRepository.isWriteCommandMethod;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link MethodMetadataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodMetadataRepository
 * @since 1.0.0
 */
class MethodMetadataRepositoryTest {

    @Test
    void testIsWriteCommandMethod() {
        assertTrue(isWriteCommandMethod(SET_METHOD));
    }

    @Test
    void testGetWriteParameterMetadataList() {
        List<ParameterMetadata> parameterMetadataList = getWriteParameterMetadataList(SET_METHOD);
        assertEquals(2, parameterMetadataList.size());
    }

    @Test
    void testFindWriteCommandMethod() {
    }

    @Test
    void testGetWriteCommandMethod() {
    }

    @Test
    void testGetWriteCommandMethods() {
    }

    @Test
    void testGetRedisCommandsInterfaceClass() {
    }

    @Test
    void testGetRedisCommandBindingFunction() {
    }
}
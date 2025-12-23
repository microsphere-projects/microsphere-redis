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


import io.microsphere.redis.metadata.ParameterMetadata;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import static io.microsphere.redis.util.RedisCommandUtils.LOAD_REDIS_COMMANDS_FUNCTION;
import static io.microsphere.redis.util.RedisCommandUtils.REDIS_COMMANDS_RESOURCE;
import static io.microsphere.redis.util.RedisCommandUtils.REDIS_WRITE_COMMANDS_RESOURCE;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodSignature;
import static io.microsphere.redis.util.RedisCommandUtils.buildParameterMetadataList;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisWriteCommands;
import static io.microsphere.redis.util.RedisCommandUtils.isRedisCommand;
import static io.microsphere.redis.util.RedisCommandUtils.isRedisWriteCommand;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisCommandUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandUtils
 * @since 1.0.0
 */
class RedisCommandUtilsTest {

    @Test
    void testConstants() {
        assertSame("META-INF/redis-commands", REDIS_COMMANDS_RESOURCE);
        assertSame("META-INF/redis-write-commands", REDIS_WRITE_COMMANDS_RESOURCE);
        assertNotNull(LOAD_REDIS_COMMANDS_FUNCTION);
    }

    @Test
    void testGetRedisCommands() {
        Set<String> redisCommands = getRedisCommands();
        assertEquals(595, redisCommands.size());
    }

    @Test
    void testGetRedisWriteCommands() {
        Set<String> redisWriteCommands = getRedisWriteCommands();
        assertEquals(177, redisWriteCommands.size());
    }

    @Test
    void testIsRedisCommand() {
        Set<String> redisCommands = getRedisCommands();
        for (String redisCommand : redisCommands) {
            assertTrue(isRedisCommand(redisCommand));
        }

        assertFalse(isRedisCommand("X"));
    }

    @Test
    void testIsRedisWrtieCommand() {
        Set<String> redisWriteCommands = getRedisWriteCommands();
        for (String redisWriteCommand : redisWriteCommands) {
            assertTrue(isRedisWriteCommand(redisWriteCommand));
        }

        assertFalse(isRedisWriteCommand("X"));
    }

    @Test
    void testBuildMethodId() {
        Method method = findMethod(RedisCommandUtils.class, "buildMethodId", Class.class, String.class, Class[].class);
        String methodId = buildMethodId(method);
        assertEquals("io.microsphere.redis.util.RedisCommandUtils.buildMethodId(java.lang.Class,java.lang.String,[Ljava.lang.Class;)", methodId);
    }

    @Test
    void testBuildMethodSignature() {
        Method[] methods = RedisCommandUtils.class.getMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();
            assertEquals(buildMethodSignature(method), buildMethodSignature(methodName, parameterTypes));
        }
    }

    @Test
    void testBuildMethodIndex() {
        Method method = findMethod(RedisCommandUtils.class, "buildMethodId", Class.class, String.class, Class[].class);
        String methodId = buildMethodId(method);
        int methodIndex = buildMethodIndex("io.microsphere.redis.util.RedisCommandUtils", "buildMethodId", Class.class.getName(), String.class.getName(), Class[].class.getName());
        assertEquals(abs(methodId.hashCode()), methodIndex);
        assertEquals(buildMethodIndex(method), methodIndex);
        assertEquals(buildMethodIndex(RedisCommandUtils.class, "buildMethodId", Class.class, String.class, Class[].class), methodIndex);
    }

    @Test
    void testBuildParameterMetadataList() {
        Method method = findMethod(RedisCommandUtils.class, "buildMethodId", Class.class, String.class, Class[].class);
        List<ParameterMetadata> parameterMetadataList = buildParameterMetadataList(method);
        assertEquals(3, parameterMetadataList.size());

        assertParameterMetadata(parameterMetadataList, 0, Class.class, "declaringClass");
        assertParameterMetadata(parameterMetadataList, 1, String.class, "methodName");
        assertParameterMetadata(parameterMetadataList, 2, Class[].class, "parameterTypes");
    }

    private void assertParameterMetadata(List<ParameterMetadata> parameterMetadataList, int parameterIndex,
                                         Class<?> parameterType, String parameterName) {
        ParameterMetadata parameterMetadata = parameterMetadataList.get(parameterIndex);
        assertEquals(parameterIndex, parameterMetadata.getParameterIndex());
        assertEquals(parameterType.getName(), parameterMetadata.getParameterType());
        assertEquals(parameterName, parameterMetadata.getParameterName());
    }
}
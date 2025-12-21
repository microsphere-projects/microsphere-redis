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


import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static io.microsphere.redis.util.RedisCommandUtils.LOAD_REDIS_COMMANDS_FUNCTION;
import static io.microsphere.redis.util.RedisCommandUtils.REDIS_COMMANDS_RESOURCE;
import static io.microsphere.redis.util.RedisCommandUtils.REDIS_WRITE_COMMANDS_RESOURCE;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodSignature;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisWriteCommands;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
    }
}
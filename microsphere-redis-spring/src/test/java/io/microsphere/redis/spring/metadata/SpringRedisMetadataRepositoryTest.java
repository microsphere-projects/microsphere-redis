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


import io.microsphere.redis.metadata.MethodInfo;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.util.RedisCommandUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.cache;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.cacheMethodInfo;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getMethodIndex;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getMethodInfo;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getRedisCommandBindingFunction;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getRedisCommandInterfaceClass;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getRedisCommandMethod;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getWriteCommandMethod;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getWriteParameterMetadataList;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.init;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.initRedisConnectionInterface;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.isWrite;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.isWriteCommandMethod;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.redisCommandInterfacesCache;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.redisCommandMethods;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_CONNECTION_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_GEO_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_HASH_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_KEY_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_LIST_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_SERVER_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_SET_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_STREAM_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_STRING_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_TX_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_ZSET_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.loadClass;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.forEach;
import static io.microsphere.util.IterableUtils.forEach;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link SpringRedisMetadataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringRedisMetadataRepository
 * @since 1.0.0
 */
class SpringRedisMetadataRepositoryTest {

    @BeforeAll
    static void beforeAll() {
        init();
    }

    @Test
    void testGetMethodIndex() {
        forEach(redisCommandMethods, method -> {
            assertNotNull(getMethodIndex(method));
        });

        assertNull(getMethodIndex(null));
    }

    @Test
    void testGetRedisCommandMethod() {
        forEach(redisCommandMethods, method -> {
            int methodIndex = buildMethodIndex(method);
            assertNotNull(getRedisCommandMethod(methodIndex));
            MethodInfo methodInfo = getMethodInfo(method);
            MethodMetadata methodMetadata = methodInfo.getMethodMetadata();
            String interfaceName = methodMetadata.getInterfaceName();
            String methodName = methodMetadata.getMethodName();
            String[] parameterTypes = methodMetadata.getParameterTypes();
            assertNotNull(getRedisCommandMethod(interfaceName, methodName, parameterTypes));
        });

        assertNull(getRedisCommandMethod(-1));
        assertNull(getRedisCommandMethod(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING_ARRAY));
    }

    @Test
    void testIsWriteCommandMethod() {
        forEach(redisCommandMethods, method -> {
            MethodInfo methodInfo = getMethodInfo(method);
            assertEquals(methodInfo.getMethodMetadata().isWrite(), isWriteCommandMethod(method));
        });
    }

    @Test
    void testGetWriteParameterMetadataList() {
        forEach(redisCommandMethods, method -> {
            MethodInfo methodInfo = getMethodInfo(method);
            MethodMetadata methodMetadata = methodInfo.getMethodMetadata();
            assertEquals(methodMetadata.isWrite(), Objects.equals(methodInfo.getParameterMetadataList(), getWriteParameterMetadataList(method)));
        });
    }

    @Test
    void testGetWriteCommandMethod() {
        forEach(redisCommandMethods, method -> {
            MethodInfo methodInfo = getMethodInfo(method);
            MethodMetadata methodMetadata = methodInfo.getMethodMetadata();
            String interfaceName = methodMetadata.getInterfaceName();
            String methodName = methodMetadata.getMethodName();
            String[] parameterTypes = methodMetadata.getParameterTypes();
            assertEquals(methodMetadata.isWrite(), Objects.equals(method, getWriteCommandMethod(interfaceName, methodName, parameterTypes)));
        });
    }

    @Test
    void testGetRedisCommandInterfaceClass() {
        assertGetRedisCommandInterfaceClass(REDIS_KEY_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_STRING_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_LIST_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_SET_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_ZSET_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_HASH_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_TX_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_CONNECTION_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_SERVER_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_STREAM_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_GEO_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME);
        assertGetRedisCommandInterfaceClass(REDIS_COMMANDS_INTERFACE_NAME);

        forEach(redisCommandInterfacesCache.keySet(), className -> {
            assertGetRedisCommandInterfaceClass(className);
        });
    }

    @Test
    void testGetRedisCommandBindingFunction() {
        forEach(RedisCommandUtils.class.getInterfaces(), type -> {
            Function<RedisConnection, Object> function = getRedisCommandBindingFunction(type.getName());
            assertNotNull(function);
        });
    }

    @Test
    void testIsWriteWithNull() {
        assertFalse(isWrite(null));
    }

    @Test
    void testCacheMethodInfoWithNull() {
        cacheMethodInfo(null, null);
    }

    @Test
    void testInitRedisConnectionInterface() {
        Method method = findMethod(String.class, "toUpperCase");
        initRedisConnectionInterface(method);

        Method method1 = findMethod(RedisCommandsExt.class, "set", byte[].class);
        initRedisConnectionInterface(method1);
    }

    @Test
    void testCache() {
        Map<String, Object> map = newHashMap();
        cache(map, "key", "value");
        cache(map, "key", "value");
    }

    void assertGetRedisCommandInterfaceClass(String interfaceName) {
        Class<?> interfaceClass = loadClass(interfaceName);
        assertEquals(interfaceClass, getRedisCommandInterfaceClass(interfaceName));
    }

    abstract class RedisCommandsExt implements RedisCommands {

        public boolean set(byte[] key) {
            return false;
        }
    }

}
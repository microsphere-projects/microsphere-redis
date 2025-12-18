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

package io.microsphere.redis.spring.util;


import io.microsphere.redis.spring.config.RedisConfig;
import io.microsphere.redis.spring.config.RedisContextConfig;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.redis.spring.interceptor.RedisMethodContext;
import io.microsphere.redis.metadata.ParameterMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisKeyCommands;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.AbstractRedisTest.SET_METHOD;
import static io.microsphere.redis.spring.AbstractRedisTest.SET_METHOD_ARGS;
import static io.microsphere.redis.spring.AbstractRedisTest.SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE;
import static io.microsphere.redis.spring.context.RedisContext.get;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_COMMANDS_PACKAGE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_COMMANDS_PACKAGE_NAME_LENGTH;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_CONNECTION_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_GEO_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_HASH_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_KEY_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_LIST_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_SERVER_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_SET_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_STREAM_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_STRING_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_TX_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.REDIS_ZSET_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.buildCommandMethodId;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.getRedisCommands;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.initializeParameters;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadParameterClasses;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.resolveInterfaceName;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.resolveSimpleInterfaceName;
import static io.microsphere.redis.util.RedisCommandUtils.buildRedisCommandMethodId;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisCommandsUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandsUtils
 * @since 1.0.0
 */
class RedisCommandsUtilsTest {

    @Test
    void testConstants() {
        assertEquals("org.springframework.data.redis.connection.", REDIS_COMMANDS_PACKAGE_NAME);
        assertEquals(42, REDIS_COMMANDS_PACKAGE_NAME_LENGTH);
        assertEquals("org.springframework.data.redis.connection.RedisKeyCommands", REDIS_KEY_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisStringCommands", REDIS_STRING_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisListCommands", REDIS_LIST_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisSetCommands", REDIS_SET_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisZSetCommands", REDIS_ZSET_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisHashCommands", REDIS_HASH_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisTxCommands", REDIS_TX_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisPubSubCommands", REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisConnectionCommands", REDIS_CONNECTION_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisServerCommands", REDIS_SERVER_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisStreamCommands", REDIS_STREAM_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisScriptingCommands", REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisGeoCommands", REDIS_GEO_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisHyperLogLogCommands", REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME);
    }

    @Test
    void testResolveSimpleInterfaceName() {
        assertEquals("RedisKeyCommands", resolveSimpleInterfaceName(REDIS_KEY_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisStringCommands", resolveSimpleInterfaceName(REDIS_STRING_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisListCommands", resolveSimpleInterfaceName(REDIS_LIST_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisSetCommands", resolveSimpleInterfaceName(REDIS_SET_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisZSetCommands", resolveSimpleInterfaceName(REDIS_ZSET_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisHashCommands", resolveSimpleInterfaceName(REDIS_HASH_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisTxCommands", resolveSimpleInterfaceName(REDIS_TX_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisPubSubCommands", resolveSimpleInterfaceName(REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisConnectionCommands", resolveSimpleInterfaceName(REDIS_CONNECTION_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisServerCommands", resolveSimpleInterfaceName(REDIS_SERVER_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisStreamCommands", resolveSimpleInterfaceName(REDIS_STREAM_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisScriptingCommands", resolveSimpleInterfaceName(REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisGeoCommands", resolveSimpleInterfaceName(REDIS_GEO_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisHyperLogLogCommands", resolveSimpleInterfaceName(REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME));
        assertEquals("RedisHyperLogLogCommands", resolveSimpleInterfaceName("RedisHyperLogLogCommands"));
    }

    @Test
    void testResolveInterfaceName() {
        assertInterfaceName(REDIS_KEY_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_STRING_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_LIST_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_SET_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_ZSET_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_HASH_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_TX_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_CONNECTION_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_SERVER_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_STREAM_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_GEO_COMMANDS_INTERFACE_NAME);
        assertInterfaceName(REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME);
        assertEquals("org.springframework.data.redis.connection.RedisHyperLogLogCommands", resolveInterfaceName("RedisHyperLogLogCommands"));
    }

    @Test
    void testGetRedisCommands() {
        testInSpringContainer(context -> {
            RedisConnectionFactory redisConnectionFactory = context.getBean(RedisConnectionFactory.class);
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            assertSame(redisConnection.keyCommands(), getRedisCommands(redisConnection, REDIS_KEY_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.stringCommands(), getRedisCommands(redisConnection, REDIS_STRING_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.listCommands(), getRedisCommands(redisConnection, REDIS_LIST_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.setCommands(), getRedisCommands(redisConnection, REDIS_SET_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.zSetCommands(), getRedisCommands(redisConnection, REDIS_ZSET_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.hashCommands(), getRedisCommands(redisConnection, REDIS_HASH_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, REDIS_TX_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, REDIS_CONNECTION_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.serverCommands(), getRedisCommands(redisConnection, REDIS_SERVER_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.streamCommands(), getRedisCommands(redisConnection, REDIS_STREAM_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.scriptingCommands(), getRedisCommands(redisConnection, REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.geoCommands(), getRedisCommands(redisConnection, REDIS_GEO_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.hyperLogLogCommands(), getRedisCommands(redisConnection, REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, ""));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, null));
            assertSame(redisConnection.commands(), getRedisCommands(redisConnection, "others"));
        }, RedisConfig.class);
    }

    @Test
    void testBuildCommandMethodId() {
        testInSpringContainer(context -> {
            RedisConnectionFactory redisConnectionFactory = context.getBean(RedisConnectionFactory.class);
            RedisConnection redisConnection = redisConnectionFactory.getConnection();
            RedisContext redisContext = get(context);
            RedisMethodContext redisMethodContext = new RedisMethodContext(redisConnection, SET_METHOD, SET_METHOD_ARGS, redisContext, redisConnectionFactory, SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE);
            RedisCommandEvent event = new RedisCommandEvent(redisMethodContext);
            assertEquals("org.springframework.data.redis.connection.DefaultedRedisConnection.set([B,[B)", buildCommandMethodId(event));
            assertEquals(buildCommandMethodId(event), buildRedisCommandMethodId(SET_METHOD));
            assertEquals(buildCommandMethodId(event), buildRedisCommandMethodId("org.springframework.data.redis.connection.DefaultedRedisConnection", "set", byte[].class, byte[].class));
        }, RedisContextConfig.class);
    }

    @Test
    void testInitializeParameters() {
        assertTrue(initializeParameters(SET_METHOD, SET_METHOD_ARGS, (parameter, integer) -> {
            ParameterMetadata metadata = parameter.getMetadata();
            assertEquals(integer, metadata.getParameterIndex());
            assertEquals(integer == 0 ? "key" : "value", metadata.getParameterName());
            assertEquals("[B", metadata.getParameterType());
        }, (parameter, integer) -> {
            assertArrayEquals((byte[]) parameter.getValue(), parameter.getRawValue());
        }));

        assertTrue(initializeParameters(SET_METHOD, SET_METHOD_ARGS, (parameter, integer) -> {
            throw new RuntimeException("For testing");
        }));

        Method randomKeyMethod = findMethod(RedisKeyCommands.class, "randomKey");

        assertFalse(initializeParameters(randomKeyMethod, EMPTY_OBJECT_ARRAY, (parameter, integer) -> {
        }));
    }

    @Test
    void testLoadParameterClasses() {
        assertLoadParameterClasses();
        assertLoadParameterClasses(String.class);
        assertLoadParameterClasses(String.class, Integer.class);
        assertLoadParameterClasses(String.class, Integer.class, ApplicationContext.class);
        assertLoadParameterClasses(String.class, Integer.class, ApplicationContext.class, this.getClass());
    }

    private void assertLoadParameterClasses(Class<?>... classes) {
        assertArrayEquals(classes, loadParameterClasses(of(classes).map(Class::getName).toArray(String[]::new)));
    }

    void assertInterfaceName(String interfaceName) {
        assertEquals(interfaceName, resolveInterfaceName(resolveSimpleInterfaceName(interfaceName)));
        assertEquals(interfaceName, resolveInterfaceName(interfaceName));
    }
}
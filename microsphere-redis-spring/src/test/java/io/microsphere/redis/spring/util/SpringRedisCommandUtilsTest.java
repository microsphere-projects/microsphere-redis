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


import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.spring.config.RedisConfig;
import io.microsphere.redis.spring.config.RedisContextConfig;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.redis.spring.interceptor.RedisMethodContext;
import io.microsphere.redis.util.RawValue;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.DefaultedRedisClusterConnection;
import org.springframework.data.redis.connection.DefaultedRedisConnection;
import org.springframework.data.redis.connection.ReactiveGeoCommands;
import org.springframework.data.redis.connection.ReactiveHashCommands;
import org.springframework.data.redis.connection.ReactiveHyperLogLogCommands;
import org.springframework.data.redis.connection.ReactiveKeyCommands;
import org.springframework.data.redis.connection.ReactiveListCommands;
import org.springframework.data.redis.connection.ReactiveNumberCommands;
import org.springframework.data.redis.connection.ReactivePubSubCommands;
import org.springframework.data.redis.connection.ReactiveScriptingCommands;
import org.springframework.data.redis.connection.ReactiveServerCommands;
import org.springframework.data.redis.connection.ReactiveSetCommands;
import org.springframework.data.redis.connection.ReactiveStreamCommands;
import org.springframework.data.redis.connection.ReactiveZSetCommands;
import org.springframework.data.redis.connection.RedisClusterCommands;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisCommandsProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisPubSubCommands;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisSentinelCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.StringRedisConnection;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.AbstractRedisTest.SET_METHOD;
import static io.microsphere.redis.spring.AbstractRedisTest.SET_METHOD_ARGS;
import static io.microsphere.redis.spring.AbstractRedisTest.SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE;
import static io.microsphere.redis.spring.context.RedisContext.get;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REACTIVE_COMMANDS_INTERFACE_NAME_PREFIX;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_EXECUTE_METHOD;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_INTERFACE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_INTERFACE_NAME_PREFIX;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_INTERFACE_NAME_SUFFIX;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_PACKAGE_NAME;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.REDIS_COMMANDS_PACKAGE_NAME_LENGTH;
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
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.buildCommandMethodId;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.getRedisCommands;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.initializeParameters;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.isRedisCommandsExecuteMethod;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.isRedisCommandsInterface;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.loadClasses;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.resolveInterfaceName;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.resolveSimpleInterfaceName;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringRedisCommandUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringRedisCommandUtils
 * @since 1.0.0
 */
class SpringRedisCommandUtilsTest {

    @Test
    void testConstants() {
        assertEquals("org.springframework.data.redis.connection.", REDIS_COMMANDS_PACKAGE_NAME);
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
        assertEquals("org.springframework.data.redis.connection.RedisCommands", REDIS_COMMANDS_INTERFACE_NAME);
        assertNotNull(REDIS_COMMANDS_EXECUTE_METHOD);
        assertEquals(42, REDIS_COMMANDS_PACKAGE_NAME_LENGTH);
        assertEquals("org.springframework.data.redis.connection.Redis", REDIS_COMMANDS_INTERFACE_NAME_PREFIX);
        assertEquals("org.springframework.data.redis.connection.Reactive", REACTIVE_COMMANDS_INTERFACE_NAME_PREFIX);
        assertEquals("Commands", REDIS_COMMANDS_INTERFACE_NAME_SUFFIX);
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
        assertEquals("RedisCommands", resolveSimpleInterfaceName(REDIS_COMMANDS_INTERFACE_NAME));
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
    void testIsRedisCommandsInterface() {
        assertTrue(isRedisCommandsInterface(REDIS_KEY_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_STRING_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_LIST_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_SET_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_ZSET_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_HASH_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_TX_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_CONNECTION_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_SERVER_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_STREAM_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_GEO_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME));
        assertTrue(isRedisCommandsInterface(REDIS_COMMANDS_INTERFACE_NAME));

        assertFalse(isRedisCommandsInterface("RedisKeyCommands"));
        assertFalse(isRedisCommandsInterface("RedisHyperLogLogCommands"));
        assertFalse(isRedisCommandsInterface("Commands"));

        assertTrue(isRedisCommandsInterface(RedisGeoCommands.class));
        assertTrue(isRedisCommandsInterface(RedisHashCommands.class));
        assertTrue(isRedisCommandsInterface(RedisHyperLogLogCommands.class));
        assertTrue(isRedisCommandsInterface(RedisKeyCommands.class));
        assertTrue(isRedisCommandsInterface(RedisListCommands.class));
        assertTrue(isRedisCommandsInterface(RedisPubSubCommands.class));
        assertTrue(isRedisCommandsInterface(RedisConnectionCommands.class));
        assertTrue(isRedisCommandsInterface(RedisScriptingCommands.class));
        assertTrue(isRedisCommandsInterface(RedisSentinelCommands.class));
        assertTrue(isRedisCommandsInterface(RedisServerCommands.class));
        assertTrue(isRedisCommandsInterface(RedisSetCommands.class));
        assertTrue(isRedisCommandsInterface(RedisStreamCommands.class));
        assertTrue(isRedisCommandsInterface(RedisStringCommands.class));
        assertTrue(isRedisCommandsInterface(RedisTxCommands.class));
        assertTrue(isRedisCommandsInterface(RedisZSetCommands.class));

        assertTrue(isRedisCommandsInterface(RedisConnection.class));
        assertTrue(isRedisCommandsInterface(DefaultedRedisConnection.class));
        assertTrue(isRedisCommandsInterface(StringRedisConnection.class));
        assertTrue(isRedisCommandsInterface(RedisClusterConnection.class));
        assertTrue(isRedisCommandsInterface(DefaultedRedisClusterConnection.class));
        assertTrue(isRedisCommandsInterface(RedisCommands.class));

        assertTrue(isRedisCommandsInterface(RedisClusterCommands.class));

        assertTrue(isRedisCommandsInterface(ReactiveGeoCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveHashCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveHyperLogLogCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveKeyCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveListCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveNumberCommands.class));
        assertTrue(isRedisCommandsInterface(ReactivePubSubCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveScriptingCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveServerCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveSetCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveStreamCommands.class));
        assertTrue(isRedisCommandsInterface(RedisStringCommands.class));
        assertTrue(isRedisCommandsInterface(ReactiveZSetCommands.class));

        assertFalse(isRedisCommandsInterface(DefaultStringRedisConnection.class));
        assertFalse(isRedisCommandsInterface(RedisCommandsProvider.class));
        assertFalse(isRedisCommandsInterface(AutoCloseable.class));
        assertFalse(isRedisCommandsInterface(Object.class));
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
            assertEquals(buildCommandMethodId(event), buildMethodId(SET_METHOD));
            assertEquals(buildCommandMethodId(event), buildMethodId("org.springframework.data.redis.connection.DefaultedRedisConnection", "set", byte[].class, byte[].class));
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
    void testIsRedisCommandsExecuteMethod() {
        assertTrue(isRedisCommandsExecuteMethod(REDIS_COMMANDS_EXECUTE_METHOD));
        assertFalse(isRedisCommandsExecuteMethod(SET_METHOD));
    }

    @Test
    void testLoadClasses() {
        assertLoadClasses();
        assertLoadClasses(String.class);
        assertLoadClasses(String.class, Integer.class);
        assertLoadClasses(String.class, Integer.class, RawValue.class);
        assertLoadClasses(String.class, Integer.class, RawValue.class, this.getClass());
    }

    private void assertLoadClasses(Class<?>... classes) {
        assertArrayEquals(classes, loadClasses(of(classes).map(Class::getName).toArray(String[]::new)));
    }

    void assertInterfaceName(String interfaceName) {
        assertEquals(interfaceName, resolveInterfaceName(resolveSimpleInterfaceName(interfaceName)));
        assertEquals(interfaceName, resolveInterfaceName(interfaceName));
    }
}
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

package io.microsphere.redis.spring.event;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.event.RedisCommandEvent.Builder.source;
import static io.microsphere.redis.spring.serializer.RedisCommandEventSerializer.VERSION_V1;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link RedisCommandEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandEvent
 * @since 1.0.0
 */
class RedisCommandEventTest {

    private static final String TEST_APPLICATION_NAME = "default";

    private static final String TEST_BEAN_NAME = "redisTemplate";

    private static final String TEST_METHOD_NAME = "execute";

    private static final String TEST_COMMAND = "SET";

    private static final byte[][] TEST_BYTES_ARRAY = new byte[0][0];

    private static final Class[] TEST_PARAMETER_TYPES = ofArray(String.class, byte[][].class);

    private static final Object[] TEST_ARGS = ofArray(TEST_COMMAND, TEST_BYTES_ARRAY);

    private static final Method TEST_METHOD = findMethod(RedisCommands.class, TEST_METHOD_NAME, TEST_PARAMETER_TYPES);

    private RedisCommandEvent redisCommandEvent;

    @BeforeEach
    void setUp() {
        this.redisCommandEvent = source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .args(TEST_ARGS)
                .serializationVersion(VERSION_V1)
                .build();
    }

    @Test
    void testGetter() {
        assertEquals(RedisCommands.class.getName(), this.redisCommandEvent.getInterfaceName());
        assertEquals(RedisCommands.class.getName(), this.redisCommandEvent.getInterfaceName());
        assertEquals(TEST_METHOD_NAME, this.redisCommandEvent.getMethodName());
        assertArrayEquals(TEST_PARAMETER_TYPES, this.redisCommandEvent.getParameterTypes());
        assertArrayEquals(TEST_PARAMETER_TYPES, this.redisCommandEvent.getParameterTypes());
        assertEquals(2, this.redisCommandEvent.getParameterCount());
        assertEquals(2, this.redisCommandEvent.getParameterCount());
        assertArrayEquals(TEST_ARGS, this.redisCommandEvent.getArgs());
        assertEquals(TEST_COMMAND, this.redisCommandEvent.getArg(0));
        assertArrayEquals(TEST_BYTES_ARRAY, (byte[][]) this.redisCommandEvent.getArg(1));
        assertEquals(TEST_APPLICATION_NAME, this.redisCommandEvent.getApplicationName());
        assertEquals(TEST_BEAN_NAME, this.redisCommandEvent.getSourceBeanName());
        assertEquals(VERSION_V1, this.redisCommandEvent.getSerializationVersion());
    }

    @Test
    void testEquals() {
        assertNotEquals(this.redisCommandEvent, null);
        assertNotEquals(this.redisCommandEvent, this);

        assertNotEquals(this.redisCommandEvent, source(this).build());

        assertNotEquals(this.redisCommandEvent, source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .build());

        assertNotEquals(this.redisCommandEvent, source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .build());

        assertNotEquals(this.redisCommandEvent, source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .build());

        assertEquals(this.redisCommandEvent, source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .args(TEST_ARGS)
                .build());

        assertEquals(this.redisCommandEvent, source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .args(TEST_ARGS)
                .serializationVersion(VERSION_V1)
                .build());

        assertEquals(this.redisCommandEvent, this.redisCommandEvent);
    }

    @Test
    void testHashCode() {
        assertEquals(this.redisCommandEvent.hashCode(), this.redisCommandEvent.hashCode());
    }

    @Test
    void testToString() {
        assertNotEquals(this.redisCommandEvent.toString(), this.toString());

        assertNotEquals(this.redisCommandEvent.toString(), source(this).build().toString());

        assertNotEquals(this.redisCommandEvent.toString(), source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .build()
                .toString());

        assertNotEquals(this.redisCommandEvent.toString(), source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .build()
                .toString());

        assertNotEquals(this.redisCommandEvent.toString(), source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .build()
                .toString());

        assertEquals(this.redisCommandEvent.toString(), source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .args(TEST_ARGS)
                .build().toString());

        assertEquals(this.redisCommandEvent.toString(), source(this)
                .applicationName(TEST_APPLICATION_NAME)
                .sourceBeanName(TEST_BEAN_NAME)
                .method(TEST_METHOD)
                .args(TEST_ARGS)
                .serializationVersion(VERSION_V1)
                .build()
                .toString());

        assertEquals(this.redisCommandEvent.toString(), this.redisCommandEvent.toString());
    }
}
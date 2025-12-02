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

package io.microsphere.redis.spring.serializer;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.HoldingValueRedisSerializerWrapper.wrap;
import static io.microsphere.redis.spring.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HoldingValueRedisSerializerWrapper} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HoldingValueRedisSerializerWrapper
 * @since 1.0.0
 */
class HoldingValueRedisSerializerWrapperTest {

    private HoldingValueRedisSerializerWrapper<Integer> serializerWrapper;

    @BeforeEach
    void setUp() {
        this.serializerWrapper = new HoldingValueRedisSerializerWrapper(INTEGER_SERIALIZER);
    }

    @Test
    void testSerialize() {
        byte[] bytes = this.serializerWrapper.serialize(1);
        byte[] bytes2 = this.serializerWrapper.serialize(1);
        assertSame(bytes, bytes2);
    }

    @Test
    void testDeserialize() {
        Integer i = 1;
        byte[] bytes = this.serializerWrapper.serialize(i);
        Integer i2 = this.serializerWrapper.deserialize(bytes);
        assertEquals(i, i2);

        Integer i3 = this.serializerWrapper.deserialize(bytes);
        assertEquals(i2, i3);
    }

    @Test
    void testCanSerialize() {
        assertTrue(this.serializerWrapper.canSerialize(Integer.class));
        assertFalse(this.serializerWrapper.canSerialize(String.class));
    }

    @Test
    void testGetTargetType() {
        assertSame(Integer.class, serializerWrapper.getTargetType());
    }

    @Test
    void testGetDelegate() {
        assertSame(INTEGER_SERIALIZER, serializerWrapper.getDelegate());
    }

    @Test
    void testWrapWithRedisSerializer() {
        RedisSerializer redisSerializer = null;
        assertSame(redisSerializer, wrap(redisSerializer));
        assertSame(this.serializerWrapper, wrap(this.serializerWrapper));
        assertNotNull(wrap(INTEGER_SERIALIZER));
    }

    @Test
    void testWrapWithRedisTemplate() {
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setDefaultSerializer(INTEGER_SERIALIZER);
        redisTemplate.setKeySerializer(INTEGER_SERIALIZER);
        redisTemplate.setValueSerializer(INTEGER_SERIALIZER);
        redisTemplate.setHashKeySerializer(INTEGER_SERIALIZER);
        redisTemplate.setHashValueSerializer(INTEGER_SERIALIZER);
        wrap(redisTemplate);
        assertTrue(redisTemplate.getDefaultSerializer() instanceof HoldingValueRedisSerializerWrapper);
        assertTrue(redisTemplate.getKeySerializer() instanceof HoldingValueRedisSerializerWrapper);
        assertTrue(redisTemplate.getValueSerializer() instanceof HoldingValueRedisSerializerWrapper);
        assertTrue(redisTemplate.getHashKeySerializer() instanceof HoldingValueRedisSerializerWrapper);
        assertTrue(redisTemplate.getHashValueSerializer() instanceof HoldingValueRedisSerializerWrapper);
    }
}
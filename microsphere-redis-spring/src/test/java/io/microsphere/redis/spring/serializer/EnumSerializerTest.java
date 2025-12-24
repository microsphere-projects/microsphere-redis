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


import io.microsphere.spring.core.SpringVersion;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Random;

import static io.microsphere.redis.spring.serializer.AbstractSerializer.SHORT_BYTES_LENGTH;
import static io.microsphere.redis.spring.serializer.EnumSerializer.calcBytesLength;
import static io.microsphere.redis.spring.serializer.LargeEnum.values;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link EnumSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnumSerializer
 * @see LargeEnum
 * @see SpringVersion
 * @since 1.0.0
 */
class EnumSerializerTest extends AbstractSerializerTest<LargeEnum> {

    @Override
    protected RedisSerializer<LargeEnum> getSerializer() {
        return new EnumSerializer(LargeEnum.class);
    }

    @Override
    protected LargeEnum getValue() {
        Random random = new Random();
        LargeEnum[] largeEnums = values();
        return largeEnums[random.nextInt(largeEnums.length)];
    }

    @Test
    void testCalcBytesLength() {
        assertEquals(2, calcBytesLength(SpringVersion.values()));
        assertEquals(2, calcBytesLength(values()));
    }

    @Test
    void testEquals() {
        RedisSerializer<LargeEnum> serializer = getSerializer();
        assertTrue(serializer.equals(serializer));
        assertTrue(serializer.equals(getSerializer()));
        assertFalse(serializer.equals(""));
    }

    @Test
    void testHashCode() {
        RedisSerializer<LargeEnum> serializer = getSerializer();
        assertEquals(serializer.hashCode(), serializer.hashCode());
        assertEquals(serializer.hashCode(), getSerializer().hashCode());
    }

    @Test
    void testGetter() {
        EnumSerializer<LargeEnum> serializer = (EnumSerializer<LargeEnum>) getSerializer();
        assertEquals(LargeEnum.class, serializer.getEnumType());
        assertEquals(LargeEnum.class, serializer.getTargetType());
        assertEquals(SHORT_BYTES_LENGTH, serializer.getBytesLength());
    }
}
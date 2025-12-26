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

import org.springframework.data.redis.connection.RedisZSetCommands.Range.Boundary;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Constructor;

import static io.microsphere.redis.spring.serializer.BooleanSerializer.BOOLEAN_SERIALIZER;
import static io.microsphere.redis.spring.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.ConstructorUtils.newInstance;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassUtils.getTypeName;
import static java.lang.System.arraycopy;

/**
 * The {@link RedisSerializer} class for {@link Boundary}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Boundary
 * @see RedisSerializer
 * @since 1.0.0
 */
public class BoundarySerializer extends AbstractSerializer<Boundary> {

    public static final BoundarySerializer BOUNDARY_SERIALIZER = new BoundarySerializer();

    @Override
    protected byte[] doSerialize(Boundary boundary) throws SerializationException {

        boolean including = boundary.isIncluding();
        int includingLength = BOOLEAN_SERIALIZER.getBytesLength();
        byte[] includingBytes = BOOLEAN_SERIALIZER.serialize(including);

        Object value = boundary.getValue();
        String valueClassName = getTypeName(value);
        byte[] valueClassNameBytes = STRING_SERIALIZER.serialize(valueClassName);
        Integer valueClassNameBytesLength = length(valueClassNameBytes);

        byte[] valueClassNameBytesLengthBytes = INTEGER_SERIALIZER.serialize(valueClassNameBytesLength);
        int valueClassNameBytesLengthBytesLength = length(valueClassNameBytesLengthBytes);

        byte[] valueBytes = Serializers.serialize(value);
        int valueBytesLength = length(valueBytes);

        byte[] bytes = new byte[includingLength + valueClassNameBytesLengthBytesLength + valueClassNameBytesLength + valueBytesLength];

        int pos = 0;
        arraycopy(includingBytes, 0, bytes, pos, includingLength);

        pos += includingLength;
        arraycopy(valueClassNameBytesLengthBytes, 0, bytes, pos, valueClassNameBytesLengthBytesLength);

        pos += valueClassNameBytesLengthBytesLength;
        arraycopy(valueClassNameBytes, 0, bytes, pos, valueClassNameBytesLength);

        pos += valueClassNameBytesLength;
        arraycopy(valueBytes, 0, bytes, pos, valueBytesLength);

        return bytes;
    }

    @Override
    protected Boundary doDeserialize(byte[] bytes) throws SerializationException {
        int length = bytes.length;
        int includingLength = BOOLEAN_SERIALIZER.getBytesLength();
        byte[] includingBytes = new byte[includingLength];

        int pos = 0;
        arraycopy(bytes, pos, includingBytes, 0, includingLength);
        boolean including = BOOLEAN_SERIALIZER.deserialize(includingBytes);
        pos += includingLength;

        int valueClassNameBytesLengthBytesLength = INTEGER_SERIALIZER.getBytesLength();
        byte[] valueClassNameBytesLengthBytes = new byte[valueClassNameBytesLengthBytesLength];
        arraycopy(bytes, pos, valueClassNameBytesLengthBytes, 0, valueClassNameBytesLengthBytesLength);
        int valueClassNameBytesLength = INTEGER_SERIALIZER.deserialize(valueClassNameBytesLengthBytes);
        pos += valueClassNameBytesLengthBytesLength;

        byte[] valueClassNameBytes = new byte[valueClassNameBytesLength];
        arraycopy(bytes, pos, valueClassNameBytes, 0, valueClassNameBytesLength);
        String valueClassName = STRING_SERIALIZER.deserialize(valueClassNameBytes);
        pos += valueClassNameBytesLength;

        int valueBytesLength = length - pos;
        byte[] valueBytes = new byte[valueBytesLength];
        arraycopy(bytes, pos, valueBytes, 0, valueBytesLength);
        Object value = Serializers.deserialize(valueBytes, valueClassName);

        return newBoundary(value, including);
    }

    static Boundary newBoundary(Object value, boolean including) {
        Constructor<Boundary> constructor = findConstructor(Boundary.class, Object.class, boolean.class);
        return newInstance(constructor, value, including);
    }
}
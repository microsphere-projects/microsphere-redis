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


import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.ByteArraySerializer.BYTE_ARRAY_SERIALIZER;

/**
 * {@link ByteArraySerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ByteArraySerializer
 * @since 1.0.0
 */
class ByteArraySerializerTest extends AbstractSerializerTest<byte[]> {

    @Override
    protected RedisSerializer<byte[]> getSerializer() {
        return BYTE_ARRAY_SERIALIZER;
    }

    @Override
    protected byte[] getValue() {
        return new byte[0];
    }
}
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

import java.util.Random;

import static io.microsphere.redis.spring.serializer.BoundarySerializer.BOUNDARY_SERIALIZER;
import static io.microsphere.redis.spring.serializer.BoundarySerializer.newBoundary;

/**
 * {@link BoundarySerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BoundarySerializer
 * @since 1.0.0
 */
class BoundarySerializerTest extends AbstractSerializerTest<Boundary> {

    @Override
    protected RedisSerializer<Boundary> getSerializer() {
        return BOUNDARY_SERIALIZER;
    }

    @Override
    protected Boundary getValue() {
        Random random = new Random();
        return newBoundary(random.nextInt(100), random.nextBoolean());
    }

    protected Object getTestData(Boundary boundary) {
        StringBuilder stringBuilder = new StringBuilder("Boundary{")
                .append("value=").append(boundary.getValue())
                .append(", including=").append(boundary.isIncluding())
                .append("}");
        return stringBuilder.toString();
    }
}
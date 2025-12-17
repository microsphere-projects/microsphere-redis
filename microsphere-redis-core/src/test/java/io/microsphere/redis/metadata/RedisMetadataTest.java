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

package io.microsphere.redis.metadata;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * {@link RedisMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMetadata
 * @since 1.0.0
 */
class RedisMetadataTest {

    private RedisMetadata redisMetadata;

    @BeforeEach
    void setUp() {
        this.redisMetadata = new RedisMetadata();
        this.redisMetadata.setVersion("3.5.5");

        MethodMetadata methodMetadata = new MethodMetadata();
        this.redisMetadata.getMethods().add(methodMetadata);
    }

    @Test
    void test() {
        assertEquals("3.5.5", this.redisMetadata.getVersion());

        assertEquals(1, this.redisMetadata.getMethods().size());
        this.redisMetadata.setMethods(this.redisMetadata.getMethods());
        assertEquals(1, this.redisMetadata.getMethods().size());

        RedisMetadata redisMetadata1 = new RedisMetadata();

        // self equal
        assertEquals(this.redisMetadata, this.redisMetadata);

        // not equal
        assertNotEquals(this.redisMetadata, redisMetadata1);

        // not equal
        assertNotEquals(this.redisMetadata, new Object());

        this.redisMetadata.merge(redisMetadata1);
        assertEquals(1, this.redisMetadata.getMethods().size());

        redisMetadata1.setMethods(this.redisMetadata.getMethods());
        assertEquals(this.redisMetadata, redisMetadata1);
        assertEquals(this.redisMetadata.hashCode(), redisMetadata1.hashCode());
        assertEquals(this.redisMetadata.toString(), redisMetadata1.toString());
    }
}
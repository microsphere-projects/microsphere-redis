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

package io.microsphere.redis.util;


import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.microsphere.redis.util.RedisCommandUtils.LOAD_REDIS_COMMANDS_FUNCTION;
import static io.microsphere.redis.util.RedisCommandUtils.REDIS_COMMANDS_RESOURCE;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisUtils.loadResources;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RedisUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisUtils
 * @since 1.0.0
 */
class RedisUtilsTest {

    @Test
    void testLoadResources() {
        Set<String> redisCommands = getRedisCommands();
        Set<String> allRedisCommands = loadResources(REDIS_COMMANDS_RESOURCE,
                inputStreams -> LOAD_REDIS_COMMANDS_FUNCTION.apply(inputStreams.get(0)));
        assertEquals(redisCommands, allRedisCommands);

        assertEquals(emptySet(), loadResources("REDIS_COMMANDS_RESOURCE", inputStreams -> emptySet()));
    }
}
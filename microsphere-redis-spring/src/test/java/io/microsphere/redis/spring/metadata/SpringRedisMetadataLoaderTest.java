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

package io.microsphere.redis.spring.metadata;


import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisWriteCommands;
import static io.microsphere.text.FormatUtils.format;
import static java.util.Locale.ENGLISH;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link SpringRedisMetadataLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringRedisMetadataLoader
 * @since 1.0.0
 */
class SpringRedisMetadataLoaderTest {

    private static final Logger logger = getLogger(SpringRedisMetadataLoaderTest.class);

    private static final Set<String> redisCommandMethodIds = new TreeSet<>();

    @BeforeAll
    static void beforeAll() {
        Method[] methods = RedisCommands.class.getMethods();
        for (Method method : methods) {
            String commandMethodId = buildMethodId(method);
            redisCommandMethodIds.add(commandMethodId);
        }
    }

    @Test
    void testLoad() {
        SpringRedisMetadataLoader loader = new SpringRedisMetadataLoader();
        RedisMetadata redisMetadata = loader.load();
        List<MethodMetadata> methods = redisMetadata.getMethods();
        assertTrue(methods.size() > 1);

        Set<String> configuredCommandMethodIds = new TreeSet<>();
        for (MethodMetadata method : methods) {
            String commandMethodId = buildMethodId(method.getInterfaceName(), method.getMethodName(), method.getParameterTypes());
            assertTrue(configuredCommandMethodIds.add(commandMethodId));
        }

        Set<String> commandMethodIds = new TreeSet<>(redisCommandMethodIds);
        commandMethodIds.removeAll(configuredCommandMethodIds);
        assertTrue(commandMethodIds.size() > 0);
    }


    @Test
    void testUnknown() {
        Method[] methods = RedisCommands.class.getMethods();
        Set<String> redisCommands = getRedisCommands();
        Set<String> redisWriteCommands = getRedisWriteCommands();
        StringBuilder supported = new StringBuilder();
        StringBuilder unsupported = new StringBuilder();

        Set<String> supportedCommands = new HashSet<>();
        Set<String> unsupportedCommands = new HashSet<>(redisCommands);

        Set<Method> supportedMethods = new HashSet<>(ofList(methods));
        Set<Method> unsupportedMethods = new HashSet<>(ofList(methods));

        for (Method method : methods) {
            String name = method.getName().toUpperCase(ENGLISH);
            if (redisCommands.contains(name)) {
                unsupportedMethods.remove(method);
            } else {
                supportedMethods.remove(method);
            }
        }

        for (Method supportedMethod : supportedMethods) {
            String command = supportedMethod.getName().toUpperCase(ENGLISH);
            supported.append(LINE_SEPARATOR);
            supported.append(format("{} , command : {} , default : {}", buildMethodId(supportedMethod), command,
                    redisWriteCommands.contains(command),
                    supportedMethod.isDefault()));
            supportedCommands.add(command);
            unsupportedCommands.remove(command);
        }

        for (Method unsupportedMethod : unsupportedMethods) {
            unsupported.append(LINE_SEPARATOR);
            unsupported.append(format("{} , default : {}", buildMethodId(unsupportedMethod), unsupportedMethod.isDefault()));
        }

        logger.trace("Supported Methods : {}", supported);
        logger.trace("Unsupported Methods : {}", unsupported);
    }
}
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


import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.SHARP;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.util.RedisUtils.loadResource;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.StringUtils.split;
import static java.lang.Math.abs;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Redis Command
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisUtils
 * @since 1.0.0
 */
public abstract class RedisCommandUtils implements Utils {

    private static final Logger logger = getLogger(RedisCommandUtils.class);

    /**
     * The resource path for Redis Commands
     */
    public static final String REDIS_COMMANDS_RESOURCE = "META-INF/redis-commands";

    /**
     * The resource path for Redis Write Commands
     */
    public static final String REDIS_WRITE_COMMANDS_RESOURCE = "META-INF/redis-write-commands";

    static final ThrowableFunction<InputStream, Set<String>> LOAD_REDIS_COMMANDS_FUNCTION = inputStream -> {
        String content = copyToString(inputStream);
        String[] lines = split(content, LINE_SEPARATOR);
        Set<String> redisCommands = new TreeSet<>();
        for (String line : lines) {
            if (line.startsWith(SHARP)) { // Comment line
                continue;
            }
            redisCommands.add(line);
            logger.trace("Redis Command : {} ", line);
        }
        return unmodifiableSet(redisCommands);
    };

    public static Set<String> getRedisCommands() {
        return loadResource(REDIS_COMMANDS_RESOURCE, LOAD_REDIS_COMMANDS_FUNCTION);
    }

    public static Set<String> getRedisWriteCommands() {
        return loadResource(REDIS_WRITE_COMMANDS_RESOURCE, LOAD_REDIS_COMMANDS_FUNCTION);
    }

    public static String buildMethodId(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return buildMethodId(method.getDeclaringClass(), methodName, parameterTypes);
    }

    public static String buildMethodId(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        return buildMethodId(declaringClass.getName(), methodName, parameterTypes);
    }

    public static String buildMethodId(String className, String methodName, Class<?>... parameterTypes) {
        int length = length(parameterTypes);
        String[] parameterTypeNames = new String[length];
        for (int i = 0; i < length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        return buildMethodId(className, methodName, parameterTypeNames);
    }

    public static String buildMethodId(String className, String methodName, String... parameterTypes) {
        StringBuilder infoBuilder = new StringBuilder(className);
        infoBuilder.append(DOT).append(methodName);
        StringJoiner paramTypesInfo = new StringJoiner(COMMA, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
        for (String parameterType : parameterTypes) {
            paramTypesInfo.add(parameterType);
        }
        infoBuilder.append(paramTypesInfo);
        return infoBuilder.toString();
    }

    public static int buildMethodIndex(String className, String methodName, String... parameterTypes) {
        String id = buildMethodId(className, methodName, parameterTypes);
        return abs(id.hashCode());
    }

    private RedisCommandUtils() {
    }
}
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


import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.util.Utils;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import static io.microsphere.collection.ListUtils.newArrayList;
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
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;

/**
 * Utility class for Redis Command related operations, including:
 * <ul>
 *   <li>Checking whether a command name is a valid Redis command or a write command</li>
 *   <li>Building method identifiers (id), numeric indexes, and method signatures</li>
 *   <li>Building {@link ParameterMetadata} lists from {@link Method} reflection data</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Check if a command is a Redis command or write command
 *   boolean isCmd   = RedisCommandUtils.isRedisCommand("SET");      // true
 *   boolean isWrite = RedisCommandUtils.isRedisWriteCommand("GET"); // false
 *
 *   // Build a method id from a reflected Method
 *   Method method = RedisStringCommands.class.getMethod("set", byte[].class, byte[].class);
 *   String id     = RedisCommandUtils.buildMethodId(method);
 *   // e.g. "org.springframework.data.redis.connection.RedisStringCommands.set([B,[B)"
 *
 *   // Build a numeric index from the method id hash
 *   int index = RedisCommandUtils.buildMethodIndex(method); // abs(id.hashCode())
 *
 *   // Build ParameterMetadata list
 *   List<ParameterMetadata> params = RedisCommandUtils.buildParameterMetadataList(method);
 * }</pre>
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

    @Nonnull
    @Immutable
    private static Set<String> redisCommands = loadResource(REDIS_COMMANDS_RESOURCE, LOAD_REDIS_COMMANDS_FUNCTION);

    @Nonnull
    @Immutable

    private static Set<String> redisWriteCommands = loadResource(REDIS_WRITE_COMMANDS_RESOURCE, LOAD_REDIS_COMMANDS_FUNCTION);

    /**
     * Returns the immutable, sorted set of all Redis command names loaded from
     * {@value #REDIS_COMMANDS_RESOURCE} on the classpath.
     *
     * @return non-null, unmodifiable set of Redis command name strings (e.g. {@code "GET"}, {@code "SET"})
     */
    @Nonnull
    @Immutable
    public static Set<String> getRedisCommands() {
        return redisCommands;
    }

    /**
     * Returns the immutable, sorted set of Redis write command names loaded from
     * {@value #REDIS_WRITE_COMMANDS_RESOURCE} on the classpath.
     *
     * @return non-null, unmodifiable set of Redis write command name strings (e.g. {@code "SET"}, {@code "DEL"})
     */
    @Nonnull
    @Immutable
    public static Set<String> getRedisWriteCommands() {
        return redisWriteCommands;
    }

    /**
     * Returns {@code true} if the given command name is a known Redis command.
     *
     * @param command the Redis command name to check (e.g. {@code "GET"})
     * @return {@code true} if the command is listed in {@value #REDIS_COMMANDS_RESOURCE}
     */
    public static boolean isRedisCommand(String command) {
        return redisCommands.contains(command);
    }

    /**
     * Returns {@code true} if the given command name is a Redis write command.
     *
     * @param command the Redis command name to check (e.g. {@code "SET"})
     * @return {@code true} if the command is listed in {@value #REDIS_WRITE_COMMANDS_RESOURCE}
     */
    public static boolean isRedisWriteCommand(String command) {
        return redisWriteCommands.contains(command);
    }

    /**
     * Builds a unique string identifier for the given {@link Method}, in the form
     * {@code "fully.qualified.ClassName.methodName(param1Type,param2Type,...)"}.
     *
     * @param method the method to build the id for
     * @return non-null method id string
     */
    public static String buildMethodId(Method method) {
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        return buildMethodId(method.getDeclaringClass(), methodName, parameterTypes);
    }

    /**
     * Builds a method id using the declaring class, method name and parameter types.
     *
     * @param declaringClass the class that declares the method
     * @param methodName     the method name
     * @param parameterTypes the parameter types (varargs)
     * @return non-null method id string
     */
    public static String buildMethodId(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        return buildMethodId(declaringClass.getName(), methodName, parameterTypes);
    }

    /**
     * Builds a method id using class name, method name and parameter type classes.
     *
     * @param className      the fully-qualified declaring class name
     * @param methodName     the method name
     * @param parameterTypes the parameter type classes (varargs)
     * @return non-null method id string
     */
    public static String buildMethodId(String className, String methodName, Class<?>... parameterTypes) {
        String[] parameterClassNames = getParameterClassNames(parameterTypes);
        return buildMethodId(className, methodName, parameterClassNames);
    }

    /**
     * Builds a method id using class name, method name and parameter type names.
     *
     * @param className      the fully-qualified declaring class name
     * @param methodName     the method name
     * @param parameterTypes the parameter type names (varargs)
     * @return non-null method id string, e.g. {@code "com.example.Foo.bar(java.lang.String,int)"}
     */
    public static String buildMethodId(String className, String methodName, String... parameterTypes) {
        StringBuilder infoBuilder = new StringBuilder(className);
        infoBuilder.append(DOT);
        infoBuilder.append(buildMethodSignature(methodName, parameterTypes));
        return infoBuilder.toString();
    }

    /**
     * Builds a method signature string (without the class name prefix) for the given {@link Method}.
     *
     * @param method the method to build the signature for
     * @return non-null signature string, e.g. {@code "set([B,[B)"}
     */
    public static String buildMethodSignature(Method method) {
        return buildMethodSignature(method.getName(), method.getParameterTypes());
    }

    /**
     * Builds a method signature string using the method name and parameter type classes.
     *
     * @param methodName     the method name
     * @param parameterTypes the parameter type classes (varargs)
     * @return non-null signature string
     */
    public static String buildMethodSignature(String methodName, Class<?>... parameterTypes) {
        String[] parameterClassNames = getParameterClassNames(parameterTypes);
        return buildMethodSignature(methodName, parameterClassNames);
    }

    /**
     * Builds a method signature string using the method name and parameter class names.
     *
     * @param methodName           the method name
     * @param parameterClassNames  the parameter type class names (varargs)
     * @return non-null signature string, e.g. {@code "set([B,[B)"}
     */
    public static String buildMethodSignature(String methodName, String... parameterClassNames) {
        StringBuilder signatureBuilder = new StringBuilder(methodName);
        StringJoiner paramTypesInfo = new StringJoiner(COMMA, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
        for (String parameterClassName : parameterClassNames) {
            paramTypesInfo.add(parameterClassName);
        }
        signatureBuilder.append(paramTypesInfo);
        return signatureBuilder.toString();
    }

    /**
     * Builds a positive numeric index for the given {@link Method} (absolute value of the method id hash code).
     *
     * @param method the method to build the index for
     * @return non-negative method index
     */
    public static int buildMethodIndex(Method method) {
        return buildMethodIndex(method.getDeclaringClass(), method.getName(), method.getParameterTypes());
    }

    /**
     * Builds a numeric index using the declaring class, method name, and parameter type classes.
     *
     * @param declaringClass the declaring class
     * @param methodName     the method name
     * @param parameterTypes the parameter types (varargs)
     * @return non-negative method index
     */
    public static int buildMethodIndex(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
        return buildMethodIndex(declaringClass.getName(), methodName, parameterTypes);
    }

    /**
     * Builds a numeric index using the class name, method name, and parameter type classes.
     *
     * @param className      the fully-qualified declaring class name
     * @param methodName     the method name
     * @param parameterTypes the parameter type classes (varargs)
     * @return non-negative method index
     */
    public static int buildMethodIndex(String className, String methodName, Class<?>... parameterTypes) {
        return buildMethodIndex(className, methodName, getParameterClassNames(parameterTypes));
    }

    /**
     * Builds a numeric index using the class name, method name, and parameter type names.
     * The index is the absolute value of the method id's hash code.
     *
     * @param className      the fully-qualified declaring class name
     * @param methodName     the method name
     * @param parameterTypes the parameter type names (varargs)
     * @return non-negative method index
     */
    public static int buildMethodIndex(String className, String methodName, String... parameterTypes) {
        String id = buildMethodId(className, methodName, parameterTypes);
        return abs(id.hashCode());
    }

    /**
     * Builds an immutable list of {@link ParameterMetadata} for all parameters of the given {@link Method}.
     *
     * @param method the method whose parameters should be described
     * @return unmodifiable list of {@link ParameterMetadata}
     */
    public static List<ParameterMetadata> buildParameterMetadataList(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return buildParameterMetadataList(method, parameterTypes);
    }

    /**
     * Builds an immutable list of {@link ParameterMetadata} for the given {@link Method} using
     * an explicitly provided array of parameter types (for cases where the method's declared types
     * differ from the runtime types to describe).
     *
     * @param method         the method whose parameter names are sourced
     * @param parameterTypes the parameter types to use when building metadata
     * @return unmodifiable list of {@link ParameterMetadata}
     */
    public static List<ParameterMetadata> buildParameterMetadataList(Method method, Class<?>[] parameterTypes) {
        int parameterCount = parameterTypes.length;
        Parameter[] parameters = method.getParameters();

        List<ParameterMetadata> parameterMetadataList = newArrayList(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            Parameter parameter = parameters[i];
            String parameterType = parameterTypes[i].getName();
            String parameterName = parameter.getName();
            ParameterMetadata parameterMetadata = new ParameterMetadata(i, parameterType, parameterName);
            parameterMetadataList.add(parameterMetadata);
        }
        return unmodifiableList(parameterMetadataList);
    }

    /**
     * Converts an array of {@link Class} objects to an array of their fully-qualified class name strings.
     *
     * @param parameterTypes the parameter type classes (varargs)
     * @return array of class name strings; empty array if {@code parameterTypes} is null or empty
     */
    public static String[] getParameterClassNames(Class<?>... parameterTypes) {
        int length = length(parameterTypes);
        String[] parameterTypeNames = new String[length];
        for (int i = 0; i < length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        return parameterTypeNames;
    }

    private RedisCommandUtils() {
    }
}
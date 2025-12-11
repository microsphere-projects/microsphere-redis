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

import io.microsphere.io.IOUtils;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.logging.Logger;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.constants.SymbolConstants.SHARP;
import static io.microsphere.io.IOUtils.copyToString;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.StringUtils.split;
import static java.util.Collections.unmodifiableSet;

/**
 * The utilities class for Redis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisUtils
 * @since 1.0.0
 */
public abstract class RedisUtils {

    private static final Logger logger = getLogger(RedisUtils.class);

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

    public static <T> T loadResource(String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget) {
        return loadResource(getDefaultClassLoader(), resourceName, inputStreamToTarget);
    }

    public static <T> T loadResource(ClassLoader classLoader, String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget) {
        URL resource = classLoader.getResource(resourceName);
        return toTarget(resource, inputStreamToTarget);
    }

    public static <T> T loadResources(String resourceName, ThrowableFunction<List<InputStream>, T> inputStreamsToTarget) {
        return loadResources(getDefaultClassLoader(), resourceName, inputStreamsToTarget);
    }

    public static <T> T loadResources(ClassLoader classLoader, String resourceName,
                                      ThrowableFunction<List<InputStream>, T> inputStreamsToTarget) {
        return execute(() -> {
            List<InputStream> inputStreams = new LinkedList<>();
            Enumeration<URL> resources = classLoader.getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                inputStreams.add(resource.openStream());
            }
            T target;
            try {
                target = inputStreamsToTarget.apply(inputStreams);
            } finally {
                forEach(inputStreams, IOUtils::close);
            }
            return target;
        });
    }

    static <T> T toTarget(URL resource, ThrowableFunction<InputStream, T> inputStreamToTarget) {
        return execute(() -> {
            try (InputStream inputStream = resource.openStream()) {
                return inputStreamToTarget.apply(inputStream);
            }
        });
    }

    private RedisUtils() {
    }
}
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

import static io.microsphere.collection.ListUtils.forEach;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;

/**
 * Core utility class for Redis resource loading. Provides helpers that locate classpath
 * resources by name, open their {@link InputStream}s, and convert them to arbitrary
 * target objects via a caller-supplied {@link ThrowableFunction}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Load a single classpath resource and parse it as a String
 *   String content = RedisUtils.loadResource(
 *       "META-INF/redis-commands",
 *       inputStream -> IOUtils.copyToString(inputStream)
 *   );
 *
 *   // Load all classpath resources with the same name and collect their lines
 *   List<String> allLines = RedisUtils.loadResources(
 *       "META-INF/redis-commands",
 *       inputStreams -> {
 *           List<String> lines = new ArrayList<>();
 *           for (InputStream is : inputStreams) {
 *               lines.addAll(Arrays.asList(IOUtils.copyToString(is).split("\n")));
 *           }
 *           return lines;
 *       }
 *   );
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandUtils
 * @since 1.0.0
 */
public abstract class RedisUtils {

    private static final Logger logger = getLogger(RedisUtils.class);

    /**
     * The default {@link ClassLoader} used for resource lookups (usually the thread-context or application class loader).
     */
    public static final ClassLoader CLASS_LOADER = getDefaultClassLoader();

    /**
     * Loads a single classpath resource using the {@linkplain #CLASS_LOADER default class loader},
     * converts its {@link InputStream} to a target object via {@code inputStreamToTarget}, and returns it.
     *
     * @param <T>                 the target type
     * @param resourceName        the classpath resource path (e.g. {@code "META-INF/redis-commands"})
     * @param inputStreamToTarget function that converts the resource stream to {@code T}
     * @return the converted result
     */
    public static <T> T loadResource(String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget) {
        return loadResource(CLASS_LOADER, resourceName, inputStreamToTarget);
    }

    /**
     * Loads a single classpath resource using the specified {@link ClassLoader},
     * converts its {@link InputStream} to a target object via {@code inputStreamToTarget}, and returns it.
     *
     * @param <T>                 the target type
     * @param classLoader         the class loader to use for resource lookup
     * @param resourceName        the classpath resource path
     * @param inputStreamToTarget function that converts the resource stream to {@code T}
     * @return the converted result
     */
    public static <T> T loadResource(ClassLoader classLoader, String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget) {
        URL resource = classLoader.getResource(resourceName);
        return toTarget(resource, inputStreamToTarget);
    }

    /**
     * Loads all classpath resources with the given name using the {@linkplain #CLASS_LOADER default class loader},
     * passes the list of their {@link InputStream}s to {@code inputStreamsToTarget}, and returns the result.
     * All streams are closed after the function returns.
     *
     * @param <T>                    the target type
     * @param resourceName           the classpath resource path
     * @param inputStreamsToTarget   function that converts a list of resource streams to {@code T}
     * @return the converted result
     */
    public static <T> T loadResources(String resourceName, ThrowableFunction<List<InputStream>, T> inputStreamsToTarget) {
        return loadResources(CLASS_LOADER, resourceName, inputStreamsToTarget);
    }

    /**
     * Loads all classpath resources with the given name using the specified {@link ClassLoader},
     * passes the list of their {@link InputStream}s to {@code inputStreamsToTarget}, and returns the result.
     * All streams are closed after the function returns.
     *
     * @param <T>                    the target type
     * @param classLoader            the class loader to use for resource lookup
     * @param resourceName           the classpath resource path
     * @param inputStreamsToTarget   function that converts a list of resource streams to {@code T}
     * @return the converted result
     */
    public static <T> T loadResources(ClassLoader classLoader, String resourceName,
                                      ThrowableFunction<List<InputStream>, T> inputStreamsToTarget) {
        return execute(() -> {
            List<InputStream> inputStreams = new LinkedList<>();
            Enumeration<URL> resources = classLoader.getResources(resourceName);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                inputStreams.add(resource.openStream());
                logger.trace("The resource[url : '{}' , name : '{}'] be loaded", resource, resourceName);
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
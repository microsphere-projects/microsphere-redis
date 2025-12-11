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

import io.microsphere.annotation.Nonnull;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The Repository for Redis Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodMetadata
 * @see ParameterMetadata
 * @see RedisMetadata
 * @since 1.0.0
 */
public interface RedisMetadataRepository {

    /**
     * Initialize the MetadataRepository
     *
     * @throws Throwable if any error occurs
     */
    void init() throws Throwable;

    /**
     * Determine whether the specified method is a write command method
     *
     * @param method {@link Method}
     * @return <code>true</code> if the specified method is a write command method; <code>false</code> otherwise
     */
    boolean isWriteCommandMethod(Method method);

    /**
     * Get the {@link ParameterMetadata} list of the specified write command method
     *
     * @param method {@link Method}
     * @return {@link ParameterMetadata} list
     */
    @Nonnull
    List<ParameterMetadata> getWriteParameterMetadataList(Method method);

    /**
     * Find the write command method of the specified {@link RedisCommandEvent}
     *
     * @param event {@link RedisCommandEvent}
     * @return write command method
     */
    @Nonnull
    default Method findWriteCommandMethod(RedisCommandEvent event) {
        return event.getMethod();
    }

    /**
     * Find the write command method of the specified interface name, method name and parameter types
     *
     * @param interfaceName  interface name
     * @param methodName     method name
     * @param parameterTypes parameter types
     * @return write command method
     */
    Method findWriteCommandMethod(String interfaceName, String methodName, String[] parameterTypes);

    /**
     * Get the write command method of the specified interface name, method name and parameter types
     *
     * @param interfaceName  interface name
     * @param methodName     method name
     * @param parameterTypes parameter types
     * @return write command method
     */
    Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes);

    /**
     * Get the write command methods
     *
     * @return write command methods
     */
    Set<Method> getWriteCommandMethods();

    /**
     * Get the Redis Commands Interface Class of the specified interface name
     *
     * @param interfaceName interface name
     * @return Redis Commands Interface Class
     */
    Class<?> getRedisCommandsInterfaceClass(String interfaceName);

    /**
     * Get the Redis Command Binding Function of the specified interface name
     *
     * @param interfaceName interface name
     * @return Redis Command Binding Function
     */
    Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName);
}
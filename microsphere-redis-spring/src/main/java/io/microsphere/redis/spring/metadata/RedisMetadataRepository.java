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
import io.microsphere.annotation.Nullable;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The Repository of Spring Data Redis Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see InMemoryRedisMetadataRepository
 * @since 1.0.0
 */
public interface RedisMetadataRepository {

    /**
     * Initialize the Redis Metadata Repository
     */
    void init();

    @Nullable
    Integer findMethodIndex(Method redisCommandMethod);

    @Nullable
    Method findRedisCommandMethod(int methodIndex);

    boolean isWriteCommandMethod(Method method);

    @Nullable
    List<ParameterMetadata> getWriteParameterMetadataList(Method method);

    @Nullable
    Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes);

    @Nonnull
    default Method findWriteCommandMethod(RedisCommandEvent event) {
        return event.getMethod();
    }

    @Nullable
    Method findWriteCommandMethod(String interfaceNme, String methodName, String... parameterTypes);

    @Nullable
    Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes);

    @Nonnull
    Set<Method> getWriteCommandMethods();

    /**
     * Gets the {@link RedisCommands} command interface for the specified Class name {@link Class}
     *
     * @param interfaceName {@link RedisCommands} Command interface class name
     * @return If not found, return <code>null<code>
     */
    @Nullable
    Class<?> getRedisCommandInterfaceClass(String interfaceName);

    @Nonnull
    Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName);
}
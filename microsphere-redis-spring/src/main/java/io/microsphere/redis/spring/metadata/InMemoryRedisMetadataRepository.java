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

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.util.RedisCommandUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.metadata.RedisMetadataLoader.loadAll;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadClass;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadClasses;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * In-Memory {@link RedisMetadataRepository} implementation class.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMetadataRepository
 * @since 1.0.0
 */
public class InMemoryRedisMetadataRepository implements RedisMetadataRepository {

    private static final Logger logger = getLogger(InMemoryRedisMetadataRepository.class);

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost) from
     * {@link RedisConnection}.
     */
    Map<String, Class<?>> redisCommandInterfacesCache = newHashMap(256);

    /**
     * Redis Command {@link Method methods} cache using {@link RedisCommandUtils#buildMethodId(Method) Method ID} as key
     */
    Map<String, Method> redisCommandMethodsCache = newHashMap(256);

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands()}) binding
     */
    Map<String, Function<RedisConnection, Object>> redisCommandBindings = newHashMap(256);

    /**
     * Write Command MethodMetadata cache
     */
    Map<Method, List<ParameterMetadata>> writeCommandMethodsMetadata = newHashMap(256);

    /**
     * Method Simple signature with {@link Method} object caching (reduces reflection cost)
     */
    Map<String, Method> writeRedisCommandMethodsCache = newHashMap(256);

    /**
     * MethodMetadata cache
     * <ul>
     *     <li>If the {@link MethodMetadata#getIndex() Method ID} is a key, the value is {@link Method}.</li>
     *     <li>If the {@link Method} is a key, the value is {@link MethodMetadata}.</li>
     * </ul>
     */
    Map<Object, Object> methodMetadataCache;

    @Override
    public void init() {
        RedisMetadata redisMetadata = loadAll();
        List<MethodMetadata> methods = redisMetadata.getMethods();
        for (MethodMetadata method : methods) {
            String interfaceName = method.getInterfaceName();
            Class<?> interfaceClass = initRedisCommandInterfacesCache(interfaceName);
            initRedisCommandMethodsCache(interfaceClass, method);
        }
    }

    @Override
    public Integer findMethodIndex(Method redisCommandMethod) {
        return 0;
    }

    @Override
    public Method findRedisCommandMethod(int methodIndex) {
        return null;
    }

    @Override
    public boolean isWriteCommandMethod(Method method) {
        return false;
    }

    @Override
    public List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        return this.writeCommandMethodsMetadata.get(method);
    }

    @Override
    public Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        return this.redisCommandMethodsCache.get(methodId);
    }

    @Override
    public Method findWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        return null;
    }

    @Override
    public Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        return this.writeRedisCommandMethodsCache.get(methodId);
    }

    @Override
    public Set<Method> getWriteCommandMethods() {
        return this.writeCommandMethodsMetadata.keySet();
    }

    @Override
    public Class<?> getRedisCommandInterfaceClass(String interfaceName) {
        return this.redisCommandInterfacesCache.get(interfaceName);
    }

    @Override
    public Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return this.redisCommandBindings.get(interfaceName);
    }

    @Nullable
    private Class<?> initRedisCommandInterfacesCache(String interfaceName) {
        Class<?> interfaceClass = loadClass(interfaceName);
        cache(this.redisCommandInterfacesCache, interfaceName, interfaceClass);
        return interfaceClass;
    }

    @Nullable
    private void initRedisCommandMethodsCache(@Nullable Class<?> interfaceClass, MethodMetadata methodMetadata) {
        String interfaceName = methodMetadata.getInterfaceName();
        if (interfaceClass == null) {
            logger.warn("The Redis Command Interface[name : '{}'] can't be loaded", interfaceName);
            return;
        }
        String methodName = methodMetadata.getMethodName();
        String[] parameterTypes = methodMetadata.getParameterTypes();
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        Class<?>[] parameterClasses = loadClasses(parameterTypes);
        Method redisCommandMethod = findMethod(interfaceClass, methodName, parameterClasses);

        if (redisCommandMethod == null) {
            logger.warn("The Redis Command Method can't be found by name : '{}' and parameterTypes : {}", interfaceName, methodName, parameterTypes);
            return;
        }
        cache(this.redisCommandMethodsCache, methodId, redisCommandMethod);

        initRedisCommandBindings(redisCommandMethod);

        initWriteCommandMethods(methodId, redisCommandMethod, methodMetadata);
    }


    private void initRedisCommandBindings(Method redisCommandMethod) {
        if (redisCommandMethod.getParameterCount() < 1) {
            Class<?> returnType = redisCommandMethod.getReturnType();
            String interfaceName = returnType.getName();
            cache(this.redisCommandBindings, interfaceName, redisConnection -> invokeMethod(redisCommandMethod, redisConnection));
            logger.trace("The Redis Command Interface '{}' binds the RedisConnection command method: '{}'", interfaceName, redisCommandMethod);
        }
    }

    private void initWriteCommandMethods(String methodId, Method redisCommandMethod, MethodMetadata methodMetadata) {
        if (methodMetadata.isWrite()) {
            cache(this.writeRedisCommandMethodsCache, methodId, redisCommandMethod);
            initWriteCommandMethodsMetadata(redisCommandMethod, methodMetadata);
        }
    }

    private void initWriteCommandMethodsMetadata(Method redisCommandMethod, MethodMetadata methodMetadata) {
        String[] parameterTypes = methodMetadata.getParameterTypes();
        int parameterCount = parameterTypes.length;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(redisCommandMethod);
        List<ParameterMetadata> parameterMetadataList = newArrayList(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i];
            String parameterName = parameterNames[i];
            ParameterMetadata parameterMetadata = new ParameterMetadata(i, parameterType, parameterName);
            parameterMetadataList.add(parameterMetadata);
        }
        cache(this.writeCommandMethodsMetadata, redisCommandMethod, parameterMetadataList);
    }

    static <K, V> boolean cache(Map<K, V> cache, K key, V value) {
        V oldValue = cache.put(key, value);
        if (oldValue == null) {
            logger.trace("Caches the entry [key : {} , value : {}] into cache", key, value);
            return true;
        } else {
            logger.trace("The entry [key : {} , value : {}] was already cached into cache", key, value);
            return false;
        }
    }
}

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
import io.microsphere.redis.metadata.MethodInfo;
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
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static java.util.stream.Collectors.toSet;
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
    Map<String, Class<?>> redisCommandInterfacesCache;

    /**
     * MethodInfo cache
     * <ul>
     *     <li>If the {@link MethodMetadata#getIndex() Method Index} is a key, the value is {@link MethodInfo}.</li>
     *     <li>If the {@link Method} is a key, the value is {@link MethodInfo}.</li>
     *     <li>If the {@link RedisCommandUtils#buildMethodId(Method) Method Id} is a key, the value is {@link MethodInfo}.</li>
     * </ul>
     */
    Map<Object, MethodInfo> methodInfoCache;

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands()}) binding
     */
    Map<String, Function<RedisConnection, Object>> redisCommandBindings;

    @Override
    public void init() {
        RedisMetadata redisMetadata = loadAll();
        List<MethodMetadata> methods = redisMetadata.getMethods();

        this.redisCommandInterfacesCache = newHashMap(128);
        this.methodInfoCache = newHashMap(methods.size() * 3);
        this.redisCommandBindings = newHashMap(32);

        for (MethodMetadata method : methods) {
            String interfaceName = method.getInterfaceName();
            Class<?> interfaceClass = initRedisCommandInterfacesCache(interfaceName);
            initMethodInfoCache(interfaceClass, method);
        }
    }

    @Override
    public Integer findMethodIndex(Method redisCommandMethod) {
        MethodInfo methodInfo = getMethodInfo(redisCommandMethod);
        return methodInfo == null ? null : methodInfo.methodMetadata().getIndex();
    }

    @Override
    public Method findRedisCommandMethod(int methodIndex) {
        MethodInfo methodInfo = getMethodInfo(methodIndex);
        return methodInfo == null ? null : methodInfo.method();
    }

    @Override
    public boolean isWriteCommandMethod(Method method) {
        return false;
    }

    @Override
    public List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        MethodInfo methodInfo = getMethodInfo(method);
        if (isWrite(methodInfo)) {
            return methodInfo.parameterMetadataList();
        }
        return null;
    }

    @Override
    public Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        MethodInfo methodInfo = getMethodInfo(interfaceName, methodName, parameterTypes);
        return methodInfo == null ? null : methodInfo.method();
    }

    @Override
    public Method findWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        return null;
    }

    @Override
    public Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        MethodInfo methodInfo = getMethodInfo(interfaceName, methodName, parameterTypes);
        if (isWrite(methodInfo)) {
            return methodInfo.method();
        }
        return null;
    }

    @Override
    public Set<Method> getWriteCommandMethods() {
        return this.methodInfoCache.values()
                .stream()
                .filter(this::isWrite)
                .map(MethodInfo::method).collect(toSet());
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
    void initMethodInfoCache(@Nullable Class<?> interfaceClass, MethodMetadata methodMetadata) {
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

        trySetAccessible(redisCommandMethod);

        List<ParameterMetadata> parameterMetadataList = getParameterMetadataList(redisCommandMethod, methodMetadata);

        MethodInfo methodInfo = new MethodInfo(methodId, redisCommandMethod, methodMetadata, parameterMetadataList);

        int index = methodMetadata.getIndex();

        cache(this.methodInfoCache, index, methodInfo);
        cache(this.methodInfoCache, redisCommandMethod, methodInfo);
        cache(this.methodInfoCache, methodId, methodInfo);

        initRedisCommandBindings(redisCommandMethod);
    }

    private MethodInfo getMethodInfo(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        return getMethodInfo(methodId);
    }

    private MethodInfo getMethodInfo(Object key) {
        return this.methodInfoCache.get(key);
    }

    private boolean isWrite(MethodInfo methodInfo) {
        return methodInfo != null && methodInfo.methodMetadata().isWrite();
    }

    private List<ParameterMetadata> getParameterMetadataList(Method redisCommandMethod, MethodMetadata methodMetadata) {
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
        return parameterMetadataList;
    }

    private void initRedisCommandBindings(Method redisCommandMethod) {
        if (redisCommandMethod.getParameterCount() < 1) {
            Class<?> returnType = redisCommandMethod.getReturnType();
            String interfaceName = returnType.getName();
            cache(this.redisCommandBindings, interfaceName, redisConnection -> invokeMethod(redisCommandMethod, redisConnection));
            logger.trace("The Redis Command Interface '{}' binds the RedisConnection command method: '{}'", interfaceName, redisCommandMethod);
        }
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
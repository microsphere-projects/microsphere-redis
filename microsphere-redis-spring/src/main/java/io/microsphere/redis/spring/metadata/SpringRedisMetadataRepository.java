package io.microsphere.redis.spring.metadata;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.MethodInfo;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.spring.util.SpringRedisCommandUtils;
import io.microsphere.redis.util.RedisCommandUtils;
import org.springframework.data.redis.connection.RedisCommands;
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
import static io.microsphere.redis.spring.serializer.Serializers.getSerializer;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.isRedisCommandsInterface;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.loadClasses;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.redis.util.RedisCommandUtils.buildParameterMetadataList;
import static io.microsphere.redis.util.RedisCommandUtils.getParameterClassNames;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ClassUtils.getAllInterfaces;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * Static repository and cache for all Spring Data Redis method metadata loaded from the
 * {@code META-INF/spring-data-redis-metadata.yaml} YAML descriptors.
 * Provides lookup methods to resolve {@link MethodInfo}, check whether a method is a Redis
 * write command, and obtain the binding function for a Redis command sub-interface.
 *
 * <p>The repository is eagerly initialised in a static initializer by calling {@link #initCache()}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Trigger initialization (no-op if already done via static block):
 *   SpringRedisMetadataRepository.init();
 *
 *   // Check whether a method is a write command
 *   Method method = RedisStringCommands.class.getMethod("set", byte[].class, byte[].class);
 *   boolean write = SpringRedisMetadataRepository.isWriteCommandMethod(method); // true
 *
 *   // Resolve a method by index
 *   Integer index = SpringRedisMetadataRepository.getMethodIndex(method);
 *   Method resolved = SpringRedisMetadataRepository.getRedisCommandMethod(index);
 *
 *   // Get the parameter metadata list for a write command
 *   List<ParameterMetadata> params = SpringRedisMetadataRepository.getWriteParameterMetadataList(method);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringRedisMetadataRepository {

    private static final Logger logger = getLogger(SpringRedisMetadataRepository.class);

    static Method[] redisConnectionMethods = RedisConnection.class.getMethods();

    static final Set<Method> redisCommandMethods = of(redisConnectionMethods)
            .filter(SpringRedisMetadataRepository::isRedisCommandMethod)
            .collect(toSet());

    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost) from
     * {@link RedisConnection}.
     */
    static final Map<String, Class<?>> redisCommandInterfacesCache = getAllInterfaces(RedisConnection.class)
            .stream()
            .filter(SpringRedisCommandUtils::isRedisCommandsInterface)
            .collect(toMap(Class::getName, t -> t));

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands()}) binding
     */
    static final Map<String, Function<RedisConnection, Object>> redisCommandBindings = newHashMap(32);

    /**
     * MethodInfo cache
     * <ul>
     *     <li>If the {@link MethodMetadata#getIndex() Method Index} is a key, the value is {@link MethodInfo}.</li>
     *     <li>If the {@link Method} is a key, the value is {@link MethodInfo}.</li>
     *     <li>If the {@link RedisCommandUtils#buildMethodId(Method) Method Id} is a key, the value is {@link MethodInfo}.</li>
     * </ul>
     */
    static final Map<Object, MethodInfo> methodInfoCache = newHashMap(4 * 1024);

    static {
        initCache();
    }

    /**
     * Triggers initialization of the Redis metadata repository.
     * The static initializer already calls {@link #initCache()} eagerly,
     * so this method is effectively a no-op, but is provided for explicit bootstrapping.
     */
    public static void init() {
    }

    /**
     * Returns the numeric method index for the given {@link Method}, or {@code null} if not found.
     *
     * @param redisCommandMethod the Redis command method
     * @return the method index, or {@code null}
     */
    @Nullable
    public static Integer getMethodIndex(Method redisCommandMethod) {
        MethodInfo methodInfo = getMethodInfo(redisCommandMethod);
        return methodInfo == null ? null : methodInfo.getMethodMetadata().getIndex();
    }

    /**
     * Returns the {@link Method} associated with the given numeric method index, or {@code null} if not found.
     *
     * @param methodIndex the method index (absolute hash of the method id)
     * @return the Redis command {@link Method}, or {@code null}
     */
    @Nullable
    public static Method getRedisCommandMethod(int methodIndex) {
        MethodInfo methodInfo = getMethodInfo(methodIndex);
        return methodInfo == null ? null : methodInfo.getMethod();
    }

    /**
     * Returns the {@link Method} identified by interface name, method name, and parameter types,
     * or {@code null} if not found.
     *
     * @param interfaceName  the fully-qualified Redis command interface name
     * @param methodName     the method name
     * @param parameterTypes the parameter type names (varargs)
     * @return the Redis command {@link Method}, or {@code null}
     */
    @Nullable
    public static Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        MethodInfo methodInfo = getMethodInfo(interfaceName, methodName, parameterTypes);
        return methodInfo == null ? null : methodInfo.getMethod();
    }

    /**
     * Returns {@code true} if the given method is a known Redis <em>write</em> command.
     *
     * @param method the method to check
     * @return {@code true} for write commands (e.g. SET, DEL)
     */
    public static boolean isWriteCommandMethod(Method method) {
        MethodInfo methodInfo = getMethodInfo(method);
        return isWrite(methodInfo);
    }

    /**
     * Returns the {@link ParameterMetadata} list for the given method if it is a write command,
     * or {@code null} if it is not a write command or is unknown.
     *
     * @param method the Redis command method
     * @return the parameter metadata list, or {@code null}
     */
    @Nullable
    public static List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        MethodInfo methodInfo = getMethodInfo(method);
        if (isWrite(methodInfo)) {
            return methodInfo.getParameterMetadataList();
        }
        return null;
    }

    /**
     * Returns the write command {@link Method} identified by interface name, method name, and parameter types,
     * or {@code null} if not found or if the method is not a write command.
     *
     * @param interfaceName  the fully-qualified Redis command interface name
     * @param methodName     the method name
     * @param parameterTypes the parameter type names (varargs)
     * @return the write command {@link Method}, or {@code null}
     */
    @Nullable
    public static Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        MethodInfo methodInfo = getMethodInfo(interfaceName, methodName, parameterTypes);
        if (isWrite(methodInfo)) {
            return methodInfo.getMethod();
        }
        return null;
    }

    /**
     * Gets the {@link RedisCommands} command interface for the specified Class name {@link Class}
     *
     * @param interfaceName {@link RedisCommands} Command interface class name
     * @return If not found, return <code>null<code>
     */
    @Nullable
    public static Class<?> getRedisCommandInterfaceClass(String interfaceName) {
        return redisCommandInterfacesCache.get(interfaceName);
    }

    /**
     * Returns the binding function that retrieves the Redis sub-command object from a
     * {@link RedisConnection} for the given interface name (e.g.
     * {@code redisConnection.stringCommands()}). Falls back to returning the connection
     * itself when no specific binding exists.
     *
     * @param interfaceName the fully-qualified Redis command interface name
     * @return non-null function
     */
    @Nonnull
    public static Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return redisCommandBindings.getOrDefault(interfaceName, redisConnection -> redisConnection);
    }

    static void initCache() {
        RedisMetadata redisMetadata = loadAll();
        List<MethodMetadata> methods = redisMetadata.getMethods();
        for (MethodMetadata method : methods) {
            String interfaceName = method.getInterfaceName();
            Class<?> interfaceClass = getRedisCommandInterfaceClass(interfaceName);
            initMethodInfo(interfaceClass, method);
        }

        initRedisCommandBindings();

        initRedisConnectionInterfaces();
    }

    private static void initRedisCommandBindings() {
        for (Method redisConnectionMethod : redisConnectionMethods) {
            initRedisCommandBindings(redisConnectionMethod);
        }
    }

    @Nullable
    static void initMethodInfo(@Nullable Class<?> interfaceClass, MethodMetadata methodMetadata) {
        String interfaceName = methodMetadata.getInterfaceName();
        if (interfaceClass == null) {
            logger.warn("The Redis Command Interface[name : '{}'] can't be loaded", interfaceName);
            return;
        }
        String methodName = methodMetadata.getMethodName();
        String[] parameterTypes = methodMetadata.getParameterTypes();
        Class<?>[] parameterClasses = loadClasses(parameterTypes);
        Method redisCommandMethod = findMethod(interfaceClass, methodName, parameterClasses);

        cacheMethodInfo(redisCommandMethod, methodMetadata);
    }

    static void initRedisConnectionInterfaces() {
        initRedisConnectionInterface();
    }

    static void initRedisConnectionInterface() {
        for (Method method : redisCommandMethods) {
            initRedisConnectionInterface(method);
        }
    }

    static void initRedisConnectionInterface(Method method) {
        // Find the method override one of The RedisCommands interfaces' methods
        MethodInfo methodInfo = getMethodInfo(method);
        if (methodInfo == null) {
            Class<?> declaringClass = method.getDeclaringClass();
            if (isAssignableFrom(RedisCommands.class, declaringClass)) {
                String methodName = method.getName();
                Class<?>[] parameterTypes = method.getParameterTypes();
                Method overridenMethod = findMethod(RedisCommands.class, methodName, parameterTypes);
                if (overridenMethod != null) {
                    methodInfo = getMethodInfo(overridenMethod);
                    if (methodInfo != null) {
                        MethodMetadata methodMetadata = methodInfo.getMethodMetadata();
                        createAndCacheMethodInfo(method, methodMetadata);
                    }
                }
            }
        }
    }

    static void createAndCacheMethodInfo(Method overrider, MethodMetadata overriddenMethodMetadata) {
        int index = buildMethodIndex(overrider);
        String interfaceName = overrider.getDeclaringClass().getName();
        String methodName = overrider.getName();
        String[] parameterNames = overriddenMethodMetadata.getParameterNames();
        String[] parameterTypes = getParameterClassNames(overrider.getParameterTypes());
        String[] commands = overriddenMethodMetadata.getCommands();
        boolean write = overriddenMethodMetadata.isWrite();

        MethodMetadata methodMetadata = new MethodMetadata();
        methodMetadata.setIndex(index);
        methodMetadata.setInterfaceName(interfaceName);
        methodMetadata.setMethodName(methodName);
        methodMetadata.setParameterNames(parameterNames);
        methodMetadata.setParameterTypes(parameterTypes);
        methodMetadata.setCommands(commands);
        methodMetadata.setWrite(write);

        cacheMethodInfo(overrider, methodMetadata);
    }

    static MethodInfo getMethodInfo(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        return getMethodInfo(methodId);
    }

    static MethodInfo getMethodInfo(Object key) {
        return methodInfoCache.get(key);
    }

    static boolean isWrite(MethodInfo methodInfo) {
        return methodInfo != null && methodInfo.getMethodMetadata().isWrite();
    }

    static void cacheMethodInfo(Method redisCommandMethod, MethodMetadata methodMetadata) {
        if (redisCommandMethod == null) {
            logger.warn("The Redis Command Method can't be found from {}", methodMetadata);
            return;
        }

        trySetAccessible(redisCommandMethod);

        List<ParameterMetadata> parameterMetadataList = getParameterMetadataList(redisCommandMethod, methodMetadata);

        parameterMetadataList.forEach(parameterMetadata -> {
            // Preload the RedisSerializer implementation for the Method parameter type
            String parameterType = parameterMetadata.getParameterType();
            getSerializer(parameterType);
        });

        MethodInfo methodInfo = new MethodInfo(redisCommandMethod, methodMetadata, parameterMetadataList);

        String methodId = methodInfo.getId();
        int index = methodMetadata.getIndex();

        cache(methodInfoCache, index, methodInfo);
        cache(methodInfoCache, redisCommandMethod, methodInfo);
        cache(methodInfoCache, methodId, methodInfo);
    }

    static List<ParameterMetadata> getParameterMetadataList(Method redisCommandMethod, MethodMetadata methodMetadata) {
        String[] parameterNames = methodMetadata.getParameterNames();
        if (parameterNames == null) {
            return buildParameterMetadataList(redisCommandMethod);
        }
        String[] parameterTypes = methodMetadata.getParameterTypes();
        int parameterCount = parameterTypes.length;
        List<ParameterMetadata> parameterMetadataList = newArrayList(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i];
            String parameterName = parameterNames[i];
            ParameterMetadata parameterMetadata = new ParameterMetadata(i, parameterType, parameterName);
            parameterMetadataList.add(parameterMetadata);
        }
        return parameterMetadataList;
    }

    static void initRedisCommandBindings(Method redisConnectionMethod) {
        if (redisConnectionMethod.getParameterCount() < 1) {
            Class<?> returnType = redisConnectionMethod.getReturnType();
            if (isRedisCommandsInterface(returnType)) {
                String interfaceName = returnType.getName();
                cache(redisCommandBindings, interfaceName, redisConnection -> invokeMethod(redisConnectionMethod, redisConnection));
                logger.trace("The Redis Command Interface '{}' binds the RedisConnection command method: '{}'", interfaceName, redisConnectionMethod);
            }
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

    static boolean isRedisCommandMethod(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        return !RedisConnection.class.equals(declaringClass)
                && RedisCommands.class.isAssignableFrom(declaringClass);
    }

    private SpringRedisMetadataRepository() {
    }
}
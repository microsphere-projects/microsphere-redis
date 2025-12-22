package io.microsphere.redis.spring.metadata;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.MethodInfo;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.util.RedisCommandUtils;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.metadata.RedisMetadataLoader.loadAll;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadClass;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadClasses;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodIndex;
import static io.microsphere.redis.util.RedisCommandUtils.getParameterClassNames;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ClassUtils.getAllInterfaces;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * Spring Data Redis Metadata Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringRedisMetadataRepository {

    private static final Logger logger = getLogger(SpringRedisMetadataRepository.class);

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    static final Method[] redisCommandMethods = RedisConnection.class.getMethods();

    static final List<Class<?>> redisCommandInterfaceClasses = getAllInterfaces(RedisConnection.class);

    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost) from
     * {@link RedisConnection}.
     */
    static final Map<String, Class<?>> redisCommandInterfacesCache = newHashMap(128);

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
     * Initialize the Redis Metadata Repository
     */
    public static void init() {
    }

    @Nullable
    public static Integer getMethodIndex(Method redisCommandMethod) {
        MethodInfo methodInfo = getMethodInfo(redisCommandMethod);
        return methodInfo == null ? null : methodInfo.getMethodMetadata().getIndex();
    }

    @Nullable
    public static Method getRedisCommandMethod(int methodIndex) {
        MethodInfo methodInfo = getMethodInfo(methodIndex);
        return methodInfo == null ? null : methodInfo.getMethod();
    }

    public static boolean isWriteCommandMethod(Method method) {
        MethodInfo methodInfo = getMethodInfo(method);
        return isWrite(methodInfo);
    }

    @Nullable
    public static List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        MethodInfo methodInfo = getMethodInfo(method);
        if (isWrite(methodInfo)) {
            return methodInfo.getParameterMetadataList();
        }
        return null;
    }

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

    @Nonnull
    public static Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return redisCommandBindings.getOrDefault(interfaceName, redisConnection -> redisConnection);
    }

    @Nullable
    public static Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        MethodInfo methodInfo = getMethodInfo(interfaceName, methodName, parameterTypes);
        return methodInfo == null ? null : methodInfo.getMethod();
    }

    static void initCache() {
        RedisMetadata redisMetadata = loadAll();
        List<MethodMetadata> methods = redisMetadata.getMethods();
        for (MethodMetadata method : methods) {
            String interfaceName = method.getInterfaceName();
            Class<?> interfaceClass = initRedisCommandInterfacesCache(interfaceName);
            if (redisCommandInterfaceClasses.contains(interfaceClass)) {
                initMethodInfo(interfaceClass, method);
            }
        }
        initRedisConnectionInterfaces();
    }

    @Nullable
    static Class<?> initRedisCommandInterfacesCache(String interfaceName) {
        Class<?> interfaceClass = loadClass(interfaceName);
        cache(redisCommandInterfacesCache, interfaceName, interfaceClass);
        return interfaceClass;
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

        if (redisCommandMethod == null) {
            logger.warn("The Redis Command Method can't be found from {}", methodMetadata);
            return;
        }

        cacheMethodInfo(redisCommandMethod, methodMetadata);

        initRedisCommandBindings(redisCommandMethod);
    }

    static void initRedisConnectionInterfaces() {
        initRedisConnectionInterface();
    }

    static void initRedisConnectionInterface() {
        for (Method method : redisCommandMethods) {
            // Find the method override one of The RedisCommands interfaces' methods
            MethodInfo methodInfo = getMethodInfo(method);
            if (methodInfo == null) {
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
        String[] parameterTypes = getParameterClassNames(overrider.getParameterTypes());
        String[] commands = overriddenMethodMetadata.getCommands();
        boolean write = overriddenMethodMetadata.isWrite();

        MethodMetadata methodMetadata = new MethodMetadata();
        methodMetadata.setIndex(index);
        methodMetadata.setInterfaceName(interfaceName);
        methodMetadata.setMethodName(methodName);
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
        trySetAccessible(redisCommandMethod);

        List<ParameterMetadata> parameterMetadataList = getParameterMetadataList(redisCommandMethod, methodMetadata);

        MethodInfo methodInfo = new MethodInfo(redisCommandMethod, methodMetadata, parameterMetadataList);

        String methodId = methodInfo.getId();
        int index = methodMetadata.getIndex();

        cache(methodInfoCache, index, methodInfo);
        cache(methodInfoCache, redisCommandMethod, methodInfo);
        cache(methodInfoCache, methodId, methodInfo);
    }

    static List<ParameterMetadata> getParameterMetadataList(Method redisCommandMethod, MethodMetadata methodMetadata) {
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

    static void initRedisCommandBindings(Method redisCommandMethod) {
        if (redisCommandMethod.getParameterCount() < 1) {
            Class<?> returnType = redisCommandMethod.getReturnType();
            String interfaceName = returnType.getName();
            cache(redisCommandBindings, interfaceName, redisConnection -> invokeMethod(redisCommandMethod, redisConnection));
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

    private SpringRedisMetadataRepository() {
    }
}
package io.microsphere.redis.spring.metadata;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.MethodMetadata;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.redis.spring.util.RedisCommandsUtils;
import io.microsphere.redis.spring.util.RedisConstants;
import io.microsphere.redis.util.RedisCommandUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisCommandsUtils.loadParameterClasses;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.getAllInterfaces;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * Redis Metadata Repository
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMetadataRepository
 * @since 1.0.0
 */
@Deprecated
public class SpringRedisMetadataRepository {

    private static final Logger logger = getLogger(SpringRedisMetadataRepository.class);

    /**
     * {@link org.springframework.data.redis.connection.DefaultedRedisConnection} was introduced in Spring Data Redis 2.0.0.RELEASE,
     * which is {@link Deprecated deprecated}, and may be removed in the future.
     *
     * @see org.springframework.data.redis.connection.DefaultedRedisConnection
     */
    private static final Class<?> DEFAULTED_REDIS_CONNECTION_CLASS = resolveClass("org.springframework.data.redis.connection.DefaultedRedisConnection");

    /**
     * Interface Class name and {@link Class} object cache (reduces class loading performance cost)
     */
    static final Map<String, Class<?>> redisCommandInterfacesCache = initRedisCommandInterfacesCache();

    /**
     * Redis Command {@link Method methods} cache using {@link RedisCommandUtils#buildMethodId(Method) Method ID} as key
     */
    static final Map<String, Method> redisCommandMethodsCache = initRedisCommandMethodsCache();

    /**
     * Command interface class name and {@link RedisConnection} command object function
     * (such as: {@link RedisConnection#keyCommands()}) binding
     */
    static final Map<String, Function<RedisConnection, Object>> redisCommandBindings = initRedisCommandBindings();

    static final Map<Method, List<ParameterMetadata>> writeCommandMethodsMetadata = new HashMap<>(256);

    /**
     * Method Simple signature with {@link Method} object caching (reduces reflection cost)
     */
    static final Map<String, Method> writeCommandMethodsCache = new HashMap<>(256);

    /**
     * MethodMetadata cache
     * <ul>
     *     <li>If the {@link MethodMetadata#getIndex() Method ID} is a key, the value is {@link Method}.</li>
     *     <li>If the {@link Method} is a key, the value is {@link MethodMetadata}.</li>
     * </ul>
     */
    static final Map<Object, Object> methodMetadataCache = initMethodMetadataCache();

    /**
     * Initialize the Redis Metadata Repository
     */
    public static void init() {
        // initialize the caches by class loading
    }

    /**
     * Caches the name of the {@link RedisCommands} command interface with the {@link Class} object cache
     */
    private static Map<String, Class<?>> initRedisCommandInterfacesCache() {
        List<Class<?>> redisCommandInterfaceClasses = getAllInterfaces(RedisCommands.class);
        int size = redisCommandInterfaceClasses.size();
        Map<String, Class<?>> redisCommandInterfacesCache = newFixedHashMap(size);
        for (int i = 0; i < size; i++) {
            Class<?> redisCommandInterfaceClass = redisCommandInterfaceClasses.get(i);
            String interfaceName = redisCommandInterfaceClass.getName();
            redisCommandInterfacesCache.put(interfaceName, redisCommandInterfaceClass);
            logger.debug("Caches the Redis Command Interface : {}", interfaceName);
        }
        return unmodifiableMap(redisCommandInterfacesCache);
    }

    private static Map<String, Method> initRedisCommandMethodsCache() {
        Collection<Class<?>> redisCommandInterfaceClasses = redisCommandInterfacesCache.values();
        Map<String, Method> redisCommandMethodsCache = new HashMap<>(512);
        for (Class<?> redisCommandInterfaceClass : redisCommandInterfaceClasses) {
            Method[] methods = redisCommandInterfaceClass.getMethods();
            for (Method method : methods) {
                String methodId = RedisCommandUtils.buildMethodId(method);
                redisCommandMethodsCache.put(methodId, method);
                trySetAccessible(method);
                logger.debug("Caches the Redis Command Method : {}", methodId);
            }
        }
        return unmodifiableMap(redisCommandMethodsCache);
    }

    private static Map<String, Function<RedisConnection, Object>> initRedisCommandBindings() {
        Class<?> redisCommandInterfaceClass = RedisConnection.class;
        Method[] redisCommandMethods = redisCommandInterfaceClass.getMethods();
        int length = redisCommandMethods.length;
        Map<String, Function<RedisConnection, Object>> redisCommandBindings = new HashMap<>(1);
        for (int i = 0; i < length; i++) {
            Method redisCommandMethod = redisCommandMethods[i];
            initRedisCommandBindings(redisCommandInterfaceClass, redisCommandMethod, redisCommandBindings);
        }
        return unmodifiableMap(redisCommandBindings);
    }

    private static Map<Object, Object> initMethodMetadataCache() {
        RedisMetadata redisMetadata = loadRedisMetadata();
        List<MethodMetadata> methodMetadataList = redisMetadata.getMethods();
        int size = methodMetadataList.size();
        Map<Object, Object> redisMetadataCache = new HashMap<>(size * 2);
        for (int i = 0; i < size; i++) {
            MethodMetadata methodMetadata = methodMetadataList.get(i);
            Method redisCommandMethod = getRedisCommandMethod(methodMetadata);

            if (redisCommandMethod == null) {
                logger.warn("The Redis Command Method[{}] can't be found in the artifact 'org.springframework.data:spring-data-redis'", methodMetadata);
                continue;
            }

            int id = methodMetadata.getIndex();
            // Put id and Method as key
            if (redisMetadataCache.put(id, redisCommandMethod) == null && redisMetadataCache.put(redisCommandMethod, methodMetadata) == null) {
                if (methodMetadata.isWrite()) {
                    Class<?>[] parameterTypes = redisCommandMethod.getParameterTypes();
                    initWriteCommandMethod(redisCommandMethod, parameterTypes);

                    Method overriddenMethod = findOverriddenMethod(redisCommandMethod, parameterTypes);
                    if (overriddenMethod != null) {
                        redisMetadataCache.put(overriddenMethod, methodMetadata);
                        initWriteCommandMethod(overriddenMethod, parameterTypes);
                    }
                }
            } else {
                throw new IllegalStateException("Duplicated Redis Command Method was found, " + methodMetadata);
            }
        }
        return redisMetadataCache;
    }

    private static Method getRedisCommandMethod(MethodMetadata methodMetadata) {
        String interfaceName = methodMetadata.getInterfaceName();
        String methodName = methodMetadata.getMethodName();
        String[] parameterTypes = methodMetadata.getParameterTypes();
        return getRedisCommandMethod(interfaceName, methodName, parameterTypes);
    }

    private static RedisMetadata loadRedisMetadata() {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        RedisMetadata redisMetadata = new RedisMetadata();
        try {
            Resource[] resources = resolver.getResources(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/META-INF/spring-data-redis-metadata.yaml");
            int size = resources.length;
            for (int i = 0; i < size; i++) {
                Resource resource = resources[i];
                redisMetadata.merge(loadRedisMetadata(resource));
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        return redisMetadata;
    }

    private static RedisMetadata loadRedisMetadata(Resource resource) throws IOException {
        Yaml yaml = new Yaml();
        RedisMetadata redisMetadata = yaml.loadAs(resource.getInputStream(), RedisMetadata.class);
        return redisMetadata;
    }

    public static Integer findMethodIndex(Method redisCommandMethod) {
        MethodMetadata methodMetadata = (MethodMetadata) methodMetadataCache.get(redisCommandMethod);
        return methodMetadata == null ? null : methodMetadata.getIndex();
    }

    public static Method findRedisCommandMethod(int methodIndex) {
        Method redisCommandMethod = (Method) methodMetadataCache.get(methodIndex);
        return redisCommandMethod;
    }

    public static boolean isWriteCommandMethod(Method method) {
        return writeCommandMethodsMetadata.containsKey(method);
    }

    public static List<ParameterMetadata> getWriteParameterMetadataList(Method method) {
        return writeCommandMethodsMetadata.get(method);
    }

    public static Method findWriteCommandMethod(RedisCommandEvent event) {
        return event.getMethod();
    }

    public static Method findWriteCommandMethod(String interfaceNme, String methodName, String... parameterTypes) {
        Method method = getWriteCommandMethod(interfaceNme, methodName, parameterTypes);
        if (method == null) {
            logger.warn("Redis event publishers and consumers have different apis. Please update consumer microsphere-spring-redis artifacts in time!");
            logger.debug("Redis command methods will use Java reflection to find (interface :{}, method name :{}, parameter list :{})...", interfaceNme, methodName, Arrays.toString(parameterTypes));
            Class<?> redisCommandInterfaceClass = getRedisCommandInterfaceClass(interfaceNme);
            if (redisCommandInterfaceClass == null) {
                logger.warn("The current Redis consumer cannot find Redis command interface: {}. Please confirm whether the spring-data artifacts API is compatible.", interfaceNme);
                return null;
            }
            Class[] parameterClasses = loadParameterClasses(parameterTypes);
            method = findMethod(redisCommandInterfaceClass, methodName, parameterClasses);
            if (method == null) {
                logger.warn("Current Redis consumer Redis command interface (class name: {}) in the method ({}), command method search end!", interfaceNme, buildMethodId(interfaceNme, methodName, parameterTypes));
                return null;
            }
        }
        return method;
    }

    public static Method getWriteCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String id = buildMethodId(interfaceName, methodName, parameterTypes);
        return writeCommandMethodsCache.get(id);
    }

    public static Set<Method> getWriteCommandMethods() {
        return writeCommandMethodsMetadata.keySet();
    }

    /**
     * Gets the {@link RedisCommands} command interface for the specified Class name {@link Class}
     *
     * @param interfaceName {@link RedisCommands} Command interface class name
     * @return If not found, return <code>null<code>
     */
    public static Class<?> getRedisCommandInterfaceClass(String interfaceName) {
        return redisCommandInterfacesCache.get(interfaceName);
    }

    public static Function<RedisConnection, Object> getRedisCommandBindingFunction(String interfaceName) {
        return redisCommandBindings.getOrDefault(interfaceName, redisConnection -> redisConnection);
    }

    public static Method getRedisCommandMethod(String interfaceName, String methodName, String... parameterTypes) {
        String methodId = buildMethodId(interfaceName, methodName, parameterTypes);
        return redisCommandMethodsCache.get(methodId);
    }

    private static void initRedisCommandBindings(Class<?> redisCommandInterfaceClass, Method redisCommandMethod, Map<String, Function<RedisConnection, Object>> redisCommandBindings) {
        Class<?> returnType = redisCommandMethod.getReturnType();
        if (redisCommandInterfaceClass.equals(returnType) && redisCommandMethod.getParameterCount() < 1) {
            String interfaceName = redisCommandInterfaceClass.getName();
            redisCommandBindings.put(interfaceName, redisConnection -> invokeMethod(redisCommandMethod, redisConnection));
            logger.debug("Redis command interface '{}' Bind RedisConnection command method['{}']", interfaceName, redisCommandMethod);
        }
    }

    private static void initWriteCommandMethod(Method method, Class<?>[] parameterTypes) {
        try {
            // Reduced Method runtime checks
            trySetAccessible(method);
            if (initWriteCommandMethodParameterMetadata(method, parameterTypes)) {
                initWriteCommandMethodCache(method, parameterTypes);
            }
        } catch (Throwable e) {
            logger.error("Unable to initialize write command method['{}'], Reason: {}", method, e.getMessage());
            if (RedisConstants.MICROSPHERE_REDIS_FAIL_FAST_ENABLED) {
                logger.error("Fail-Fast mode is activated and an exception is about to be thrown. You can disable Fail-Fast mode with the JVM startup parameter -D{}=false", RedisConstants.MICROSPHERE_REDIS_FAIL_FAST_ENABLED_PROPERTY_NAME);
                throw new IllegalArgumentException(e);
            }
        }
    }

    private static boolean initWriteCommandMethodParameterMetadata(Method method, Class<?>[] parameterTypes) {
        if (writeCommandMethodsMetadata.containsKey(method)) {
            return false;
        }
        List<ParameterMetadata> parameterMetadataList = RedisCommandsUtils.buildParameterMetadata(method, parameterTypes);
        writeCommandMethodsMetadata.put(method, parameterMetadataList);

        logger.debug("Caches the Redis Write Command Method['{}'] Parameter Metadata : {}",
                method, parameterMetadataList);
        return true;
    }

    private static void initWriteCommandMethodCache(Method method, Class<?>[] parameterTypes) {
        Class<?> declaredClass = method.getDeclaringClass();
        String id = RedisCommandUtils.buildMethodId(declaredClass.getName(), method.getName(), parameterTypes);
        if (writeCommandMethodsCache.putIfAbsent(id, method) == null) {
            logger.debug("Caches the Redis Write Command Method : {}", id);
        } else {
            logger.warn("The Redis Write Command Method[{}] was cached", id, method);
        }
    }

    /**
     * Find the overridden method.
     * <p>
     * The class DefaultedRedisConnection overrides the methods of RedisCommands and its' sub interfaces
     *
     * @param method         {@link Method}
     * @param parameterTypes the types of parameters
     * @return <code>null</code> if not found
     * @see org.springframework.data.redis.connection.DefaultedRedisConnection
     */
    @Nullable
    private static Method findOverriddenMethod(Method method, Class<?>[] parameterTypes) {
        return findMethod(DEFAULTED_REDIS_CONNECTION_CLASS, method.getName(), parameterTypes);
    }
}
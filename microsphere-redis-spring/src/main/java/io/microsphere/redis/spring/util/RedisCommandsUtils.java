package io.microsphere.redis.spring.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.Parameter;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getWriteParameterMetadataList;
import static io.microsphere.redis.spring.serializer.Serializers.getSerializer;
import static io.microsphere.redis.spring.serializer.Serializers.serializeRawParameter;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.getRedisWriteCommands;
import static io.microsphere.redis.util.RedisUtils.CLASS_LOADER;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.StringUtils.INDEX_NOT_FOUND;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.util.Collections.unmodifiableList;
import static org.springframework.util.ClassUtils.forName;

/**
 * {@link RedisCommands Redis Command} Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class RedisCommandsUtils {

    private static final Logger logger = getLogger(RedisCommandsUtils.class);

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    /**
     * The package name of {@link RedisCommands}
     */
    public static final String REDIS_COMMANDS_PACKAGE_NAME = "org.springframework.data.redis.connection.";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisKeyCommands}
     */
    public static final String REDIS_KEY_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisKeyCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisStringCommands}
     */
    public static final String REDIS_STRING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStringCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisListCommands}
     */
    public static final String REDIS_LIST_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisListCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisSetCommands}
     */
    public static final String REDIS_SET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisSetCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisZSetCommands}
     */
    public static final String REDIS_ZSET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisZSetCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisHashCommands}
     */
    public static final String REDIS_HASH_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHashCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisTxCommands}
     */
    public static final String REDIS_TX_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisTxCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisPubSubCommands}
     */
    public static final String REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisPubSubCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisConnectionCommands}
     */
    public static final String REDIS_CONNECTION_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisConnectionCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisServerCommands}
     */
    public static final String REDIS_SERVER_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisServerCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisStreamCommands}
     */
    public static final String REDIS_STREAM_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStreamCommands";

    /**
     *
     */
    public static final String REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisScriptingCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisGeoCommands}
     */
    public static final String REDIS_GEO_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisGeoCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisHyperLogLogCommands}
     */
    public static final String REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHyperLogLogCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisCommands}
     */
    public static final String REDIS_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisCommands";

    /**
     * The {@link Method} of {@link RedisCommands#execute(String, byte[][])}
     */
    public static final Method REDIS_COMMANDS_EXECUTE_METHOD = findMethod(RedisCommands.class, "execute", String.class, byte[][].class);

    static final int REDIS_COMMANDS_PACKAGE_NAME_LENGTH = REDIS_COMMANDS_PACKAGE_NAME.length();

    static final String REDIS_COMMANDS_INTERFACE_NAME_PREFIX = REDIS_COMMANDS_PACKAGE_NAME + "Redis";

    static final String REACTIVE_COMMANDS_INTERFACE_NAME_PREFIX = REDIS_COMMANDS_PACKAGE_NAME + "Reactive";

    static final String REDIS_COMMANDS_INTERFACE_NAME_SUFFIX = "Commands";

    static final Set<String> redisWriteCommands = getRedisWriteCommands();

    static final ConcurrentMap<String, Class<?>> classesCache = new ConcurrentHashMap<>(256);

    public static String resolveSimpleInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf(REDIS_COMMANDS_PACKAGE_NAME);
        if (index == 0) {
            return interfaceName.substring(REDIS_COMMANDS_PACKAGE_NAME_LENGTH);
        } else {
            return interfaceName;
        }
    }

    public static String resolveInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf(DOT_CHAR);
        if (index == INDEX_NOT_FOUND) {
            return REDIS_COMMANDS_PACKAGE_NAME + interfaceName;
        } else {
            return interfaceName;
        }
    }

    public static boolean isRedisCommandsInterface(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            if (RedisCommands.class.isAssignableFrom(interfaceClass)) {
                return true;
            }
            return isRedisCommandsInterface(interfaceClass.getName());
        }
        return false;
    }

    public static boolean isRedisCommandsInterface(String interfaceClassName) {
        return interfaceClassName.endsWith(REDIS_COMMANDS_INTERFACE_NAME_SUFFIX) &&
                (interfaceClassName.startsWith(REDIS_COMMANDS_INTERFACE_NAME_PREFIX)
                        || interfaceClassName.startsWith(REACTIVE_COMMANDS_INTERFACE_NAME_PREFIX));
    }

    @Nonnull
    public static Object getRedisCommands(RedisConnection redisConnection, String interfaceName) {
        Object redisCommands = null;
        if (isNotBlank(interfaceName)) {
            switch (interfaceName) {
                case REDIS_KEY_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.keyCommands();
                case REDIS_STRING_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.stringCommands();
                case REDIS_LIST_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.listCommands();
                case REDIS_SET_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.setCommands();
                case REDIS_ZSET_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.zSetCommands();
                case REDIS_HASH_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.hashCommands();
                case REDIS_TX_COMMANDS_INTERFACE_NAME, REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME,
                     REDIS_CONNECTION_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.commands();
                case REDIS_SERVER_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.serverCommands();
                case REDIS_STREAM_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.streamCommands();
                case REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.scriptingCommands();
                case REDIS_GEO_COMMANDS_INTERFACE_NAME -> redisCommands = redisConnection.geoCommands();
                case REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME ->
                        redisCommands = redisConnection.hyperLogLogCommands();
                default -> redisCommands = redisConnection.commands();
            }
        }
        return redisCommands == null ? redisConnection.commands() : redisCommands;
    }

    public static String buildCommandMethodId(RedisCommandEvent event) {
        return buildMethodId(event.getMethod());
    }

    /**
     * @param method         the Redis command {@link Method}
     * @param args           the parameter values of the Redis command {@link Method}
     * @param consumer       The one {@link BiConsumer BiConsumer} of {@link Parameter} and its index
     * @param otherConsumers The others {@link BiConsumer BiConsumers} of {@link Parameter} and its index
     * @return if the parameters from the write method, return <code>true</code>, or <code>false</code>
     */
    public static boolean initializeParameters(Method method, Object[] args, BiConsumer<Parameter, Integer> consumer,
                                               BiConsumer<Parameter, Integer>... otherConsumers) {

        boolean sourceFromWriteMethod = true;

        List<ParameterMetadata> parameterMetadataList = null;

        try {
            // First, attempt to get the cached list of ParameterMetadata from the write method
            parameterMetadataList = getWriteParameterMetadataList(method);
            // If not found, try to build them
            if (parameterMetadataList == null) {
                if (method == REDIS_COMMANDS_EXECUTE_METHOD) {
                    String command = (String) args[0];
                }

                sourceFromWriteMethod = false;
                parameterMetadataList = buildParameterMetadataList(method);
            }

            int size = parameterMetadataList.size();
            int otherConsumerCount = otherConsumers.length;

            if (size > 0) {
                for (int i = 0; i < size; i++) {
                    Object parameterValue = args[i];
                    ParameterMetadata parameterMetadata = parameterMetadataList.get(i);
                    Parameter parameter = new Parameter(parameterValue, parameterMetadata);
                    // serialize parameter
                    serializeRawParameter(parameter);
                    // consumer one
                    consumer.accept(parameter, i);
                    // consumer others
                    for (int j = 0; j < otherConsumerCount; j++) {
                        BiConsumer<Parameter, Integer> parameterConsumer = otherConsumers[j];
                        parameterConsumer.accept(parameter, i);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Redis failed to initialize Redis command method parameter {}!", parameterMetadataList, e);
        }

        return sourceFromWriteMethod;
    }

    public static List<ParameterMetadata> buildParameterMetadataList(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return buildParameterMetadata(method, parameterTypes);
    }

    public static List<ParameterMetadata> buildParameterMetadata(Method method, Class<?>[] parameterTypes) {
        int parameterCount = parameterTypes.length;
        String[] parameterNames = parameterNameDiscoverer.getParameterNames(method);
        List<ParameterMetadata> parameterMetadataList = newArrayList(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i].getName();
            String parameterName = parameterNames[i];
            ParameterMetadata parameterMetadata = new ParameterMetadata(i, parameterType, parameterName);
            parameterMetadataList.add(parameterMetadata);
            // Preload the RedisSerializer implementation for the Method parameter type
            getSerializer(parameterType);
        }
        return unmodifiableList(parameterMetadataList);
    }

    public static Class<?>[] loadClasses(String... classNames) {
        int length = classNames.length;
        Class<?>[] classes = new Class[length];
        for (int i = 0; i < length; i++) {
            classes[i] = loadClass(classNames[i]);
        }
        return classes;
    }

    public static Class<?> loadClass(String className) {
        return loadClass(CLASS_LOADER, className);
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className) {
        return classesCache.computeIfAbsent(className, k -> {
            try {
                return forName(className, classLoader);
            } catch (ClassNotFoundException e) {
                logger.warn("The ClassLoader[{}] can't load the Class[name : '{}']", classLoader, className);
                return null;
            }
        });
    }

    private RedisCommandsUtils() {
    }
}
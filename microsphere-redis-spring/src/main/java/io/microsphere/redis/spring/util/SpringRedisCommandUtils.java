package io.microsphere.redis.spring.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.Parameter;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getWriteParameterMetadataList;
import static io.microsphere.redis.spring.serializer.Serializers.serializeRawParameter;
import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;
import static io.microsphere.redis.util.RedisCommandUtils.buildParameterMetadataList;
import static io.microsphere.redis.util.RedisCommandUtils.isRedisWriteCommand;
import static io.microsphere.redis.util.RedisUtils.CLASS_LOADER;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.util.StringUtils.INDEX_NOT_FOUND;
import static io.microsphere.util.StringUtils.isNotBlank;
import static org.springframework.util.ClassUtils.forName;

/**
 * Utility class for Spring Data Redis command interface resolution and parameter initialisation.
 * Provides helpers for:
 * <ul>
 *   <li>Resolving a Redis command sub-interface (e.g. {@link org.springframework.data.redis.connection.RedisStringCommands})
 *       from a {@link RedisConnection} by interface name</li>
 *   <li>Checking whether a class is a Redis command interface</li>
 *   <li>Abbreviating / expanding interface class names</li>
 *   <li>Initialising {@link io.microsphere.redis.metadata.Parameter} instances for a write command
 *       method call and serialising their values</li>
 *   <li>Loading classes by name with a local cache</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Check whether a class is a Redis command sub-interface
 *   boolean isCmd = SpringRedisCommandUtils.isRedisCommandsInterface(RedisStringCommands.class); // true
 *
 *   // Resolve the sub-command object from a RedisConnection by interface name
 *   Object stringCommands = SpringRedisCommandUtils.getRedisCommands(
 *           redisConnection, RedisConstants.REDIS_STRING_COMMANDS_INTERFACE_NAME);
 *
 *   // Abbreviate the full interface name for compact wire format
 *   String simple = SpringRedisCommandUtils.resolveSimpleInterfaceName(
 *           "org.springframework.data.redis.connection.RedisStringCommands"); // "RedisStringCommands"
 *
 *   // Expand an abbreviated name back to the full name
 *   String full = SpringRedisCommandUtils.resolveInterfaceName("RedisStringCommands");
 *   // "org.springframework.data.redis.connection.RedisStringCommands"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class SpringRedisCommandUtils {

    private static final Logger logger = getLogger(SpringRedisCommandUtils.class);

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
     *
     * @since Spring Data Redis 2.2
     */
    public static final String REDIS_STREAM_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStreamCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisScriptingCommands}
     */
    public static final String REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisScriptingCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisGeoCommands}
     *
     * @since Spring Data Redis 1.8
     */
    public static final String REDIS_GEO_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisGeoCommands";

    /**
     * The interface class name of {@link org.springframework.data.redis.connection.RedisHyperLogLogCommands}
     *
     * @since Spring Data Redis 1.5
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

    static final ConcurrentMap<String, Class<?>> classesCache = new ConcurrentHashMap<>(256);

    /**
     * Returns the simple (unqualified) form of a Redis command interface name by stripping
     * the {@code org.springframework.data.redis.connection.} package prefix, or the original
     * name if the prefix is not present.
     *
     * @param interfaceName the fully-qualified or simple interface name
     * @return the simple interface name, e.g. {@code "RedisStringCommands"}
     */
    public static String resolveSimpleInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf(REDIS_COMMANDS_PACKAGE_NAME);
        if (index == 0) {
            return interfaceName.substring(REDIS_COMMANDS_PACKAGE_NAME_LENGTH);
        } else {
            return interfaceName;
        }
    }

    /**
     * Returns the fully-qualified form of a Redis command interface name by prepending
     * the {@code org.springframework.data.redis.connection.} package prefix when the name
     * contains no dot, or the original name otherwise.
     *
     * @param interfaceName the simple or fully-qualified interface name
     * @return the fully-qualified interface name
     */
    public static String resolveInterfaceName(String interfaceName) {
        int index = interfaceName.indexOf(DOT_CHAR);
        if (index == INDEX_NOT_FOUND) {
            return REDIS_COMMANDS_PACKAGE_NAME + interfaceName;
        } else {
            return interfaceName;
        }
    }

    /**
     * Returns {@code true} if the given class is a Redis command sub-interface (i.e. it is
     * an interface that either extends {@link RedisCommands} or has a name matching the
     * Spring Data Redis command interface naming convention).
     *
     * @param interfaceClass the class to test
     * @return {@code true} if the class is a Redis command interface
     */
    public static boolean isRedisCommandsInterface(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            if (RedisCommands.class.isAssignableFrom(interfaceClass)) {
                return true;
            }
            return isRedisCommandsInterface(interfaceClass.getName());
        }
        return false;
    }

    /**
     * Returns {@code true} if the given class name follows the Spring Data Redis command interface
     * naming convention ({@code Redis*Commands} or {@code Reactive*Commands} in the
     * {@code org.springframework.data.redis.connection} package).
     *
     * @param interfaceClassName the fully-qualified class name to test
     * @return {@code true} if the name matches the Redis command interface convention
     */
    public static boolean isRedisCommandsInterface(String interfaceClassName) {
        return interfaceClassName.endsWith(REDIS_COMMANDS_INTERFACE_NAME_SUFFIX) &&
                (interfaceClassName.startsWith(REDIS_COMMANDS_INTERFACE_NAME_PREFIX)
                        || interfaceClassName.startsWith(REACTIVE_COMMANDS_INTERFACE_NAME_PREFIX));
    }

    /**
     * Retrieves the Redis command sub-object (e.g. the result of
     * {@code redisConnection.stringCommands()}) for the given interface name.  Falls back to
     * {@link RedisConnection#commands()} for unknown or null interface names.
     *
     * @param redisConnection the source {@link RedisConnection}
     * @param interfaceName   the fully-qualified Redis command interface name
     * @return the matching command sub-object; never {@code null}
     */
    @Nonnull
    public static Object getRedisCommands(RedisConnection redisConnection, String interfaceName) {
        Object redisCommands = null;
        if (isNotBlank(interfaceName)) {
            switch (interfaceName) {
                case REDIS_KEY_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.keyCommands();
                    break;
                case REDIS_STRING_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.stringCommands();
                    break;
                case REDIS_LIST_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.listCommands();
                    break;
                case REDIS_SET_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.setCommands();
                    break;
                case REDIS_ZSET_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.zSetCommands();
                    break;
                case REDIS_HASH_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.hashCommands();
                    break;
                case REDIS_TX_COMMANDS_INTERFACE_NAME:
                case REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME:
                case REDIS_CONNECTION_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection;
                    break;
                case REDIS_SERVER_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.serverCommands();
                    break;
                case REDIS_STREAM_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection;
                    break;
                case REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.scriptingCommands();
                    break;
                case REDIS_GEO_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.geoCommands();
                    break;
                case REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME:
                    redisCommands = redisConnection.hyperLogLogCommands();
                    break;
                default:
                    redisCommands = redisConnection;
            }
        }
        return redisCommands == null ? redisConnection : redisCommands;
    }

    /**
     * Builds the method id string for the Redis command method carried by the given event.
     *
     * @param event the {@link RedisCommandEvent} whose method should be used
     * @return non-null method id string
     */
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
                if (isRedisCommandsExecuteMethod(method)) {
                    String command = (String) args[0];
                    sourceFromWriteMethod = isRedisWriteCommand(command);
                } else {
                    sourceFromWriteMethod = false;
                }
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

    /**
     * Determine whether the method is the {@link RedisCommands#execute(String, byte[]...)} method
     *
     * @param method the method to test
     * @return <code>true</code> if the method is the {@link RedisCommands#execute(String, byte[]...)} method
     */
    public static boolean isRedisCommandsExecuteMethod(Method method) {
        return REDIS_COMMANDS_EXECUTE_METHOD.equals(method);
    }

    /**
     * Loads an array of {@link Class} objects by their fully-qualified names, using the shared
     * {@link io.microsphere.redis.util.RedisUtils#CLASS_LOADER} and a local cache.
     *
     * @param classNames the fully-qualified class names to load
     * @return array of loaded {@link Class} objects; elements may be {@code null} if the class
     *         could not be found
     */
    public static Class<?>[] loadClasses(String... classNames) {
        int length = classNames.length;
        Class<?>[] classes = new Class[length];
        for (int i = 0; i < length; i++) {
            classes[i] = loadClass(classNames[i]);
        }
        return classes;
    }

    /**
     * Loads a single {@link Class} by its fully-qualified name using the default
     * {@link io.microsphere.redis.util.RedisUtils#CLASS_LOADER}.
     *
     * @param className the fully-qualified class name
     * @return the loaded {@link Class}, or {@code null} if not found
     */
    public static Class<?> loadClass(String className) {
        return loadClass(CLASS_LOADER, className);
    }

    /**
     * Loads a single {@link Class} by name using the given {@link ClassLoader}, caching the
     * result to avoid repeated class-loading overhead.
     *
     * @param classLoader the class loader to use
     * @param className   the fully-qualified class name
     * @return the loaded {@link Class}, or {@code null} if not found
     */
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

    private SpringRedisCommandUtils() {
    }
}
package io.microsphere.redis.spring.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.redis.metadata.Parameter;
import io.microsphere.redis.metadata.ParameterMetadata;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.Method;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiConsumer;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.constants.SymbolConstants.LEFT_PARENTHESIS;
import static io.microsphere.constants.SymbolConstants.RIGHT_PARENTHESIS;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getWriteParameterMetadataList;
import static io.microsphere.redis.spring.serializer.Serializers.getSerializer;
import static io.microsphere.redis.spring.serializer.Serializers.serializeRawParameter;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.INDEX_NOT_FOUND;
import static io.microsphere.util.StringUtils.isNotBlank;
import static java.util.Collections.unmodifiableList;

/**
 * {@link RedisCommands Redis Command} Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class RedisCommandsUtils {

    private static final Logger logger = getLogger(RedisCommandsUtils.class);

    static final String REDIS_COMMANDS_PACKAGE_NAME = "org.springframework.data.redis.connection.";

    static final int REDIS_COMMANDS_PACKAGE_NAME_LENGTH = REDIS_COMMANDS_PACKAGE_NAME.length();

    private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public static final String REDIS_KEY_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisKeyCommands";

    public static final String REDIS_STRING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStringCommands";

    public static final String REDIS_LIST_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisListCommands";

    public static final String REDIS_SET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisSetCommands";

    public static final String REDIS_ZSET_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisZSetCommands";

    public static final String REDIS_HASH_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHashCommands";

    public static final String REDIS_TX_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisTxCommands";

    public static final String REDIS_PUB_SUB_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisPubSubCommands";

    public static final String REDIS_CONNECTION_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisConnectionCommands";

    public static final String REDIS_SERVER_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisServerCommands";

    public static final String REDIS_STREAM_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisStreamCommands";

    public static final String REDIS_SCRIPTING_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisScriptingCommands";

    public static final String REDIS_GEO_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisGeoCommands";

    public static final String REDIS_HYPER_LOG_LOG_COMMANDS_INTERFACE_NAME = "org.springframework.data.redis.connection.RedisHyperLogLogCommands";

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
        return buildCommandMethodId(event.getMethod());
    }

    public static String buildCommandMethodId(Method redisCommandMethod) {
        String interfaceName = redisCommandMethod.getDeclaringClass().getName();
        String methodName = redisCommandMethod.getName();
        Class<?>[] parameterTypes = redisCommandMethod.getParameterTypes();
        return buildCommandMethodId(interfaceName, methodName, parameterTypes);
    }

    public static String buildCommandMethodId(String interfaceName, String methodName, Class<?>... parameterTypes) {
        int length = parameterTypes.length;
        String[] parameterTypeNames = new String[length];
        for (int i = 0; i < length; i++) {
            parameterTypeNames[i] = parameterTypes[i].getName();
        }
        return buildCommandMethodId(interfaceName, methodName, parameterTypeNames);
    }

    public static String buildCommandMethodId(String interfaceName, String methodName, String... parameterTypes) {
        StringBuilder infoBuilder = new StringBuilder(interfaceName);
        infoBuilder.append(DOT_CHAR).append(methodName);
        StringJoiner paramTypesInfo = new StringJoiner(COMMA, LEFT_PARENTHESIS, RIGHT_PARENTHESIS);
        for (String parameterType : parameterTypes) {
            paramTypesInfo.add(parameterType);
        }
        infoBuilder.append(paramTypesInfo);
        return infoBuilder.toString();
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

    public static Class[] loadParameterClasses(String... parameterTypes) {
        int parameterCount = parameterTypes.length;
        Class[] parameterClasses = new Class[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            String parameterType = parameterTypes[i];
            Class parameterClass = resolveClass(parameterType);
            parameterClasses[i] = parameterClass;
        }
        return parameterClasses;
    }

    private RedisCommandsUtils() {
    }
}
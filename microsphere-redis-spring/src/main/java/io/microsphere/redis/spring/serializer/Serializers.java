package io.microsphere.redis.spring.serializer;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.metadata.Parameter;
import org.springframework.core.ResolvableType;
import org.springframework.data.domain.Range;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.serializer.BooleanSerializer.BOOLEAN_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ByteArraySerializer.BYTE_ARRAY_SERIALIZER;
import static io.microsphere.redis.spring.serializer.DoubleSerializer.DOUBLE_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ExpirationSerializer.EXPIRATION_SERIALIZER;
import static io.microsphere.redis.spring.serializer.GeoLocationSerializer.GEO_LOCATION_SERIALIZER;
import static io.microsphere.redis.spring.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;
import static io.microsphere.redis.spring.serializer.PointSerializer.POINT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.RangeSerializer.RANGE_SERIALIZER;
import static io.microsphere.redis.spring.serializer.RedisZSetCommandsRangeSerializer.REDIS_ZSET_COMMANDS_RANGE_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ShortSerializer.SHORT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.SortParametersSerializer.SORT_PARAMETERS_SERIALIZER;
import static io.microsphere.redis.spring.serializer.WeightsSerializer.WEIGHTS_SERIALIZER;
import static io.microsphere.util.ClassUtils.getType;
import static io.microsphere.util.ClassUtils.getTypeName;
import static io.microsphere.util.StringUtils.isBlank;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * {@link RedisSerializer} Utilities class, mainly used for Redis command method parameter type
 * serialization and deserialization
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class Serializers {

    private static final Logger logger = getLogger(Serializers.class);

    private static final ClassLoader classLoader = Serializers.class.getClassLoader();

    public static final JdkSerializationRedisSerializer DEFAULT_SERIALIZER = new JdkSerializationRedisSerializer();

    public static final StringRedisSerializer STRING_SERIALIZER = new StringRedisSerializer();

    /**
     * Generic parameterized {@link RedisSerializer}
     * Key is the full name of the type, and Value is implemented as {@link RedisSerializer}
     */
    static final Map<String, RedisSerializer<?>> typedSerializers = new HashMap<>(32);

    static {
        initializeBuiltinSerializers();
        initializeParameterizedSerializers();
    }

    @Nullable
    public static RedisSerializer<?> getSerializer(Object object) {
        return getSerializer(getType(object));
    }

    @Nullable
    public static <T> RedisSerializer<T> getSerializer(Class<?> type) {
        return type == null ? null : (RedisSerializer<T>) getSerializer(type.getName());
    }

    @Nullable
    public static RedisSerializer<?> getSerializer(String typeName) {
        if (isBlank(typeName)) {
            return null;
        }
        RedisSerializer<?> serializer = typedSerializers.get(typeName);

        if (serializer == null) {
            logger.debug("RedisSerializer implementation class of type {} not found, default RedisSerializer implementation class will be used: {}", typeName, DEFAULT_SERIALIZER.getClass().getName());
            serializer = DEFAULT_SERIALIZER;
            typedSerializers.put(typeName, serializer);
        } else {
            logger.trace("Find the RedisSerializer implementation class of type {} : {}", typeName, serializer.getClass().getName());
        }

        return serializer;
    }

    @Nullable
    public static byte[] serializeRawParameter(Parameter parameter) {
        if (parameter == null) {
            return null;
        }
        byte[] rawParameterValue = parameter.getRawValue();
        if (rawParameterValue == null) {
            Object parameterValue = parameter.getValue();
            RedisSerializer serializer = getSerializer(parameter.getParameterType());
            if (serializer != null) {
                rawParameterValue = serializer.serialize(parameterValue);
                parameter.setRawValue(rawParameterValue);
            }
        }
        return rawParameterValue;
    }

    @Nullable
    public static byte[] defaultSerialize(Object object) {
        return DEFAULT_SERIALIZER.serialize(object);
    }

    /**
     * Deserialize by {@link #DEFAULT_SERIALIZER}
     *
     * @param bytes bytes
     * @param <T>   the type of the deserialized object
     * @return
     * @throws ClassCastException if the object cannot be deserialized to the specified type
     */
    @Nullable
    public static <T> T defaultDeserialize(byte[] bytes) {
        return (T) DEFAULT_SERIALIZER.deserialize(bytes);
    }

    @Nullable
    public static byte[] serialize(Object object) {
        return object == null ? null : serialize(object, object.getClass());
    }

    @Nullable
    public static byte[] serialize(Object object, Class type) {
        if (object == null) {
            return null;
        }
        RedisSerializer redisSerializer = getSerializer(type);
        return redisSerializer == null ? null : redisSerializer.serialize(object);
    }

    @Nullable
    public static Object deserialize(byte[] bytes, String parameterType) {
        RedisSerializer<?> serializer = getSerializer(parameterType);
        if (serializer == null) {
            logger.error("Unable to deserialize the byte stream to an object of target type {}", parameterType);
            return null;
        }
        Object object = serializer.deserialize(bytes);
        return object;
    }

    @Nullable
    public static <T> T deserialize(byte[] bytes, Class<T> type) {
        RedisSerializer<?> serializer = getSerializer(type);
        if (serializer == null) {
            return null;
        }
        Object object = serializer.deserialize(bytes);
        if (type.isInstance(object)) {
            return type.cast(object);
        } else {
            logger.error("Unable to deserialize the byte stream to an object of target type : '{}'", type);
            return null;
        }
    }

    /**
     * Initializes built-in Serializers
     */
    private static void initializeBuiltinSerializers() {

        // Initializes the simple type Serializers
        initializeSimpleSerializers();

        // Initializes the array type Serializers
        initializeArrayTypeSerializers();

        // Initializes the collection type Serializers
        initializeCollectionTypeSerializers();

        // Initializes the enumeration type Serializers
        initializeEnumerationSerializers();

        // Initializes the Spring Data Redis type Serializers
        initializeSpringDataRedisSerializers();
    }

    /**
     * Initializes the simple type Serializers
     */
    private static void initializeSimpleSerializers() {

        // boolean or Boolean type 
        register(boolean.class, BOOLEAN_SERIALIZER);
        register(Boolean.class, BOOLEAN_SERIALIZER);

        // short or Short type
        register(short.class, SHORT_SERIALIZER);
        register(Short.class, SHORT_SERIALIZER);

        // int or Integer type 
        register(int.class, INTEGER_SERIALIZER);
        register(Integer.class, INTEGER_SERIALIZER);

        // long or Long type 
        register(long.class, LONG_SERIALIZER);
        register(Long.class, LONG_SERIALIZER);

        // double or Double type 
        register(double.class, DOUBLE_SERIALIZER);
        register(Double.class, DOUBLE_SERIALIZER);

        // String type 
        register(String.class, STRING_SERIALIZER);
    }

    /**
     * Initializes collection type Serializers
     */
    private static void initializeCollectionTypeSerializers() {

        // Iterable type 
        register(Iterable.class, DEFAULT_SERIALIZER);

        // Iterator type 
        register(Iterator.class, DEFAULT_SERIALIZER);

        // Collection type 
        register(Collection.class, DEFAULT_SERIALIZER);

        // List type 
        register(List.class, DEFAULT_SERIALIZER);

        // Set type 
        register(Set.class, DEFAULT_SERIALIZER);

        // Map type 
        register(Map.class, DEFAULT_SERIALIZER);

        // Queue type 
        register(Queue.class, DEFAULT_SERIALIZER);
    }

    /**
     * Initializes Array type Serializers
     */
    private static void initializeArrayTypeSerializers() {

        // byte[] type 
        register(byte[].class, BYTE_ARRAY_SERIALIZER);

        // int[] type 
        register(int[].class, DEFAULT_SERIALIZER);

        // byte[][] type 
        register(byte[][].class, DEFAULT_SERIALIZER);
    }

    /**
     * Initializes Enumeration type Serializers
     */
    private static void initializeEnumerationSerializers() {
        register(TimeUnit.class, new EnumSerializer(TimeUnit.class));
    }

    /**
     * Initializes Spring Data Redis type Serializers
     */
    private static void initializeSpringDataRedisSerializers() {

        // org.springframework.data.redis.core.types.Expiration type 
        register(Expiration.class, EXPIRATION_SERIALIZER);

        // org.springframework.data.redis.connection.SortParameters type 
        register(SortParameters.class, SORT_PARAMETERS_SERIALIZER);

        // org.springframework.data.redis.connection.RedisListCommands.Position type 
        register(Position.class, new EnumSerializer(Position.class));

        // org.springframework.data.redis.connection.RedisStringCommands.SetOption type 
        register(SetOption.class, new EnumSerializer(SetOption.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Range type 
        register(RedisZSetCommands.Range.class, REDIS_ZSET_COMMANDS_RANGE_SERIALIZER);

        // org.springframework.data.domain.Range
        register(Range.class, RANGE_SERIALIZER);

        // org.springframework.data.redis.connection.zset.Aggregate
        register(Aggregate.class, new EnumSerializer(Aggregate.class));

        // org.springframework.data.redis.connection.zset.Weights type 
        register(Weights.class, WEIGHTS_SERIALIZER);

        // org.springframework.data.redis.connection.ReturnType type 
        register(ReturnType.class, new EnumSerializer(ReturnType.class));

        // org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation type 
        register(GeoLocation.class, GEO_LOCATION_SERIALIZER);

        // org.springframework.data.geo.Point type 
        register(Point.class, POINT_SERIALIZER);
    }

    /**
     * Initializes Parameterized Serializers
     */
    private static void initializeParameterizedSerializers() {
        List<RedisSerializer> serializers = loadSerializers();
        initializeParameterizedSerializers(serializers);
    }

    private static void initializeParameterizedSerializers(List<RedisSerializer> serializers) {
        for (RedisSerializer serializer : serializers) {
            initializeParameterizedSerializer(serializer);
        }
    }

    static void initializeParameterizedSerializer(RedisSerializer serializer) {
        Class<?> parameterizedType = ResolvableType.forType(serializer.getClass()).as(RedisSerializer.class).getGeneric(0).resolve();

        if (parameterizedType != null) {
            register(parameterizedType, serializer);
        } else {
            logger.warn("RedisSerializer implementation class: {} could not find parameter type", serializer.getClass());
        }
    }

    private static List<RedisSerializer> loadSerializers() {
        return loadFactories(RedisSerializer.class, classLoader);
    }

    static void register(Class<?> type, RedisSerializer<?> serializer) {
        String typeName = type.getName();
        RedisSerializer oldSerializer = typedSerializers.put(typeName, serializer);
        logger.debug("The RedisSerializer[class : '{}' , target type : '{}'] for type['{}'] was registered", getTypeName(serializer), getTypeName(serializer.getTargetType()), getTypeName(type));
        if (oldSerializer != null && !Objects.equals(oldSerializer, serializer)) {
            logger.warn("The RedisSerializer for type['{}'] has been replaced old [class : '{}' , target type : '{}'] -> new [class : '{}' , target type : '{}']",
                    getTypeName(type), getTypeName(oldSerializer), getTypeName(oldSerializer.getTargetType()), getTypeName(serializer), getTypeName(serializer.getTargetType()));
        }
    }

    private Serializers() {
    }
}
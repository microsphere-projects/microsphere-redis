package io.microsphere.redis.spring.serializer;

import io.microsphere.redis.metadata.Parameter;
import io.microsphere.redis.metadata.ParameterMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static io.microsphere.redis.spring.serializer.BooleanSerializer.BOOLEAN_SERIALIZER;
import static io.microsphere.redis.spring.serializer.DoubleSerializer.DOUBLE_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ExpirationSerializer.EXPIRATION_SERIALIZER;
import static io.microsphere.redis.spring.serializer.GeoLocationSerializer.GEO_LOCATION_SERIALIZER;
import static io.microsphere.redis.spring.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;
import static io.microsphere.redis.spring.serializer.PointSerializer.POINT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.RedisZSetCommandsRangeSerializer.REDIS_ZSET_COMMANDS_RANGE_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.DEFAULT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.defaultDeserialize;
import static io.microsphere.redis.spring.serializer.Serializers.defaultSerialize;
import static io.microsphere.redis.spring.serializer.Serializers.deserialize;
import static io.microsphere.redis.spring.serializer.Serializers.getSerializer;
import static io.microsphere.redis.spring.serializer.Serializers.initializeParameterizedSerializer;
import static io.microsphere.redis.spring.serializer.Serializers.register;
import static io.microsphere.redis.spring.serializer.Serializers.serialize;
import static io.microsphere.redis.spring.serializer.Serializers.serializeRawParameter;
import static io.microsphere.redis.spring.serializer.ShortSerializer.SHORT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.SortParametersSerializer.SORT_PARAMETERS_SERIALIZER;
import static io.microsphere.redis.spring.serializer.WeightsSerializer.WEIGHTS_SERIALIZER;
import static io.microsphere.util.ArrayUtils.EMPTY_BYTE_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.data.redis.core.types.Expiration.seconds;

/**
 * {@link Serializers} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Serializers
 * @since 1.0.0
 */
class SerializersTest {

    @Test
    void testTypedSerializers() {
        assertFalse(Serializers.typedSerializers.isEmpty());
    }

    @Test
    void testGetSerializer() {

        RedisSerializer serializer = getSerializer(Expiration.class);
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer(seconds(1));
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer(Expiration.class.getName());
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer((Class) null);
        assertNull(serializer);

        serializer = getSerializer((String) null);
        assertNull(serializer);

        serializer = getSerializer((Object) null);
        assertNull(serializer);
    }

    @Test
    void testSerialize() {
        String object = "";
        assertArrayEquals(serialize(object), serialize(object, String.class));
    }

    @Test
    void testSerializeWithNull() {
        assertNull(serialize(null));
        assertNull(serialize(null, String.class));
        assertNull(serialize("", null));
    }

    @Test
    void testDeserialize() {
        String object = "hello";
        byte[] bytes = serialize(object);
        assertEquals(object, deserialize(bytes, String.class));
        assertEquals(object, deserialize(bytes, String.class.getName()));
    }

    @Test
    void testDeserializeWithNull() {
        assertNull(deserialize(EMPTY_BYTE_ARRAY, (String) null));
        assertNull(deserialize(EMPTY_BYTE_ARRAY, (Class) null));
    }

    @Test
    void testDeserializeOnMismatchType() {
        String object = "hello";
        byte[] bytes = serialize(object);
        assertNull(deserialize(bytes, Integer.class));
    }

    @Test
    void testDefaultSerialize() {
        String object = "hello";
        assertNotNull(defaultSerialize(object));
    }

    @Test
    void testDefaultDeserialize() {
        String object = "hello";
        byte[] bytes = defaultSerialize(object);
        assertEquals(object, defaultDeserialize(bytes));
    }

    @Test
    void testSerializeRawParameter() {
        String value = "hello";
        ParameterMetadata parameterMetadata = new ParameterMetadata(0, String.class.getName(), "name");
        Parameter parameter = new Parameter(value, parameterMetadata);
        assertArrayEquals(serialize(value), serializeRawParameter(parameter));
        assertArrayEquals(serialize(value), serializeRawParameter(parameter));
    }

    @Test
    void testSerializeRawParameterWithNull() {
        assertNull(serializeRawParameter(null));
    }

    @Test
    void testSerializeRawParameterOnRedisSerializerNotFound() {
        ParameterMetadata parameterMetadata = new ParameterMetadata(0, "", "name");
        Parameter parameter = new Parameter(new Integer(0), parameterMetadata);
        assertNull(serializeRawParameter(parameter));
    }

    @Test
    void testGetSimpleSerializers() {
        // boolean 或 Boolean type
        assertEquals(getSerializer(boolean.class), BOOLEAN_SERIALIZER);
        assertEquals(getSerializer(Boolean.class), BOOLEAN_SERIALIZER);

        // short 或 Short type
        assertEquals(getSerializer(short.class), SHORT_SERIALIZER);
        assertEquals(getSerializer(Short.class), SHORT_SERIALIZER);

        // int 或 Integer type
        assertEquals(getSerializer(int.class), INTEGER_SERIALIZER);
        assertEquals(getSerializer(Integer.class), INTEGER_SERIALIZER);

        // long 或 Long type
        assertEquals(getSerializer(long.class), LONG_SERIALIZER);
        assertEquals(getSerializer(Long.class), LONG_SERIALIZER);

        // double 或 Double type
        assertEquals(getSerializer(double.class), DOUBLE_SERIALIZER);
        assertEquals(getSerializer(Double.class), DOUBLE_SERIALIZER);

        // String type
        assertEquals(getSerializer(String.class), STRING_SERIALIZER);
    }

    @Test
    void testGetArrayTypeSerializers() {
        // byte[] type
        assertEquals(getSerializer(byte[][].class), DEFAULT_SERIALIZER);

        // int[] type
        assertEquals(getSerializer(int[].class), DEFAULT_SERIALIZER);

        // byte[][] type
        assertEquals(getSerializer(int[].class), DEFAULT_SERIALIZER);
    }

    @Test
    void testGetCollectionTypeSerializers() {
        // Iterable type
        assertEquals(getSerializer(Iterable.class), DEFAULT_SERIALIZER);

        // Iterator type
        assertEquals(getSerializer(Iterator.class), DEFAULT_SERIALIZER);

        // Collection type
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // List type
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Set type
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Map type
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Queue type
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);
    }

    @Test
    void testGetEnumerationSerializers() {
        assertEquals(getSerializer(TimeUnit.class), new EnumSerializer(TimeUnit.class));
    }

    @Test
    void testGetSpringDataRedisSerializers() {

        // org.springframework.data.redis.core.types.Expiration type
        assertEquals(getSerializer(Expiration.class), EXPIRATION_SERIALIZER);

        // org.springframework.data.redis.connection.SortParameters type
        assertEquals(getSerializer(SortParameters.class), SORT_PARAMETERS_SERIALIZER);

        // org.springframework.data.redis.connection.RedisListCommands.Position type
        assertEquals(getSerializer(RedisListCommands.Position.class), new EnumSerializer(RedisListCommands.Position.class));

        // org.springframework.data.redis.connection.RedisStringCommands.SetOption type
        assertEquals(getSerializer(RedisStringCommands.SetOption.class), new EnumSerializer(RedisStringCommands.SetOption.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Range type
        assertEquals(getSerializer(RedisZSetCommands.Range.class), REDIS_ZSET_COMMANDS_RANGE_SERIALIZER);

        // org.springframework.data.redis.connection.zset.Aggregate
        assertEquals(getSerializer(Aggregate.class), new EnumSerializer(Aggregate.class));

        // org.springframework.data.redis.connection.zset.Weights type
        assertEquals(getSerializer(Weights.class), WEIGHTS_SERIALIZER);

        // org.springframework.data.redis.connection.ReturnType type
        assertEquals(getSerializer(ReturnType.class), new EnumSerializer(ReturnType.class));

        // org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation type
        assertEquals(getSerializer(RedisGeoCommands.GeoLocation.class), GEO_LOCATION_SERIALIZER);

        // org.springframework.data.geo.Point type
        assertEquals(getSerializer(Point.class), POINT_SERIALIZER);
    }

    @Test
    void testInitializeParameterizedSerializerOnFailed() {
        initializeParameterizedSerializer(new RedisSerializer() {
            @Override
            public byte[] serialize(Object value) throws SerializationException {
                return new byte[0];
            }

            @Override
            public Object deserialize(byte[] bytes) throws SerializationException {
                return null;
            }
        });
    }

    @Test
    void testRegister() {
        register(String.class, DEFAULT_SERIALIZER);
        register(String.class, STRING_SERIALIZER);
        register(String.class, STRING_SERIALIZER);
    }
}

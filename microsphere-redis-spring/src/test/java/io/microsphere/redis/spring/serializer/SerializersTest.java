package io.microsphere.redis.spring.serializer;

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

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import static io.microsphere.redis.spring.serializer.Serializers.DEFAULT_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.getSerializer;
import static io.microsphere.redis.spring.serializer.Serializers.STRING_SERIALIZER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Serializers} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
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

        serializer = getSerializer("org.springframework.data.redis.core.types.Expiration");
        assertEquals(ExpirationSerializer.class, serializer.getClass());

        serializer = getSerializer((Class) null);
        assertNull(serializer);
    }

    @Test
    void testGetSimpleSerializers() {
        // boolean 或 Boolean 类型
        assertEquals(getSerializer(boolean.class), BooleanSerializer.BOOLEAN_SERIALIZER);
        assertEquals(getSerializer(Boolean.class), BooleanSerializer.BOOLEAN_SERIALIZER);

        // int 或 Integer 类型
        assertEquals(getSerializer(int.class), IntegerSerializer.INTEGER_SERIALIZER);
        assertEquals(getSerializer(Integer.class), IntegerSerializer.INTEGER_SERIALIZER);

        // long 或 Long 类型
        assertEquals(getSerializer(long.class), LongSerializer.LONG_SERIALIZER);
        assertEquals(getSerializer(Long.class), LongSerializer.LONG_SERIALIZER);

        // double 或 Double 类型
        assertEquals(getSerializer(double.class), DoubleSerializer.DOUBLE_SERIALIZER);
        assertEquals(getSerializer(Double.class), DoubleSerializer.DOUBLE_SERIALIZER);

        // String 类型
        assertEquals(getSerializer(String.class), STRING_SERIALIZER);
    }

    @Test
    void testGetArrayTypeSerializers() {
        // byte[] 类型
        assertEquals(getSerializer(byte[][].class), DEFAULT_SERIALIZER);

        // int[] 类型
        assertEquals(getSerializer(int[].class), DEFAULT_SERIALIZER);

        // byte[][] 类型
        assertEquals(getSerializer(int[].class), DEFAULT_SERIALIZER);
    }

    @Test
    void testGetCollectionTypeSerializers() {
        // Iterable 类型
        assertEquals(getSerializer(Iterable.class), DEFAULT_SERIALIZER);

        // Iterator 类型
        assertEquals(getSerializer(Iterator.class), DEFAULT_SERIALIZER);

        // Collection 类型
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // List 类型
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Set 类型
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Map 类型
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);

        // Queue 类型
        assertEquals(getSerializer(Collection.class), DEFAULT_SERIALIZER);
    }

    @Test
    void testGetEnumerationSerializers() {
        assertEquals(getSerializer(TimeUnit.class), new EnumSerializer(TimeUnit.class));
    }

    @Test
    void testGetSpringDataRedisSerializers() {

        // org.springframework.data.redis.core.types.Expiration 类型
        assertEquals(getSerializer(Expiration.class), ExpirationSerializer.EXPIRATION_SERIALIZER);

        // org.springframework.data.redis.connection.SortParameters 类型
        assertEquals(getSerializer(SortParameters.class), SortParametersSerializer.SORT_PARAMETERS_SERIALIZER);

        // org.springframework.data.redis.connection.RedisListCommands.Position 类型
        assertEquals(getSerializer(RedisListCommands.Position.class), new EnumSerializer(RedisListCommands.Position.class));

        // org.springframework.data.redis.connection.RedisStringCommands.SetOption 类型
        assertEquals(getSerializer(RedisStringCommands.SetOption.class), new EnumSerializer(RedisStringCommands.SetOption.class));

        // org.springframework.data.redis.connection.RedisZSetCommands.Range 类型
        assertEquals(getSerializer(RedisZSetCommands.Range.class), RangeSerializer.RANGE_SERIALIZER);

        // org.springframework.data.redis.connection.zset.Aggregate
        assertEquals(getSerializer(Aggregate.class), new EnumSerializer(Aggregate.class));

        // org.springframework.data.redis.connection.zset.Weights 类型
        assertEquals(getSerializer(Weights.class), WeightsSerializer.WEIGHTS_SERIALIZER);

        // org.springframework.data.redis.connection.ReturnType 类型
        assertEquals(getSerializer(ReturnType.class), new EnumSerializer(ReturnType.class));

        // org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation 类型
        assertEquals(getSerializer(RedisGeoCommands.GeoLocation.class), GeoLocationSerializer.GEO_LOCATION_SERIALIZER);

        // org.springframework.data.geo.Point 类型
        assertEquals(getSerializer(Point.class), PointSerializer.POINT_SERIALIZER);
    }
}

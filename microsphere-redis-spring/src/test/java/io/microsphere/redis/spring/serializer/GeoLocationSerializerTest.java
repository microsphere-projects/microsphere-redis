package io.microsphere.redis.spring.serializer;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * {@link GeoLocationSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class GeoLocationSerializerTest extends AbstractSerializerTest<RedisGeoCommands.GeoLocation> {

    @Override
    protected RedisSerializer<RedisGeoCommands.GeoLocation> getSerializer() {
        return GeoLocationSerializer.GEO_LOCATION_SERIALIZER;
    }

    @Override
    protected RedisGeoCommands.GeoLocation getValue() {
        return new RedisGeoCommands.GeoLocation("Hello,World", new Point(12.3, 45.6));
    }
}

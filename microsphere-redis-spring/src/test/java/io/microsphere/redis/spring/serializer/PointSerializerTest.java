package io.microsphere.redis.spring.serializer;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.PointSerializer.POINT_SERIALIZER;

/**
 * {@link PointSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class PointSerializerTest extends AbstractSerializerTest<Point> {

    @Override
    protected RedisSerializer<Point> getSerializer() {
        return POINT_SERIALIZER;
    }

    @Override
    protected Point getValue() {
        return new Point(12.3, 45.6);
    }
}

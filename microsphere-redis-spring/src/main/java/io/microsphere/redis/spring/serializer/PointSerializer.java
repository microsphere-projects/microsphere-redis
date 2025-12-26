package io.microsphere.redis.spring.serializer;

import org.springframework.data.geo.Point;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static io.microsphere.redis.spring.serializer.DoubleSerializer.DOUBLE_SERIALIZER;

/**
 * {@link Point} {@link RedisSerializer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PointSerializer extends AbstractSerializer<Point> {

    public static final PointSerializer POINT_SERIALIZER = new PointSerializer();

    @Override
    protected int calcBytesLength() {
        return DOUBLE_SERIALIZER.getBytesLength() * 2;
    }

    @Override
    protected byte[] doSerialize(Point point) throws SerializationException {
        double x = point.getX();
        double y = point.getY();
        byte[] xBytes = DOUBLE_SERIALIZER.serialize(x);
        byte[] yBytes = DOUBLE_SERIALIZER.serialize(y);
        int length = xBytes.length + yBytes.length;
        byte[] bytes = new byte[length];
        int index = 0;
        for (int i = 0; i < xBytes.length; i++) {
            bytes[index++] = xBytes[i];
        }
        for (int i = 0; i < yBytes.length; i++) {
            bytes[index++] = yBytes[i];
        }
        return bytes;
    }

    @Override
    protected Point doDeserialize(byte[] bytes) throws SerializationException {
        int length = bytes.length;
        int size = length / 2;
        byte[] xBytes = new byte[size];
        byte[] yBytes = new byte[size];
        int index = 0;
        for (int i = 0; i < size; i++) {
            xBytes[i] = bytes[index++];
        }
        for (int i = 0; i < size; i++) {
            yBytes[i] = bytes[index++];
        }
        double x = DOUBLE_SERIALIZER.deserialize(xBytes);
        double y = DOUBLE_SERIALIZER.deserialize(yBytes);
        return new Point(x, y);
    }
}
package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;

/**
 * Java {@code double} or {@link Double} type {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class DoubleSerializer extends AbstractSerializer<Double> {

    public static final DoubleSerializer DOUBLE_SERIALIZER = new DoubleSerializer();

    @Override
    protected int calcBytesLength() {
        return DOUBLE_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Double aDouble) throws SerializationException {
        double doubleValue = aDouble.doubleValue();
        long longValue = Double.doubleToLongBits(doubleValue);
        return LONG_SERIALIZER.serialize(longValue);
    }

    @Override
    protected Double doDeserialize(byte[] bytes) throws SerializationException {
        long longValue = LONG_SERIALIZER.deserialize(bytes);
        double doubleValue = Double.longBitsToDouble(longValue);
        return doubleValue;
    }
}
package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 * Java {@code boolean} or {@link Boolean} type {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class BooleanSerializer extends AbstractSerializer<Boolean> {

    public static final BooleanSerializer BOOLEAN_SERIALIZER = new BooleanSerializer();

    private static final byte NULL_VALUE = -1;

    private static final byte TRUE_VALUE = 1;

    private static final byte FALSE_VALUE = 0;

    @Override
    protected int calcBytesLength() {
        return BOOLEAN_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Boolean booleanValue) throws SerializationException {
        byte byteValue = booleanValue ? TRUE_VALUE : FALSE_VALUE;
        byte[] bytes = new byte[]{byteValue};
        return bytes;
    }

    @Override
    protected Boolean doDeserialize(byte[] bytes) throws SerializationException {
        byte byteValue = bytes[0];
        return byteValue == TRUE_VALUE ? true : false;
    }
}
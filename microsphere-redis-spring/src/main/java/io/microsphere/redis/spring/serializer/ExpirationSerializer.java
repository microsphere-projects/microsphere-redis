package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.concurrent.TimeUnit;

import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;
import static java.lang.System.arraycopy;

/**
 * {@link Expiration} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Expiration
 * @see RedisSerializer
 * @since 1.0.0
 */
public class ExpirationSerializer extends AbstractSerializer<Expiration> {

    private static final EnumSerializer<TimeUnit> timeUnitEnumSerializer = new EnumSerializer<>(TimeUnit.class);

    private static final int expirationTimeBytesLength = LONG_SERIALIZER.getBytesLength();

    private static final int timeUnitBytesLength = timeUnitEnumSerializer.getBytesLength();

    public static final ExpirationSerializer EXPIRATION_SERIALIZER = new ExpirationSerializer();

    @Override
    protected int calcBytesLength() {
        return expirationTimeBytesLength + timeUnitBytesLength;
    }

    @Override
    protected byte[] doSerialize(Expiration expiration) throws SerializationException {

        int bytesLength = getBytesLength();

        long expirationTime = expiration.getExpirationTime();

        byte[] expirationTimeBytes = LONG_SERIALIZER.serialize(expirationTime);

        byte[] bytes = new byte[bytesLength];

        arraycopy(expirationTimeBytes, 0, bytes, 0, expirationTimeBytesLength);

        TimeUnit timeUnit = expiration.getTimeUnit();
        byte ordinal = (byte) timeUnit.ordinal();

        for (int i = expirationTimeBytesLength; i < bytesLength; i++) {
            bytes[i] = ordinal;
        }

        return bytes;
    }

    @Override
    protected Expiration doDeserialize(byte[] bytes) throws SerializationException {
        int expirationTimeBytesLength = this.expirationTimeBytesLength;
        int timeUnitBytesLength = this.timeUnitBytesLength;
        int bytesLength = this.getBytesLength();

        // ExpirationTime array
        byte[] expirationTimeBytes = new byte[expirationTimeBytesLength];
        arraycopy(bytes, 0, expirationTimeBytes, 0, expirationTimeBytesLength);

        // TimeUnit array
        byte[] timeUnitBytes = new byte[timeUnitBytesLength];
        arraycopy(bytes, expirationTimeBytesLength, timeUnitBytes, 0, timeUnitBytesLength);

        long expirationTime = LONG_SERIALIZER.deserialize(expirationTimeBytes);

        TimeUnit timeUnit = timeUnitEnumSerializer.deserialize(timeUnitBytes);

        return Expiration.from(expirationTime, timeUnit);
    }
}
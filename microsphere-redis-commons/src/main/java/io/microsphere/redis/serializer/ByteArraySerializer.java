package io.microsphere.redis.serializer;

/**
 * byte[] {@link Serializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ByteArraySerializer implements Serializer<byte[]> {

    public static final ByteArraySerializer INSTANCE = new ByteArraySerializer();

    @Override
    public byte[] serialize(byte[] bytes) throws RuntimeException {
        return bytes;
    }

    @Override
    public byte[] deserialize(byte[] bytes) throws RuntimeException {
        return bytes;
    }
}

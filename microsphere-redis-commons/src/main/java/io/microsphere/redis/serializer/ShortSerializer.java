package io.microsphere.redis.serializer;

/**
 * Java {@code boolean} or {@link Boolean} type {@link Serializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ShortSerializer extends AbstractSerializer<Short> {

    public static final ShortSerializer INSTANCE = new ShortSerializer();

    @Override
    protected int calcBytesLength() {
        return SHORT_BYTES_LENGTH;
    }

    @Override
    protected byte[] doSerialize(Short aShort) throws RuntimeException {
        short shortValue = aShort.shortValue();
        byte[] bytes = new byte[]{
                (byte) (shortValue >>> 8),
                (byte) (shortValue & 0xFF)};
        return bytes;
    }

    @Override
    protected Short doDeserialize(byte[] bytes) throws RuntimeException {
        return (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
    }

}

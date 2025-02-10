package io.microsphere.redis.serializer;


import io.microsphere.reflect.JavaType;

import static io.microsphere.reflect.JavaType.from;

/**
 * Abstract {@link Serializer} Class
 *
 * @param <T> Serialized/Deserialized type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializer<T> implements Serializer<T> {

    public static final int UNBOUND_BYTES_LENGTH = -1;

    public static final int BOOLEAN_BYTES_LENGTH = 1;

    public static final int BYTE_BYTES_LENGTH = 1;

    public static final int SHORT_BYTES_LENGTH = 2;

    public static final int INTEGER_BYTES_LENGTH = 4;

    public static final int FLOAT_BYTES_LENGTH = 4;

    public static final int LONG_BYTES_LENGTH = 8;

    public static final int DOUBLE_BYTES_LENGTH = 8;

    private final Class<T> targetType;

    private final int bytesLength;

    public AbstractSerializer() {
        this.targetType = resolveTargetType();
        this.bytesLength = calcBytesLength();
    }

    protected Class<T> resolveTargetType() {
        return resolveTargetType(this.getClass());
    }

    @Override
    public final byte[] serialize(T t) throws RuntimeException {
        // null compatible case
        if (t == null) {
            return null;
        }

        return doSerialize(t);
    }

    @Override
    public final T deserialize(byte[] bytes) throws RuntimeException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        // Compatible byte array fixed case
        if (bytesLength != UNBOUND_BYTES_LENGTH && bytes.length != getBytesLength()) {
            return null;
        }

        return doDeserialize(bytes);
    }

    @Override
    public final Class<T> getTargetType() {
        return targetType;
    }

    public Class<T> getParameterizedClass() {
        return getTargetType();
    }

    public int getBytesLength() {
        return bytesLength;
    }

    protected int calcBytesLength() {
        return UNBOUND_BYTES_LENGTH;
    }

    protected abstract byte[] doSerialize(T t) throws RuntimeException;

    protected abstract T doDeserialize(byte[] bytes) throws RuntimeException;

    public static <T> Class<T> resolveTargetType(Class<?> type) {
        JavaType javaType = from(type)
                .as(AbstractSerializer.class)
                .getGenericType(0);
        return javaType.toClass();
    }
}

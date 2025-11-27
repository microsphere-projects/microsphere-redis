package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Objects;

import static io.microsphere.redis.spring.serializer.IntegerSerializer.INTEGER_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ShortSerializer.SHORT_SERIALIZER;

/**
 * {@link Enum} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class EnumSerializer<E extends Enum> implements RedisSerializer<E> {

    private static final String VALUES_METHOD_NAME = "values";

    private static final int BYTE_BYTES_LENGTH = 1;

    private static final int SHORT_BYTES_LENGTH = 2;

    private static final int INTEGER_BYTES_LENGTH = 4;

    private final Class<E> enumType;

    private final E[] enums;

    private final int bytesLength;

    public EnumSerializer(Class<E> enumType) {
        this.enumType = enumType;
        this.enums = invokeValues(getValuesMethod(enumType));
        this.bytesLength = calcBytesLength(enums);
    }

    private Method getValuesMethod(Class<E> enumType) {
        Method valuesMethod = ReflectionUtils.findMethod(enumType, VALUES_METHOD_NAME);
        ReflectionUtils.makeAccessible(valuesMethod);
        return valuesMethod;
    }

    private int calcBytesLength(E[] enums) {
        int enumsLength = enums.length;
        if (enumsLength < Byte.MAX_VALUE) {
            return BYTE_BYTES_LENGTH;   // 1 byte
        } else if (enumsLength < Short.MAX_VALUE) {
            return SHORT_BYTES_LENGTH;  // 2 bytes
        } else {
            return INTEGER_BYTES_LENGTH; // 4 bytes -> int
        }
    }

    private E[] invokeValues(Method valuesMethod) {
        return (E[]) ReflectionUtils.invokeMethod(valuesMethod, null);
    }

    @Override
    public byte[] serialize(Enum e) throws SerializationException {
        // null compatible case
        if (e == null) {
            return null;
        }

        // RedisSerializer<String> delegate = Serializers.stringSerializer;
        // String name = e.name();
        // return delegate.serialize(name);

        int ordinal = e.ordinal();
        byte[] bytes = new byte[bytesLength];

        switch (bytesLength) {
            case BYTE_BYTES_LENGTH: // Most scenarios match
                bytes[0] = (byte) ordinal;
                break;
            case SHORT_BYTES_LENGTH:
                bytes = SHORT_SERIALIZER.serialize((short) ordinal);
                break;
            case INTEGER_BYTES_LENGTH:
                bytes = INTEGER_SERIALIZER.serialize(ordinal);
        }

        return bytes;
    }

    @Override
    public E deserialize(byte[] bytes) throws SerializationException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        int ordinal = 0;

        ordinal = switch (bytesLength) {
            case BYTE_BYTES_LENGTH -> bytes[0];
            case SHORT_BYTES_LENGTH -> SHORT_SERIALIZER.deserialize(bytes);
            case INTEGER_BYTES_LENGTH -> INTEGER_SERIALIZER.deserialize(bytes);
            default -> throw new IllegalArgumentException("Unsupported bytes length: " + bytesLength);
        };

        return enums[ordinal];
    }

    public Class<E> getEnumType() {
        return enumType;
    }

    public int getBytesLength() {
        return bytesLength;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EnumSerializer)) return false;
        EnumSerializer that = (EnumSerializer) o;
        return enumType.equals(that.enumType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enumType);
    }

    @Override
    public Class<?> getTargetType() {
        return enumType;
    }
}

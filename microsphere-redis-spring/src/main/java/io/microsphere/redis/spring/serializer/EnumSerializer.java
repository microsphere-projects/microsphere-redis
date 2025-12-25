package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.lang.reflect.Method;
import java.util.Objects;

import static io.microsphere.redis.spring.serializer.ShortSerializer.SHORT_SERIALIZER;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static java.lang.Byte.MAX_VALUE;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

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

    private final Class<E> enumType;

    private final E[] enums;

    private final int bytesLength;

    public EnumSerializer(Class<E> enumType) {
        this.enumType = enumType;
        this.enums = invokeValues(getValuesMethod(enumType));
        this.bytesLength = calcBytesLength(enums);
    }

    private Method getValuesMethod(Class<E> enumType) {
        Method valuesMethod = findMethod(enumType, VALUES_METHOD_NAME);
        trySetAccessible(valuesMethod);
        return valuesMethod;
    }

    static <E extends Enum<E>> int calcBytesLength(E[] enums) {
        int enumsLength = enums.length;
        return enumsLength < MAX_VALUE ? BYTE_BYTES_LENGTH : SHORT_BYTES_LENGTH;
    }

    private E[] invokeValues(Method valuesMethod) {
        return (E[]) invokeMethod(valuesMethod, null);
    }

    @Override
    public byte[] serialize(Enum e) throws SerializationException {
        // null compatible case
        if (e == null) {
            return null;
        }

        int ordinal = e.ordinal();
        final byte[] bytes;

        if (bytesLength == BYTE_BYTES_LENGTH) { // Most scenarios match
            bytes = new byte[1];
            bytes[0] = (byte) ordinal;
        } else {
            bytes = SHORT_SERIALIZER.serialize((short) ordinal);
        }

        return bytes;
    }

    @Override
    public E deserialize(byte[] bytes) throws SerializationException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        int ordinal = bytesLength == BYTE_BYTES_LENGTH ? bytes[0] : SHORT_SERIALIZER.deserialize(bytes);
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

    public Class<?> getTargetType() {
        return enumType;
    }
}
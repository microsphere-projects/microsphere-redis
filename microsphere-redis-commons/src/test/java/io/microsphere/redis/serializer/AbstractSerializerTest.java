package io.microsphere.redis.serializer;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static io.microsphere.redis.serializer.AbstractSerializer.resolveTargetType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link Serializer} Test
 *
 * @param <T> Serialization type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializerTest<T> {

    @Test
    public void test() {
        test(this::getValue);
    }

    @Test
    public void testNull() {
        test(this::getNullValue);
    }

    public void test(Supplier<T> valueSupplier) {
        T value = valueSupplier.get();
        Serializer<T> serializer = getSerializer();
        byte[] bytes = serializer.serialize(value);
        T deserialized = serializer.deserialize(bytes);
        if (value != null && deserialized != null) {
            assertEquals(getTestData(value), getTestData(deserialized));
        } else {
            assertEquals(value, deserialized);
        }

        Class<?> targetType = serializer.getTargetType();
        Class<?> parameterType = resolveTargetType(serializer.getClass());
        assertSame(targetType, parameterType);
        assertTrue(serializer.canSerialize(parameterType));

        if (serializer instanceof AbstractSerializer) {
            AbstractSerializer abstractSerializer = (AbstractSerializer) serializer;
            assertSame(targetType, abstractSerializer.getParameterizedClass());
        }
    }

    protected Object getTestData(T value) {
        return value;
    }

    protected abstract Serializer<T> getSerializer();

    protected abstract T getValue();

    protected T getNullValue() {
        return null;
    }
}

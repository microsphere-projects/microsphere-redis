package io.microsphere.redis.spring.serializer;

import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.function.Supplier;

import static io.microsphere.redis.spring.serializer.Serializers.canSerialize;
import static io.microsphere.redis.spring.serializer.Serializers.getTargetType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.core.ResolvableType.forType;

/**
 * Abstract {@link RedisSerializer} Test
 *
 * @param <T> Serialization type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AbstractSerializer
 * @since 1.0.0
 */
public abstract class AbstractSerializerTest<T> {

    @Test
    void test() {
        test(this::getValue);
    }

    @Test
    void testNull() {
        test(this::getNullValue);
    }

    void test(Supplier<T> valueSupplier) {
        T value = valueSupplier.get();
        RedisSerializer<T> serializer = getSerializer();
        byte[] bytes = serializer.serialize(value);
        T deserialized = serializer.deserialize(bytes);
        if (value != null && deserialized != null) {
            assertEquals(getTestData(value), getTestData(deserialized));
        } else {
            assertEquals(value, deserialized);
        }

        Class<?> targetType = getTargetType(serializer);
        ResolvableType resolvableType = forType(getClass()).getSuperType().getGeneric(0);
        Class<?> parameterType = resolvableType.resolve();

        assertSame(targetType, parameterType);
        assertTrue(canSerialize(serializer, parameterType));

        if (serializer instanceof AbstractSerializer) {
            AbstractSerializer abstractSerializer = (AbstractSerializer) serializer;
            ResolvableType parameterizedType = abstractSerializer.getParameterizedType();
            assertSame(parameterType, parameterizedType.resolve());
            assertSame(targetType, abstractSerializer.getParameterizedClass());
        }
    }

    protected Object getTestData(T value) {
        return value;
    }

    protected abstract RedisSerializer<T> getSerializer();

    protected abstract T getValue();

    protected T getNullValue() {
        return null;
    }
}

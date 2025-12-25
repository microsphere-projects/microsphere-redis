package io.microsphere.redis.spring.serializer;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.BooleanSerializer.BOOLEAN_SERIALIZER;

/**
 * {@link BooleanSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class BooleanSerializerTest extends AbstractSerializerTest<Boolean> {

    @Override
    protected RedisSerializer<Boolean> getSerializer() {
        return BOOLEAN_SERIALIZER;
    }

    @Override
    protected Boolean getValue() {
        return true;
    }

    @Test
    void testFalse() {
        test(this::falseValue);
    }

    private Boolean falseValue() {
        return Boolean.FALSE;
    }
}

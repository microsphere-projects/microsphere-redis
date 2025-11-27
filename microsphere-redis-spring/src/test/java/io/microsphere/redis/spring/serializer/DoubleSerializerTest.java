package io.microsphere.redis.spring.serializer;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.DoubleSerializer.DOUBLE_SERIALIZER;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link DoubleSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class DoubleSerializerTest extends AbstractSerializerTest<Double> {

    @Override
    protected RedisSerializer<Double> getSerializer() {
        return DOUBLE_SERIALIZER;
    }

    @Override
    protected Double getValue() {
        return 123456.789;
    }

    @Test
    void test() {
        super.test();

        assertNull(DOUBLE_SERIALIZER.deserialize(new byte[0]));
    }
}

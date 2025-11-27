package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.DoubleSerializer.DOUBLE_SERIALIZER;

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
}

package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;

/**
 * {@link LongSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class LongSerializerTest extends AbstractSerializerTest<Long> {

    @Override
    protected RedisSerializer<Long> getSerializer() {
        return LONG_SERIALIZER;
    }

    @Override
    protected Long getValue() {
        return 1234567890L;
    }
}

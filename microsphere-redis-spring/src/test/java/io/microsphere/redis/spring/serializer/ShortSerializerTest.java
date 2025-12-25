package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.ShortSerializer.SHORT_SERIALIZER;

/**
 * {@link ShortSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class ShortSerializerTest extends AbstractSerializerTest<Short> {

    @Override
    protected RedisSerializer<Short> getSerializer() {
        return SHORT_SERIALIZER;
    }

    @Override
    protected Short getValue() {
        return 128;
    }
}

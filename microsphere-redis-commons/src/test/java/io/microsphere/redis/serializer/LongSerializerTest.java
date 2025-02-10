package io.microsphere.redis.serializer;

/**
 * {@link LongSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class LongSerializerTest extends AbstractSerializerTest<Long> {

    @Override
    protected Serializer<Long> getSerializer() {
        return LongSerializer.INSTANCE;
    }

    @Override
    protected Long getValue() {
        return 1234567890L;
    }
}

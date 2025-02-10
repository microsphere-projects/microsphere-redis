package io.microsphere.redis.serializer;

/**
 * {@link IntegerSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class IntegerSerializerTest extends AbstractSerializerTest<Integer> {

    @Override
    protected Serializer<Integer> getSerializer() {
        return IntegerSerializer.INSTANCE;
    }

    @Override
    protected Integer getValue() {
        return 123456789;
    }
}

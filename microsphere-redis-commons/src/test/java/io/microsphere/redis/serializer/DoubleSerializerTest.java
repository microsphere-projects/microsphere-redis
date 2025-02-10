package io.microsphere.redis.serializer;

/**
 * {@link DoubleSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DoubleSerializerTest extends AbstractSerializerTest<Double> {

    @Override
    protected Serializer<Double> getSerializer() {
        return DoubleSerializer.INSTANCE;
    }

    @Override
    protected Double getValue() {
        return 123456.789;
    }
}

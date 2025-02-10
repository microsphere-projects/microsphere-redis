package io.microsphere.redis.serializer;


import org.junit.jupiter.api.Test;

/**
 * {@link BooleanSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class BooleanSerializerTest extends AbstractSerializerTest<Boolean> {

    @Override
    protected Serializer<Boolean> getSerializer() {
        return BooleanSerializer.INSTANCE;
    }

    @Override
    protected Boolean getValue() {
        return true;
    }

    @Test
    public void testFalse() {
        test(this::falseValue);
    }

    private Boolean falseValue() {
        return Boolean.FALSE;
    }
}

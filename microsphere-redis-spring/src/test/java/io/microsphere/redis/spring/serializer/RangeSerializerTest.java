package io.microsphere.redis.spring.serializer;


import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.serializer.RedisSerializer;

import static org.springframework.data.redis.connection.RedisZSetCommands.Range.range;
import static org.springframework.data.redis.connection.RedisZSetCommands.Range.unbounded;

/**
 * {@link RangeSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class RangeSerializerTest extends AbstractSerializerTest<RedisZSetCommands.Range> {

    @Override
    protected RedisSerializer<RedisZSetCommands.Range> getSerializer() {
        return RangeSerializer.RANGE_SERIALIZER;
    }

    @Override
    protected RedisZSetCommands.Range getValue() {
        return range();
    }

    @Override
    protected Object getTestData(RedisZSetCommands.Range value) {
        StringBuilder testData = new StringBuilder();
        RedisZSetCommands.Range.Boundary max = value.getMax();
        RedisZSetCommands.Range.Boundary min = value.getMin();
        buildTestData(testData, max);
        buildTestData(testData, min);
        return testData.toString();
    }

    @Test
    void testGte() {
        test(() -> range().gte(1));
        test(() -> range().gte(1).lt(2));
        test(() -> range().gte(1).lte(2));
    }

    @Test
    void testGt() {
        test(() -> range().gt(1));
        test(() -> range().gt(1).lt(2));
        test(() -> range().gt(1).lte(2));
    }


    @Test
    void testLt() {
        test(() -> range().lt(2));
        test(() -> range().lt(2).gt(1));
        test(() -> range().lt(2).gte(1));
    }

    @Test
    void testLte() {
        test(() -> range().lte(2));
        test(() -> range().lte(2).gt(1));
        test(() -> range().lte(1).gt(1));
    }

    @Test
    void testUnbounded() {
        test(() -> unbounded());
    }

    private void buildTestData(StringBuilder testData, RedisZSetCommands.Range.Boundary boundary) {
        if (boundary != null) {
            testData.append(boundary.getValue()).append(boundary.isIncluding());
        }
    }
}

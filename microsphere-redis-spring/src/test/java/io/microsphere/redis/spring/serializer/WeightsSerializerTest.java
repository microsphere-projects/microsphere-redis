package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.serializer.RedisSerializer;

import static io.microsphere.redis.spring.serializer.WeightsSerializer.INSTANCE;

/**
 * {@link WeightsSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class WeightsSerializerTest extends AbstractSerializerTest<Weights> {

    @Override
    protected RedisSerializer<Weights> getSerializer() {
        return INSTANCE;
    }

    @Override
    protected Weights getValue() {
        return Weights.of(1.0, 2.0, 3.0);
    }

    @Override
    protected Object getTestData(Weights value) {
        return value.toList();
    }
}

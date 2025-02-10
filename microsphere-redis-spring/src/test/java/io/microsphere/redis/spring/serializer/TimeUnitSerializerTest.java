package io.microsphere.redis.spring.serializer;

import io.microsphere.redis.serializer.EnumSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.concurrent.TimeUnit;

/**
 * {@link TimeUnit} {@link EnumSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TimeUnitSerializerTest extends AbstractSerializerTest<TimeUnit> {

    @Override
    protected RedisSerializer<TimeUnit> getSerializer() {
        return new EnumSerializer(TimeUnit.class);
    }

    @Override
    protected TimeUnit getValue() {
        return TimeUnit.SECONDS;
    }
}

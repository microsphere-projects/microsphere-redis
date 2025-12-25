package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Random;

import static org.springframework.data.redis.connection.RedisStringCommands.SetOption.values;

/**
 * {@link SetOption} {@link EnumSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class SetOptionSerializerTest extends AbstractSerializerTest<SetOption> {

    @Override
    protected RedisSerializer<SetOption> getSerializer() {
        return new EnumSerializer(SetOption.class);
    }

    @Override
    protected SetOption getValue() {
        Random random = new Random();
        SetOption[] values = values();
        int index = random.nextInt(values.length);
        return values[index];
    }
}
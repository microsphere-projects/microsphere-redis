package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.RedisListCommands.Position;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Random;

import static org.springframework.data.redis.connection.RedisListCommands.Position.AFTER;
import static org.springframework.data.redis.connection.RedisListCommands.Position.BEFORE;

/**
 * {@link Position} {@link EnumSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class PositionSerializerTest extends AbstractSerializerTest<Position> {

    @Override
    protected RedisSerializer<Position> getSerializer() {
        return new EnumSerializer(Position.class);
    }

    @Override
    protected Position getValue() {
        Random random = new Random();
        return random.nextBoolean() ? BEFORE : AFTER;
    }
}

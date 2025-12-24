package io.microsphere.redis.spring.serializer;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.RedisZSetCommands.Range.Boundary;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static io.microsphere.redis.spring.serializer.RangeModel.from;
import static io.microsphere.redis.spring.serializer.RangeSerializer.RANGE_SERIALIZER;
import static org.springframework.data.redis.connection.RedisZSetCommands.Range.range;
import static org.springframework.data.redis.connection.RedisZSetCommands.Range.unbounded;


/**
 * {@link RedisZSetCommands.Range} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see Range
 * @see Boundary
 * @see RangeSerializer
 * @since 1.0.0
 */
public class RedisZSetCommandsRangeSerializer extends AbstractSerializer<RedisZSetCommands.Range> {

    public static final RedisZSetCommandsRangeSerializer REDIS_ZSET_COMMANDS_RANGE_SERIALIZER = new RedisZSetCommandsRangeSerializer();

    @Override
    protected byte[] doSerialize(RedisZSetCommands.Range range) throws SerializationException {
        return RANGE_SERIALIZER.serialize(from(range));
    }

    @Override
    protected RedisZSetCommands.Range doDeserialize(byte[] bytes) throws SerializationException {
        Range rangeDomain = RANGE_SERIALIZER.deserialize(bytes);
        return toRange(rangeDomain);
    }

    static RedisZSetCommands.Range toRange(Range rangeDomain) {
        Range.Bound lowerBound = rangeDomain.getLowerBound();
        Range.Bound upperBound = rangeDomain.getUpperBound();
        Object lowerBoundValue = lowerBound.getValue().orElse(null);
        Object upperBoundValue = upperBound.getValue().orElse(null);
        boolean isLowerInclusive = lowerBound.isInclusive();
        boolean isUpperInclusive = upperBound.isInclusive();

        return toRange(lowerBoundValue, upperBoundValue, isLowerInclusive, isUpperInclusive);
    }

    static RedisZSetCommands.Range toRange(Object lowerBoundValue, Object upperBoundValue, boolean isLowerInclusive, boolean isUpperInclusive) {
        RedisZSetCommands.Range range = range();
        if (lowerBoundValue != null) {
            range = isLowerInclusive ? range.gte(lowerBoundValue) : range.gt(lowerBoundValue);
        }
        if (upperBoundValue != null) {
            range = isUpperInclusive ? range.lte(upperBoundValue) : range.lt(upperBoundValue);
        }
        if (lowerBoundValue == null && upperBoundValue == null && isLowerInclusive && isUpperInclusive) {
            range = unbounded();
        }
        return range;
    }
}
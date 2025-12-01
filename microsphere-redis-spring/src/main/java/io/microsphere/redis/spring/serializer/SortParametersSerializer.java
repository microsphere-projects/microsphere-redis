package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.connection.SortParameters.Order;
import org.springframework.data.redis.connection.SortParameters.Range;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.redis.spring.serializer.BooleanSerializer.BOOLEAN_SERIALIZER;
import static io.microsphere.redis.spring.serializer.ByteArraySerializer.BYTE_ARRAY_SERIALIZER;
import static io.microsphere.redis.spring.serializer.LongSerializer.LONG_SERIALIZER;
import static io.microsphere.redis.spring.serializer.Serializers.defaultSerialize;
import static io.microsphere.redis.spring.serializer.SortParametersSerializer.RangeSerializer.RANGE_SERIALIZER;
import static io.microsphere.util.ArrayUtils.isNotEmpty;

/**
 * {@link SortParameters} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultSortParameters
 * @since 1.0.0
 */
public class SortParametersSerializer extends AbstractSerializer<SortParameters> {

    private static final String ORDER_KEY = "o";

    private static final String ALPHABETIC_KEY = "a";

    private static final String BY_PATTERN_KEY = "b";

    private static final String GET_PATTERN_KEY = "g";

    private static final String LIMIT_KEY = "l";

    private static final EnumSerializer orderEnumSerializer = new EnumSerializer(Order.class);

    public static final SortParametersSerializer SORT_PARAMETERS_SERIALIZER = new SortParametersSerializer();

    @Override
    protected byte[] doSerialize(SortParameters sortParameters) throws SerializationException {
        Map<String, Object> data = new HashMap<>(5);

        Order order = sortParameters.getOrder();
        if (order != null) {
            data.put(ORDER_KEY, orderEnumSerializer.serialize(order));
        }

        Boolean alphabetic = sortParameters.isAlphabetic();
        if (alphabetic != null) {
            data.put(ALPHABETIC_KEY, BOOLEAN_SERIALIZER.serialize(alphabetic));
        }

        byte[] byPattern = sortParameters.getByPattern();
        if (byPattern != null) {
            data.put(BY_PATTERN_KEY, BYTE_ARRAY_SERIALIZER.serialize(byPattern));
        }

        byte[][] getPattern = sortParameters.getGetPattern();
        if (isNotEmpty(getPattern)) {
            data.put(GET_PATTERN_KEY, defaultSerialize(getPattern));
        }

        Range limit = sortParameters.getLimit();
        if (limit != null) {
            data.put(LIMIT_KEY, RANGE_SERIALIZER.serialize(limit));
        }

        return defaultSerialize(data);
    }

    @Override
    protected SortParameters doDeserialize(byte[] bytes) throws SerializationException {
        DefaultSortParameters sortParameters = new DefaultSortParameters();

        Map<String, Object> data = Serializers.deserialize(bytes, Map.class);

        byte[] orderBytes = (byte[]) data.get(ORDER_KEY);
        sortParameters.setOrder((Order) orderEnumSerializer.deserialize(orderBytes));

        byte[] alphabeticBytes = (byte[]) data.get(ALPHABETIC_KEY);
        sortParameters.setAlphabetic(BOOLEAN_SERIALIZER.deserialize(alphabeticBytes));

        byte[] byPatternBytes = (byte[]) data.get(BY_PATTERN_KEY);
        sortParameters.setByPattern(BYTE_ARRAY_SERIALIZER.deserialize(byPatternBytes));

        byte[] getPatternBytes = (byte[]) data.get(GET_PATTERN_KEY);
        sortParameters.setGetPattern(Serializers.deserialize(getPatternBytes, byte[][].class));

        byte[] limitBytes = (byte[]) data.get(LIMIT_KEY);
        sortParameters.setLimit(RANGE_SERIALIZER.deserialize(limitBytes));

        return sortParameters;
    }

    public static class RangeSerializer extends AbstractSerializer<Range> {

        public static final RangeSerializer RANGE_SERIALIZER = new RangeSerializer();

        @Override
        protected int calcBytesLength() {
            return LONG_SERIALIZER.getBytesLength() * 2;
        }

        @Override
        protected byte[] doSerialize(Range range) throws SerializationException {
            long start = range.getStart();
            long count = range.getCount();

            byte[] startBytes = LONG_SERIALIZER.serialize(start);
            byte[] countBytes = LONG_SERIALIZER.serialize(count);
            int length = startBytes.length + countBytes.length;
            byte[] bytes = new byte[length];
            int index = 0;
            for (int i = 0; i < startBytes.length; i++) {
                bytes[index++] = startBytes[i];
            }
            for (int i = 0; i < countBytes.length; i++) {
                bytes[index++] = countBytes[i];
            }

            return bytes;
        }

        @Override
        protected Range doDeserialize(byte[] bytes) throws SerializationException {
            int length = bytes.length;
            int size = length / 2;
            byte[] startBytes = new byte[size];
            byte[] countBytes = new byte[size];
            int index = 0;
            for (int i = 0; i < size; i++) {
                startBytes[i] = bytes[index++];
            }
            for (int i = 0; i < size; i++) {
                countBytes[i] = bytes[index++];
            }
            long start = LONG_SERIALIZER.deserialize(startBytes);
            long count = LONG_SERIALIZER.deserialize(countBytes);

            return new Range(start, count);
        }
    }
}
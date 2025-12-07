package io.microsphere.redis.spring.serializer;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.DefaultSortParameters;
import org.springframework.data.redis.connection.SortParameters;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Arrays;
import java.util.StringJoiner;

import static io.microsphere.redis.spring.serializer.SortParametersSerializer.SORT_PARAMETERS_SERIALIZER;
import static java.lang.String.valueOf;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.deepToString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.data.redis.connection.SortParameters.Order.ASC;

/**
 * {@link SortParametersSerializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
class SortParametersSerializerTest extends AbstractSerializerTest<SortParameters> {

    @Override
    protected RedisSerializer<SortParameters> getSerializer() {
        return SORT_PARAMETERS_SERIALIZER;
    }

    @Test
    void test() {
        test(this::getValue);
        test(this::getSortParameters);
    }

    @Override
    protected SortParameters getValue() {
        return new DefaultSortParameters("a".getBytes(UTF_8),
                new SortParameters.Range(0, 10),
                new byte[0][0],
                ASC,
                true);
    }

    SortParameters getSortParameters() {
        return new DefaultSortParameters("a".getBytes(UTF_8),
                new SortParameters.Range(0, 10),
                new byte[][]{new byte[]{1, 2, 3}},
                ASC,
                true);
    }

    @Override
    protected Object getTestData(SortParameters value) {
        StringJoiner stringJoiner = new StringJoiner(":");
        return stringJoiner
                .add(valueOf(value.getOrder()))
                .add(valueOf(value.getLimit().getStart()))
                .add(valueOf(value.getLimit().getCount()))
                .add(valueOf(value.isAlphabetic()))
                .add(Arrays.toString(value.getByPattern()))
                .add(deepToString(value.getGetPattern()))
                .toString();
    }

    @Test
    void testSortParametersWithoutValue() {
        SortParameters sortParameters = new DefaultSortParameters();
        byte[] bytes = SORT_PARAMETERS_SERIALIZER.serialize(sortParameters);
        assertNotNull(bytes);

        SortParameters deserialized = SORT_PARAMETERS_SERIALIZER.deserialize(bytes);
        assertNotNull(deserialized);
        assertNull(deserialized.getByPattern());
        assertNotNull(deserialized.getGetPattern());
        assertNull(deserialized.getLimit());
        assertNull(deserialized.getOrder());
        assertNull(deserialized.isAlphabetic());
    }
}
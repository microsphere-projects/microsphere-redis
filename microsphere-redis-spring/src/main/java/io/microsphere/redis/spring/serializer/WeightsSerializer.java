package io.microsphere.redis.spring.serializer;

import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.util.List;

import static io.microsphere.redis.spring.serializer.Serializers.defaultSerialize;

/**
 * {@link Weights} {@link RedisSerializer} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class WeightsSerializer extends AbstractSerializer<Weights> {

    public static final WeightsSerializer INSTANCE = new WeightsSerializer();

    @Override
    protected byte[] doSerialize(Weights weights) throws SerializationException {
        List<Double> doubles = weights.toList();
        return defaultSerialize(doubles);
    }

    @Override
    protected Weights doDeserialize(byte[] bytes) throws SerializationException {
        List<Double> doubles = Serializers.deserialize(bytes, List.class);
        int size = doubles.size();
        double[] weights = new double[size];
        for (int i = 0; i < size; i++) {
            weights[i] = doubles.get(i);
        }
        return Weights.of(weights);
    }
}

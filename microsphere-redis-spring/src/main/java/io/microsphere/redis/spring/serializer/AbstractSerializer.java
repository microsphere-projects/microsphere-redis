package io.microsphere.redis.spring.serializer;

import io.microsphere.annotation.Nonnull;
import org.springframework.core.ResolvableType;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import static org.springframework.core.ResolvableType.forClass;

/**
 * Convenience base class for {@link RedisSerializer} implementations that handles null-safety
 * and fixed-length byte-array validation, delegating the actual encode/decode logic to
 * {@link #doSerialize(Object)} and {@link #doDeserialize(byte[])}.
 *
 * <p>The expected serialized byte-array length is determined by {@link #calcBytesLength()}.
 * Sub-classes that always produce arrays of a fixed size (e.g. 4 bytes for {@link Integer})
 * should override this method to return that size; those with variable-length output should
 * return {@link #UNBOUND_BYTES_LENGTH}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   public class IntegerSerializer extends AbstractSerializer<Integer> {
 *
 *       public static final IntegerSerializer INTEGER_SERIALIZER = new IntegerSerializer();
 *
 *       @Override
 *       protected int calcBytesLength() {
 *           return INTEGER_BYTES_LENGTH; // 4
 *       }
 *
 *       @Override
 *       protected byte[] doSerialize(Integer value) {
 *           ByteBuffer buffer = ByteBuffer.allocate(INTEGER_BYTES_LENGTH);
 *           buffer.putInt(value);
 *           return buffer.array();
 *       }
 *
 *       @Override
 *       protected Integer doDeserialize(byte[] bytes) {
 *           return ByteBuffer.wrap(bytes).getInt();
 *       }
 *   }
 *
 *   byte[] bytes = INTEGER_SERIALIZER.serialize(42);  // 4-byte big-endian int
 *   int value   = INTEGER_SERIALIZER.deserialize(bytes); // 42
 * }</pre>
 *
 * @param <T> Serialized/Deserialized type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AbstractSerializer<T> implements RedisSerializer<T> {

    public static final int UNBOUND_BYTES_LENGTH = -1;

    public static final int BOOLEAN_BYTES_LENGTH = 1;

    public static final int BYTE_BYTES_LENGTH = 1;

    public static final int SHORT_BYTES_LENGTH = 2;

    public static final int INTEGER_BYTES_LENGTH = 4;

    public static final int FLOAT_BYTES_LENGTH = 4;

    public static final int LONG_BYTES_LENGTH = 8;

    public static final int DOUBLE_BYTES_LENGTH = 8;

    private final ResolvableType type;

    private final Class<T> targetType;

    private final int bytesLength;

    /**
     * Resolves the serializable type parameter {@code T} via reflection and pre-calculates
     * the expected serialized byte-array length.
     */
    public AbstractSerializer() {
        this.type = resolvableType();
        this.targetType = (Class<T>) type.resolve();
        this.bytesLength = calcBytesLength();
    }

    @Override
    public final byte[] serialize(T t) throws SerializationException {
        // null compatible case
        if (t == null) {
            return null;
        }

        return doSerialize(t);
    }

    @Override
    public final T deserialize(byte[] bytes) throws SerializationException {
        // null compatible case
        if (bytes == null) {
            return null;
        }

        // Compatible byte array fixed case
        if (bytesLength != UNBOUND_BYTES_LENGTH && bytesLength != bytes.length) {
            return null;
        }

        return doDeserialize(bytes);
    }

    @Override
    public final Class<T> getTargetType() {
        return targetType;
    }

    /**
     * Returns the {@link ResolvableType} for the generic type parameter {@code T}.
     *
     * @return the resolved type; never {@code null}
     */
    public ResolvableType getParameterizedType() {
        return type;
    }

    /**
     * Returns the raw {@link Class} for the generic type parameter {@code T}.
     * Delegates to {@link #getTargetType()}.
     *
     * @return the target class; never {@code null}
     */
    public Class<T> getParameterizedClass() {
        return getTargetType();
    }

    /**
     * Returns the expected length of the serialized byte array, or {@link #UNBOUND_BYTES_LENGTH}
     * if variable-length serialization is used.
     *
     * @return the fixed bytes length (e.g. {@code 4} for {@code Integer}), or {@code -1}
     */
    public int getBytesLength() {
        return bytesLength;
    }

    /**
     * Calculates the expected fixed serialized byte-array length.
     * Sub-classes with a fixed-size representation must override this to return the correct size.
     *
     * @return the fixed byte length, or {@link #UNBOUND_BYTES_LENGTH} ({@code -1}) for variable-length
     */
    protected int calcBytesLength() {
        return UNBOUND_BYTES_LENGTH;
    }

    /**
     * Performs the actual serialization of a non-null {@code t} value.
     *
     * @param t the value to serialize; never {@code null}
     * @return the serialized byte array; must not be {@code null}
     * @throws SerializationException if serialization fails
     */
    protected abstract byte[] doSerialize(@Nonnull T t) throws SerializationException;

    /**
     * Performs the actual deserialization of a non-null {@code bytes} array.
     *
     * @param bytes the byte array to deserialize; never {@code null}
     * @return the deserialized value
     * @throws SerializationException if deserialization fails
     */
    protected abstract T doDeserialize(@Nonnull byte[] bytes) throws SerializationException;

    private ResolvableType resolvableType() {
        return forClass(getClass()).as(RedisSerializer.class).getGeneric(0);
    }
}
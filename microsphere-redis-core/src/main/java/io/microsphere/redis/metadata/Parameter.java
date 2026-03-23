package io.microsphere.redis.metadata;

import io.microsphere.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;

/**
 * Encapsulates a single parameter of a Redis command method, holding both the
 * Java object value, its {@link ParameterMetadata}, and an optional serialized
 * raw byte representation.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   ParameterMetadata metadata = new ParameterMetadata(0, "java.lang.String", "key");
 *   Parameter parameter = new Parameter("my-key", metadata);
 *   parameter.setRawValue("my-key".getBytes(StandardCharsets.UTF_8));
 *
 *   Object value = parameter.getValue();           // "my-key"
 *   int index = parameter.getParameterIndex();     // 0
 *   String type = parameter.getParameterType();    // "java.lang.String"
 *   byte[] raw = parameter.getRawValue();          // UTF-8 bytes of "my-key"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class Parameter {

    private final Object value;

    private final ParameterMetadata metadata;

    private @Nullable byte[] rawValue;

    /**
     * Creates a {@link Parameter} with the given value and metadata.
     *
     * @param value    the Java object value of this parameter
     * @param metadata the {@link ParameterMetadata} describing this parameter
     */
    public Parameter(Object value, ParameterMetadata metadata) {
        this.value = value;
        this.metadata = metadata;
    }

    /**
     * Returns the Java object value of this parameter.
     *
     * @return the parameter value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the {@link ParameterMetadata} associated with this parameter.
     *
     * @return non-null {@link ParameterMetadata}
     */
    public ParameterMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the serialized raw byte array representation of this parameter value, if available.
     *
     * @return the raw value bytes, or {@code null} if not yet serialized
     */
    public @Nullable byte[] getRawValue() {
        return rawValue;
    }

    /**
     * Sets the serialized raw byte array representation of this parameter value.
     *
     * @param rawValue the raw value bytes, may be {@code null}
     */
    public void setRawValue(@Nullable byte[] rawValue) {
        this.rawValue = rawValue;
    }

    /**
     * Returns the zero-based index position of this parameter in the method signature.
     *
     * @return the parameter index
     */
    public int getParameterIndex() {
        return metadata.getParameterIndex();
    }

    /**
     * Returns the fully-qualified type name of this parameter.
     *
     * @return parameter type name, e.g. {@code "java.lang.String"}
     */
    public String getParameterType() {
        return metadata.getParameterType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Parameter)) return false;

        Parameter parameter = (Parameter) o;

        if (!Objects.equals(value, parameter.value)) return false;
        if (!Objects.equals(metadata, parameter.metadata)) return false;
        return Arrays.equals(rawValue, parameter.rawValue);
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(rawValue);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, Parameter.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("value=" + value)
                .add("metadata=" + metadata)
                .add("rawValue=" + Arrays.toString(rawValue))
                .toString();
    }
}
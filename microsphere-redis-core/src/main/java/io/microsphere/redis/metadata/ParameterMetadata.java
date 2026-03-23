package io.microsphere.redis.metadata;

import io.microsphere.annotation.Nullable;

import java.util.Objects;
import java.util.StringJoiner;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;

/**
 * Immutable metadata describing a single parameter of a Redis command method,
 * including its position index, fully-qualified type name, and optional parameter name.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Create with explicit name
 *   ParameterMetadata pm = new ParameterMetadata(0, "java.lang.String", "key");
 *   System.out.println(pm.getParameterIndex()); // 0
 *   System.out.println(pm.getParameterType());  // "java.lang.String"
 *   System.out.println(pm.getParameterName());  // "key"
 *
 *   // Create with auto-generated name "arg0"
 *   ParameterMetadata pm2 = new ParameterMetadata(0, "java.lang.String");
 *   System.out.println(pm2.getParameterName()); // "arg0"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public final class ParameterMetadata {

    private final int parameterIndex;

    private final String parameterType;

    private final @Nullable String parameterName;

    /**
     * Creates a {@link ParameterMetadata} with an auto-generated name {@code "argN"} where N is
     * the {@code parameterIndex}.
     *
     * @param parameterIndex zero-based index of the parameter in the method signature
     * @param parameterType  fully-qualified type name of the parameter
     */
    public ParameterMetadata(int parameterIndex, String parameterType) {
        this(parameterIndex, parameterType, "arg" + parameterIndex);
    }

    /**
     * Creates a {@link ParameterMetadata} with an explicit parameter name.
     *
     * @param parameterIndex zero-based index of the parameter in the method signature
     * @param parameterType  fully-qualified type name of the parameter
     * @param parameterName  the parameter name (may be {@code null})
     */
    public ParameterMetadata(int parameterIndex, String parameterType, @Nullable String parameterName) {
        this.parameterIndex = parameterIndex;
        this.parameterType = parameterType;
        this.parameterName = parameterName;
    }

    /**
     * Returns the zero-based position index of this parameter in the method signature.
     *
     * @return parameter index
     */
    public int getParameterIndex() {
        return parameterIndex;
    }

    /**
     * Returns the fully-qualified type name of this parameter.
     *
     * @return parameter type name, e.g. {@code "java.lang.String"} or {@code "[B"} for {@code byte[]}
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * Returns the name of this parameter, or {@code null} if unavailable.
     *
     * @return parameter name, e.g. {@code "key"}, or auto-generated {@code "arg0"}, or {@code null}
     */
    @Nullable
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterMetadata)) return false;
        ParameterMetadata that = (ParameterMetadata) o;
        return parameterIndex == that.parameterIndex && Objects.equals(parameterType, that.parameterType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterIndex, parameterType);
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, ParameterMetadata.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("parameterIndex=" + parameterIndex)
                .add("parameterType='" + parameterType + "'")
                .add("parameterName='" + parameterName + "'")
                .toString();
    }
}

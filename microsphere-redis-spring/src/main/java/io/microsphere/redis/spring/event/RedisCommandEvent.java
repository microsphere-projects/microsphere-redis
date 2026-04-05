package io.microsphere.redis.spring.event;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.redis.spring.interceptor.RedisMethodContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisPubSubCommands;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;
import static io.microsphere.redis.spring.serializer.RedisCommandEventSerializer.VERSION_DEFAULT;
import static io.microsphere.redis.spring.serializer.RedisCommandEventSerializer.VERSION_V1;
import static io.microsphere.util.ArrayUtils.arrayToString;


/**
 * Spring {@link ApplicationEvent} that captures a single Redis command invocation together with
 * its method, arguments, source bean name, and application name.  Published by
 * {@link io.microsphere.redis.spring.interceptor.EventPublishingRedisCommandInterceptor} after
 * each intercepted Redis command.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Build from a RedisMethodContext inside an interceptor:
 *   RedisCommandEvent event = new RedisCommandEvent(redisMethodContext);
 *
 *   // Build via the fluent Builder:
 *   Method method = RedisStringCommands.class.getMethod("set", byte[].class, byte[].class);
 *   RedisCommandEvent event = RedisCommandEvent.Builder
 *           .source(redisConnectionFactory)
 *           .applicationName("my-app")
 *           .sourceBeanName("redisTemplate")
 *           .method(method)
 *           .args(key, value)
 *           .build();
 *
 *   System.out.println(event.getMethodName());    // "set"
 *   System.out.println(event.getApplicationName()); // "my-app"
 *   System.out.println(event.getParameterCount()); // 2
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisCommands
 * @see RedisKeyCommands
 * @see RedisStringCommands
 * @see RedisListCommands
 * @see RedisSetCommands
 * @see RedisZSetCommands
 * @see RedisHashCommands
 * @see RedisTxCommands
 * @see RedisPubSubCommands
 * @see RedisConnectionCommands
 * @see RedisServerCommands
 * @see RedisScriptingCommands
 * @see RedisGeoCommands
 * @see RedisHyperLogLogCommands
 * @since 1.0.0
 */
public class RedisCommandEvent extends ApplicationEvent {

    private static final long serialVersionUID = -1L;

    private final transient String applicationName;

    private final transient String sourceBeanName;

    private final transient Method method;

    private final transient Object[] args;

    private transient String interfaceName;

    private transient Class<?>[] parameterTypes;

    private transient int parameterCount = -1;

    private transient byte serializationVersion = VERSION_V1;

    protected RedisCommandEvent(Object source, String applicationName, String sourceBeanName, Method method, Object... args) {
        super(source);
        this.applicationName = applicationName;
        this.sourceBeanName = sourceBeanName;
        this.method = method;
        this.args = args;
    }

    /**
     * Creates a {@link RedisCommandEvent} from the given {@link RedisMethodContext}.
     *
     * @param redisMethodContext the execution context of the intercepted Redis method
     */
    public RedisCommandEvent(@Nonnull RedisMethodContext redisMethodContext) {
        this(redisMethodContext, redisMethodContext.getApplicationName(), redisMethodContext.getSourceBeanName(), redisMethodContext.getMethod(), redisMethodContext.getArgs());
    }

    public static class Builder {

        private final Object source;

        private String applicationName;

        private String sourceBeanName;

        private Method method;

        private Object[] args;

        private byte serializationVersion = VERSION_DEFAULT;

        protected Builder(Object source) {
            this.source = source;
        }

        public static Builder source(Object source) {
            return new Builder(source);
        }

        public Builder applicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder sourceBeanName(String sourceBeanName) {
            this.sourceBeanName = sourceBeanName;
            return this;
        }

        public Builder method(Method method) {
            this.method = method;
            return this;
        }

        public Builder args(Object... args) {
            this.args = args;
            return this;
        }

        public Builder serializationVersion(byte serializationVersion) {
            this.serializationVersion = serializationVersion;
            return this;
        }

        public Method getMethod() {
            return method;
        }

        public RedisCommandEvent build() {
            RedisCommandEvent redisCommandEvent = new RedisCommandEvent(source, applicationName, sourceBeanName, method, args);
            redisCommandEvent.setSerializationVersion(serializationVersion);
            return redisCommandEvent;
        }
    }

    /**
     * Command method
     *
     * @return
     */
    @Nonnull
    public Method getMethod() {
        return method;
    }

    /**
     * @return Command interface name, such as：
     * <ul>
     *     <li>"org.springframework.data.redis.connection.RedisStringCommands"</li>
     *     <li>"org.springframework.data.redis.connection.RedisHashCommands"</li>
     * </ul>
     */
    @Nonnull
    public String getInterfaceName() {
        String interfaceName = this.interfaceName;
        if (interfaceName == null) {
            interfaceName = resolveInterfaceName(this.method);
            this.interfaceName = interfaceName;
        }
        return interfaceName;
    }

    private String resolveInterfaceName(Method method) {
        Class<?> declaringClass = method.getDeclaringClass();
        String className = declaringClass.getName();
        return className;
    }

    /**
     * Returns the simple name of the intercepted Redis command method.
     *
     * @return method name, e.g. {@code "set"} or {@code "get"}
     */
    @Nonnull
    public String getMethodName() {
        return method.getName();
    }

    /**
     * Returns the parameter types of the intercepted method, lazily cached from the {@link Method} reflection data.
     *
     * @return array of parameter type classes; never {@code null}
     */
    @Nonnull
    public Class<?>[] getParameterTypes() {
        Class<?>[] parameterTypes = this.parameterTypes;
        if (parameterTypes == null) {
            parameterTypes = method.getParameterTypes();
            this.parameterTypes = parameterTypes;
        }
        return parameterTypes;
    }

    /**
     * Returns the number of parameters of the intercepted Redis command method, lazily cached.
     *
     * @return parameter count (≥ 0)
     */
    public int getParameterCount() {
        int parameterCount = this.parameterCount;
        if (parameterCount == -1) {
            parameterCount = method.getParameterCount();
            this.parameterCount = parameterCount;
        }
        return parameterCount;
    }

    /**
     * Returns the actual arguments passed to the intercepted Redis command.
     *
     * @return the argument array; may be {@code null} for commands with no parameters
     */
    @Nullable
    public Object[] getArgs() {
        return this.args;
    }

    /**
     * Returns the argument at the specified zero-based position.
     *
     * @param index the zero-based argument index
     * @return the argument value at {@code index}; may be {@code null}
     */
    @Nullable
    public Object getArg(int index) {
        return this.args[index];
    }

    /**
     * @return Event source Application name
     */
    @Nonnull
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Source Bean name (non-serialized field, initialized by the consumer)
     *
     * @return Source Bean name
     */
    @Nullable
    public String getSourceBeanName() {
        return this.sourceBeanName;
    }

    /**
     * Sets the serialization version used when this event is serialized (e.g. for messaging).
     *
     * @param serializationVersion the version byte; see {@link io.microsphere.redis.spring.serializer.RedisCommandEventSerializer}
     */
    public void setSerializationVersion(byte serializationVersion) {
        this.serializationVersion = serializationVersion;
    }

    /**
     * Returns the serialization version of this event.
     *
     * @return the version byte
     */
    public byte getSerializationVersion() {
        return serializationVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RedisCommandEvent)) {
            return false;
        }
        RedisCommandEvent that = (RedisCommandEvent) o;
        return Objects.equals(applicationName, that.applicationName) &&
                Objects.equals(sourceBeanName, that.sourceBeanName) &&
                Objects.equals(method, that.method) &&
                Arrays.deepEquals(args, that.args);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(applicationName, sourceBeanName, method);
        result = 31 * result + Arrays.hashCode(args);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, RedisCommandEvent.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("applicationName='" + applicationName + "'")
                .add("sourceBeanName='" + sourceBeanName + "'")
                .add("method=" + method)
                .add("args=" + arrayToString(args))
                .toString();
    }
}
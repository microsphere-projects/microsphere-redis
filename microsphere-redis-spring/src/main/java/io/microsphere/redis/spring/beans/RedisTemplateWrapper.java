package io.microsphere.redis.spring.beans;

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import static io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor.newProxyRedisConnection;

/**
 * A transparent {@link RedisTemplate} subclass that intercepts every Redis connection by wrapping
 * it in a JDK-proxy created by
 * {@link RedisConnectionFactoryProxyBeanPostProcessor#newProxyRedisConnection}.
 * Interception is only active when {@link RedisContext#isEnabled()} returns {@code true}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically created by RedisTemplateWrapperBeanPostProcessor, but can be instantiated directly:
 *   RedisTemplate<String, Object> delegate = applicationContext.getBean("redisTemplate", RedisTemplate.class);
 *   RedisContext redisContext = applicationContext.getBean(RedisContext.BEAN_NAME, RedisContext.class);
 *   RedisTemplateWrapper<String, Object> wrapper =
 *           new RedisTemplateWrapper<>("redisTemplate", delegate, redisContext);
 *
 *   // Use the wrapper exactly like a regular RedisTemplate
 *   wrapper.opsForValue().set("key", "value");
 * }</pre>
 *
 * @param <K> Redis Key type
 * @param <V> Redis Value type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisTemplateWrapper<K, V> extends RedisTemplate<K, V> implements DelegatingWrapper {

    private final String beanName;

    private final RedisTemplate<K, V> delegate;

    private final RedisContext redisContext;

    /**
     * Creates a new {@link RedisTemplateWrapper} that delegates all operations to the given
     * {@code delegate} template and copies its configuration (connection factory, serializers, etc.).
     *
     * @param beanName     the Spring bean name of the wrapped template (used during interceptor dispatching)
     * @param delegate     the original {@link RedisTemplate} to delegate to
     * @param redisContext the {@link RedisContext} that controls interception behaviour
     */
    public RedisTemplateWrapper(String beanName, RedisTemplate<K, V> delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
        init();
    }

    private void init() {
        configure(this.delegate, this);
    }

    static void configure(RedisTemplate<?, ?> source, RedisTemplate<?, ?> target) {
        // Set the connection
        target.setConnectionFactory(source.getConnectionFactory());
        target.setExposeConnection(source.isExposeConnection());

        // Set the RedisSerializers
        target.setEnableDefaultSerializer(source.isEnableDefaultSerializer());
        target.setDefaultSerializer(source.getDefaultSerializer());
        target.setKeySerializer(source.getKeySerializer());
        target.setValueSerializer(source.getValueSerializer());
        target.setHashKeySerializer(source.getHashKeySerializer());
        target.setHashValueSerializer(source.getHashValueSerializer());
        target.setStringSerializer(source.getStringSerializer());
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        if (this.isEnabled()) {
            return newProxyRedisConnection(connection, this.getRedisContext(), this.getDelegate(), this.getBeanName());
        }
        return connection;
    }

    /**
     * Returns {@code true} when the Redis interceptor infrastructure is active (i.e.
     * {@link RedisContext#isEnabled()} returns {@code true}).
     *
     * @return {@code true} if interception is currently enabled
     */
    public boolean isEnabled() {
        return this.redisContext.isEnabled();
    }

    /**
     * Returns the Spring bean name under which the wrapped {@link RedisTemplate} is registered.
     *
     * @return the delegate bean name; never {@code null}
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * Returns the {@link RedisContext} associated with this wrapper.
     *
     * @return the {@link RedisContext}; never {@code null}
     */
    public RedisContext getRedisContext() {
        return this.redisContext;
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }
}
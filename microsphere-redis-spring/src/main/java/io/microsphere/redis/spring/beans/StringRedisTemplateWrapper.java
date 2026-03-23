package io.microsphere.redis.spring.beans;

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import static io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor.newProxyRedisConnection;
import static io.microsphere.redis.spring.beans.RedisTemplateWrapper.configure;


/**
 * A transparent {@link StringRedisTemplate} subclass that intercepts every Redis connection by wrapping
 * it in a JDK-proxy created by
 * {@link RedisConnectionFactoryProxyBeanPostProcessor#newProxyRedisConnection}.
 * Interception is only active when {@link RedisContext#isEnabled()} returns {@code true}.
 * All configuration (serializers, connection factory, etc.) is copied from the delegate template.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Typically created by RedisTemplateWrapperBeanPostProcessor, but can be instantiated directly:
 *   StringRedisTemplate delegate = applicationContext.getBean(StringRedisTemplate.class);
 *   RedisContext redisContext = applicationContext.getBean(RedisContext.BEAN_NAME, RedisContext.class);
 *   StringRedisTemplateWrapper wrapper =
 *           new StringRedisTemplateWrapper("stringRedisTemplate", delegate, redisContext);
 *
 *   // Use the wrapper exactly like a regular StringRedisTemplate
 *   wrapper.opsForValue().set("key", "value");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class StringRedisTemplateWrapper extends StringRedisTemplate implements DelegatingWrapper {

    private final String beanName;

    private final StringRedisTemplate delegate;

    private final RedisContext redisContext;

    /**
     * Creates a new {@link StringRedisTemplateWrapper} that delegates all operations to the given
     * {@code delegate} and copies its configuration (connection factory, serializers, etc.).
     *
     * @param beanName     the Spring bean name of the wrapped template
     * @param delegate     the original {@link StringRedisTemplate} to delegate to
     * @param redisContext the {@link RedisContext} that controls interception behaviour
     */
    public StringRedisTemplateWrapper(String beanName, StringRedisTemplate delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
        init();
    }

    private void init() {
        configure(this.delegate, this);
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
     * Returns the {@link RedisContext} associated with this wrapper.
     *
     * @return the {@link RedisContext}; never {@code null}
     */
    public RedisContext getRedisContext() {
        return this.redisContext;
    }

    /**
     * {@inheritDoc}
     * Returns the wrapped {@link StringRedisTemplate} delegate.
     */
    @Override
    public Object getDelegate() {
        return this.delegate;
    }

    /**
     * Returns the Spring bean name under which the wrapped {@link StringRedisTemplate} is registered.
     *
     * @return the delegate bean name; never {@code null}
     */
    public String getBeanName() {
        return this.beanName;
    }
}
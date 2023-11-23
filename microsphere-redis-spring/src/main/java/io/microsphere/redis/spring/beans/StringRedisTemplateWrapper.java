package io.microsphere.redis.spring.beans;

import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * {@link StringRedisTemplate} Wrapper class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class StringRedisTemplateWrapper extends StringRedisTemplate implements DelegatingWrapper {

    private final String beanName;

    private final StringRedisTemplate delegate;

    private final RedisContext redisContext;

    public StringRedisTemplateWrapper(String beanName, StringRedisTemplate delegate, RedisContext redisContext) {
        this.beanName = beanName;
        this.delegate = delegate;
        this.redisContext = redisContext;
        init();
    }

    private void init() {
        RedisTemplateWrapper.configure(delegate, this);
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        if (isEnabled()) {
            return RedisTemplateWrapper.newProxyRedisConnection(connection, redisContext, beanName);
        }
        return connection;
    }

    public boolean isEnabled() {
        return redisContext.isEnabled();
    }

    @Override
    public Object getDelegate() {
        return delegate;
    }
}

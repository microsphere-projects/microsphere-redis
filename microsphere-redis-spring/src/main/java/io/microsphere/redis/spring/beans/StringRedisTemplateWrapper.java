package io.microsphere.redis.spring.beans;

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import static io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor.newProxyRedisConnection;
import static io.microsphere.redis.spring.beans.RedisTemplateWrapper.configure;


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
        configure(this.delegate, this);
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        if (this.isEnabled()) {
            return newProxyRedisConnection(connection, this.getRedisContext(), this.getDelegate(), this.getBeanName());
        }
        return connection;
    }

    public boolean isEnabled() {
        return this.redisContext.isEnabled();
    }

    public RedisContext getRedisContext() {
        return this.redisContext;
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }

    public String getBeanName() {
        return this.beanName;
    }
}
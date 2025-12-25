package io.microsphere.redis.spring.beans;

import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import static io.microsphere.redis.spring.beans.RedisConnectionFactoryProxyBeanPostProcessor.newProxyRedisConnection;

/**
 * {@link RedisTemplate} Wrapper class, compatible with {@link RedisTemplate}
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

    public boolean isEnabled() {
        return this.redisContext.isEnabled();
    }

    public String getBeanName() {
        return this.beanName;
    }

    public RedisContext getRedisContext() {
        return this.redisContext;
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }
}
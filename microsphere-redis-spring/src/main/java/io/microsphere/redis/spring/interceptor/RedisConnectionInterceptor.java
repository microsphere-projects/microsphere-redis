package io.microsphere.redis.spring.interceptor;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * {@link RedisConnection} interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMethodInterceptor
 * @see RedisConnection
 * @since 1.0.0
 */
public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> {
}
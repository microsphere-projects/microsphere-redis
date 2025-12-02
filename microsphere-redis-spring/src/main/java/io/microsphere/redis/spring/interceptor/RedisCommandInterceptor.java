package io.microsphere.redis.spring.interceptor;

import org.springframework.data.redis.connection.RedisCommands;

/**
 * {@link RedisCommands Redis Command} interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMethodInterceptor
 * @see RedisCommands
 * @since 1.0.0
 */
public interface RedisCommandInterceptor extends RedisMethodInterceptor<RedisCommands> {
}
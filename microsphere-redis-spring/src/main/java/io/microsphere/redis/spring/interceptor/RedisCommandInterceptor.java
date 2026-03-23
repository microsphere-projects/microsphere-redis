package io.microsphere.redis.spring.interceptor;

import org.springframework.data.redis.connection.RedisCommands;

/**
 * Specialization of {@link RedisMethodInterceptor} for intercepting {@link RedisCommands}
 * method invocations.  Implementations are discovered from the Spring application context and
 * invoked by {@link InterceptingRedisConnectionInvocationHandler} around each Redis command.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @Component
 *   public class LoggingRedisCommandInterceptor implements RedisCommandInterceptor {
 *
 *       @Override
 *       public void beforeExecute(RedisMethodContext<RedisCommands> context) {
 *           System.out.println("Before: " + context.getMethod().getName());
 *       }
 *
 *       @Override
 *       public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) {
 *           System.out.println("After: " + context.getMethod().getName() + " result=" + result);
 *       }
 *
 *       @Override
 *       public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMethodInterceptor
 * @see RedisCommands
 * @since 1.0.0
 */
public interface RedisCommandInterceptor extends RedisMethodInterceptor<RedisCommands> {
}
package io.microsphere.redis.spring.interceptor;

import org.springframework.data.redis.connection.RedisConnection;

/**
 * Specialization of {@link RedisMethodInterceptor} for intercepting {@link RedisConnection}
 * method invocations at the lowest connection level. Implementations are discovered from the
 * Spring application context and invoked by {@link InterceptingRedisConnectionInvocationHandler}
 * around every {@link RedisConnection} method call.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @Component
 *   public class MetricsRedisConnectionInterceptor implements RedisConnectionInterceptor {
 *
 *       @Override
 *       public void beforeExecute(RedisMethodContext<RedisConnection> context) {
 *           context.start();
 *       }
 *
 *       @Override
 *       public void afterExecute(RedisMethodContext<RedisConnection> context, Object result, Throwable failure) {
 *           context.stop();
 *           long ms = context.getDuration(TimeUnit.MILLISECONDS);
 *           System.out.println(context.getMethod().getName() + " took " + ms + " ms");
 *       }
 *
 *       @Override
 *       public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMethodInterceptor
 * @see RedisConnection
 * @since 1.0.0
 */
public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> {
}
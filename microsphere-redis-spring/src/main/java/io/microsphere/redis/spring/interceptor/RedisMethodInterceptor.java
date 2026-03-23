package io.microsphere.redis.spring.interceptor;

import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * SPI interface for intercepting Redis method invocations at a given target type (e.g.
 * {@link org.springframework.data.redis.connection.RedisConnection} or
 * {@link org.springframework.data.redis.connection.RedisCommands}).
 * Implementations are collected by {@link InterceptingRedisConnectionInvocationHandler} and
 * invoked before and after each Redis method call.
 *
 * <p>All methods have default no-op implementations so that implementors only need to override
 * the phases they care about.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   @Component
 *   public class TimingRedisMethodInterceptor implements RedisMethodInterceptor<RedisCommands> {
 *
 *       @Override
 *       public void beforeExecute(RedisMethodContext<RedisCommands> context) {
 *           context.start();
 *       }
 *
 *       @Override
 *       public void afterExecute(RedisMethodContext<RedisCommands> context,
 *                                Object result, Throwable failure) {
 *           context.stop();
 *           System.out.println(context.getMethod().getName()
 *               + " took " + context.getDuration(TimeUnit.MILLISECONDS) + " ms");
 *       }
 *
 *       @Override
 *       public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
 *   }
 * }</pre>
 *
 * @param <T> The target type of the Redis interface
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisMethodContext
 * @since 1.0.0
 */
public interface RedisMethodInterceptor<T> extends Ordered {

    /**
     * Intercept {@link T The target Redis instance} method before execution
     *
     * @param context {@link RedisMethodContext}
     * @throws Throwable When method implementations execute exceptions
     */
    default void beforeExecute(RedisMethodContext<T> context) throws Throwable {
        beforeExecute(context.getTarget(), context.getMethod(), context.getArgs(), context.getSourceBeanName());
    }

    /**
     * Intercept {@link T The target Redis instance} method before execution
     *
     * @param target         {@link T The target Redis instance}
     * @param method         {@link T The target Redis instance} executing {@link Method}
     * @param args           {@link T The target Redis instance} executing {@link Method} arguments
     * @param sourceBeanName The {@link Optional} of Source Bean Name
     * @throws Throwable When method implementations execute exceptions
     */
    default void beforeExecute(T target, Method method, Object[] args, @Nullable String sourceBeanName) throws Throwable {
    }

    /**
     * Intercept {@link T The target Redis instance} method after execution
     *
     * @param context {@link RedisMethodContext}
     * @param result  The nullable {@link T The target Redis instance} method execution result
     * @param failure The nullable {@link Throwable Throwable} caused by Redis method execution
     * @throws Throwable When method implementations execute exceptions
     */
    default void afterExecute(RedisMethodContext<T> context, @Nullable Object result, @Nullable Throwable failure) throws Throwable {
        afterExecute(context.getTarget(), context.getMethod(), context.getArgs(), context.getSourceBeanName(), result, failure);
    }

    /**
     * Intercept {@link T The target Redis instance} method after execution
     *
     * @param target         {@link T The target Redis instance}
     * @param method         {@link T The target Redis instance} executing {@link Method}
     * @param args           {@link T The target Redis instance} executing {@link Method} arguments
     * @param sourceBeanName The {@link Optional} of Source Bean Name
     * @param result         The nullable {@link T The target Redis instance} method execution result
     * @param failure        The nullable {@link Throwable Throwable} caused by Redis method execution
     * @throws Throwable When method implementations execute exceptions
     */
    default void afterExecute(T target, Method method, Object[] args, @Nullable String sourceBeanName,
                              @Nullable Object result, @Nullable Throwable failure) throws Throwable {
    }

    /**
     * Handle interception error
     *
     * @param context {@link RedisMethodContext}
     * @param before  If <code>true</code>, it indicates error occurs on {@link #beforeExecute(RedisMethodContext)}, or {@link #afterExecute(RedisMethodContext, Object, Throwable)}
     * @param result  The nullable {@link T The target Redis instance} method execution result
     * @param failure The nullable {@link Throwable Throwable} caused by Redis method execution
     * @param error   {@link Throwable error} caused by interception
     */
    default void handleError(RedisMethodContext<T> context, boolean before, @Nullable Object result,
                             @Nullable Throwable failure, Throwable error) {
    }
}
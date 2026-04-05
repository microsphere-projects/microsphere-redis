/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.redis.spring.interceptor;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.redis.metadata.Parameter;
import io.microsphere.redis.metadata.ParameterMetadata;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.isWriteCommandMethod;
import static io.microsphere.redis.spring.util.SpringRedisCommandUtils.initializeParameters;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertTrue;
import static java.lang.System.nanoTime;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * Execution context for a single intercepted Redis method call.  Encapsulates the target
 * Redis object, the reflected method, its arguments, timing data, the source bean info, and
 * a key-value attribute bag for sharing data between interceptor phases.
 *
 * <p>Instances are created by {@link InterceptingRedisConnectionInvocationHandler} and stored
 * in a thread-local for cross-interceptor access via {@link #get()} / {@link #set(RedisMethodContext)}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Inside a RedisCommandInterceptor:
 *   @Override
 *   public void beforeExecute(RedisMethodContext<RedisCommands> context) throws Throwable {
 *       context.start(); // start nano-timer
 *       System.out.println("Command: " + context.getMethod().getName());
 *       System.out.println("Is write: " + context.isWriteMethod());
 *   }
 *
 *   @Override
 *   public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) {
 *       context.stop();
 *       long ms = context.getDuration(TimeUnit.MILLISECONDS);
 *       System.out.println("Duration: " + ms + " ms, result=" + result);
 *   }
 * }</pre>
 *
 * @param <T> the type of the target Redis interface (e.g. {@link org.springframework.data.redis.connection.RedisConnection}
 *            or {@link org.springframework.data.redis.connection.RedisCommands})
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisMethodContext<T> {

    private static final Logger logger = getLogger(RedisMethodContext.class);

    private static final Parameter[] EMPTY_PARAMETERS = new Parameter[0];

    private static final ThreadLocal<RedisMethodContext<?>> redisMethodContextThreadLocal = new ThreadLocal<>();

    private final T target;

    private final Method method;

    private final Object[] args;

    private Parameter[] parameters = null;

    private Boolean write = null;

    private final RedisContext redisContext;

    private final Object sourceBean;

    private final String sourceBeanName;

    private Boolean sourceFromRedisTemplate = null;

    private Boolean sourceFromRedisConnectionFactory = null;

    private long startTimeNanos = -1;

    private long durationNanos = -1;

    private Map<String, Object> attributes;

    /**
     * Creates a {@link RedisMethodContext} without a source bean reference.
     *
     * @param target       the target Redis interface instance being invoked
     * @param method       the {@link Method} being invoked on the target
     * @param args         the arguments passed to the method
     * @param redisContext the {@link RedisContext} providing shared infrastructure
     */
    public RedisMethodContext(T target, Method method, Object[] args, RedisContext redisContext) {
        this(target, method, args, redisContext, null, null);
    }

    /**
     * Creates a {@link RedisMethodContext} with an explicit source bean reference.
     *
     * @param target         the target Redis interface instance being invoked
     * @param method         the {@link Method} being invoked on the target
     * @param args           the arguments passed to the method
     * @param redisContext   the {@link RedisContext} providing shared infrastructure
     * @param sourceBean     the originating Spring bean (e.g. a {@link org.springframework.data.redis.core.RedisTemplate})
     * @param sourceBeanName the Spring bean name of the originating bean
     */
    public RedisMethodContext(T target, Method method, Object[] args, RedisContext redisContext, Object sourceBean, String sourceBeanName) {
        this.target = target;
        this.method = method;
        this.args = args;
        this.redisContext = redisContext;
        this.sourceBean = sourceBean;
        this.sourceBeanName = sourceBeanName;
    }

    /**
     * Returns the target Redis interface instance (e.g. a {@link org.springframework.data.redis.connection.RedisConnection}).
     *
     * @return the target; never {@code null}
     */
    public T getTarget() {
        return this.target;
    }

    /**
     * Returns the reflected {@link Method} that was invoked on the target.
     *
     * @return the method; never {@code null}
     */
    public Method getMethod() {
        return this.method;
    }

    /**
     * Returns the actual arguments supplied to the invoked method.
     *
     * @return the argument array; may be {@code null} for no-argument methods
     */
    public Object[] getArgs() {
        return this.args;
    }

    /**
     * Returns the originating Spring bean (e.g. the {@link org.springframework.data.redis.core.RedisTemplate}
     * or the {@link org.springframework.data.redis.connection.RedisConnectionFactory}).
     *
     * @return the source bean, or {@code null} if not available
     */
    public Object getSourceBean() {
        return this.sourceBean;
    }

    /**
     * Returns the Spring bean name of the originating bean.
     *
     * @return the source bean name, or {@code null} if not set
     */
    public String getSourceBeanName() {
        return this.sourceBeanName;
    }

    /**
     * Returns the {@link RedisContext} associated with this method invocation.
     *
     * @return the {@link RedisContext}; never {@code null}
     */
    public RedisContext getRedisContext() {
        return this.redisContext;
    }

    private void initParameters() {
        int size = length(args);
        final Parameter[] parameters;
        final boolean write;

        if (size > 1) {
            parameters = new Parameter[size];
            write = initializeParameters(method, args, (parameter, index) -> {
                parameters[index] = parameter;
            });
        } else {
            parameters = EMPTY_PARAMETERS;
            write = false;
        }

        this.parameters = parameters;
        this.write = write;
    }

    /**
     * Start and record the time in nano seconds, the initialized value is negative
     */
    public void start() {
        this.startTimeNanos = nanoTime();
    }

    /**
     * Stop and record the time in nano seconds, the initialized value is negative
     *
     * @throws IllegalArgumentException if {@link #start()} is not execute before
     */
    public void stop() throws IllegalArgumentException {
        assertTrue(startTimeNanos > 0, () -> "'stop()' method must not be invoked before the execution of 'start()' method");
        this.durationNanos = nanoTime() - startTimeNanos;
    }

    /**
     * Set the attribute into the current {@link RedisMethodContext context}
     *
     * @param name  the attribute name
     * @param value the attribute value
     * @param <T>   the type of attribute value
     */
    public <T> void setAttribute(String name, T value) {
        Map<String, Object> attributes = getAttributes(true);
        attributes.put(name, value);
    }

    /**
     * Get the attribute from the current {@link RedisMethodContext context}
     *
     * @param name the attribute name
     * @param <T>  the type of attribute value
     * @return the attribute value
     */
    public <T> T getAttribute(String name) {
        return doInAttributes(attributes -> attributes.get(name));
    }

    /**
     * Has the attribute or not
     *
     * @param name the attribute name
     * @return If the attribute is present, return <code>true</code>, or <code>false</code>
     */
    public boolean hasAttribute(String name) {
        return getAttribute(name) != null;
    }

    /**
     * Remove the attribute from the current {@link RedisMethodContext context}
     *
     * @param name the attribute name
     * @param <T>  the type of attribute value
     * @return the attribute value
     */
    public <T> T removeAttribute(String name) {
        return doInAttributes(attributes -> attributes.remove(name));
    }

    /**
     * Get the attributes from the current {@link RedisMethodContext context}
     *
     * @return non-null and read-only {@link Map}
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> attributes = getAttributes(false);
        return attributes == null ? emptyMap() : unmodifiableMap(attributes);
    }

    private <T> T doInAttributes(Function<Map<String, Object>, Object> function) {
        return doInAttributes(false, function);
    }

    private <T> T doInAttributes(boolean created, Function<Map<String, Object>, Object> function) {
        Map<String, Object> attributes = getAttributes(created);
        if (attributes != null) {
            return (T) function.apply(attributes);
        }
        return null;
    }

    private Map<String, Object> getAttributes(boolean created) {
        Map<String, Object> attributes = this.attributes;
        if (attributes == null && created) {
            attributes = new HashMap<>();
            this.attributes = attributes;
        }
        return attributes;
    }

    /**
     * Get the start time in nano seconds
     *
     * @return If the value is negative, it indicates {@link #start()} method was not executed
     */
    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    /**
     * Get the execution duration time of redis method in nano seconds
     *
     * @return If the value is negative, it indicates the duration can't not be evaluated,
     * because {@link #start()} method was not executed
     */
    public long getDurationNanos() {
        return durationNanos;
    }

    /**
     * Get the execution duration time of redis method in the specified {@link TimeUnit time unit}
     *
     * @return If the value is negative, it indicates the duration can't not be evaluated,
     * because {@link #start()} method was not executed
     */
    public long getDuration(TimeUnit timeUnit) {
        long durationNanos = getDurationNanos();
        return timeUnit.convert(durationNanos, TimeUnit.NANOSECONDS);
    }

    /**
     * Returns {@code true} if the intercepted method is a Redis write command.
     * The result is lazily resolved from the {@link io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository}
     * or from initialized parameters when {@code isWriteMethod(true)} is called first.
     *
     * @return {@code true} for write commands (e.g. SET, DEL)
     */
    public boolean isWriteMethod() {
        if (write == null) {
            return isWriteMethod(false);
        }
        return write;
    }

    /**
     * Returns {@code true} if the intercepted method is a Redis write command, optionally
     * initialising parameter metadata (which also populates the {@link #getParameters()} result).
     *
     * @param initializedParameters if {@code true}, also calls {@link #initParameters()} so that
     *                              {@link #getParameters()} returns meaningful values afterwards
     * @return {@code true} for write commands
     */
    public boolean isWriteMethod(boolean initializedParameters) {
        if (initializedParameters) {
            initParameters();
        } else {
            this.write = isWriteCommandMethod(method);
        }
        return this.write;
    }

    /**
     * Returns the {@link Parameter} array for the intercepted method, lazily initialising it
     * on the first call.
     *
     * @return array of {@link Parameter}; empty array for no-parameter methods
     */
    public Parameter[] getParameters() {
        if (parameters == null) {
            initParameters();
        }
        return parameters;
    }

    /**
     * Returns the number of parameters in the intercepted method (equivalent to
     * {@code getParameters().length}).
     *
     * @return parameter count (≥ 0)
     */
    public int getParameterCount() {
        return getParameters().length;
    }

    /**
     * Finds and returns the {@link Parameter} with the given parameter name, or
     * {@code null} if no parameter with that name is found.
     *
     * @param parameterName the parameter name to look up
     * @return the matching {@link Parameter}, or {@code null}
     */
    @Nullable
    public Parameter getParameter(Object parameterName) {
        Parameter[] parameters = getParameters();
        for (Parameter parameter : parameters) {
            ParameterMetadata metadata = parameter.getMetadata();
            if (Objects.equals(parameterName, metadata.getParameterName())) {
                return parameter;
            }
        }
        return null;
    }

    /**
     * Returns the {@link Parameter} at the given zero-based index.
     *
     * @param index the zero-based parameter position
     * @return the {@link Parameter} at {@code index}
     */
    public Parameter getParameter(int index) {
        return getParameters()[index];
    }

    @NonNull
    public RedisConfiguration getRedisConfiguration() {
        return redisContext.getRedisConfiguration();
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return redisContext.getBeanFactory();
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return redisContext.getApplicationContext();
    }

    public ClassLoader getClassLoader() {
        return redisContext.getClassLoader();
    }

    public Set<String> getRedisTemplateBeanNames() {
        return redisContext.getRedisTemplateBeanNames();
    }

    public Set<String> getRedisConnectionFactoryBeanNames() {
        return redisContext.getRedisConnectionFactoryBeanNames();
    }

    public boolean isEnabled() {
        return redisContext.isEnabled();
    }

    public ConfigurableEnvironment getEnvironment() {
        return redisContext.getEnvironment();
    }

    public boolean isCommandEventExposed() {
        return redisContext.isCommandEventExposed();
    }

    public String getApplicationName() {
        return redisContext.getApplicationName();
    }

    /**
     * Returns {@code true} when the source bean originates from a
     * {@link org.springframework.data.redis.core.RedisTemplate} bean.
     *
     * @return {@code true} if the source is a {@link org.springframework.data.redis.core.RedisTemplate}
     */
    public boolean isSourceFromRedisTemplate() {
        Boolean sourceFromRedisTemplate = this.sourceFromRedisTemplate;
        if (sourceFromRedisTemplate == null) {
            sourceFromRedisTemplate = redisContext.getRedisTemplateBeanNames().contains(sourceBeanName);
            this.sourceFromRedisTemplate = sourceFromRedisTemplate;
        }
        return sourceFromRedisTemplate;
    }

    /**
     * Returns {@code true} when the source bean originates from a
     * {@link org.springframework.data.redis.connection.RedisConnectionFactory} bean.
     *
     * @return {@code true} if the source is a {@link org.springframework.data.redis.connection.RedisConnectionFactory}
     */
    public boolean isSourceFromRedisConnectionFactory() {
        Boolean sourceFromRedisConnectionFactory = this.sourceFromRedisConnectionFactory;
        if (sourceFromRedisConnectionFactory == null) {
            sourceFromRedisConnectionFactory = redisContext.getRedisConnectionFactoryBeanNames().contains(sourceBeanName);
            this.sourceFromRedisConnectionFactory = sourceFromRedisConnectionFactory;
        }
        return sourceFromRedisConnectionFactory;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RedisMethodContext.class.getSimpleName() + "[", "]")
                .add("target=" + this.target)
                .add("method=" + this.method)
                .add("args=" + arrayToString(this.getArgs()))
                .add("write=" + this.isWriteMethod())
                .add("parameters=" + arrayToString(this.getParameters()))
                .add("redisContext=" + this.redisContext)
                .add("sourceBean=" + this.sourceBean)
                .add("sourceBeanName='" + this.sourceBeanName + "'")
                .add("startTimeNanos=" + this.startTimeNanos)
                .add("durationNanos=" + this.durationNanos)
                .toString();
    }

    /**
     * Stores the given {@link RedisMethodContext} in the current thread's thread-local variable,
     * making it accessible to other components via {@link #get()}.
     *
     * @param redisMethodContext the context to store; should not be {@code null}
     */
    public static void set(RedisMethodContext redisMethodContext) {
        redisMethodContextThreadLocal.set(redisMethodContext);
        logger.trace("{} stores into ThreadLocal", redisMethodContext);
    }

    /**
     * Returns the {@link RedisMethodContext} bound to the current thread, or {@code null} if
     * none has been set.
     *
     * @param <T> the target type
     * @return the current thread's context, or {@code null}
     */
    public static <T> RedisMethodContext<T> get() {
        return (RedisMethodContext<T>) redisMethodContextThreadLocal.get();
    }

    /**
     * Removes the {@link RedisMethodContext} from the current thread, preventing memory leaks
     * in thread-pool environments.
     */
    public static void clear() {
        redisMethodContextThreadLocal.remove();
    }
}

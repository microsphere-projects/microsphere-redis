package io.microsphere.redis.spring.context;

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisCommandEventExposed;

/**
 * {@link RedisModuleInitializer} implementation that conditionally activates the Redis interceptor
 * infrastructure by registering an {@link EnableRedisInterceptor}-annotated configuration class
 * into the application context's bean-definition registry.
 *
 * <p>The initializer is enabled when the property
 * {@code microsphere.redis.interceptor.enabled} resolves to {@code true}.
 * If Redis command event exposure is also enabled, the standard
 * {@code Config} class (with {@code exposeCommandEvent = true}) is registered; otherwise the
 * {@code NoExposingCommandEventConfig} variant is used.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Registered automatically via spring.factories.
 *   // To enable the Redis interceptor at runtime, set in application.properties:
 *   //   microsphere.redis.interceptor.enabled=true
 *   //   microsphere.redis.wrapped-redis-templates=*
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisModuleInitializer
 * @since 1.0.0
 */
public class RedisInterceptorModuleInitializer implements RedisModuleInitializer {

    private static final Logger logger = getLogger(RedisInterceptorModuleInitializer.class);

    @Override
    public boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        ConfigurableEnvironment environment = context.getEnvironment();
        String propertyName = MICROSPHERE_REDIS_INTERCEPTOR_ENABLED_PROPERTY_NAME;
        boolean enabled = environment.getProperty(propertyName, boolean.class, DEFAULT_MICROSPHERE_REDIS_INTERCEPTOR_ENABLED);
        logger.trace("Microsphere Redis Interceptor is '{}'", enabled ? "Enabled" : "Disabled");
        return enabled;
    }

    @Override
    public void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
        ConfigurableEnvironment environment = context.getEnvironment();
        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry, environment);
        Class<?> configClass = isMicrosphereRedisCommandEventExposed(environment) ? Config.class : NoExposingCommandEventConfig.class;
        reader.register(configClass);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @EnableRedisInterceptor(wrapRedisTemplates = DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER)
    private static class Config {
    }

    @EnableRedisInterceptor(wrapRedisTemplates = DEFAULT_WRAP_REDIS_TEMPLATE_PLACEHOLDER, exposeCommandEvent = false)
    private static class NoExposingCommandEventConfig {
    }
}
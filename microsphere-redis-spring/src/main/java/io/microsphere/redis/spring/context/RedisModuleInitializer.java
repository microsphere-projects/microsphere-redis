package io.microsphere.redis.spring.context;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * SPI interface for Microsphere Redis module initializers discovered and invoked by
 * {@link RedisInitializer} during application context initialization.  Implementations
 * are loaded via {@link org.springframework.core.io.support.SpringFactoriesLoader} and
 * sorted by {@link Ordered} precedence before being conditionally applied.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Implement a custom module initializer (registered via spring.factories):
 *   public class MyRedisModuleInitializer implements RedisModuleInitializer {
 *
 *       public boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
 *           return context.getEnvironment().getProperty("my.redis.module.enabled", boolean.class, false);
 *       }
 *
 *       public void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry) {
 *           BeanRegistrar.registerBeanDefinition(registry, MyRedisBeanProcessor.class);
 *       }
 *
 *       public int getOrder() { return Ordered.LOWEST_PRECEDENCE; }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface RedisModuleInitializer extends Ordered {

    /**
     * Checks whether this initializer supports the given application context.
     *
     * @param context  the {@link ConfigurableApplicationContext} being initialized
     * @param registry the {@link BeanDefinitionRegistry} for registering bean definitions
     * @return {@code true} if this initializer should be applied; {@code false} to skip it
     */
    boolean supports(ConfigurableApplicationContext context, BeanDefinitionRegistry registry);

    /**
     * Initializes this module's features by registering bean definitions into the given
     * {@link BeanDefinitionRegistry}.  Called only when {@link #supports} returned {@code true}.
     *
     * @param context  the {@link ConfigurableApplicationContext} being initialized
     * @param registry the {@link BeanDefinitionRegistry} for registering bean definitions
     */
    void initialize(ConfigurableApplicationContext context, BeanDefinitionRegistry registry);
}

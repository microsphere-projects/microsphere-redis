package io.microsphere.redis.spring.event;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;

import java.util.Set;

import static io.microsphere.lang.function.Streams.filterSet;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.startsWith;

/**
 * Spring {@link SmartApplicationListener} that listens for
 * {@link EnvironmentChangeEvent}s (from Spring Cloud) and republishes
 * the changed property names as a {@link RedisConfigurationPropertyChangedEvent} on the application
 * context, filtered to properties whose names start with the prefix
 * {@code microsphere.redis.}.
 *
 * <p>Only registered when Spring Cloud's {@code EnvironmentChangeEvent} class is available on the
 * classpath (checked via {@link #supports(ClassLoader)}).
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Automatically registered by RedisConfigurationBeanDefinitionRegistrar when supported.
 *   // Can be checked at configuration time:
 *   boolean supported = PropagatingRedisConfigurationPropertyChangedEventApplicationListener
 *           .supports(getClass().getClassLoader());
 *
 *   // Listening for the propagated event:
 *   @Component
 *   public class MyListener implements ApplicationListener<RedisConfigurationPropertyChangedEvent> {
 *       public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
 *           System.out.println("Changed Redis properties: " + event.getPropertyNames());
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisConfigurationPropertyChangedEvent
 * @since 1.0.0
 */
public class PropagatingRedisConfigurationPropertyChangedEventApplicationListener implements SmartApplicationListener, BeanClassLoaderAware {

    public static final String ENVIRONMENT_CHANGE_EVENT_CLASS_NAME = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";

    private Class<?> eventType;

    private final ConfigurableApplicationContext context;

    /**
     * Creates a listener that re-publishes Redis-property-change events onto the given context.
     *
     * @param context the application context to which {@link RedisConfigurationPropertyChangedEvent}s
     *                will be published
     */
    public PropagatingRedisConfigurationPropertyChangedEventApplicationListener(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return eventType.isAssignableFrom(this.eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent e) {
        EnvironmentChangeEvent environmentChangeEvent = (EnvironmentChangeEvent) e;
        Set<String> keys = filterSet(environmentChangeEvent.getKeys(), this::isRedisPropertyName);

        RedisConfigurationPropertyChangedEvent event = new RedisConfigurationPropertyChangedEvent(this.context, keys);
        this.context.publishEvent(event);
    }

    boolean isRedisPropertyName(String key) {
        return startsWith(key, MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.eventType = resolveClass(ENVIRONMENT_CHANGE_EVENT_CLASS_NAME, classLoader);
    }

    /**
     * Returns {@code true} when Spring Cloud's {@code EnvironmentChangeEvent} class is available
     * on the given class loader, meaning this listener can be safely registered.
     *
     * @param classLoader the class loader to check
     * @return {@code true} if Spring Cloud is present on the classpath
     */
    public static boolean supports(ClassLoader classLoader) {
        return isPresent(ENVIRONMENT_CHANGE_EVENT_CLASS_NAME, classLoader);
    }
}
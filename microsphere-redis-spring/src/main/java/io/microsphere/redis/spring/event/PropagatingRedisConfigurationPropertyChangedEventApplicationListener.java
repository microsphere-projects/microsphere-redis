package io.microsphere.redis.spring.event;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.SmartApplicationListener;

import java.util.Set;

import static io.microsphere.lang.function.Streams.filterSet;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.ClassLoaderUtils.isPresent;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.StringUtils.startsWith;

/**
 * {@link EnvironmentChangeEvent} {@link ApplicationListener} propagates {@link RedisConfigurationPropertyChangedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisConfigurationPropertyChangedEvent
 * @since 1.0.0
 */
public class PropagatingRedisConfigurationPropertyChangedEventApplicationListener implements SmartApplicationListener, BeanClassLoaderAware {

    public static final String ENVIRONMENT_CHANGE_EVENT_CLASS_NAME = "org.springframework.cloud.context.environment.EnvironmentChangeEvent";

    private Class<?> eventType;

    private final ConfigurableApplicationContext context;

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

    public static boolean supports(ClassLoader classLoader) {
        return isPresent(ENVIRONMENT_CHANGE_EVENT_CLASS_NAME, classLoader);
    }
}
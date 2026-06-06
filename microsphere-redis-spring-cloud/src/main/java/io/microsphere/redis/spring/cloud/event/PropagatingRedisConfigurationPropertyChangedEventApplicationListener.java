package io.microsphere.redis.spring.cloud.event;

import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Set;

import static io.microsphere.lang.function.Streams.filterSet;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.StringUtils.startsWith;

/**
 * Spring {@link ApplicationListener} that listens for {@link EnvironmentChangeEvent}s (from Spring Cloud) and
 * republishes the changed property names as a {@link RedisConfigurationPropertyChangedEvent} on the application
 * context, filtered to properties whose names start with the prefix {@code microsphere.redis.}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisConfigurationPropertyChangedEvent
 * @since 1.0.0
 */
public class PropagatingRedisConfigurationPropertyChangedEventApplicationListener implements ApplicationListener<EnvironmentChangeEvent> {

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
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = filterSet(event.getKeys(), this::isRedisPropertyName);
        this.context.publishEvent(new RedisConfigurationPropertyChangedEvent(this.context, keys));
    }

    boolean isRedisPropertyName(String key) {
        return startsWith(key, MICROSPHERE_REDIS_PROPERTY_NAME_PREFIX);
    }
}
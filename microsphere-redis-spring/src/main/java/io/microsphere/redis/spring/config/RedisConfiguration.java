package io.microsphere.redis.spring.config;

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import io.microsphere.redis.spring.util.RedisUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisUtils.isMicrosphereRedisCommandEventExposed;
import static io.microsphere.redis.spring.util.RedisUtils.isMicrosphereRedisEnabled;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;

/**
 * Redis Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisConfiguration implements ApplicationListener<RedisConfigurationPropertyChangedEvent>, ApplicationContextAware {

    private static final Logger logger = getLogger(RedisConfiguration.class);

    /**
     * {@link RedisConfiguration} Bean Name
     */
    public static final String BEAN_NAME = "microsphere:redisConfiguration";

    protected ConfigurableApplicationContext context;

    protected ConfigurableEnvironment environment;

    protected String applicationName;

    protected volatile boolean enabled;

    @Override
    public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
        logger.trace("onApplicationEvent : {}", event);
        if (event.hasProperty(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME)) {
            setEnabled();
        }
    }

    public void setEnabled() {
        this.enabled = isMicrosphereRedisEnabled(environment);
    }

    public ConfigurableEnvironment getEnvironment() {
        return this.environment;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public boolean isCommandEventExposed() {
        return isMicrosphereRedisCommandEventExposed(this.environment);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = asConfigurableApplicationContext(context);
        this.environment = asConfigurableEnvironment(context.getEnvironment());
        this.applicationName = RedisUtils.getApplicationName(this.environment);
        setEnabled();
    }

    public static RedisConfiguration get(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, RedisConfiguration.class);
    }
}
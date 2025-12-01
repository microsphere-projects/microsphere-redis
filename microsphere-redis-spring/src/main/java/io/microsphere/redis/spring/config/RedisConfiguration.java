package io.microsphere.redis.spring.config;

import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.util.RedisConstants.COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_COMMAND_EVENT_EXPOSED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.ENABLED_PROPERTY_NAME;
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
        if (event.hasProperty(ENABLED_PROPERTY_NAME)) {
            setEnabled();
        }
    }

    public void setEnabled() {
        this.enabled = isEnabled(context);
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
        return isCommandEventExposed(this.context);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = asConfigurableApplicationContext(context);
        this.environment = asConfigurableEnvironment(this.context.getEnvironment());
        this.applicationName = resolveApplicationName(this.environment);
        setEnabled();
    }

    protected String resolveApplicationName(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "default");
        return applicationName;
    }

    public static boolean isEnabled(ApplicationContext context) {
        return getBoolean(context, ENABLED_PROPERTY_NAME, DEFAULT_ENABLED, "Configuration", "enabled");
    }

    public static boolean isCommandEventExposed(ApplicationContext context) {
        return getBoolean(context, COMMAND_EVENT_EXPOSED_PROPERTY_NAME, DEFAULT_COMMAND_EVENT_EXPOSED, "Command Event", "exposed");
    }

    public static boolean getBoolean(ApplicationContext context, String propertyName, boolean defaultValue, String feature, String statusIfTrue) {
        Environment environment = context.getEnvironment();
        Boolean propertyValue = environment.getProperty(propertyName, Boolean.class);
        boolean value = propertyValue == null ? defaultValue : propertyValue.booleanValue();
        logger.trace("Microsphere Redis {} is '{}' in the Spring ApplicationContext[id :'{}' , property name: '{}' , property value: {} , default value: {}",
                feature, (value ? statusIfTrue : "not " + statusIfTrue), context.getId(), propertyName, propertyValue, defaultValue);
        return value;
    }

    public static RedisConfiguration get(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, RedisConfiguration.class);
    }
}
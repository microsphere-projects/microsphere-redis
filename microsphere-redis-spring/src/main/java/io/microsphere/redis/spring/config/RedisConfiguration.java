package io.microsphere.redis.spring.config;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.context.RedisContext.findRestTemplateBeanNames;
import static io.microsphere.redis.spring.util.RedisConstants.ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED;
import static io.microsphere.redis.spring.util.RedisConstants.DEFAULT_MICROSPHERE_REDIS_ENABLED;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.spring.util.RedisConstants.WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimAllWhitespace;

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
        if (event.hasProperty(MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME)) {
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
        this.applicationName = getApplicationName(this.environment);
        setEnabled();
    }

    public static String getApplicationName(Environment environment) {
        String applicationName = environment.getProperty("spring.application.name", "default");
        return applicationName;
    }

    public static boolean isEnabled(ApplicationContext context) {
        return getBoolean(context, MICROSPHERE_REDIS_ENABLED_PROPERTY_NAME, DEFAULT_MICROSPHERE_REDIS_ENABLED, "Configuration", "enabled");
    }

    public static boolean isCommandEventExposed(ApplicationContext context) {
        return getBoolean(context, MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED_PROPERTY_NAME, DEFAULT_MICROSPHERE_REDIS_COMMAND_EVENT_EXPOSED, "Command Event", "exposed");
    }

    @Nonnull
    @Immutable
    public static Set<String> getWrappedRedisTemplateBeanNames(ConfigurableListableBeanFactory beanFactory,
                                                               Environment environment, String[] wrapRedisTemplates) {
        if (isEmpty(wrapRedisTemplates)) {
            return resolveWrappedRedisTemplateBeanNames(beanFactory, environment);
        } else {
            return resolveWrappedRedisTemplateBeanNames(environment, wrapRedisTemplates);
        }
    }

    /**
     * Resolve the wrapped {@link RedisTemplate} Bean Names
     *
     * @param environment        {@link Environment}
     * @param wrapRedisTemplates The wrapped {@link RedisTemplate} Bean Names
     * @return non-null
     */
    @Nonnull
    static Set<String> resolveWrappedRedisTemplateBeanNames(Environment environment, String[] wrapRedisTemplates) {
        Set<String> wrappedRedisTemplateBeanNames = newLinkedHashSet(wrapRedisTemplates.length);
        for (String wrapRedisTemplate : wrapRedisTemplates) {
            String wrappedRedisTemplateBeanName = environment.resolveRequiredPlaceholders(wrapRedisTemplate);
            Set<String> beanNames = commaDelimitedListToSet(wrappedRedisTemplateBeanName);
            for (String beanName : beanNames) {
                String name = trimAllWhitespace(beanName);
                if (hasText(name)) {
                    wrappedRedisTemplateBeanNames.add(name);
                }
            }
        }
        return unmodifiableSet(wrappedRedisTemplateBeanNames);
    }

    /**
     * Resolve the wrapped {@link RedisTemplate} Bean Name list, the default value is from {@link Collections#emptySet()}
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @return If no configuration is found, {@link Collections#emptySet()} is returned
     */
    @Nonnull
    static Set<String> resolveWrappedRedisTemplateBeanNames(ConfigurableListableBeanFactory beanFactory, Environment environment) {
        Set<String> wrappedRedisTemplateBeanNames = environment.getProperty(WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME, Set.class);
        if (wrappedRedisTemplateBeanNames == null) {
            return emptySet();
        } else if (ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES.equals(wrappedRedisTemplateBeanNames)) {
            return findRestTemplateBeanNames(beanFactory);
        } else {
            String[] beanNames = wrappedRedisTemplateBeanNames.toArray(EMPTY_STRING_ARRAY);
            return resolveWrappedRedisTemplateBeanNames(environment, beanNames);
        }
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
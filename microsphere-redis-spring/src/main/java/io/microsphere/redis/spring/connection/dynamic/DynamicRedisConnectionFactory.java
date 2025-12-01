package io.microsphere.redis.spring.connection.dynamic;

import io.microsphere.logging.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConnection;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static io.microsphere.util.StringUtils.isBlank;
import static java.util.Collections.unmodifiableMap;

/**
 * Dynamic {@link RedisConnectionFactory} Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class DynamicRedisConnectionFactory implements RedisConnectionFactory, SmartInitializingSingleton,
        ApplicationContextAware, BeanNameAware {

    private static final Logger logger = getLogger(DynamicRedisConnectionFactory.class);

    /**
     * Default {@link RedisConnectionFactory} Bean name
     */
    public static final String DEFAULT_REDIS_CONNECTION_FACTORY_BEAN_NAME = "redisConnectionFactory";

    private static final ThreadLocal<String> beanNameHolder = new ThreadLocal<>();

    private String beanName;

    private ApplicationContext context;

    private Map<String, RedisConnectionFactory> redisConnectionFactories;

    private String defaultRedisConnectionFactoryBeanName = DEFAULT_REDIS_CONNECTION_FACTORY_BEAN_NAME;

    private RedisConnectionFactory defaultRedisConnectionFactory;

    @Override
    public RedisConnection getConnection() {
        return determineTargetRedisConnectionFactory().getConnection();
    }

    @Override
    public RedisClusterConnection getClusterConnection() {
        return determineTargetRedisConnectionFactory().getClusterConnection();
    }

    @Override
    public boolean getConvertPipelineAndTxResults() {
        return determineTargetRedisConnectionFactory().getConvertPipelineAndTxResults();
    }

    @Override
    public RedisSentinelConnection getSentinelConnection() {
        return determineTargetRedisConnectionFactory().getSentinelConnection();
    }

    @Override
    public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
        return determineTargetRedisConnectionFactory().translateExceptionIfPossible(ex);
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        initialize();
    }

    public void setDefaultRedisConnectionFactoryBeanName(String defaultRedisConnectionFactoryBeanName) {
        this.defaultRedisConnectionFactoryBeanName = defaultRedisConnectionFactoryBeanName;
    }

    public String getDefaultRedisConnectionFactoryBeanName() {
        return defaultRedisConnectionFactoryBeanName;
    }

    protected void initialize() {
        this.redisConnectionFactories = getRedisConnectionFactories();
        this.defaultRedisConnectionFactory = getDefaultRedisConnectionFactory();
    }

    @NonNull
    protected RedisConnectionFactory determineTargetRedisConnectionFactory() {
        String targetBeanName = getTargetBeanName();
        if (isBlank(targetBeanName)) {
            RedisConnectionFactory defaultRedisConnectionFactory = getDefaultRedisConnectionFactory();
            logger.trace("Current target Bean Name is not set or is executed in a multi-threaded environment, using the default RedisConnectionFactory Bean[name: '{}']", defaultRedisConnectionFactoryBeanName);
            return defaultRedisConnectionFactory;
        }
        logger.trace("Start toggle destination RedisConnectionFactory Bean[name: '{}']", targetBeanName);
        RedisConnectionFactory targetRedisConnectionFactory = getRedisConnectionFactory(targetBeanName);
        logger.trace("RedisConnectionFactory Bean[name: '{}']", targetBeanName);
        return targetRedisConnectionFactory;
    }

    protected RedisConnectionFactory getRedisConnectionFactory(String beanName) {
        Map<String, RedisConnectionFactory> redisConnectionFactories = getRedisConnectionFactories();
        RedisConnectionFactory redisConnectionFactory = redisConnectionFactories.get(beanName);
        assertNotNull(redisConnectionFactory, () -> format("RedisConnectionFactory Bean[name : '{}'] is not existed", beanName));
        return redisConnectionFactory;
    }

    protected RedisConnectionFactory getDefaultRedisConnectionFactory() {
        if (defaultRedisConnectionFactory == null) {
            defaultRedisConnectionFactory = resolveDefaultRedisConnectionFactory();
        }
        return defaultRedisConnectionFactory;
    }

    protected Map<String, RedisConnectionFactory> getRedisConnectionFactories() {
        if (redisConnectionFactories == null) {
            redisConnectionFactories = resolveRedisConnectionFactories();
        }
        return redisConnectionFactories;
    }

    private RedisConnectionFactory resolveDefaultRedisConnectionFactory() {
        String beanName = defaultRedisConnectionFactoryBeanName;
        assertNotEmpty(beanName, () -> "The default RedisConnectionFactory Bean Name cannot be left blank");
        return getRedisConnectionFactory(beanName);
    }

    private Map<String, RedisConnectionFactory> resolveRedisConnectionFactories() {
        Map<String, RedisConnectionFactory> redisConnectionFactories = new HashMap<>(context.getBeansOfType(RedisConnectionFactory.class));
        // Remove the current Bean
        redisConnectionFactories.remove(beanName);
        assertNotEmpty(redisConnectionFactories, () -> "RedisConnectionFactory Beans do not exist");
        return unmodifiableMap(redisConnectionFactories);
    }

    /**
     * Switch the target {@link RedisConnectionFactory}
     * Please read the following tips carefully:
     * <ul>
     *     <li>When this method is called, actively call the {@link #clearTarget()} method to clear the tag state,
     *     especially in non-Web request threading scenarios, to avoid memory leakage sharing (although the framework does clear it at the end of the HTTP request).</li>
     *     <li>When the method invocation, if in a custom thread pool scene, please pay attention to copy
     *     the current < code > ThreadLocal cache redisConnectionFactoryBeanName < code > to the target thread</li>
     *     <li>When a < code > redisConnectionFactoryBeanName < code > points to Bean in the current application context does not exist,
     *     will throw an exception</li>
     * </ul>
     *
     * @param redisConnectionFactoryBeanName Target {@link RedisConnectionFactory} Bean name
     */
    public static void switchTarget(String redisConnectionFactoryBeanName) {
        beanNameHolder.set(redisConnectionFactoryBeanName);
        logger.trace("Switch target RedisConnectionFactory Bean Name: '{}'", redisConnectionFactoryBeanName);
    }

    public static void clearTarget() {
        String targetBeanName = getTargetBeanName();
        beanNameHolder.remove();
        logger.trace("Clear target RedisConnectionFactory Bean Name: '{}'", targetBeanName);
    }

    protected static String getTargetBeanName() {
        return beanNameHolder.get();
    }
}

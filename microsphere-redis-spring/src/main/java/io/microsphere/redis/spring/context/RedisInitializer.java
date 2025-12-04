package io.microsphere.redis.spring.context;

import io.microsphere.logging.Logger;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.init;
import static io.microsphere.redis.spring.util.RedisSpringUtils.isMicrosphereRedisEnabled;
import static io.microsphere.spring.core.env.PropertySourcesUtils.containsBootstrapPropertySource;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactories;

/**
 * Redis Initializer
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    static {
        init();
    }

    private static final Logger logger = getLogger(RedisInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        if (supports(context)) {
            ClassLoader classLoader = context.getClassLoader();
            // Load RedisModuleInitializer list
            List<RedisModuleInitializer> redisModuleInitializers = loadFactories(RedisModuleInitializer.class, classLoader);
            // Sort RedisModuleInitializer list
            sort(redisModuleInitializers);
            ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
            BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
            for (RedisModuleInitializer redisModuleInitializer : redisModuleInitializers) {
                boolean supports = redisModuleInitializer.supports(context, registry);
                logger.trace("ApplicationContext[id : '{}'] {} support to initialize RedisModuleInitializer[class : {} , order : {}]",
                        context.getId(), supports ? "does" : "does not", redisModuleInitializer.getClass(), redisModuleInitializer.getOrder());
                if (supports) {
                    redisModuleInitializer.initialize(context, registry);
                }
            }
        }
    }

    private boolean supports(ConfigurableApplicationContext context) {
        ConfigurableEnvironment environment = context.getEnvironment();
        if (!isMicrosphereRedisEnabled(environment)) {
            return false;
        }
        if (isBootstrapContext(environment)) {
            logger.warn("The application context [id: {}, class: {}] is a BootstrapContext", context.getId(), context.getClass());
            return false;
        }
        return true;
    }

    private boolean isBootstrapContext(ConfigurableEnvironment environment) {
        return containsBootstrapPropertySource(environment);
    }
}
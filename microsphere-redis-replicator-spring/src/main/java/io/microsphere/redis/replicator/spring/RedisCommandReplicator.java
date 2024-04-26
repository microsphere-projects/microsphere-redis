package io.microsphere.redis.replicator.spring;

import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.replicator.spring.event.handler.RedisCommandReplicatedEventHandler;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import io.microsphere.spring.util.SpringFactoriesLoaderUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.CONSUMER_EVENT_HANDLE_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.event.handler.RedisCommandReplicatedEventHandler.EventHandleName.REFLECT;
import static io.microsphere.redis.spring.beans.Wrapper.tryUnwrap;
import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.getRedisCommandBindingFunction;


/**
 * Redis Command Replicator
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RedisCommandReplicator implements ApplicationListener<RedisCommandReplicatedEvent>,
        ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(RedisCommandReplicator.class);

    public static final String BEAN_NAME = "redisCommandReplicator";

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCommandReplicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = tryUnwrap(redisConnectionFactory, RedisConnectionFactory.class);
    }


    @Override
    public void onApplicationEvent(RedisCommandReplicatedEvent event) {
        try {
            handleRedisCommandEvent(event);
        } catch (Throwable e) {
            logger.error("[Redis-Replicator-Event] Failed to process Redis command event [{}]", event, e);
        }
    }

    private void handleRedisCommandEvent(RedisCommandReplicatedEvent event) throws Throwable {
        RedisCommandEvent redisCommandEvent = event.getSourceEvent();
        Method method = findWriteCommandMethod(redisCommandEvent);
        if (method != null) {
            String interfaceNme = redisCommandEvent.getInterfaceName();
            RedisConnection redisConnection = getRedisConnection();
            Object[] args = redisCommandEvent.getArgs();
            Function<RedisConnection, Object> bindingFunction = getRedisCommandBindingFunction(interfaceNme);
            Object redisCommandObject = bindingFunction.apply(redisConnection);
            // Native method implementation
            // ReflectionUtils.invokeMethod(method, redisCommandObject, args);
            eventHandler.handleEvent(method, redisCommandObject, args);
        }
    }

    private RedisConnection getRedisConnection() {
        return redisConnectionFactory.getConnection();
    }

    RedisCommandReplicatedEventHandler eventHandler;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        String eventHandleType = applicationContext.getEnvironment()
                .getProperty(CONSUMER_EVENT_HANDLE_PROPERTY_NAME, String.class, REFLECT.name());
        eventHandler = SpringFactoriesLoaderUtils.loadFactories(applicationContext, RedisCommandReplicatedEventHandler.class)
                .stream()
                .filter(eventHandle -> eventHandle.name().equals(eventHandleType))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No eventHandle found"));
    }
}

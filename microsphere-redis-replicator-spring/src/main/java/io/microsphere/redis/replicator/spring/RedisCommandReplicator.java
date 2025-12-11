package io.microsphere.redis.replicator.spring;

import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.findWriteCommandMethod;
import static io.microsphere.redis.spring.metadata.SpringRedisMetadataRepository.getRedisCommandBindingFunction;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getRawRedisConnection;
import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static org.springframework.util.ReflectionUtils.invokeMethod;


/**
 * Redis Command Replicator
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisCommandReplicatedEvent
 * @since 1.0.0
 */
public class RedisCommandReplicator implements ApplicationListener<RedisCommandReplicatedEvent> {

    private static final Logger logger = getLogger(RedisCommandReplicator.class);

    public static final String BEAN_NAME = "microsphere:redisCommandReplicator";

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisCommandReplicator(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
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
        String interfaceNme = redisCommandEvent.getInterfaceName();
        RedisConnection redisConnection = getRedisConnection();
        Object[] args = redisCommandEvent.getArgs();
        Function<RedisConnection, Object> bindingFunction = getRedisCommandBindingFunction(interfaceNme);
        Object redisCommandObject = bindingFunction.apply(redisConnection);
        // TODO: Native method implementation
        trySetAccessible(method);
        invokeMethod(method, redisCommandObject, args);
    }

    RedisConnection getRedisConnection() {
        RedisConnection redisConnection = this.redisConnectionFactory.getConnection();
        return getRawRedisConnection(redisConnection);
    }
}
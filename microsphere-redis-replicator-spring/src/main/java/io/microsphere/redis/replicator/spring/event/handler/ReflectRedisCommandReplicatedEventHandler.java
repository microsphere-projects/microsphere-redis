package io.microsphere.redis.replicator.spring.event.handler;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static io.microsphere.redis.replicator.spring.event.handler.RedisCommandReplicatedEventHandler.EventHandleName.REFLECT;

public class ReflectRedisCommandReplicatedEventHandler implements RedisCommandReplicatedEventHandler {

    @Override
    public void handleEvent(Method method, Object redisCommandObject, Object... args) throws Throwable {

        ReflectionUtils.invokeMethod(method, redisCommandObject, args);
    }

    @Override
    public String name() {
        return REFLECT.name();
    }
}

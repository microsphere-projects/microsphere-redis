package io.microsphere.redis.replicator.spring.event.handler;

import java.lang.reflect.Method;

public interface RedisCommandReplicatedEventHandler {
    void handleEvent(Method method, Object redisCommandObject, Object... args) throws Throwable;

    String name();

    enum EventHandleName{
        REFLECT,
        METHOD_HANDLE;
    }
}

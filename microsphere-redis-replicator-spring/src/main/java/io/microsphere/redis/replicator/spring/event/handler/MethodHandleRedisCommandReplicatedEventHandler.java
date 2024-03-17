package io.microsphere.redis.replicator.spring.event.handler;

import io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.transferMethodToMethodSignature;

public class MethodHandleRedisCommandReplicatedEventHandler implements RedisCommandReplicatedEventHandler {

    @Override
    public void handleEvent(Method method, Object redisCommandObject, Object... args) throws Throwable {
        String methodSignature = transferMethodToMethodSignature(method);
        int length = args.length;
        Object[] arguments = new Object[1 + args.length];
        arguments[0] = redisCommandObject;
        for (int i = 0; i < length; i++) {
            arguments[i + 1] = args[i];
        }
        RedisCommandsMethodHandles.getMethodHandleBy(methodSignature).invokeWithArguments(arguments);
    }

    @Override
    public String name() {
        return EventHandleName.METHOD_HANDLE.name();
    }
}

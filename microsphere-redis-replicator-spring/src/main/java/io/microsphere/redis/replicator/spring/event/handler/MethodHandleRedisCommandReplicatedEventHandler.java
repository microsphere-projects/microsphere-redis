package io.microsphere.redis.replicator.spring.event.handler;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.getMethodHandleBy;

public class MethodHandleRedisCommandReplicatedEventHandler implements RedisCommandReplicatedEventHandler {

    @Override
    public void handleEvent(Method method, Object redisCommandObject, Object... args) throws Throwable {
        int length = args.length;
        Object[] arguments = new Object[1 + args.length];
        arguments[0] = redisCommandObject;
        System.arraycopy(args, 0, arguments, 1, length);
        getMethodHandleBy(method).invokeWithArguments(arguments);
    }

    @Override
    public String name() {
        return EventHandleName.METHOD_HANDLE.name();
    }
}

package io.microsphere.redis.spring.metadata;

import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.getAllRedisCommandMethods;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.initRedisCommandMethodHandle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class RedisCommandsMethodHandlesTest {

    private Class<RedisCommands> redisCommandClass = RedisCommands.class;

    static int methodCount = RedisCommands.class.getMethods().length - 1;

    @Test
    void shouldGetAllMethodInfoFromRedisCommand() {
        List<MethodInfo> list = getAllRedisCommandMethods();

        assertThat(list)
                .isNotNull()
                .hasSize(methodCount);
    }

    @Disabled
    @Test
    void shouldGetMethodHandleFroMethodInfo() {
        Map<String, MethodHandle> map = initRedisCommandMethodHandle();
        assertThat(map)
                .isNotNull()
                .hasSize(methodCount);
        assertThatNoException().isThrownBy(RedisCommandsMethodHandles::initRedisCommandMethodHandle);
    }
}

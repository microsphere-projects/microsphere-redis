package io.microsphere.redis.spring.metadata;

import org.jboss.jandex.MethodInfo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;

import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.generateMethodHandle;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.getAllRedisCommandMethods;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.initRedisCommandMethodHandle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class RedisCommandsMethodHandlesTest {

    static int methodCount = RedisCommands.class.getMethods().length - 1;

    @Test
    void shouldGetAllMethodInfoFromRedisCommand() {
        List<MethodInfo> list = getAllRedisCommandMethods();

        assertThat(list)
                .isNotNull()
                .hasSize(methodCount);
    }

    @Test
    void shouldGetMethodHandleMapFromMethodInfo() {
        try (MockedStatic<RedisCommandsMethodHandles> mockStatic = mockStatic(RedisCommandsMethodHandles.class)) {
            mockStatic.when(RedisCommandsMethodHandles::getAllRedisCommandMethods).thenCallRealMethod();
            mockStatic.when(RedisCommandsMethodHandles::initRedisCommandMethodHandle).thenCallRealMethod();

            MethodHandle mockMethodHandle = mock(MethodHandle.class);
            mockStatic.when(() -> generateMethodHandle(any(MethodInfo.class))).thenReturn(mockMethodHandle);

            Map<String, MethodHandle> map = initRedisCommandMethodHandle();
            assertThat(map)
                    .isNotNull()
                    .hasSize(methodCount);
        }
    }

    @Disabled
    @Test
    void shouldNewMethodHandleInstanceByMethodInfo() {
        MethodInfo methodInfo = mock(MethodInfo.class);
        MethodHandle methodHandle = generateMethodHandle(methodInfo);
        assertThat(methodHandle)
                .isNotNull();
    }
}

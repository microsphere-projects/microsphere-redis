package io.microsphere.redis.spring.metadata;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.PrimitiveType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisStringCommands;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.findMethodHandle;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.getAllRedisCommandMethods;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.getClassBy;
import static io.microsphere.redis.spring.metadata.RedisCommandsMethodHandles.initRedisCommandMethodHandle;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
            mockStatic.when(() -> findMethodHandle(any(MethodInfo.class))).thenReturn(mockMethodHandle);

            Map<String, MethodHandle> map = initRedisCommandMethodHandle();
            assertThat(map)
                    .isNotNull()
                    .hasSize(methodCount);
        }
    }

    @Disabled
    @Test
    void shouldNewMethodHandleInstanceByMethodInfo() {
        MethodInfo methodInfo = getMethodInfo();

        MethodHandle methodHandle = findMethodHandle(methodInfo);
        assertThat(methodHandle)
                .isNotNull();
    }

    @ParameterizedTest(name = "test: {0}")
    @MethodSource
    void shouldLoadPrimitiveClass(PrimitiveType primitiveType, Class<?> expected) {
        Class<?> klass = getClassBy(primitiveType.asPrimitiveType());
        assertThat(klass).isEqualTo(expected);
    }

    static Stream<Arguments> shouldLoadPrimitiveClass() {
        return Stream.of(
                arguments(named("boolean", PrimitiveType.BOOLEAN), boolean.class),
                arguments(named("byte", PrimitiveType.BYTE), byte.class),
                arguments(named("short", PrimitiveType.SHORT), short.class),
                arguments(named("int", PrimitiveType.INT), int.class),
                arguments(named("long", PrimitiveType.LONG), long.class),
                arguments(named("float", PrimitiveType.FLOAT), float.class),
                arguments(named("double", PrimitiveType.DOUBLE), double.class),
                arguments(named("char", PrimitiveType.CHAR), char.class)
        );
    }

    @Test
    void shouldLoadArrayClass() {

    }

    private static MethodInfo getMethodInfo() {
        try {
            Index index = Index.of(RedisStringCommands.class);
            ClassInfo classInfo = index.getClassByName(RedisStringCommands.class);
            Optional<MethodInfo> setMethod = classInfo.methods().stream().filter(methodInfo -> methodInfo.name().equals("set")).findFirst();
            return setMethod.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

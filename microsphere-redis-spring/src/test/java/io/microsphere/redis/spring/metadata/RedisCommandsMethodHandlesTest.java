package io.microsphere.redis.spring.metadata;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.VoidType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;

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
import static org.jboss.jandex.ArrayType.builder;
import static org.jboss.jandex.DotName.createSimple;
import static org.jboss.jandex.Type.Kind.CLASS;
import static org.jboss.jandex.Type.create;
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

    @Test
    void shouldNewMethodHandleInstanceByMethodInfo() {
        MethodInfo methodInfo = getMethodInfo();

        MethodHandle methodHandle = findMethodHandle(methodInfo);
        assertThat(methodHandle)
                .isNotNull();
    }

    @ParameterizedTest(name = "test: {0}")
    @MethodSource
    void shouldGetClassWhenTypeIsPrimitiveClass(PrimitiveType primitiveType, Class<?> expected) {
        Class<?> klass = getClassBy(primitiveType);
        assertThat(klass).isEqualTo(expected);
    }

    static Stream<Arguments> shouldGetClassWhenTypeIsPrimitiveClass() {
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

    @ParameterizedTest(name = "test: {0}")
    @MethodSource
    void shouldGetClassWhenTypeIsArrayClass(ArrayType arrayType, Class<?> expected) {
        Class<?> klass = getClassBy(arrayType);
        assertThat(klass).isEqualTo(expected);
    }

    static Stream<Arguments> shouldGetClassWhenTypeIsArrayClass() {
        return Stream.of(
                arguments(named("byte[]", builder(PrimitiveType.BYTE, 1).build()), byte[].class),
                arguments(named("byte[][]", builder(PrimitiveType.BYTE, 2).build()), byte[][].class),
                arguments(named("int[]", builder(PrimitiveType.INT, 1).build()), int[].class),
                arguments(named("String[]", builder(create(createSimple(String.class), CLASS), 1).build()), String[].class),
                arguments(named("RecordId[]", builder(create(createSimple(RecordId.class), CLASS), 1).build()), RecordId[].class),
                arguments(named("StreamOffset[]", builder(create(createSimple(StreamOffset.class), CLASS), 1).build()), StreamOffset[].class)
        );
    }

    @Test
    void shouldGetVoidClass() {
        Class<?> klass = getClassBy(VoidType.VOID);
        assertThat(klass).isEqualTo(void.class);
    }

    @Test
    void shouldGetClassWhenTypeIsParameterizedType() {
        ParameterizedType parameterizedType = ParameterizedType.builder(List.class).addArgument(ClassType.create(String.class)).build();
        Class<?> klass = getClassBy(parameterizedType);
        assertThat(klass).isEqualTo(List.class);
    }

    @Test
    void shouldGetClassWhenTypeIsClassType() {
        ClassType classType = ClassType.create(Object.class);
        Class<?> klass = getClassBy(classType);
        assertThat(klass).isEqualTo(Object.class);
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

package io.microsphere.redis.spring.metadata;

import io.microsphere.redis.spring.metadata.exception.MethodHandleNotFoundException;
import io.microsphere.util.ClassLoaderUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.VoidType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisCommands;
import org.springframework.data.redis.connection.RedisConnectionCommands;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisHashCommands;
import org.springframework.data.redis.connection.RedisHyperLogLogCommands;
import org.springframework.data.redis.connection.RedisKeyCommands;
import org.springframework.data.redis.connection.RedisListCommands;
import org.springframework.data.redis.connection.RedisPubSubCommands;
import org.springframework.data.redis.connection.RedisScriptingCommands;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.connection.RedisSetCommands;
import org.springframework.data.redis.connection.RedisStreamCommands;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.RedisTxCommands;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.redisCommandMethodsCache;
import static java.util.stream.Collectors.toMap;

public class RedisCommandsMethodHandles {

    private static final Logger logger = LoggerFactory.getLogger(RedisCommandsMethodHandles.class);

    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    private static final ClassLoader CURRENT_CLASS_LOADER = RedisCommandsMethodHandles.class.getClassLoader();

    private static final List<Class<?>> TARGET_CLASSES;
    private static final Index REDIS_COMMANDS_INDEX;
    private static final Map<String, MethodHandle> METHOD_HANDLE_MAP;
    private static final Map<Method, MethodInfo> METHOD_METHOD_INFO_MAP;
    private static final Map<Method, MethodHandle> METHOD_METHOD_HANDLE_MAP;

    private RedisCommandsMethodHandles() {
    }

    /**
     * find MethodHandle from METHOD_HANDLE_MAP
     *
     * @param methodSignature {@link MethodInfo#toString()}
     * @return a MethodHandle
     * @throws MethodHandleNotFoundException
     */
    public static MethodHandle getMethodHandleBy(String methodSignature) throws MethodHandleNotFoundException {
        MethodHandle methodHandle = METHOD_HANDLE_MAP.get(methodSignature);
        if (Objects.isNull(methodHandle)) {
            logger.error("can't find MethodHandle from RedisCommands methodSignature:{}", methodSignature);
            throw new MethodHandleNotFoundException("can't find MethodHandle from RedisCommands", methodSignature);
        }
        return methodHandle;
    }

    public static MethodHandle getMethodHandleBy(Method method) throws MethodHandleNotFoundException {
        MethodHandle methodHandle = METHOD_METHOD_HANDLE_MAP.get(method);
        if (Objects.isNull(methodHandle)) {
            logger.error("can't find MethodHandle from RedisCommands methodSignature:{}", method.getName());
            throw new MethodHandleNotFoundException("can't find MethodHandle from RedisCommands", method.toString());
        }
        return methodHandle;
    }

    /**
     * find MethodInfo from METHOD_MAP
     *
     * @param method
     * @return {@link MethodInfo#toString()}
     */
    public static String transferMethodToMethodSignature(Method method) {
        MethodInfo methodInfo = METHOD_METHOD_INFO_MAP.get(method);
        if (Objects.isNull(methodInfo)) {
            throw new IllegalArgumentException();
        }
        return methodInfo.toString();
    }

    /**
     * get all public methods from {@link RedisCommands} <br />
     * exclude {@link RedisCommands#execute} <br />
     * exclude private lambda method from {@link RedisStreamCommands}
     * <li>lambda$xDel$1</li>
     * <li>lambda$xAck$0</li>
     *
     * @return List of  RedisCommands all MethodInfo(include super interface)
     */
    static List<MethodInfo> getAllRedisCommandMethods() {
        return REDIS_COMMANDS_INDEX.getClassByName(RedisCommands.class)
                .interfaceNames()
                .stream()
                .map(REDIS_COMMANDS_INDEX::getClassByName)
                .flatMap(classInfo -> classInfo.methods().stream())
                .filter(methodInfo -> Modifier.isPublic(methodInfo.flags()))
                .collect(Collectors.toList());
    }

    static Map<String, MethodHandle> initRedisCommandMethodHandle() {
        return getAllRedisCommandMethods()
                .stream()
                .map(methodInfo -> new MethodRecord(methodInfo.toString(), findMethodHandleBy(methodInfo)))
                .collect(toMap(MethodRecord::methodSignature, MethodRecord::methodHandle));
    }

    static MethodHandle findMethodHandleBy(MethodInfo methodInfo) {
        Class<?> klass = getClassBy(ClassType.create(methodInfo.declaringClass().name()));

        String methodName = methodInfo.name();

        MethodType methodType = getMethodType(methodInfo);
        try {
            return RedisCommandsMethodHandles.PUBLIC_LOOKUP.findVirtual(klass, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            logger.error("Error occurred when find MethodHandle.\n methodInfo:{}", methodInfo, e);
            throw new RuntimeException(e);
        }
    }

    private static MethodType getMethodType(MethodInfo methodInfo) {
        Class<?> returnTypeKlass = getClassBy(methodInfo.returnType());

        MethodParameterInfo[] array = methodInfo.parameters().toArray(new MethodParameterInfo[]{});
        Class<?>[] parameterKlass = new Class<?>[array.length];
        for (int i = 0; i < array.length; i++) {
            parameterKlass[i] = getClassBy(array[i].type());
        }

        return MethodType.methodType(returnTypeKlass, parameterKlass);
    }

    static Class<?> getClassBy(Type type) {
        if (type instanceof VoidType) {
            return void.class;
        }

        if (type instanceof PrimitiveType) {
            return TypeHelper.PRIMITIVE_TYPE_CLASS_TABLE.get(type.asPrimitiveType().primitive());
        }

        if (type instanceof ArrayType) {
            ArrayType arrayType = type.asArrayType();
            // NOTE: arrayType.elementType().name()
            // example java.lang.String
            // when use jdk21 local() value is "String" prefix() is "java.lang"
            // when use jdk8 local() value is "java.lang.String" prefix() is null
            String local = arrayType.elementType().name().local();
            String elementType;
            if (local.lastIndexOf(".") != -1) {
                elementType = local.substring(local.lastIndexOf(".") + 1);
            } else {
                elementType = local;
            }
            // NOTE: use String.repeat() when use jdk11+ and remove Apache commons lang3 StringUtils dependency
            String repeat = StringUtils.repeat("[]", arrayType.dimensions());
            Class<?> klass = TypeHelper.ARRAY_TYPE_CLASS_TABLE.get(elementType + repeat);

            if (Objects.isNull(klass)) {
                throw new RuntimeException("need to add Class");
            }
            return klass;
        }

        return ClassLoaderUtils.loadClass(type.name().toString(), CURRENT_CLASS_LOADER);
    }

    static Map<Method, MethodInfo> initRedisCommandMethodInfo() {
        return redisCommandMethodsCache.values()
                .stream()
                .collect(toMap(
                        Function.identity(),
                        method -> REDIS_COMMANDS_INDEX.getClassByName(method.getDeclaringClass())
                                .method(method.getName(), getParameterTypes(method.getParameterTypes()))
                ));
    }

    private static Type[] getParameterTypes(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(parameterType -> {
                    if (parameterType.isArray()) {
                        return Type.create(DotName.createSimple(parameterType), Type.Kind.ARRAY);
                    } else {
                        return Type.create(DotName.createSimple(parameterType), Type.Kind.CLASS);
                    }
                }).toArray(Type[]::new);
    }

    static {
        // NOTE: use List.of() to simplify the initial logic
        TARGET_CLASSES = new ArrayList<>();
        TARGET_CLASSES.add(RedisCommands.class);
        TARGET_CLASSES.add(RedisKeyCommands.class);
        TARGET_CLASSES.add(RedisStringCommands.class);
        TARGET_CLASSES.add(RedisListCommands.class);
        TARGET_CLASSES.add(RedisSetCommands.class);
        TARGET_CLASSES.add(RedisZSetCommands.class);
        TARGET_CLASSES.add(RedisHashCommands.class);
        TARGET_CLASSES.add(RedisTxCommands.class);
        TARGET_CLASSES.add(RedisPubSubCommands.class);
        TARGET_CLASSES.add(RedisConnectionCommands.class);
        TARGET_CLASSES.add(RedisServerCommands.class);
        TARGET_CLASSES.add(RedisStreamCommands.class);
        TARGET_CLASSES.add(RedisScriptingCommands.class);
        TARGET_CLASSES.add(RedisGeoCommands.class);
        TARGET_CLASSES.add(RedisHyperLogLogCommands.class);

        try {
            REDIS_COMMANDS_INDEX = Index.of(TARGET_CLASSES);
        } catch (IOException e) {
            logger.error("Index RedisCommands Error", e);
            throw new RuntimeException(e);
        }
        METHOD_HANDLE_MAP = initRedisCommandMethodHandle();
        METHOD_METHOD_INFO_MAP = initRedisCommandMethodInfo();
        METHOD_METHOD_HANDLE_MAP = METHOD_METHOD_INFO_MAP.keySet()
                .stream()
                .collect(toMap(Function.identity(), key -> METHOD_HANDLE_MAP.get(METHOD_METHOD_INFO_MAP.get(key).toString())));
    }

    /**
     * NOTE: Use Record Class when use jdk 17+
     */
    static class MethodRecord {
        String methodSignature;
        MethodHandle methodHandle;

        public MethodRecord(String methodSignature, MethodHandle methodHandle) {
            this.methodSignature = methodSignature;
            this.methodHandle = methodHandle;
        }

        public String methodSignature() {
            return methodSignature;
        }

        public MethodHandle methodHandle() {
            return methodHandle;
        }
    }

    static class TypeHelper {
        private static final EnumMap<PrimitiveType.Primitive, Class<?>> PRIMITIVE_TYPE_CLASS_TABLE = new EnumMap<>(PrimitiveType.Primitive.class);
        private static final Map<String, Class<?>> ARRAY_TYPE_CLASS_TABLE = new HashMap<>();

        private TypeHelper() {
        }

        static {
            // NOTE: use new EnumMap(Map.of()) to simplify the code when use jdk11+
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.BOOLEAN, boolean.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.BYTE, byte.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.SHORT, short.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.INT, int.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.LONG, long.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.FLOAT, float.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.DOUBLE, double.class);
            PRIMITIVE_TYPE_CLASS_TABLE.put(PrimitiveType.Primitive.CHAR, char.class);

            ARRAY_TYPE_CLASS_TABLE.put("byte[]", byte[].class);
            ARRAY_TYPE_CLASS_TABLE.put("byte[][]", byte[][].class);
            ARRAY_TYPE_CLASS_TABLE.put("int[]", int[].class);
            ARRAY_TYPE_CLASS_TABLE.put("String[]", String[].class);
            ARRAY_TYPE_CLASS_TABLE.put("RecordId[]", RecordId[].class);
            ARRAY_TYPE_CLASS_TABLE.put("StreamOffset[]", StreamOffset[].class);
        }

    }
}

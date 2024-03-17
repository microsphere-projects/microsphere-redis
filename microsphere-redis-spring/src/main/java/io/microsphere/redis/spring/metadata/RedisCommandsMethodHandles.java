package io.microsphere.redis.spring.metadata;

import org.apache.commons.lang3.StringUtils;
import org.jboss.jandex.ArrayType;
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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.microsphere.util.ClassLoaderUtils.*;

public class RedisCommandsMethodHandles {

    private static final Logger logger = LoggerFactory.getLogger(RedisCommandsMethodHandles.class);

    private static final MethodHandles.Lookup PUBLIC_LOOKUP = MethodHandles.publicLookup();

    private static final ClassLoader CURRENT_CLASS_LOADER = RedisCommandsMethodHandles.class.getClassLoader();

    private static List<Class<?>> TARGET_CLASSES;

    /**
     * get all public methods from {@link RedisCommands} <br />
     * exclude {@link RedisCommands#execute} <br />
     * exclude private lambda method from {@link RedisStreamCommands}
     * <li>lambda$xDel$1</li>
     * <li>lambda$xAck$0</li>
     *
     * @return
     */
    static List<MethodInfo> getAllRedisCommandMethods() {
        try {
            Index index = Index.of(TARGET_CLASSES);

            return index.getClassByName(RedisCommands.class)
                    .interfaceNames()
                    .stream()
                    .map(index::getClassByName)
                    .flatMap(classInfo -> classInfo.methods().stream())
                    .filter(methodInfo -> Modifier.isPublic(methodInfo.flags()))
                    .collect(Collectors.toList());
        } catch (IOException e) {

            logger.error("Can't get RedisCommands Methods", e);

            throw new RuntimeException("Can't get RedisCommands Methods");
        }
    }

    static Map<String, MethodHandle> initRedisCommandMethodHandle() {
        List<MethodInfo> methods = getAllRedisCommandMethods();

        return methods.stream()
                .map(methodInfo -> {
                    String methodSignature = methodInfo.toString();
                    MethodHandle methodHandle = findMethodHandle(methodInfo);
                    return new MethodRecord(methodSignature, methodHandle);
                })
                .collect(Collectors.toMap(MethodRecord::methodSignature, MethodRecord::methodHandle));
    }


    static MethodHandle findMethodHandle(MethodInfo methodInfo) {
        try {
            Class<?> klass = loadClass(methodInfo.declaringClass().name().toString(), CURRENT_CLASS_LOADER);

            String methodName = methodInfo.name();

            Class<?> returnTypeKlass = loadClass(methodInfo.returnType().toString(), CURRENT_CLASS_LOADER);

            MethodParameterInfo[] array = methodInfo.parameters().toArray(new MethodParameterInfo[]{});
            Class<?>[] parameterKlass = new Class<?>[array.length];
            for (int i = 0; i < array.length; i++) {
                parameterKlass[i] = loadClass(array[i].type().toString(), CURRENT_CLASS_LOADER);
            }
            MethodType methodType = MethodType.methodType(returnTypeKlass, parameterKlass);

            return RedisCommandsMethodHandles.PUBLIC_LOOKUP.findVirtual(klass, methodName, methodType);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            logger.error("Error occurred when find MethodHandle.\n methodInfo:{}", methodInfo, e);
            throw new RuntimeException(e);
        }
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
            // when use jdk21 local() value is "String" prefix is "java.lang"
            // when use jdk8 local() value is "java.lang.String" prefix is null
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
        return null;
    }

    static {
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

        static {
            // NOTE: use new EnumMap(Map.of()) when use jdk11+
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

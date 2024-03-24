package io.microsphere.redis.replicator.spring;

import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.lang.reflect.Method;

import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static io.microsphere.redis.spring.serializer.RedisCommandEventSerializer.VERSION_V1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.utility.DockerImageName.parse;

@Testcontainers
class HandleRedisCommandReplicatedEventTest {
    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(parse("redis:latest")).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.redis.host", () -> redisContainer.getHost());
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Nested
    @SpringJUnitConfig(classes = {
            RedisCommandReplicator.class,
            RedisAutoConfiguration.class
    })
    @TestPropertySource(properties = {
            "microsphere.redis.replicator.consumer.event.handler=REFLECT"
    })
    class ReflectEventHandleTest {
        @Autowired
        ApplicationContext applicationContext;

        @Autowired
        RedisTemplate<String, String> redisTemplate;

        @Test
        void invokeMethodByReflect() {
            String key = "Reflect";
            String expected = "Reflect";
            RedisCommandReplicatedEvent redisCommandReplicatedEvent = getRedisCommandReplicatedEvent(key, expected);
            applicationContext.publishEvent(redisCommandReplicatedEvent);

            String value = redisTemplate.opsForValue().get(key);
            assertThat(value).isEqualTo(expected);
        }
    }

    @Nested
    @SpringJUnitConfig(classes = {
            RedisCommandReplicator.class,
            RedisAutoConfiguration.class
    })
    @TestPropertySource(properties = {
            "microsphere.redis.replicator.consumer.event.handler=METHOD_HANDLE"
    })
    class MethodHandleEventHandleTest {

        @Autowired
        ApplicationContext applicationContext;

        @Autowired
        RedisTemplate<String, String> redisTemplate;


        @Test
        void invokeMethodByMethodHandle() {
            String key = "MethodHandle";
            String expected = "MethodHandle";
            RedisCommandReplicatedEvent redisCommandReplicatedEvent = getRedisCommandReplicatedEvent(key, expected);
            applicationContext.publishEvent(redisCommandReplicatedEvent);


            String value = redisTemplate.opsForValue().get(key);
            assertThat(value).isEqualTo(expected);
        }
    }


    private static RedisCommandReplicatedEvent getRedisCommandReplicatedEvent(String key, String value) {
        String interfaceName = "org.springframework.data.redis.connection.RedisStringCommands";
        String methodName = "set";
        String[] parameterTypes = new String[]{"[B", "[B"};
        Method method = findWriteCommandMethod(interfaceName, methodName, parameterTypes);

        RedisCommandEvent redisCommandEvent = RedisCommandEvent.Builder.source("test").applicationName("test-application").method(method).args(key.getBytes(), value.getBytes()).serializationVersion(VERSION_V1).build();

        return new RedisCommandReplicatedEvent(redisCommandEvent, "domain");
    }

}

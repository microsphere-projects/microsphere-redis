package io.microsphere.redis.benchmark;

import io.microsphere.redis.replicator.spring.event.handler.MethodHandleRedisCommandReplicatedEventHandler;
import io.microsphere.redis.replicator.spring.event.handler.RedisCommandReplicatedEventHandler;
import io.microsphere.redis.replicator.spring.event.handler.ReflectRedisCommandReplicatedEventHandler;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static io.microsphere.redis.spring.metadata.RedisMetadataRepository.findWriteCommandMethod;
import static org.testcontainers.utility.DockerImageName.parse;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 20, time = 1)
@Fork(3)
public class RedisCommandReplicatedEventHandlerBenchmark {
    static GenericContainer<?> redisContainer;
    RedisStringCommands redisStringCommands;
    Method method;
    RedisCommandReplicatedEventHandler methodHandleHandler;
    RedisCommandReplicatedEventHandler reflectHandler;

    byte[] key;
    byte[] value;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        // 启动 PostgreSQL 容器
        redisContainer = new GenericContainer<>(parse("redis:latest")).withExposedPorts(6379);
        redisContainer.start();

        String interfaceName = "org.springframework.data.redis.connection.RedisStringCommands";
        String methodName = "set";
        String[] parameterTypes = new String[]{"[B", "[B"};
        method = findWriteCommandMethod(interfaceName, methodName, parameterTypes);

        LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisContainer.getHost(), redisContainer.getFirstMappedPort()));
        redisConnectionFactory.afterPropertiesSet();
        RedisConnection connection = redisConnectionFactory.getConnection();
        redisStringCommands = connection.stringCommands();

        methodHandleHandler = new MethodHandleRedisCommandReplicatedEventHandler();
        reflectHandler = new ReflectRedisCommandReplicatedEventHandler();
        key = "key".getBytes(StandardCharsets.UTF_8);
        value = "value".getBytes(StandardCharsets.UTF_8);
    }

    @Benchmark
    public void benchmarkDirect() throws Throwable {
        redisStringCommands.set(key, value);
    }

    @Benchmark
    public void benchmarkMethodHandleRedisCommandReplicatedEventHandler() throws Throwable {
        methodHandleHandler.handleEvent(method, redisStringCommands, key, value);
    }

    @Benchmark
    public void benchmarkReflectRedisCommandReplicatedEventHandler() throws Throwable {
        reflectHandler.handleEvent(method, redisStringCommands, key, value);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        if (redisContainer != null) {
            redisContainer.stop();
        }
    }
}

# microsphere-redis-spring

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring` (flagship module) |
| Git branch | `release` (0.2.x line) |
| Latest release | `0.2.14` |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-spring` |
| Compatibility | Spring Data Redis 3.0.0 – 3.5.x, Spring Framework 6.x, JDK 17+ |
| Key dependencies | `microsphere-redis-core` (compile), `spring-data-redis` (optional), `snakeyaml`, `jakarta.servlet-api` (optional) |

## Overview

`microsphere-redis-spring` provides **non-intrusive Redis operation interception** for Spring applications:

- Monitor every Redis command executed through `RedisTemplate`, `StringRedisTemplate` or a raw `RedisConnection`,
  without changing application code
- Publish Spring `RedisCommandEvent` for every intercepted command (audit logs, metrics, replication)
- Plug in custom interceptors through the `RedisCommandInterceptor` / `RedisConnectionInterceptor` SPIs
- Wrap templates so serialized values keep a link back to their original Java objects ("holding value")
- Route requests to different `RedisConnectionFactory` beans dynamically
- Recover raw command arguments that Spring Data Redis serializes away, through a catalog of
  `RedisSerializer` implementations

## Installation

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>0.2.14</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>

    <!-- you must bring your own Redis client stack -->
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>
</dependencies>
```

`spring-data-redis` is declared `optional` by the module, so it is **not** pulled transitively.

## Quick Start

```java
@Configuration
@EnableRedisInterceptor                      // enable interception for all templates ("*")
public class RedisInterceptionConfig {
}
```

Then consume command events anywhere in the context:

```java
@Component
class AuditListener {

    @EventListener
    public void onRedisCommandEvent(RedisCommandEvent event) {
        System.out.printf("%s %s %s -> %s%n",
                event.getSourceBeanName(),      // e.g. "stringRedisTemplate"
                event.getInterfaceName(),       // e.g. RedisStringCommands
                event.getMethodName(),          // e.g. "set"
                Arrays.toString(event.getArgs()));
    }
}
```

## Enablement Annotations

### @EnableRedisInterceptor

Meta-annotated with `@EnableRedisContext` and `@Import(RedisInterceptorBeanDefinitionRegistrar.class)`.

| Attribute | Type | Default | Meaning |
|-----------|------|---------|---------|
| `wrapRedisTemplates` | `String[]` | `{}` (empty = intercept via connection-factory proxy, all beans) | bean names (or `${...}` placeholders, `*` wildcard) of the `RedisTemplate`/`StringRedisTemplate` beans to replace with wrapper beans |
| `exposeCommandEvent` | `boolean` | `true` | whether `RedisCommandEvent` is published (also gated by property `microsphere.redis.command-event.exposed`) |
| `sources` | `BeanSource[]` | — | where interceptors are discovered from: `BEAN_FACTORY`, `SPRING_FACTORIES`, `JAVA_SERVICE_PROVIDER` |

### @EnableRedisContext

Registers `RedisContext` (+ implies `@EnableRedisConfiguration`). It is also triggered automatically by
`@EnableRedisInterceptor`.

### @EnableRedisConfiguration

Registers the `RedisConfiguration` bean bound to the `microsphere.redis.*` environment properties.

## Configuration Properties

Defined in `io.microsphere.redis.spring.util.RedisConstants`:

| Property | Default | Purpose |
|----------|---------|---------|
| `microsphere.redis.enabled` | `true` | master switch for the whole framework |
| `microsphere.redis.interceptor.enabled` | `true` | enable/disable interception |
| `microsphere.redis.command-event.exposed` | `true` | publish `RedisCommandEvent` |
| `microsphere.redis.wrapped-redis-templates` | `*` | template bean names to wrap |
| `microsphere.redis.fail-fast` | `true` | fail startup when Redis is unavailable |

## Interceptor SPI

Two sibling interfaces extend the generic callback contract `RedisMethodInterceptor<T>`:

```java
public interface RedisMethodInterceptor<T> extends Ordered {
    default void beforeExecute(RedisMethodContext<T> context) throws Throwable { ... }
    default void beforeExecute(T target, Method method, Object[] args, String sourceBeanName) throws Throwable { ... }
    default void afterExecute(RedisMethodContext<T> context, Object result, Throwable failure) throws Throwable { ... }
    default void afterExecute(T target, Method method, Object[] args, String sourceBeanName,
                              Object result, Throwable failure) throws Throwable { ... }
    default void handleError(RedisMethodContext<T> context, boolean before, Object result,
                             Throwable failure, Throwable interceptionFailure) { ... }
    // isEnabled() and getOrder() control participation and ordering
}

public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> {}
public interface RedisCommandInterceptor  extends RedisMethodInterceptor<RedisCommands> {}
```

`RedisMethodContext<T>` gives interceptors everything about the in-flight call:

- `getTarget()`, `getMethod()`, `getArgs()`, `getSourceBean()`, `getSourceBeanName()`
- `getRedisContext()`, `getRedisConfiguration()`, `getEnvironment()`, `getApplicationContext()`
- parameter access with metadata: `getParameters()`, `getParameter(String name)`, `getParameter(int index)`
- command classification: `isWriteMethod()`, `isCommandEventExposed()`, `isSourceFromRedisTemplate()`
- timing: `start()` / `stop()` / `getDurationNanos()` / `getDuration(TimeUnit)`
- mutable per-invocation attributes: `setAttribute(name, value)` / `getAttribute(name)`
- thread-bound: `RedisMethodContext.set(...)`, `get()`, `clear()`

Interceptor failures never break the business call — they are routed to `handleError` and logged.

### Registering interceptors

1. **Java SPI** — file `META-INF/services/io.microsphere.redis.spring.interceptor.RedisCommandInterceptor`
   (and/or `...RedisConnectionInterceptor`) containing implementation class names, with `sources` including
   `JAVA_SERVICE_PROVIDER` (the default registrar sources include all three).
2. **Spring beans** — declare the interceptor as a `@Component`; discovered with `sources = BEAN_FACTORY`.
3. **spring.factories** — key `io.microsphere.redis.spring.interceptor.RedisCommandInterceptor`
   (used by the bundled `EventPublishingRedisCommandInterceptor`).

## Events

| Event | Extends | Key accessors |
|-------|---------|---------------|
| `RedisOperationEvent` | `ApplicationEvent` | base class holding method/args metadata |
| `RedisCommandEvent` | `ApplicationEvent` | `getMethod()`, `getInterfaceName()`, `getMethodName()`, `getParameterTypes()`, `getParameterCount()`, `getArgs()`, `getArg(int)`, `getApplicationName()`, `getSourceBeanName()`, `getSerializationVersion()`/`setSerializationVersion(byte)` (wire-format tag used by the replicator) |
| `RedisConfigurationPropertyChangedEvent` | `ApplicationContextEvent` | `getEnvironment()`, `getPropertyNames()`, `hasProperty(name)`, `getRedisConfiguration()` |

`EventPublishingRedisCommandInterceptor` is the built-in `RedisCommandInterceptor` that turns every intercepted
command into a `RedisCommandEvent` (order-controlled, active only when command events are exposed).

## Template Wrappers & Holding-Value Serializers

When `wrapRedisTemplates` selects beans, `RedisTemplateWrapperBeanPostProcessor` replaces them with:

- `RedisTemplateWrapper<K,V>` (extends `RedisTemplate`) and `StringRedisTemplateWrapper` (extends `StringRedisTemplate`)

Both override `preProcessConnection(...)` to wrap the `RedisConnection` in the intercepting proxy, and both implement
`DelegatingWrapper` (`getDelegate()` returns the raw connection/template).

`WrapperProcessors` runs pluggable `WrapperProcessor` beans over the wrappers, notably:

- `HoldingValueRedisTemplateWrapperProcessor` / `HoldingValueStringRedisTemplateWrapperProcessor` — replace each
  `RedisSerializer` with `HoldingValueRedisSerializerWrapper`, which records `object ↔ byte[]` pairs into the
  thread-bound `ValueHolder` while delegating serialization. Interceptors can then call `ValueHolder.getValue(bytes)`
  to recover the **original Java object** from already-serialized command arguments.

When no templates are listed for wrapping, `RedisConnectionFactoryProxyBeanPostProcessor` proxies every
`RedisConnectionFactory` instead — so connections obtained via `template.execute(connectionCallback)` are intercepted
as well. `newProxyRedisConnection(...)`, `getRawRedisConnectionFactory(...)` are its utility entry points.

## Serializers (`io.microsphere.redis.spring.serializer`)

`Serializers` is a registry of static instances covering the Spring Data Redis value types that have no default
serializer — used to recover raw command arguments and to serialize `RedisCommandEvent` payloads:

| Serializer | Target type |
|------------|-------------|
| `Serializers.DEFAULT_SERIALIZER` (`JdkSerializationRedisSerializer`), `STRING_SERIALIZER` (`StringRedisSerializer`) | baseline |
| `BooleanSerializer` / `ByteArraySerializer` / `DoubleSerializer` / `IntegerSerializer` / `LongSerializer` / `ShortSerializer` | primitives |
| `EnumSerializer` | enums (compact ordinal-style encoding) |
| `ExpirationSerializer` | `RedisStringCommands.SetOption`/expiration |
| `BoundarySerializer`, `RangeSerializer` (+`RangeModel`), `RedisZSetCommandsRangeSerializer` | ZSET range/boundary |
| `GeoLocationSerializer`, `PointSerializer` | GEO types |
| `SortParametersSerializer` (nested `RangeSerializer`) | `SortParameters` |
| `WeightsSerializer` | `RedisZSetCommands.Weights` |
| `RedisCommandEventSerializer` | serialize a full `RedisCommandEvent` to `byte[]` (registered in the module's `META-INF/spring.factories` as a `RedisSerializer` SPI) |

All typed serializers extend `AbstractSerializer<T>` (Template Method: `serialize/deserialize` are final and delegate
to `doSerialize`/`doDeserialize`; fixed-width types implement `calcBytesLength()`).

## DynamicRedisConnectionFactory

`io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory` is a delegating
`RedisConnectionFactory` that selects the target factory **per thread**:

```java
DynamicRedisConnectionFactory.switchTarget("anotherFactoryBeanName"); // bind current thread
try {
    redisTemplate.opsForValue().set("k", "v");                        // goes to that cluster
} finally {
    DynamicRedisConnectionFactory.clearTarget();
}
```

- `setDefaultRedisConnectionFactoryBeanName(String)` — fallback when no target is set
- `getTargetBeanName()` — current thread's target
- `DynamicRedisConnectionFactoryCleanerListener` (a `ServletRequestListener`) clears the thread-local when an HTTP
  request completes, preventing leaks on pooled threads (requires `jakarta.servlet-api`)

## Code Examples

### Custom connection interceptor (from `LoggingRedisConnectionInterceptor`, registered via `META-INF/services/io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor`)

```java
public class LoggingRedisConnectionInterceptor implements RedisConnectionInterceptor {

    @Override
    public void beforeExecute(RedisMethodContext context) throws Throwable {
        logger.info("beforeExecute - context: {}", context);
    }

    @Override
    public void afterExecute(RedisMethodContext<RedisConnection> context, Object result, Throwable failure) {
        logger.info("afterExecute - context: {} , result: {}", context, result);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

### Full annotation configuration (from `EnableRedisInterceptorTest`)

```java
@ContextConfiguration(classes = {LoggingRedisCommandInterceptor.class, MyConfig.class})
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true",
        "microsphere.redis.wrapped-rest-templates=redisTemplate",
})
@EnableRedisInterceptor(wrapRedisTemplates = {
        "${microsphere.redis.wrapped-rest-templates}",   // placeholder
        " redisTemplate , stringRedisTemplate"            // comma-separated names
}, sources = {
        BEAN_FACTORY,          // interceptors declared as Spring beans
        SPRING_FACTORIES,      // interceptors declared in spring.factories
        JAVA_SERVICE_PROVIDER  // interceptors declared in META-INF/services
})
class MyInterceptionTest extends AbstractRedisCommandEventTest {
}
```

### Consuming command events while writing through templates (from `AbstractRedisCommandEventTest`)

```java
context.addApplicationListener((ApplicationListener<RedisCommandEvent>) event -> {
    // record key -> value observed on the wire
    data.put(event.getArg(0), event.getArg(1));
});

stringRedisTemplate.opsForValue().set("Key-1", "Value-1");
// raw connection execution is intercepted as well:
stringRedisTemplate.execute(c -> {
    c.execute("SET", keySerializer.serialize("Key-3"), valueSerializer.serialize("Value-3"));
    return c;
}, true);
```

### Disabling command events (from `EnableRedisInterceptorWithoutExposeCommandEventTest`)

```java
@EnableRedisInterceptor(exposeCommandEvent = false)
class QuietConfig {
}
```

## Version & Compatibility

| Item | Value |
|------|-------|
| Version line | 0.2.x (`release` branch), latest release **0.2.14** |
| Spring Data Redis | **3.0.0 – 3.5.x** (metadata YAML regenerated against 3.5.x; `version:` header records the generator source, e.g. `3.5.10`) |
| Spring Boot / Framework | 3.x / 6.x (`jakarta.*` namespace) |
| JDK | 17+ |
| Legacy | For Spring Data Redis 2.1.0 – 2.7.x / Spring Boot 2.x use the 0.1.x line — [../../1.x/microsphere-redis-spring.md](../1.x/microsphere-redis-spring.md) |

## Related Modules

- [microsphere-redis-core](./microsphere-redis-core.md) — metadata model this module loads from `META-INF/spring-data-redis-metadata.yaml` (via `SpringRedisMetadataLoader` SPI + `SpringRedisMetadataRepository`)
- [microsphere-redis-spring-boot](./microsphere-redis-spring-boot.md) — `@ConditionalOnRedis*` conditions
- [microsphere-redis-replicator-spring](./microsphere-redis-replicator-spring.md) — consumes `RedisCommandEvent`
- Developer guide: [../../developer-guide/current/microsphere-redis-spring.md](../../developer-guide/current/microsphere-redis-spring.md)

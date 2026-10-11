# microsphere-redis-spring — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-spring:0.1.14` |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, Java 8+ (servlet API: `javax.servlet`) |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-spring.md](../current/microsphere-redis-spring.md).

## Overview

The integration module for plain Spring (non-Boot) applications: it proxies every `RedisConnection`
obtained from your `RedisConnectionFactory` (or wraps named `RedisTemplate` beans), routes each command
through an ordered interceptor chain, and publishes a `RedisCommandEvent` for every successful **write**
command. All runtime behavior is metadata-driven from `META-INF/spring-data-redis-metadata.yaml`
(generated against Spring Data Redis 2.7.18 on this line).

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring</artifactId>
    <version>0.1.14</version>
</dependency>
```

Requires `spring-data-redis` (2.1.0–2.7.x) and `io.microsphere:microsphere-redis-core` on the classpath.

## Configuration Properties

| Property | Default | Effect |
|----------|---------|--------|
| `microsphere.redis.enabled` | `true` | master switch; `false` disables all registration |
| `microsphere.redis.interceptor.enabled` | `true` | interception on/off |
| `microsphere.redis.command-event.exposed` | `true` | publish `RedisCommandEvent` for writes |
| `microsphere.redis.wrapped-redis-templates` | `*` | which templates to wrap instead of proxying the factory |
| `microsphere.redis.fail-fast` | `true` | fail startup on inconsistent wiring |

## API Reference

### Enablement annotations

- `@EnableRedisConfiguration` — registers `RedisConfiguration` only.
- `@EnableRedisContext` — adds `RedisContext` (`microsphere:redisContext`).
- `@EnableRedisInterceptor` — the full pipeline. Attributes:
  - `String[] wrapRedisTemplates() default {}` — template bean-name patterns; empty = proxy the factory instead;
  - `boolean exposeCommandEvent() default true`;
  - `BeanSource[] sources()` — where user interceptors are discovered
    (`BEAN_FACTORY`, `SPRING_FACTORIES`, `JAVA_SERVICE_PROVIDER`).

In Spring Boot 2 apps no annotation is needed: `RedisInitializer` + `RedisInterceptorModuleInitializer`
auto-register via `spring.factories`.

### Interception contract

```java
public interface RedisMethodInterceptor<T> extends Ordered {
    default void beforeExecute(RedisMethodContext<T> context) { }
    default void afterExecute(RedisMethodContext<T> context, Object result, Throwable failure) { }
    default void handleError(RedisMethodContext<T> context, boolean beforeExecute,
                             Object result, Object failure,
                             Object interceptionFailure) { }
}
// bind to the interface you care about:
public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> { }
public interface RedisCommandInterceptor   extends RedisMethodInterceptor<RedisCommands> { }
```

`RedisMethodContext` gives you: `getTarget/getMethod/getArgs`, `getSourceBean/ getSourceBeanName`,
`getAttribute/setAttribute`, timing (`getDuration(TimeUnit)`), `isWriteMethod()`, and parameter binding
(`getParameter(name)`). `RedisMethodContext.get()` (static) exposes the in-flight context on the same thread.

### Events

`RedisCommandEvent` (write commands only, `failure == null`): `getMethod`, `getMethodName`,
`getInterfaceName`, `getParameterTypes`, `getArgs/getArg(int)`, `getSourceBeanName`,
`getApplicationName`, `getTimestamp`.

### Serializers

`io.microsphere.redis.spring.serializer.*` — typed serializers for value-object parameters
(`Expiration`, `Boundary`, `Range`, `GeoLocation`, `Point`, `SortParameters`, ZSet range/weights…),
fronted by `Serializers` lookups. **1.x naming note:** the ZSet weights serializer is
`RedisZSetCommandsWeightsSerializer` (renamed to `WeightsSerializer` in 0.2.x).
`HoldingValueRedisSerializerWrapper` records object↔bytes pairs so interceptors can recover original
arguments from their serialized forms via `ValueHolder`.

### DynamicRedisConnectionFactory

Thread-routable factory for multi-instance scenarios: `DynamicRedisConnectionFactory.switchTarget(beanName)`
/ `clearTarget()` per thread; on 1.x the request-scoped cleanup listener is built on **`javax.servlet`**
`ServletRequestListener` (0.2.x uses `jakarta.servlet`).

## Code Examples

```java
// 1) Plain Spring: enable interception explicitly
@Configuration
@EnableRedisInterceptor
public class RedisInterceptConfig { }

// 2) A timing/audit interceptor (pattern from redis-spring interceptor tests)
public class SlowCommandInterceptor implements RedisCommandInterceptor {
    @Override
    public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) {
        if (failure == null && context.isWriteMethod() && context.getDuration(MILLISECONDS) > 100) {
            log.warn("slow write {} from bean {} took {}ms",
                    context.getMethod().getName(), context.getSourceBeanName(),
                    context.getDuration(MILLISECONDS));
        }
    }
}

// 3) React to write commands
@EventListener
public void onRedisCommand(RedisCommandEvent event) {
    byte[] key = (byte[]) event.getArg(0);   // SET key value ...
}
```

## Version & Compatibility

| microsphere-redis | branch | Spring Data Redis | Spring Boot | Java | Servlet |
|-------------------|--------|-------------------|-------------|------|---------|
| `0.1.x` (this doc, latest `0.1.14`) | `release-1.x` | 2.1.0 – 2.7.x | 2.x | 8+ | javax |
| `0.2.x` (latest `0.2.14`) | `release` | 3.0.0 – 3.5.x | 3.x | 17 | jakarta |

API surface (annotations, property keys, interceptor contract, events) is intentionally identical across
the two lines; the differences are coordinate- and JDK-level (javax→jakarta, records in core,
serializer renames).

## Related Modules

- [core (1.x)](./microsphere-redis-core.md) · [generator (1.x)](./microsphere-redis-generator.md) · [spring-test (1.x)](./microsphere-redis-spring-test.md)
- Developer guide: [../../developer-guide/1.x/microsphere-redis-spring.md](../../developer-guide/1.x/microsphere-redis-spring.md)

# microsphere-redis-spring-boot

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-boot` |
| Git branch | `release` (0.2.x line) |
| Latest release | `0.2.14` |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-spring-boot` |
| Compatibility | Spring Boot 3.x (auto-configuration infra), Spring Data Redis 3.0.0 – 3.5.x, JDK 17+ |
| Key dependencies | `microsphere-redis-spring` (compile), `spring-boot-starter` / `spring-boot-starter-actuator` (optional) |

## Overview

`microsphere-redis-spring-boot` exposes the enablement state of Microsphere Redis as **declarative Spring Boot
conditions**. Use them on your own `@Configuration` classes, `@Bean` methods or components so they materialize only
when the Redis interception stack is actually on:

| Annotation | Satisfied when |
|------------|----------------|
| `@ConditionalOnRedisEnabled` | `microsphere.redis.enabled` is `true` (default `true`, `matchIfMissing`) |
| `@ConditionalOnRedisInterceptorEnabled` | Redis enabled **and** `microsphere.redis.interceptor.enabled` is not `false` |
| `@ConditionalOnRedisAvailable` | Redis enabled **and** these classes are present: `org.springframework.data.redis.connection.RedisConnection`, `io.microsphere.redis.metadata.RedisMetadataLoader`, `io.microsphere.redis.spring.interceptor.RedisMethodInterceptor` |

All three are meta-annotated on `@ConditionalOnProperty` / `@ConditionalOnClass` from Spring Boot — they carry no
attributes of their own (`@Target(TYPE)`; `@ConditionalOnRedisEnabled` also allows `ANNOTATION_TYPE`).

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-spring-boot</artifactId>
</dependency>
```

## API Reference

### @ConditionalOnRedisEnabled

```java
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(name = "microsphere.redis.enabled", matchIfMissing = true)
public @interface ConditionalOnRedisEnabled {}
```

### @ConditionalOnRedisInterceptorEnabled

```java
@ConditionalOnRedisEnabled
@ConditionalOnProperty(name = "microsphere.redis.interceptor.enabled", matchIfMissing = true)
public @interface ConditionalOnRedisInterceptorEnabled {}
```

### @ConditionalOnRedisAvailable

```java
@Target(TYPE)
@ConditionalOnRedisEnabled
@ConditionalOnClass(name = {
        "org.springframework.data.redis.connection.RedisConnection",
        "io.microsphere.redis.metadata.RedisMetadataLoader",
        "io.microsphere.redis.spring.interceptor.RedisMethodInterceptor"})
public @interface ConditionalOnRedisAvailable {}
```

## Code Examples

The pattern validated by `ConditionalOnRedisAvailableTest` / `ConditionalOnRedisEnabledTest`:

```java
@Configuration
@ConditionalOnRedisAvailable   // whole config only exists when the Redis stack is on the classpath and enabled
class MetricsConfig {

    @Bean
    @ConditionalOnRedisInterceptorEnabled
    public RedisCommandMetrics redisCommandMetrics() {
        return new RedisCommandMetrics();
    }
}
```

Toggling in tests or production is a plain property change:

```properties
microsphere.redis.enabled=false                    # disables everything below ConditionalOnRedis*
microsphere.redis.interceptor.enabled=false        # keeps configuration, drops interception
```

`RedisInitializerSpringBootTest` demonstrates that the `RedisInitializer`
(`ApplicationContextInitializer`) from `microsphere-redis-spring` still bootstraps automatically in a Boot context.

## Version & Compatibility

| Item | Value |
|------|-------|
| Version line | 0.2.x (`release` branch), latest release **0.2.14** |
| Spring Boot | 3.x (`jakarta` stack). Spring Boot 4 users get auto-config ordering from the `spring-cloud` module's `@AutoConfigureAfter`, which already anticipates the Boot 4 `DataRedisAutoConfiguration` class name |
| JDK | 17+ |
| Legacy | 0.1.x line for Spring Boot 2 stacks: [../../1.x/microsphere-redis-spring-boot.md](../1.x/microsphere-redis-spring-boot.md) |

## Related Modules

- [microsphere-redis-spring](./microsphere-redis-spring.md) — what the conditions gate
- [microsphere-redis-spring-cloud](./microsphere-redis-spring-cloud.md) — depends on this module
- Developer guide: [../../developer-guide/current/microsphere-redis-spring-boot.md](../../developer-guide/current/microsphere-redis-spring-boot.md)

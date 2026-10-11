# microsphere-redis-spring-boot — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-boot` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-spring-boot:0.1.14` |
| Compatibility | Spring Boot `2.x`, Spring Data Redis `2.1.0 – 2.7.x`, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-spring-boot.md](../current/microsphere-redis-spring-boot.md).
> Sources are identical to the 0.2.x module — same three condition annotations.

## Overview

Adds a reusable Spring Boot **condition vocabulary** for microsphere-redis. The interception itself
still auto-configures through `microsphere-redis-spring`; this module lets *your* Boot 2
auto-configurations and beans follow the same enablement rules.

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-boot</artifactId>
    <version>0.1.14</version>
</dependency>
```

## API Reference

| Annotation | Equivalent condition | Use when |
|------------|--------------------|----------|
| `@ConditionalOnRedisEnabled` | `@ConditionalOnProperty("microsphere.redis.enabled", matchIfMissing = true)` | your bean needs Redis integration active |
| `@ConditionalOnRedisInterceptorEnabled` | enabled **and** `microsphere.redis.interceptor.enabled` (matchIfMissing) | your bean depends on interception/events |
| `@ConditionalOnRedisAvailable` | enabled + classpath has `RedisConnection`, `RedisMetadataLoader`, `RedisMethodInterceptor` | guard whole configuration classes (`@Target(TYPE)`) |

## Usage Example

```java
@Configuration
@ConditionalOnRedisAvailable
public class AuditConfiguration {

    @Bean
    @ConditionalOnRedisInterceptorEnabled
    public RedisAuditService redisAuditService() { ... }
}
```

With `microsphere.redis.enabled=false` (or spring-data-redis absent) the entire configuration disappears —
handy for test profiles and feature toggles.

## Version & Compatibility

| microsphere-redis | Spring Boot | Notes |
|-------------------|-------------|-------|
| `0.1.14` (this) | 2.x | conditions evaluate against Boot 2 property binding |
| `0.2.14` | 3.x | identical annotations, Boot 3 coordinates |

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md) · [spring-cloud (1.x)](./microsphere-redis-spring-cloud.md)
- Developer guide: [../../developer-guide/1.x/microsphere-redis-spring-boot.md](../../developer-guide/1.x/microsphere-redis-spring-boot.md)

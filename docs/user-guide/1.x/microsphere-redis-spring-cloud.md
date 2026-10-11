# microsphere-redis-spring-cloud — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-cloud` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-spring-cloud:0.1.14` |
| Compatibility | Spring Boot `2.x`, Spring Cloud 2021/2022-era context libs, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-spring-cloud.md](../current/microsphere-redis-spring-cloud.md).

## Overview

Spring Cloud adaptation: makes every `microsphere.redis.*` property **hot-refreshable** through
`/actuator/refresh` — a changed property becomes a `RedisConfigurationPropertyChangedEvent`, which the
core `RedisConfiguration` re-binds, so interception and command-event exposure can be toggled at runtime
without a restart. It also contributes feature metadata for actuator `HasFeatures` output.

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-cloud</artifactId>
    <version>0.1.14</version>
</dependency>
```

Activates only when `spring-cloud-context` (`EnvironmentChangeEvent`) is on the classpath and
`microsphere.redis.enabled` is true; auto-configured after Boot's
`org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration`.
(1.x difference from 0.2.x: 0.2.x also orders after the relocated
`org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration` for Boot 4 readiness.)

## API Reference

| Element | Purpose |
|---------|---------|
| `RedisCloudAutoConfiguration` | the entry point; imports the listener below |
| `PropagatingRedisConfigurationPropertyChangedEventApplicationListener` | filters `EnvironmentChangeEvent` keys to the `microsphere.redis.` prefix and republishes them as `RedisConfigurationPropertyChangedEvent` |
| `META-INF/config/default/features.yaml` | declarative feature list for actuator (abstract Spring Data Redis API names + named microsphere classes) |

## Usage Example

```bash
# flip interception off on a running instance
curl -X POST http://host/actuator/refresh \
     -H 'Content-Type: application/json' \
     -d '{"microsphere.redis.interceptor.enabled":"false"}'
```

All `microsphere.redis.*` keys — including `microsphere.redis.replicator.*` — participate automatically.

## Version & Compatibility

| microsphere-redis | Spring Boot | Redis autoconfig ordered after |
|-------------------|-------------|--------------------------------|
| `0.1.14` (this) | 2.x | `o.s.b.autoconfigure.data.redis.RedisAutoConfiguration` |
| `0.2.14` | 3.x | both the Boot 2.x-era and Boot 4.0-era relocated classes (by name) |

## Related Modules

- [spring-boot (1.x)](./microsphere-redis-spring-boot.md) — the conditions it builds on
- Developer guide: [../../developer-guide/1.x/microsphere-redis-spring-cloud.md](../../developer-guide/1.x/microsphere-redis-spring-cloud.md)

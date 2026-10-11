# microsphere-redis-spring-test — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-test` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-spring-test:0.1.14` (test scope) |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-spring-test.md](../current/microsphere-redis-spring-test.md).
> This module is **byte-for-byte equivalent to the 0.2.x one** (no source diff between branches).

## Overview

Published test-support: a SpringJUnit base class wired to a local Redis plus canonical command fixtures,
so your interception tests don't reinvent Redis plumbing.

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-test</artifactId>
    <version>0.1.14</version>
    <scope>test</scope>
</dependency>
```

## API Reference

| Element | Purpose |
|---------|---------|
| `AbstractRedisTest` | `@Disabled @SpringJUnitConfig(RedisConfig.class)` base class; autowires `redisTemplate`, `stringRedisTemplate`, `context`; provides `assertRedisTemplateSet/assertStringRedisTemplateSet/assertSet` |
| `AbstractRedisTest.SET_METHOD` | the reflective `RedisConnection.set(byte[],byte[])` method — stable identity to compare interceptor captures against |
| `AbstractRedisTest.SET_METHOD_ARGS` | `["key".bytes, "value".bytes]` |
| `AbstractRedisTest.SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE` | `"redisTemplate"` |
| `config.RedisConfig` | static `@Bean`s: `redisTemplate`, `stringRedisTemplate`, and a `LettuceConnectionFactory("127.0.0.1", 6379)` that calls `afterPropertiesSet()` + `validateConnection()` (fails fast if Redis is down) |

## Code Example

```java
// Enable the (disabled-by-default) base and test one intercepted write
public class MyInterceptorTest extends AbstractRedisTest {

    @Test
    void setFlowsThroughInterceptor() {
        assertRedisTemplateSet("k", "v");                 // passes through the proxy untouched
        // ... assert what your interceptor captured for SET_METHOD/SET_METHOD_ARGS
    }
}
```

A local Redis must be running (the repository's `docker/` compose brings one up).

## Version & Compatibility

| microsphere-redis | Spring Data Redis | Spring Boot | Java |
|-------------------|-------------------|-------------|------|
| `0.1.14` (this) | 2.1.0 – 2.7.x | 2.x | 8+ |
| `0.2.14` | 3.0.0 – 3.5.x | 3.x | 17 |

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md)
- Developer guide: [../../developer-guide/1.x/microsphere-redis-spring-test.md](../../developer-guide/1.x/microsphere-redis-spring-test.md)

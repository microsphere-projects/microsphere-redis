# microsphere-redis-spring-test

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-test` |
| Git branch | `release` (0.2.x line) |
| Latest release | `0.2.14` |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-spring-test` |
| Compatibility | JUnit 5 (Jupiter), Spring Test 6.x, Spring Data Redis 3.0.0 – 3.5.x, JDK 17+ |
| Scope | `test` — consumed only as a test-scope dependency |

## Overview

`microsphere-redis-spring-test` gives you a ready-to-run JUnit 5 base class wired to a **real local Redis server**.
It removes the boilerplate of declaring `RedisTemplate` / `StringRedisTemplate` / connection factory beans in every
integration test of Microsphere Redis features.

The project's own tests (in `microsphere-redis-spring`, `microsphere-redis-replicator-spring`, …) all extend it,
against a Redis instance provided by the repository's `docker/` compose setup (CI starts Redis at `127.0.0.1:6379`).

## Installation

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

Run a Redis server locally (e.g. `docker run -p 6379:6379 redis`) or via the repo's `docker/` compose file before
executing the tests.

## API Reference

### AbstractRedisTest

```java
@Disabled
@SpringJUnitConfig(classes = RedisConfig.class)
public abstract class AbstractRedisTest {

    public static final Method SET_METHOD;           // RedisConnection#set(byte[], byte[])
    public static final Object[] SET_METHOD_ARGS;    // ["key","value"] as UTF-8 bytes
    public static final String SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE; // "redisTemplate"

    @Autowired @Qualifier("redisTemplate")
    protected RedisTemplate redisTemplate;

    @Autowired @Qualifier("stringRedisTemplate")
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected ConfigurableApplicationContext context;

    protected void assertRedisTemplateSet(String key, String value);
    protected void assertStringRedisTemplateSet(String key, String value);
    public static void assertSet(RedisTemplate redisTemplate, String key, String value);
}
```

Notes:

- The class is `@Disabled` at the base level; subclasses are executed explicitly or by CI with Redis available.
- `assertSet(...)` performs `opsForValue().set(key, value)` then asserts the read-back equals the written value.

### RedisConfig

`io.microsphere.redis.spring.test.config.RedisConfig` declares three static `@Bean`s:

| Bean | Type | Details |
|------|------|---------|
| `redisTemplate` | `RedisTemplate` | wired to the factory below |
| `stringRedisTemplate` | `StringRedisTemplate` | wired to the factory below |
| `redisConnectionFactory` | `LettuceConnectionFactory` | hard-wired to `127.0.0.1:6379`; calls `afterPropertiesSet()` and `validateConnection()` eagerly, so tests fail fast when Redis is absent |

## Code Example

A typical subclass — combining the base fixtures with `@EnableRedisInterceptor` (pattern used by
`AbstractRedisCommandEventTest` → `RedisCommandEventIntegrationTest`):

```java
@EnableRedisInterceptor
class MyRedisCommandEventTest extends AbstractRedisTest {

    @Test
    void setPublishesCommandEvent() {
        List<RedisCommandEvent> events = new ArrayList<>();
        context.addApplicationListener((ApplicationListener<RedisCommandEvent>) events::add);

        assertStringRedisTemplateSet("key-A", "value-A");

        assertEquals(1, events.size());
        assertEquals("set", events.get(0).getMethodName());
    }
}
```

## Version & Compatibility

| Item | Value |
|------|-------|
| Version line | 0.2.x (`release` branch), latest release **0.2.14** |
| Redis server | any version accepting `SET`/`GET` over RESP (standalone instance on `127.0.0.1:6379`) |
| Spring stack | Spring Test 6.x / Spring Data Redis 3.0.0 – 3.5.x, Lettuce client |
| JDK | 17+ |
| Legacy | 0.1.x equivalent for Spring Boot 2 stacks: [../../1.x/microsphere-redis-spring-test.md](../1.x/microsphere-redis-spring-test.md) |

## Related Modules

- [microsphere-redis-spring](./microsphere-redis-spring.md) — the features most often tested with this module
- Developer guide: [../../developer-guide/current/microsphere-redis-spring-test.md](../../developer-guide/current/microsphere-redis-spring-test.md)

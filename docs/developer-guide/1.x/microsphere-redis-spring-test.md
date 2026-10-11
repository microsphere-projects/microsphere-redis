# microsphere-redis-spring-test — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-test` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Test-support library |

## 1.x Status

`git diff --stat origin/release-1.x origin/release -- microsphere-redis-spring-test` is **empty**:
the module is byte-for-byte identical on both branches. `AbstractRedisTest` (disabled by default,
`@SpringJUnitConfig(RedisConfig.class)`, `SET_METHOD`/`SET_METHOD_ARGS`/`SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE`
fixtures, `assertSet` helpers) and `RedisConfig` (static `@Bean` trio with a
`LettuceConnectionFactory("127.0.0.1", 6379)` that validates eagerly) work the same on the Boot 2 line.

For the full source analysis, patterns and flow, see the
[0.2.x spring-test developer guide](../current/microsphere-redis-spring-test.md).

## 1.x-Specific Notes

- Test dependencies resolve from `microsphere-spring-cloud-dependencies:0.1.26` (JUnit 5, spring-test 5.x,
  Lettuce 6.x-era).
- `docker/` compose on `release-1.x` provides the same local Redis used by these tests.

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-spring-test.md](../../user-guide/1.x/microsphere-redis-spring-test.md)
- Chinese: [../zh/microsphere-redis-spring-test.md](zh/microsphere-redis-spring-test.md)

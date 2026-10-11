# microsphere-redis-spring-boot — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring-boot` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Spring Boot 2 condition vocabulary |

## 1.x Status

`git diff --stat origin/release-1.x origin/release -- microsphere-redis-spring-boot/src/main` is **empty**:
the three condition annotations are identical on both lines —

- `@ConditionalOnRedisEnabled` → `@ConditionalOnProperty("microsphere.redis.enabled", matchIfMissing = true)`
  (ConditionalOnRedisEnabled.java:73 on both branches);
- `@ConditionalOnRedisInterceptorEnabled` → enabled + `microsphere.redis.interceptor.enabled` (:72–73);
- `@ConditionalOnRedisAvailable` → enabled + `@ConditionalOnClass(name = {RedisConnection,
  RedisMetadataLoader, RedisMethodInterceptor})` (:57–62), `@Target(TYPE)`.

Full analysis, pattern table and enablement flowchart:
[../current/microsphere-redis-spring-boot.md](../current/microsphere-redis-spring-boot.md).

## 1.x-Specific Notes

- Evaluated against Spring Boot **2.x** condition machinery; `@ConditionalOnClass(name=...)` semantics are
  the same, so applications relying on it are migration-safe to 0.2.x.
- Boot 2 users: the actuator `HasFeatures` contributions of the sibling cloud module (its 1.x
  `features.yaml`) reference Boot 2-era customizer class names; 0.2.x adds the Boot 3.1/3.4-only entries
  (`RedisConnectionDetails`, `LettuceClientOptionsBuilderCustomizer`).

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-spring-boot.md](../../user-guide/1.x/microsphere-redis-spring-boot.md)
- Chinese: [../zh/microsphere-redis-spring-boot.md](zh/microsphere-redis-spring-boot.md)

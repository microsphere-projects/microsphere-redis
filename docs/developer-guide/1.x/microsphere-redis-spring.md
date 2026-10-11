# microsphere-redis-spring — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-spring` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Core Spring integration |
| Baseline | Java 8, `javax.servlet`, Spring Data Redis 2.x |

> The interception architecture is **identical** to the
> [0.2.x spring developer guide](../current/microsphere-redis-spring.md) — same bootstrap chain
> (`RedisInitializer` → `RedisModuleInitializer` → registrar → `RedisContext`), same proxy/wrap strategies,
> same interceptor contract, same metadata repository triple-key caches, same property keys.
> This document records the verified 1.x deltas.

## Verified Source Diffs vs 0.2.x (`git diff --stat release-1.x release -- redis-spring/src/main`)

Only 8 Java files differ (everything else — annotations, context, interceptor, event, beans,
metadata, util packages — is unchanged):

| File | Delta on 1.x |
|------|--------------|
| `serializer/RedisZSetCommandsWeightsSerializer.java` | exists on 1.x only (58 lines) — serializes `RedisZSetCommands.Weights` under the long name; renamed to `WeightsSerializer` on 0.2.x |
| `serializer/WeightsSerializer.java` | 0.2.x-only counterpart (37 lines) |
| `serializer/Serializers.java` | 1.x registry wires `RedisZSetCommandsWeightsSerializer`; otherwise the same static-singleton catalog (86-line diff driven mostly by the rename and registration shuffling) |
| `serializer/AbstractSerializer.java` | 10-line diff: Java 8-friendly generics/utils, no records |
| `serializer/ByteArraySerializer.java`, `EnumSerializer.java` | 1-line diffs (imports/annotation cosmetics) |
| `serializer/HoldingValueRedisSerializerWrapper.java` | 6-line diff, same object↔bytes holding contract |
| `serializer/RedisZSetCommandsRangeSerializer.java` | 2-line diff |
| `beans/WrapperProcessors.java` | 4-line diff (processor list composition) |
| `connection/dynamic/web/DynamicRedisConnectionFactoryCleanerListener.java` | **`javax.servlet` `ServletRequestListener`** (0.2.x: `jakarta.servlet`) |
| `util/SpringRedisCommandUtils.java` | 57-line diff — SDR 2.x command-surface helpers (no 3.x-only interfaces) |
| `resources/META-INF/spring-data-redis-metadata.yaml` | `version: 2.7.18` vs `3.5.10` (306-line diff = SDR 2.x vs 3.x method sets) |

`spring.factories` on 1.x registers the **same three entries** as 0.2.x:

```properties
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.redis.spring.context.RedisInitializer
io.microsphere.redis.spring.context.RedisModuleInitializer=\
io.microsphere.redis.spring.context.RedisInterceptorModuleInitializer
org.springframework.data.redis.serializer.RedisSerializer=\
io.microsphere.redis.spring.serializer.RedisCommandEventSerializer
```

plus the `META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader` SPI file.

## What This Means in Practice

- **API-compatible**: `@EnableRedisInterceptor` attributes (`wrapRedisTemplates`, `exposeCommandEvent`,
  `sources`), `RedisMethodContext`, interceptor interfaces, `RedisCommandEvent`, property keys —
  verified identical. Interceptor code written on 1.x compiles unchanged on 0.2.x.
- **Metadata-boundary**: a command that exists only in SDR 3.x has no `MethodMetadata` on 1.x, so
  `isWriteMethod()`/index lookups will not resolve it — do not replicate across lines.
- **Servlet namespace**: `DynamicRedisConnectionFactory` thread-cleanup relies on `javax.servlet`
  on this line; under Tomcat 10 / Boot 3 use 0.2.x instead.
- **Serializer rename gotcha**: bean/class references to
  `io.microsphere.redis.spring.serializer.RedisZSetCommandsWeightsSerializer` (1.x) must be updated to
  `WeightsSerializer` when migrating to 0.2.x.

## Design Patterns / Key Flows

All ten patterns (JDK proxy, AOP factory proxy, template decorator, interceptor chain, context object,
observer, template method, registry, SPI, BeanDefinition mutation, thread-local session state) and the
three diagrams apply unchanged — see
[../current/microsphere-redis-spring.md](../current/microsphere-redis-spring.md).

## Testing Strategy

Same suite shape as 0.2.x (annotation/context tests + integration against local Redis via
spring-test module); 1.x builds run against the Boot 2.x dependency line from the 0.1.26 stack.

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-spring.md](../../user-guide/1.x/microsphere-redis-spring.md)
- Chinese: [../zh/microsphere-redis-spring.md](zh/microsphere-redis-spring.md)

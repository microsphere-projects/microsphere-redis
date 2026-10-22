# Source Diff (src/main): `0.2.14` vs `0.1.14`

This document compares the `src/main` content of the two latest tags of Microsphere Redis,
module by module, for Maven modules that exist under the **same name** in both tags.

- **`0.2.14`** — 0.2.x line: **Java 17**, **Jakarta EE**, Spring Data Redis **3.5.10** (Spring Boot 3.x).
- **`0.1.14`** — 0.1.x line: **Java 8**, **Java EE (`javax`)**, Spring Data Redis **2.7.18** (Spring Boot 2.x).

The comparison **excludes** the following categories, which are expected by design between the two
lines and are therefore *not* listed in the tables:

1. **Java 8 vs Java 17 language/API syntax** — `record`, pattern-matching `instanceof`, `switch`
   expressions, and their direct consequences (e.g. `getData()` → `data()`).
2. **Java EE vs Jakarta EE API** — package renames such as `javax.servlet.*` → `jakarta.servlet.*`.

Modules whose name does not match across the two tags are ignored.

## Module coverage

| Maven module | `src/main` present in both? | Meaningful differences (after exclusions) |
|---|---|---|
| `microsphere-redis-core` | Yes | None (only Java-17 `record`/`instanceof` changes, excluded) |
| `microsphere-redis-dependencies` | Yes | None |
| `microsphere-redis-generator` | Yes | None (only a blank-line cleanup) |
| `microsphere-redis-parent` | Yes | None |
| `microsphere-redis-replicator-spring` | Yes | Yes |
| `microsphere-redis-spring` | Yes | Yes |
| `microsphere-redis-spring-boot` | Yes | None |
| `microsphere-redis-spring-cloud` | Yes | Yes |
| `microsphere-redis-spring-test` | Yes | None |

---

## `microsphere-redis-core`

Only two files differ, both purely Java-17 language features and thus excluded:

- `metadata/RedisMetadata.java` — `equals()` rewritten with pattern-matching `instanceof RedisMetadata that`.
- `util/RawValue.java` — converted from a class to a `record`; `util/ValueHolder.java` follows with `getData()` → `data()`.

**No functional difference.**

---

## `microsphere-redis-replicator-spring`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `kafka/producer/KafkaProducerRedisCommandEventListener` | `kafkaTemplate.send(...).completable()` | `kafkaTemplate.send(...)` | **API compatibility** — newer Spring for Apache Kafka `KafkaTemplate.send()` already returns `CompletableFuture`, so `.completable()` is dropped. |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration` | Guards `KafkaTemplate` with `instanceof ApplicationContextAware`/`SmartInitializingSingleton`/`DisposableBean` before calling | Calls `setApplicationContext()`, `afterSingletonsInstantiated()`, `destroy()` directly; adds `setObservationEnabled(true)` | **API compatibility + functional** — `KafkaTemplate` now implements those lifecycle interfaces natively; observation support is enabled for the new micrometer-observation pipeline. |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration` | `destroy...() throws Exception` | `destroy...()` (no checked exception) | **API compatibility** — `KafkaTemplate.destroy()` no longer declares a checked exception. |

---

## `microsphere-redis-spring`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `beans/WrapperProcessors` | `getResolvableType((AbstractBeanDefinition) beanDefinition)` helper | `beanDefinition.getResolvableType()` | **API compatibility** — Spring's `BeanDefinition` now exposes `getResolvableType()` directly, removing the need for a cast + helper. |
| `serializer/AbstractSerializer` | `resolveTargetType(this)` static helper; also resolved via `AbstractSerializer`/`RedisSerializer` branch | private `resolvableType()` always `as(RedisSerializer.class)` | **API compatibility** — `RedisSerializer` itself declares the target-type resolution, so the extra branch is unnecessary on 3.x. |
| `serializer/AbstractSerializer`, `ByteArraySerializer`, `EnumSerializer` | `getTargetType()` overrides nothing | `getTargetType()` annotated `@Override` | **API compatibility** — `getTargetType()` became an interface method on `RedisSerializer` in Spring Data Redis 3.x. |
| `serializer/HoldingValueRedisSerializerWrapper` | Delegates via `Serializers.canSerialize(...)` / `Serializers.getTargetType(...)` reflection helpers | Delegates via `this.delegate.canSerialize(...)` / `getTargetType()` | **API compatibility** — the interface methods are now native, so the reflection fallback is removed. |
| `serializer/Serializers` | Reflection-based `canSerialize`/`getTargetType`/`doGetTargetType` via `MethodHandle` for pre-2.2 compatibility; `doRegister(...)`; imports `RedisZSetCommands.Aggregate`/`Weights` | Reflection helpers deleted; relies on `RedisSerializer` native methods; imports `connection.zset.Aggregate`/`Weights`; `register()` inlined and logs via `serializer.getTargetType()` | **API compatibility** — the baseline moved to Spring Data Redis 3.x, so the backward-compat reflection shim is dropped. |
| `serializer/RedisZSetCommandsWeightsSerializer` (**deleted**) → `serializer/WeightsSerializer` (**added**) | Serializer for `RedisZSetCommands.Weights` | Serializer for `connection.zset.Weights` | **API reorganization** — `Weights` moved to the `zset` sub-package in Spring Data Redis 3.x; the serializer is renamed to match. (Note: the new file omits the Apache license header.) |
| `serializer/RedisZSetCommandsRangeSerializer` | Normal class | Marked `@Deprecated` | **API reorganization** — `RedisZSetCommands.Range` is superseded by `org.springframework.data.domain.Range`, so this serializer is deprecated. |
| `util/SpringRedisCommandUtils` | Legacy `switch` mapping: TX/PubSub/Connection/default/stream return `redisConnection`; stream falls through to `redisConnection` | `getRedisCommands()` returns `redisConnection.commands()` for TX/PubSub/Connection/default, and `redisConnection.streamCommands()` for the stream interface | **API compatibility + functional** — Spring Data Redis 3.x adds `RedisCommandsProvider.commands()` and `RedisConnection.streamCommands()`, so the correct sub-interfaces are now returned instead of the connection itself. (The `switch` → `switch`-expression rewrite is excluded as Java-17 syntax.) |
| `resources/META-INF/spring-data-redis-metadata.yaml` | `version: 2.7.18` | `version: 3.5.10` | **Functional** — regenerated command metadata against Spring Data Redis 3.5.10. Changes reflect the upstream API surface: `RedisZSetCommands.Range`→`domain.Range`, `RedisZSetCommands.Limit`→`connection.Limit`, `Aggregate`/`Weights`→`connection.zset.*`; `slaveOf`/`slaveOfNoOne`→`replicaOf`/`replicaOfNoOne` (`SLAVEOF`→`REPLICAOF`); `bgWriteAof`→`bgReWriteAof`; new `ReactiveStringCommands.setGet` / `RedisStringCommands.setGet` overloads. |

---

## `microsphere-redis-spring-cloud`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `autoconfigure/RedisCloudAutoConfiguration` | `@AutoConfigureAfter` lists only `...data.redis.RedisAutoConfiguration` (Boot `[2.0, 4.0)`) | Adds `org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration` (Boot `[4.0,)`) | **Functional** — keeps correct auto-configuration ordering across the Spring Boot 4.x relocation of the Redis auto-config class. |
| `resources/META-INF/config/default/features.yaml` | No connection factories; `RedisZSetCommandsRangeSerializer` only | Adds `RedisConnectionFactory` / `ReactiveRedisConnectionFactory`; adds Boot 3.4 `LettuceClientOptionsBuilderCustomizer` and Boot 3.1 `RedisConnectionDetails`; registers `WeightsSerializer` | **Functional** — the feature-detection manifest is extended to cover newer Spring Boot Redis customizers and the renamed serializer. |

---

## Notes on the excluded categories

The following diffs appear between the two tags but are **intentionally excluded** from the tables above:

| Location | Change | Excluded category |
|---|---|---|
| `core/RedisMetadata` | `equals()` uses pattern-matching `instanceof` | Java 8 vs Java 17 syntax |
| `core/RawValue`, `core/ValueHolder` | class → `record`, `getData()` → `data()` | Java 8 vs Java 17 syntax |
| `spring/DynamicRedisConnectionFactoryCleanerListener` | `javax.servlet.*` → `jakarta.servlet.*` | Java EE vs Jakarta EE API |
| `spring/SpringRedisCommandUtils` | `switch` statement → `switch` expression | Java 8 vs Java 17 syntax |

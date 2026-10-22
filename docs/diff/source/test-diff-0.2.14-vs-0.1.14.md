# Source Diff (src/test): `0.2.14` vs `0.1.14`

This document compares the `src/test` content of the two latest tags of Microsphere Redis,
module by module, for Maven modules that exist under the **same name** in both tags.

- **`0.2.14`** — 0.2.x line: **Java 17**, **Jakarta EE**, Spring Data Redis **3.5.10** (Spring Boot 3.x).
- **`0.1.14`** — 0.1.x line: **Java 8**, **Java EE (`javax`)**, Spring Data Redis **2.7.18** (Spring Boot 2.x).

The same exclusion rules as [`main-diff-0.2.14-vs-0.1.14.md`](./main-diff-0.2.14-vs-0.1.14.md) apply:
Java 8/17 syntax differences and Java EE/Jakarta EE API differences are not listed.

## Module coverage

| Maven module | `src/test` present in both? | Meaningful differences (after exclusions) |
|---|---|---|
| `microsphere-redis-core` | Yes | None (record accessor change, excluded) |
| `microsphere-redis-generator` | Yes | None |
| `microsphere-redis-replicator-spring` | Yes | Yes |
| `microsphere-redis-spring` | Yes | Yes |
| `microsphere-redis-spring-boot` | Yes | None |
| `microsphere-redis-spring-cloud` | Yes | Yes |
| `microsphere-redis-spring-test` | Yes | None |

---

## `microsphere-redis-core`

- `util/RawValueTest` — `rawValue.getData()` → `rawValue.data()`.

Consequence of the `RawValue` → `record` migration; **excluded** as a Java-17 syntax change. **No functional difference.**

---

## `microsphere-redis-replicator-spring`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `kafka/KafkaRedisReplicatorModuleInitializerTest` | Casts the context to `DefaultResourceLoader` (via `instanceof`) before `setClassLoader(...)` | Calls `this.context.setClassLoader(...)` directly | **API compatibility** — the test context type now exposes `setClassLoader(...)` natively, so the `instanceof`/cast guard is unnecessary. |

---

## `microsphere-redis-spring`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `AbstractRedisCommandEventTest` | `assertThrows(RuntimeException.class, ...)` for an unknown command | `assertThrows(RedisSystemException.class, ...)` | **API compatibility** — Spring Data Redis 3.x throws the more specific `RedisSystemException`, so the assertion is tightened. |
| `serializer/AbstractSerializerTest` | Uses reflection helpers `getTargetType(serializer)` / `canSerialize(serializer, type)`; resolves generic via `forType(getClass()).as(AbstractSerializerTest.class)` | Calls `serializer.getTargetType()` / `serializer.canSerialize(type)`; resolves generic via `forType(getClass()).getSuperType()` | **API compatibility** — mirrors the production change: native `RedisSerializer` methods replace the removed reflection helpers. |
| `serializer/SerializersTest` | Tests `canSerialize`, `getTargetType`, `doGetTargetType`; uses `RedisZSetCommands.Weights` + `REDIS_ZSET_COMMANDS_WEIGHTS_SERIALIZER` | Those tests removed (helpers deleted); uses `connection.zset.Weights` + `WEIGHTS_SERIALIZER` | **API compatibility / reorganization** — follows the `Serializers` cleanup and the `Weights`→`zset` move. |
| `serializer/RedisZSetCommandsWeightsSerializerTest` (**deleted**) → `serializer/WeightsSerializerTest` (**added**) | Tests serializer for `RedisZSetCommands.Weights` | Tests serializer for `connection.zset.Weights` | **API reorganization** — renamed test matching the renamed serializer. |
| `util/SpringRedisCommandUtilsTest` | Loads stream interfaces reflectively (`loadClass(...)` guarded), asserts `assertSameType(...)` (compares `getClass()`), expects `RedisStringCommands.set(...)` as method id | Asserts `RedisStreamCommands`/`ReactiveStreamCommands` directly, adds `RedisCommandsProvider` negative case, uses `assertSame(...)` against `redisConnection.commands()` / `streamCommands()`, expects `DefaultedRedisConnection.set(...)` as method id | **API compatibility + functional** — Spring Data Redis 3.x always provides the stream interfaces and routes defaulted interface methods through `DefaultedRedisConnection` / `RedisCommandsProvider`, so the reflective-optional guards become direct assertions. |

---

## `microsphere-redis-spring-cloud`

| File | `0.1.14` | `0.2.14` | Reason |
|---|---|---|---|
| `event/PropagatingRedisConfigurationPropertyChangedEventApplicationListenerTest` | Registers the listener but never removes it | Adds `this.context.removeApplicationListener(listener)` at the end | **Functional / test hygiene** — deregisters the listener to avoid cross-test leakage and to close the file (adds trailing newline). |

---

## Notes on the excluded categories

| Location | Change | Excluded category |
|---|---|---|
| `core/RawValueTest` | `getData()` → `data()` (record accessor) | Java 8 vs Java 17 syntax |
| `spring/RedisMethodContextTest` | Drops `param.isNamePresent()` fallback because 0.2.x compiles with `-parameters` (parameter names always available) | Java 8 vs Java 17 build/language behavior |
| `spring/EnumSerializerTest` | `calcBytesLength(SpringVersion.values())` expects `2` → `1` | Dependency-driven value change (Spring `SpringVersion` enum cardinality), not an implementation difference in this project |

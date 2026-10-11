# microsphere-redis-core — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-core` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-core:0.1.14` |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, Java 8+ |

> This document covers the **1.x** version line. For the 0.2.x line, see
> [../current/microsphere-redis-core.md](../current/microsphere-redis-core.md).

## Overview

`microsphere-redis-core` is the framework-agnostic foundation of microsphere-redis: the Redis command
metadata model, the metadata loader SPI, the command catalogs, and the raw-value (`byte[]` ↔ object)
holding mechanism. It has **no Spring dependency**, so you can use it standalone to reason about
Redis commands (e.g. "is `GETSET` a write command?") in any tooling.

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-core</artifactId>
    <version>0.1.14</version>
</dependency>
```

(or via the `microsphere-redis-dependencies` BOM — see its [1.x guide](./microsphere-redis-dependencies.md)).

## API Reference

### Metadata model (`io.microsphere.redis.metadata`)

| Type | Purpose |
|------|---------|
| `RedisMetadata` | `{version, List<MethodMetadata>}` loaded from YAML; `merge(RedisMetadata)` folds loader contributions (the other's version wins, methods appended) |
| `MethodMetadata` | one command method: `index`, `interfaceName`, `methodName`, `parameterNames`, `parameterTypes`, `commands`, `write` |
| `MethodInfo` | runtime join of reflective `Method` + `MethodMetadata` + parameter metadata |
| `ParameterMetadata` / `Parameter` | parameter description (index/type/name) and its runtime binding (`value`, `rawValue`) |
| `RedisMetadataLoader` | SPI; `loadAll()` discovers implementations via `ServiceLoader` and merges them |

### Utilities (`io.microsphere.redis.util`)

| Type | Purpose |
|------|---------|
| `RawValue` | immutable wrapper over `byte[]` with content-based `equals/hashCode` (1.x: a final-field class with `RawValue.of(bytes)` factory and constructor — in 0.2.x it became a `record`) |
| `ValueHolder` | per-thread (`ThreadLocal`) bidirectional cache mapping Java objects ↔ serialized bytes; `set/getValue/getRawValue/get/clear` |
| `RedisCommandUtils` | command catalogs from `META-INF/redis-commands` and `META-INF/redis-write-commands`; `isRedisCommand(name)`, `isRedisWriteCommand(name)`; `buildMethodId/Signature/Index` canonical id grammar |
| `RedisUtils` | classpath resource readers `loadResource`/`loadResources` |

## Code Examples

```java
// Is a server command a write command? (source pattern: RedisCommandUtilsTest)
assertTrue(isRedisWriteCommand("SET"));
assertFalse(isRedisWriteCommand("GET"));

// Canonical method identity — the same grammar the YAML generator uses
int index = RedisCommandUtils.buildMethodIndex(
        "org.springframework.data.redis.connection.KeyCommands", "expire", "[B");

// Raw-value round trip: recover the original object from serialized bytes
byte[] bytes = "value".getBytes(UTF_8);
ValueHolder holder = ValueHolder.get();
holder.set("value", bytes);                       // done for you by redis-spring serializers
Object original = ValueHolder.getValue(bytes);    // -> "value"
ValueHolder.clear();                              // required at thread end
```

## Version & Compatibility

| microsphere-redis | branch | Spring Data Redis | Spring Boot | Java |
|-------------------|--------|-------------------|-------------|------|
| `0.1.x` (this doc, latest `0.1.14`) | `release-1.x` | 2.1.0 – 2.7.x | 2.x | 8+ |
| `0.2.x` (latest `0.2.14`) | `release` | 3.0.0 – 3.5.x | 3.x | 17 |

1.x `RawValue` predates the record rewrite; the rest of this module's API is source-compatible between
the two lines, so code written against it generally compiles on both.

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md) — the runtime consumer
- Developer perspective: [../../developer-guide/1.x/microsphere-redis-core.md](../../developer-guide/1.x/microsphere-redis-core.md)

# microsphere-redis-core

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-core` |
| Git branch | `release` (0.2.x line) |
| Latest release | `0.2.14` |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-core` |
| Compatibility | Spring Data Redis 3.0.0 – 3.5.x metadata, JDK 17+ |
| External dependencies | `microsphere-lang` (utilities), no Spring dependency |

## Overview

`microsphere-redis-core` is the **foundation layer** of Microsphere Redis. It models *Redis command metadata* — the
mapping between Spring Data Redis `Redis*Commands` interface methods and the actual Redis server commands (e.g.
`RedisStringCommands#set(byte[], byte[])` ↔ `SET`) — and provides small utilities that the higher-level Spring modules
build upon.

The module deliberately has **no dependency on Spring**. It can be used standalone for tooling, code generation or
metadata analysis.

You normally do not add this module directly; it arrives transitively through `microsphere-redis-spring`. Add it
explicitly only when you need the metadata model without the Spring integration.

Key capabilities:

- Immutable POJO metadata model: [`RedisMetadata`](#redismetadata), [`MethodMetadata`](#methodmetadata),
  [`ParameterMetadata`](#parametermetadata), [`Parameter`](#parameter), [`MethodInfo`](#methodinfo)
- SPI for metadata contribution: [`RedisMetadataLoader`](#redismetadataloader-spi)
- Redis command catalogs: [`RedisCommandUtils`](#rediscommandutils)
- Thread-bound raw value cache: [`ValueHolder`](#valueholder) and [`RawValue`](#rawvalue)
- Classpath resource loading helpers: [`RedisUtils`](#redisutils)

## Installation

Recommended: import the BOM first, then declare the module.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>0.2.14</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-core</artifactId>
    </dependency>
</dependencies>
```

## API Reference

### RedisMetadata

`io.microsphere.redis.metadata.RedisMetadata` — top-level container: a schema `version` string plus a list of
`MethodMetadata`. Typically deserialized from `META-INF/spring-data-redis-metadata.yaml` on the classpath.

| Method | Description |
|--------|-------------|
| `String getVersion()` / `void setVersion(String)` | metadata schema/version (e.g. the Spring Data Redis version it was generated from) |
| `List<MethodMetadata> getMethods()` | lazily-initialized, mutable method list |
| `RedisMetadata merge(RedisMetadata another)` | adopts `another`'s version and appends its methods; returns `this` for chaining |

The `merge(...)` semantics make `RedisMetadata` a **composite of contributions**: every SPI loader contributes one
instance and `loadAll()` folds them together.

### MethodMetadata

`io.microsphere.redis.metadata.MethodMetadata` — describes one Redis command method of a Spring Data Redis
`Redis*Commands` interface.

| Property (getter/setter) | Meaning / example |
|---------------------------|-------------------|
| `int index` | numeric method id — absolute hash of the method id, set by `buildMethodIndex(...)` |
| `String interfaceName` | declaring interface, e.g. `org.springframework.data.redis.connection.RedisStringCommands` |
| `String methodName` | e.g. `set` |
| `String[] parameterNames` | e.g. `["key", "value"]` (from Javadoc/`-parameters`) |
| `String[] parameterTypes` | JVM type names, e.g. `["[B", "[B"]` for `(byte[], byte[])` |
| `String[] commands` | mapped Redis commands, e.g. `["SET"]` |
| `boolean write` | whether the method issues a write command |

### ParameterMetadata / Parameter

- `ParameterMetadata` — immutable: `ParameterMetadata(int parameterIndex, String parameterType)`,
  `ParameterMetadata(int parameterIndex, String parameterType, String parameterName)`; accessors
  `getParameterIndex()`, `getParameterType()`, `getParameterName()` (nullable).
- `Parameter` — a runtime *binding* of one argument: `Parameter(Object value, ParameterMetadata metadata)` with
  `getValue()`, `getMetadata()`, `getRawValue()` / `setRawValue(byte[])` (nullable raw serialization),
  `getParameterIndex()`, `getParameterType()`.

### MethodInfo

`MethodInfo(Method method, MethodMetadata methodMetadata, List<ParameterMetadata> parameterMetadataList)` joins a
live `java.lang.reflect.Method` with its metadata. Accessors: `getId()`, `getIndex()`, `getName()`, `getMethod()`,
`getMethodMetadata()`, `getParameterMetadataList()`.

### RedisMetadataLoader (SPI)

`io.microsphere.redis.metadata.RedisMetadataLoader` is a `ServiceLoader` SPI:

```java
public interface RedisMetadataLoader {
    RedisMetadata load();                 // contribution of this loader

    static RedisMetadata loadAll() {      // discovers all META-INF/services
                                          // implementations and merges them
    }
}
```

Register a custom implementation in
`META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader` (one class name per line).

### RedisCommandUtils

`io.microsphere.redis.util.RedisCommandUtils` loads the command catalogs bundled in the module
(`META-INF/redis-commands` and `META-INF/redis-write-commands`):

| Method | Description |
|--------|-------------|
| `Set<String> getRedisCommands()` | all known Redis command names |
| `Set<String> getRedisWriteCommands()` | the write-command subset |
| `boolean isRedisCommand(String)` / `boolean isRedisWriteCommand(String)` | membership checks (case-sensitive catalog) |
| `String buildMethodId(Method)` and overloads `(Class<?>, String, Class<?>...)`, `(String, String, Class<?>...)`, `(String, String, String...)` | canonical method id: `interfaceName.methodName(paramTypeA,paramTypeB)` |
| `String buildMethodSignature(...)` overloads | readable signature `methodName(typeA,typeB)` |
| `int buildMethodIndex(...)` overloads | absolute hash of the method id — the stable `index` used in `MethodMetadata` |
| `List<ParameterMetadata> buildParameterMetadataList(Method [, Class<?>[]])` | builds parameter metadata from reflection |
| `String[] getParameterClassNames(Class<?>...)` | JVM names of parameter types |

### ValueHolder and RawValue

`ValueHolder` is a **thread-bound bidirectional cache** between Java objects and their serialized `byte[]` form. It is
the mechanism that lets interceptors recover the original command argument from the serialized bytes that actually
reach the Redis connection:

```java
public class ValueHolder {
    public ValueHolder(int initialCapacity)
    public void set(Object value, byte[] rawValue)   // remember both directions
    public Object getValue(byte[] rawValue)          // bytes -> original object (or null)
    public byte[] getRawValue(Object value)          // object -> bytes (or null)
    public static ValueHolder get()                  // ThreadLocal current instance
    public static void clear()                       // remove the ThreadLocal instance
}
```

`RawValue` is `record RawValue(byte[] data)` with factory `RawValue.of(byte[])` — the value-side wrapper used inside
the cache.

### RedisUtils

`io.microsphere.redis.util.RedisUtils` — classpath resource loading:

- `<T> T loadResource(String, ThrowableFunction<InputStream, T>)` (+ overload with explicit `ClassLoader`)
- `<T> T loadResources(String, ThrowableFunction<List<InputStream>, T>)` (+ overload)
- `ClassLoader CLASS_LOADER` — the default class loader used

These helpers back both the command catalogs and the YAML metadata loading in the Spring module.

## Code Examples

Loading all metadata contributed on the classpath (from `RedisMetadataLoaderTest`):

```java
RedisMetadata metadata = RedisMetadataLoader.loadAll();
System.out.println(metadata.getVersion());        // e.g. "3.5.10"
System.out.println(metadata.getMethods().size()); // number of mapped command methods
```

A custom loader (registered under `META-INF/services`), mirroring `TestRedisMetadataLoader`:

```java
public class MyRedisMetadataLoader implements RedisMetadataLoader {
    @Override
    public RedisMetadata load() {
        RedisMetadata metadata = new RedisMetadata();
        metadata.setVersion("1.0.0");
        return metadata;
    }
}
```

Holding and recovering raw values (from `ValueHolderTest.testSetAndGet`):

```java
ValueHolder holder = new ValueHolder(4);
String value = "test-value";
byte[] rawValue = "test-raw-value".getBytes();
holder.set(value, rawValue);

assertEquals(value, holder.getValue(rawValue));           // bytes -> object
assertArrayEquals(rawValue, holder.getRawValue(value));   // object -> bytes
```

Method identity for interception (from `RedisCommandUtilsTest`):

```java
Method method = RedisStringCommands.class.getMethod("set", byte[].class, byte[].class);
String methodId = RedisCommandUtils.buildMethodId(method);       // "…RedisStringCommands.set([B,[B"
int index = RedisCommandUtils.buildMethodIndex(method);          // stable numeric id == MethodMetadata.index
assertTrue(RedisCommandUtils.isRedisWriteCommand("SET"));
```

## Version & Compatibility

| Item | Value |
|------|-------|
| Version line | 0.2.x (`release` branch), latest release **0.2.14** |
| Build JDK | 17 (module uses `record` and pattern-matching `instanceof`) |
| Runtime JDK | 17+ |
| Metadata target | Spring Data Redis `Redis*Commands` interfaces 3.0.0 – 3.5.x (metadata YAML regenerated per Spring Data Redis version, see `version:` key) |
| Legacy line | 0.1.x (`release-1.x` branch) targets Spring Data Redis 2.1.0 – 2.7.x — see [../../1.x/microsphere-redis-core.md](../1.x/microsphere-redis-core.md) |

## Related Modules

- [microsphere-redis-spring](./microsphere-redis-spring.md) — consumes the metadata model at runtime
- [microsphere-redis-generator](./microsphere-redis-generator.md) — produces the metadata YAML this module models
- Developer guide: [../../developer-guide/current/microsphere-redis-core.md](../../developer-guide/current/microsphere-redis-core.md)

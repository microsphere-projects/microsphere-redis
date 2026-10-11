# microsphere-redis-generator — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-generator` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-generator:0.1.x` (build-time only, not in BOM) |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x` (1.x metadata generated against **2.7.18**), JDK **9+** |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-generator.md](../current/microsphere-redis-generator.md).

## Overview

Build-time tool that parses the `spring-data-redis` **sources jar** with a custom JDK `Doclet` and emits
`META-INF/spring-data-redis-metadata.yaml` — the mapping of every `Redis*Commands` interface method to
Redis server commands — into the `microsphere-redis-spring` resources.

**1.x difference from 0.2.x:** the generator module is only part of the reactor under the
`java9+` profile (`<activation><jdk>[9,)</jdk></activation>` in the root pom), because the 1.x runtime
line targets Java 8 while the doclet needs JDK 9+ APIs. On JDK 8 builds the pre-generated YAML simply
ships with the module.

## Usage

You rarely invoke it directly — building the module runs it automatically:

```bash
# from the repository root, on the release-1.x branch, JDK 9+
mvn -pl microsphere-redis-generator -am package
# 1) maven-dependency-plugin unpacks spring-data-redis:2.7.x sources (*Commands.java only)
# 2) exec-maven-plugin runs SpringDataRedisMetadataGenerator
#    -> microsphere-redis-spring/src/main/resources/META-INF/spring-data-redis-metadata.yaml
```

Manual invocation (same signature):

```bash
java -classpath <generator classpath> --add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
  io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator \
  <source-path> <target-metadata.yaml>
```

## Output Contract

```yaml
version: 2.7.18
methods:
- index: 498782566
  interfaceName: org.springframework.data.redis.connection.RedisHyperLogLogCommands
  methodName: pfAdd
  parameterNames: [key, values]
  parameterTypes: ['[B', '[[B']
  commands: [PFADD]
  write: true
```

The file loads into `microsphere-redis-core`'s `RedisMetadata`; `index` follows the same
`RedisCommandUtils.buildMethodIndex` grammar the runtime uses, so any Spring Data Redis 2.x version's
YAML is self-consistent with the 1.x repository.

## Version & Compatibility

| microsphere-redis | Spring Data Redis parsed | JDK requirement |
|-------------------|--------------------------|-----------------|
| `0.1.14` (this line) | 2.1.0 – 2.7.x (bundled YAML: 2.7.18) | build on 9+, runtime line is Java 8 |
| `0.2.14` | 3.0.0 – 3.5.x (bundled YAML: 3.5.10) | 17 (unconditional module in reactor) |

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md) — consumes the YAML
- [microsphere-redis-core (1.x)](./microsphere-redis-core.md) — supplies the catalogs the generator validates against
- Developer guide: [../../developer-guide/1.x/microsphere-redis-generator.md](../../developer-guide/1.x/microsphere-redis-generator.md)

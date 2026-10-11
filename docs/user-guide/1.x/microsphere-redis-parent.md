# microsphere-redis-parent — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-parent` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-parent:0.1.14` (pom) |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-parent.md](../current/microsphere-redis-parent.md).

## Overview

The inheritance parent for the 1.x modules. It imports the `microsphere-spring-cloud-dependencies:0.1.26`
BOM, which pins the Spring Framework 5.x / Spring Boot 2.x / Spring Data Redis 2.7.x stack — that is what
makes the whole `0.1.x` line the "Spring Boot 2" distribution of microsphere-redis.

## Usage as parent

```xml
<parent>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.1.14</version>
</parent>
```

## Usage as BOM (preferred for existing Boot 2 apps)

Most applications should not replace their parent; import the sibling BOM instead —
[microsphere-redis-dependencies (1.x)](./microsphere-redis-dependencies.md).

## Structure Notes (1.x)

- Root reactor (`microsphere-redis/pom.xml`): `revision=0.1.15-SNAPSHOT`, tip after published `0.1.14`.
- Unlike 0.2.x, the root pom declares **no `java.version` override**; the baseline comes from
  `microsphere-spring-cloud-parent:0.1.26` (Java 8 line), and the `microsphere-redis-generator` module
  is only activated under the `java9+` JDK profile.
- Modules: parent, dependencies, core, spring, spring-test, replicator-spring, spring-boot, spring-cloud
  (same topology as 0.2.x).

## Version & Compatibility

| microsphere-redis | branch | Spring Cloud stack | Spring Data Redis | Spring Boot |
|-------------------|--------|--------------------|-------------------|-------------|
| `0.1.14` | `release-1.x` | microsphere-spring-cloud 0.1.26 | 2.1.0 – 2.7.x | 2.x |
| `0.2.14` | `release` | microsphere-spring-cloud 0.2.26 | 3.0.0 – 3.5.x | 3.x |

## Related Modules

- [microsphere-redis-dependencies (1.x)](./microsphere-redis-dependencies.md)
- Developer guide: [../../developer-guide/1.x/microsphere-redis-parent.md](../../developer-guide/1.x/microsphere-redis-parent.md)

# microsphere-redis-dependencies — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-dependencies` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-dependencies:0.1.14` (pom / BOM) |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-dependencies.md](../current/microsphere-redis-dependencies.md).

## Overview

Version-only BOM for the six runtime modules of the 1.x line. Importing it inside
`dependencyManagement` pins every microsphere-redis artifact to `0.1.14` without touching your
Spring Boot parent.

## Managed Artifacts

| artifactId | purpose |
|------------|---------|
| `microsphere-redis-core` | metadata model + utilities |
| `microsphere-redis-spring` | interception pipeline |
| `microsphere-redis-spring-test` | test-support fixtures |
| `microsphere-redis-replicator-spring` | Kafka command replication |
| `microsphere-redis-spring-boot` | Boot 2 condition annotations |
| `microsphere-redis-spring-cloud` | cloud property refresh |

`microsphere-redis-generator` is intentionally **not managed** (build-time tool, not a runtime dependency).

## Installation

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.microsphere</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>0.1.14</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.microsphere</groupId>
        <artifactId>microsphere-redis-spring</artifactId> <!-- no version needed -->
    </dependency>
</dependencies>
```

## Version & Compatibility

- The BOM's parent is `microsphere-redis-parent:0.1.14`, so importing it transitively aligns Spring Data
  Redis 2.7.x-era versions — verified against the generated metadata header `version: 2.7.18`.
- Never mix: import either the `0.1.x` BOM **or** the `0.2.x` BOM, matching your Spring Boot major version
  (2.x ↔ 0.1.x, 3.x ↔ 0.2.x).

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md) — the module you most likely add first
- Developer guide: [../../developer-guide/1.x/microsphere-redis-dependencies.md](../../developer-guide/1.x/microsphere-redis-dependencies.md)

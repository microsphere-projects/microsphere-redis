# microsphere-redis-dependencies — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-dependencies` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Build: BOM (`dependencyManagement` only, pom) |

## Verified Facts (`git show origin/release-1.x:microsphere-redis-dependencies/pom.xml`)

Manages the same **six** runtime modules at `${revision}` and at the same line positions as 0.2.x:

| artifactId | line |
|------------|------|
| `microsphere-redis-core` | :26 |
| `microsphere-redis-spring` | :32 |
| `microsphere-redis-spring-test` | :38 |
| `microsphere-redis-replicator-spring` | :44 |
| `microsphere-redis-spring-boot` | :50 |
| `microsphere-redis-spring-cloud` | :56 |

`microsphere-redis-generator` is again intentionally excluded (build-time tool). The parent is
`microsphere-redis-parent` (→ `microsphere-spring-cloud-dependencies:0.1.26`), so a 1.x BOM import aligns
consumers to Boot 2.x / SDR 2.7.x-era versions.

## Version-Selection Guidance

| Your stack | Use |
|-----------|-----|
| Spring Boot 2.x / spring-data-redis 2.1.0–2.7.x / Java 8+ | `microsphere-redis-dependencies:0.1.14` (this line) |
| Spring Boot 3.x / spring-data-redis 3.0.0–3.5.x / Java 17 | `microsphere-redis-dependencies:0.2.14` ([../current/microsphere-redis-dependencies.md](../current/microsphere-redis-dependencies.md)) |

Never mix the two BOMs in one dependency tree — the metadata YAML, servlet namespace and serializer class
names diverge (see [1.x spring guide](./microsphere-redis-spring.md)).

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-dependencies.md](../../user-guide/1.x/microsphere-redis-dependencies.md)
- Chinese: [../zh/microsphere-redis-dependencies.md](zh/microsphere-redis-dependencies.md)

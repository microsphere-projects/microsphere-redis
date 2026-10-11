# User Guide

English | [中文](./zh/README.md)

The User Guide is written for **application developers** consuming Microsphere Redis as a dependency. Each Maven
module has its own document containing:

- **API explanations** — every public class, annotation, event and interceptor with method-level descriptions
- **Code examples** — derived from the project's own test cases under `src/test/java`
- **Version information** — artifact coordinates, released versions and the compatibility range of each module

## Choose your release line

| Release line | Docs | Git branch | Version | Spring Data Redis | Typical stack |
|--------------|------|-----------|---------|-------------------|---------------|
| Current      | [`current/`](./current/) | `release`   | 0.2.x (`0.2.14`) | 3.0.0 – 3.5.x | Spring Boot 3.x, Java 17+ |
| Legacy 1.x   | [`1.x/`](./1.x/)         | `release-1.x` | 0.1.x (`0.1.14`) | 2.1.0 – 2.7.x | Spring Boot 2.x, Java 8+ |

## Module documents (current / 0.2.x)

- [microsphere-redis-parent](./current/microsphere-redis-parent.md)
- [microsphere-redis-dependencies](./current/microsphere-redis-dependencies.md)
- [microsphere-redis-core](./current/microsphere-redis-core.md)
- [microsphere-redis-generator](./current/microsphere-redis-generator.md)
- [microsphere-redis-spring](./current/microsphere-redis-spring.md)
- [microsphere-redis-spring-test](./current/microsphere-redis-spring-test.md)
- [microsphere-redis-replicator-spring](./current/microsphere-redis-replicator-spring.md)
- [microsphere-redis-spring-boot](./current/microsphere-redis-spring-boot.md)
- [microsphere-redis-spring-cloud](./current/microsphere-redis-spring-cloud.md)

Chinese translations of every module document live in [`current/zh/`](./current/zh/) with identical file names.

## Module documents (1.x / 0.1.x)

The same nine modules are documented under [`1.x/`](./1.x/) (with Chinese in [`1.x/zh/`](./1.x/zh/)); note that on the
1.x line `microsphere-redis-generator` is only activated on JDK 9+.

## Recommended reading order

1. [core](./current/microsphere-redis-core.md) — understand the Redis command metadata model
2. [spring](./current/microsphere-redis-spring.md) — the main feature set: interception, events, wrappers, serializers
3. [spring-boot](./current/microsphere-redis-spring-boot.md) / [spring-cloud](./current/microsphere-redis-spring-cloud.md) — auto-configuration entry points
4. [replicator-spring](./current/microsphere-redis-replicator-spring.md) — Kafka-based replication
5. [spring-test](./current/microsphere-redis-spring-test.md) — writing your own integration tests

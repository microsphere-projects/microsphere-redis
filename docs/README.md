# Microsphere Redis Documentation

> English is the default language. Chinese translations are stored in a parallel `zh/` directory with identical file names.

Microsphere Redis is an enhanced Redis integration framework that extends Spring Data Redis with non-intrusive command
interception, event publishing, dynamic connection factories and Redis replication over Kafka.

This documentation set covers **two release lines** of the project:

| **Docs Directory** | **Git Branch** | **Version Line** | **Spring Data Redis Compatibility** | **Latest Release** |
|--------------------|----------------|------------------|-------------------------------------|--------------------|
| `current/`         | `release`      | 0.2.x            | 3.0.0 – 3.5.x (Spring Boot 3.x, Java 17+) | `0.2.14`                 |
| `1.x/`             | `release-1.x`  | 0.1.x            | 2.1.0 – 2.7.x (Spring Boot 2.x, Java 8+)  | `0.1.14`                 |

## Documentation Structure

```
docs/
├── README.md                    # This file
├── zh/README.md                 # 中文引导页
├── user-guide/                  # User Guide (English, default)
│   ├── README.md                # User Guide index
│   ├── zh/README.md             # 用户文档索引（中文）
│   ├── current/                 # User Guide for the `release` branch (0.2.x)
│   │   ├── microsphere-redis-parent.md
│   │   ├── microsphere-redis-dependencies.md
│   │   ├── microsphere-redis-core.md
│   │   ├── microsphere-redis-generator.md
│   │   ├── microsphere-redis-spring.md
│   │   ├── microsphere-redis-spring-test.md
│   │   ├── microsphere-redis-replicator-spring.md
│   │   ├── microsphere-redis-spring-boot.md
│   │   ├── microsphere-redis-spring-cloud.md
│   │   └── zh/                  # 同名中文文档
│   └── 1.x/                     # User Guide for the `release-1.x` branch (0.1.x)
│       └── ...                  # Same module files, zh/ included
└── developer-guide/             # Developer Guide (English, default)
    ├── README.md                # Developer Guide index
    ├── zh/README.md             # 开发者文档索引（中文）
    ├── current/                 # Developer Guide for the `release` branch
    │   └── ...                  # Same module files, zh/ included
    └── 1.x/                     # Developer Guide for the `release-1.x` branch
        └── ...
```

## Which Document Do I Need?

### [User Guide](./user-guide/README.md)

For **application developers** who consume Microsphere Redis as a library. It contains, per Maven module:

- API explanations (classes, annotations, events, interceptors, serializers)
- Runnable code examples (derived from the project test cases)
- Version information and compatibility ranges

### [Developer Guide](./developer-guide/README.md)

For **contributors and maintainers** who study or extend the framework. It contains, per Maven module:

- Architecture design and package-level source analysis
- Design patterns applied in the implementation
- Mermaid flowcharts for complex flows (command interception, event publishing, replication pipeline…)
- Mermaid diagrams of inter-module dependencies

## Maven Modules at a Glance

| **Module**                              | **Layer**              | **Purpose**                                              |
|-----------------------------------------|------------------------|----------------------------------------------------------|
| **microsphere-redis-parent**            | Build                  | Parent POM, BOM version management, Spring Cloud profiles |
| **microsphere-redis-dependencies**      | Build                  | Project-wide Bill of Materials                           |
| **microsphere-redis-core**              | Foundation             | Redis command metadata model and utilities (no Spring)   |
| **microsphere-redis-generator**         | Tooling                | Javadoc Doclet generating the metadata YAML              |
| **microsphere-redis-spring**            | Core Integration       | Interception, events, template wrappers, serializers     |
| **microsphere-redis-spring-test**       | Testing                | Abstract test base with real Redis connection            |
| **microsphere-redis-replicator-spring** | Replication            | Redis command replication via Kafka                      |
| **microsphere-redis-spring-boot**       | Boot Integration       | `@ConditionalOnRedis*` annotations, auto-configuration   |
| **microsphere-redis-spring-cloud**      | Cloud Integration      | Config-property propagation to Redis configuration       |

## License

Apache License 2.0.

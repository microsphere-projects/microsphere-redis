# Microsphere Redis

> Microsphere Projects for Redis

[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/microsphere-projects/microsphere-redis)
[![zread](https://img.shields.io/badge/Ask_Zread-_.svg?style=flat&color=00b0aa&labelColor=000000&logo=data%3Aimage%2Fsvg%2Bxml%3Bbase64%2CPHN2ZyB3aWR0aD0iMTYiIGhlaWdodD0iMTYiIHZpZXdCb3g9IjAgMCAxNiAxNiIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4KPHBhdGggZD0iTTQuOTYxNTYgMS42MDAxSDIuMjQxNTZDMS44ODgxIDEuNjAwMSAxLjYwMTU2IDEuODg2NjQgMS42MDE1NiAyLjI0MDFWNC45NjAxQzEuNjAxNTYgNS4zMTM1NiAxLjg4ODEgNS42MDAxIDIuMjQxNTYgNS42MDAxSDQuOTYxNTZDNS4zMTUwMiA1LjYwMDEgNS42MDE1NiA1LjMxMzU2IDUuNjAxNTYgNC45NjAxVjIuMjQwMUM1LjYwMTU2IDEuODg2NjQgNS4zMTUwMiAxLjYwMDEgNC45NjE1NiAxLjYwMDFaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00Ljk2MTU2IDEwLjM5OTlIMi4yNDE1NkMxLjg4ODEgMTAuMzk5OSAxLjYwMTU2IDEwLjY4NjQgMS42MDE1NiAxMS4wMzk5VjEzLjc1OTlDMS42MDE1NiAxNC4xMTM0IDEuODg4MSAxNC4zOTk5IDIuMjQxNTYgMTQuMzk5OUg0Ljk2MTU2QzUuMzE1MDIgMTQuMzk5OSA1LjYwMTU2IDE0LjExMzQgNS42MDE1NiAxMy43NTk5VjExLjAzOTlDNS42MDE1NiAxMC42ODY0IDUuMzE1MDIgMTAuMzk5OSA0Ljk2MTU2IDEwLjM5OTlaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik0xMy43NTg0IDEuNjAwMUgxMS4wMzg0QzEwLjY4NSAxLjYwMDEgMTAuMzk4NCAxLjg4NjY0IDEwLjM5ODQgMi4yNDAxVjQuOTYwMUMxMC4zOTg0IDUuMzEzNTYgMTAuNjg1IDUuNjAwMSAxMS4wMzg0IDUuNjAwMUgxMy43NTg0QzE0LjExMTkgNS42MDAxIDE0LjM5ODQgNS4zMTM1NiAxNC4zOTg0IDQuOTYwMVYyLjI0MDFDMTQuMzk4NCAxLjg4NjY0IDE0LjExMTkgMS42MDAxIDEzLjc1ODQgMS42MDAxWiIgZmlsbD0iI2ZmZiIvPgo8cGF0aCBkPSJNNCAxMkwxMiA0TDQgMTJaIiBmaWxsPSIjZmZmIi8%2BCjxwYXRoIGQ9Ik00IDEyTDEyIDQiIHN0cm9rZT0iI2ZmZiIgc3Ryb2tlLXdpZHRoPSIxLjUiIHN0cm9rZS1saW5lY2FwPSJyb3VuZCIvPgo8L3N2Zz4K&logoColor=ffffff)](https://zread.ai/microsphere-projects/microsphere-redis)
[![Maven Build](https://github.com/microsphere-projects/microsphere-redis/actions/workflows/maven-build.yml/badge.svg)](https://github.com/microsphere-projects/microsphere-redis/actions/workflows/maven-build.yml)
[![Codecov](https://codecov.io/gh/microsphere-projects/microsphere-redis/branch/dev/graph/badge.svg)](https://app.codecov.io/gh/microsphere-projects/microsphere-redis)
![Maven](https://img.shields.io/maven-central/v/io.github.microsphere-projects/microsphere-gateway.svg)
![License](https://img.shields.io/github/license/microsphere-projects/microsphere-redis.svg)

Microsphere Redis is an enhanced Redis integration framework that extends and improves Spring Data Redis capabilities,
providing advanced features for enterprise applications. This library helps developers address common Redis
implementation challenges in Spring applications with minimal configuration and maximum flexibility.

## Purpose and Scope

The microsphere-redis project is a multi-module Maven framework that enhances Redis integration in Spring applications
by providing:

- Non-intrusive Redis operation interception - Monitor and intercept Redis commands without modifying application code
- Spring Framework integration - Deep integration with Spring's dependency injection and auto-configuration
- Enterprise monitoring capabilities - Comprehensive event publishing and metrics collection
- Redis replication support - Advanced replication features with Kafka integration
- Modular architecture - Clean separation of concerns across multiple Maven modules

## Modules

| **Module**                              | **Purpose**                                                                         |
|-----------------------------------------|-------------------------------------------------------------------------------------|
| **microsphere-redis-parent**            | Defines the parent POM with dependency management and Spring Cloud version profiles |
| **microsphere-redis-dependencies**      | Centralizes dependency management for all project modules                           |
| **microsphere-redis-core**              | Foundational components and utilities for Redis operations                          |
| **microsphere-redis-spring**            | Spring Framework integration with enhanced Redis capabilities                       |
| **microsphere-redis-replicator-spring** | Redis replication support for Spring applications                                   |
| **microsphere-redis-generator**         | Redis Metadata generation                                                           |

## Getting Started

The easiest way to get started is by adding the Microsphere Redis BOM (Bill of Materials) to your project's
pom.xml:

```xml

<dependencyManagement>
    <dependencies>
        ...
        <!-- Microsphere Redis Dependencies -->
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>${microsphere-redis.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

`${microsphere-spring-boot.version}` has two branches:

| **Branches** | **Purpose**                                     | **Latest Version** |
|--------------|-------------------------------------------------|--------------------|
| **0.2.x**    | Compatible with Spring Data Redis 3.0.0 - 3.5.x | 0.2.0              |
| **0.1.x**    | Compatible with Spring Data Redis 2.0.0 - 2.7.x | 0.1.0              |

Then add the specific modules you need:

```xml

<dependencies>
    <!-- Microsphere Redis Spring -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
</dependencies>
```

Using the Redis Replicator module:

```xml

<dependencies>
    <!-- Microsphere Redis Replicator Spring -->
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-replicator-spring</artifactId>
    </dependency>
</dependencies>
```

## Building from Source

You don't need to build from source unless you want to try out the latest code or contribute to the project.

To build the project, follow these steps:

1. Clone the repository:

```bash
git clone https://github.com/microsphere-projects/microsphere-redis.git
```

2. Build the source:

- Linux/MacOS:

```bash
./mvnw package
```

- Windows:

```powershell
mvnw.cmd package
```

## Contributing

We welcome your contributions! Please read [Code of Conduct](./CODE_OF_CONDUCT.md) before submitting a pull request.

## Reporting Issues

* Before you log a bug, please search
  the [issues](https://github.com/microsphere-projects/microsphere-redis/issues)
  to see if someone has already reported the problem.
* If the issue doesn't already
  exist, [create a new issue](https://github.com/microsphere-projects/microsphere-redis/issues/new).
* Please provide as much information as possible with the issue report.

## Documentation

### User Guide

[DeepWiki Host](https://deepwiki.com/microsphere-projects/microsphere-redis)

[ZRead Host](https://zread.ai/microsphere-projects/microsphere-redis)

### Wiki

[Github Host](https://github.com/microsphere-projects/microsphere-redis/wiki)

### JavaDoc

- [microsphere-redis-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-redis-spring)
- [microsphere-redis-replicator-spring](https://javadoc.io/doc/io.github.microsphere-projects/microsphere-redis-replicator-spring)

## License

The Microsphere Spring is released under the [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0).
# microsphere-redis-dependencies

| Item | Value |
|------|-------|
| Module | `microsphere-redis-dependencies` |
| Git branch | `release` |
| Version line | 0.2.x (latest release `0.2.14`, branch tip `0.2.15-SNAPSHOT`) |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-dependencies` |
| Packaging | `pom` (Bill of Materials) |
| Role | Version management for every published Microsphere Redis module |
| Compatibility | Manages artifacts that run on Spring Data Redis 3.0.0 – 3.5.x / Spring Boot 3.x / JDK 17+ |

## Overview

`microsphere-redis-dependencies` is the project-wide **BOM**. It contains a single `<dependencyManagement>` block that pins
all six consumable Microsphere Redis modules to the same version, so that an application never has to write (or keep
synchronized) a version per module:

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-core</artifactId>
            <version>${revision}</version>
        </dependency>
        <!-- microsphere-redis-spring, -spring-test, -replicator-spring,
             -spring-boot, -spring-cloud: same groupId and ${revision} -->
    </dependencies>
</dependencyManagement>
```

At release time `${revision}` is resolved by `flatten-maven-plugin`, so the POM deployed to the repository carries a
concrete version. Excerpt of the published `0.2.13` BOM, as an illustration of the released shape:

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.2.13</version>
    <relativePath>../microsphere-redis-parent/pom.xml</relativePath>
</parent>
<artifactId>microsphere-redis-dependencies</artifactId>
<version>0.2.13</version>
<packaging>pom</packaging>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-core</artifactId>
            <version>0.2.13</version>
        </dependency>
        ...
    </dependencies>
</dependencyManagement>
```

**When do you need it?** As soon as an application uses *any* Microsphere Redis module — most applications use two or more
(for example `microsphere-redis-spring-boot` plus `microsphere-redis-spring-test`). It is also the safest way to consume a
single module, because it guarantees the transitive `microsphere-redis-core` version matches the module you asked for.

If you are *building modules that extend the framework* and want its build configuration as well, inherit
[microsphere-redis-parent](./microsphere-redis-parent.md) instead of (or in addition to) importing this BOM: the parent
supplies plugin management and platform BOM imports, but **not** the `microsphere-redis-*` versions.

## Installation

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>${microsphere-redis.version}</version> <!-- 0.2.14 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring-boot</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Notes:

- `scope` and `version` of the managed entries are supplied by the BOM; you still declare the dependency itself and its
  `<scope>test</scope>` where applicable.
- In a Spring Boot project, place the Microsphere Redis import **before** other BOM imports if you want its coordinates to
  win in the (unlikely) case of overlap; Maven keeps the first declaration in `dependencyManagement`.
- Importing a BOM never adds a dependency: the modules are still brought in explicitly.

## Managed Artifacts

| Artifact | Default scope in an application | Purpose |
|----------|--------------------------------|---------|
| `microsphere-redis-core` | `compile` (usually transitive) | Redis command metadata model and utilities; see [microsphere-redis-core](./microsphere-redis-core.md). |
| `microsphere-redis-spring` | `compile` | Command interception, events, template wrappers, serializers; see [microsphere-redis-spring](./microsphere-redis-spring.md). |
| `microsphere-redis-spring-test` | `test` | Abstract test base with a real Redis connection; see [microsphere-redis-spring-test](./microsphere-redis-spring-test.md). |
| `microsphere-redis-replicator-spring` | `compile` | Redis command replication over Kafka; see [microsphere-redis-replicator-spring](./microsphere-redis-replicator-spring.md). |
| `microsphere-redis-spring-boot` | `compile` | Auto-configuration and `@ConditionalOnRedis*` conditions; see [microsphere-redis-spring-boot](./microsphere-redis-spring-boot.md). |
| `microsphere-redis-spring-cloud` | `compile` | Spring Cloud property propagation; see [microsphere-redis-spring-cloud](./microsphere-redis-spring-cloud.md). |

Not managed by this BOM:

| Artifact | Why |
|----------|-----|
| `microsphere-redis-generator` | Build-time tooling of this repository; applications must not depend on it. See [microsphere-redis-generator](./microsphere-redis-generator.md). |
| `spring-data-redis`, `lettuce-core`, `snakeyaml`, Spring Boot / Spring Cloud artifacts | Versioned by `spring-boot-dependencies` / `spring-cloud-dependencies`; declare those BOMs in your project (or inherit a parent that imports them). |
| `microsphere-java-core`, `microsphere-annotation-processor`, `microsphere-spring-*` | Versioned by the Microsphere Spring Cloud / Spring Boot dependency chains, not by this BOM. |

## Using It With The Parent

A library module that extends Microsphere Redis typically combines both:

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.2.14</version>
    <relativePath/>
</parent>

<artifactId>my-redis-extension</artifactId>
<version>${revision}</version>

<properties>
    <revision>1.0.0-SNAPSHOT</revision>
</properties>

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
```

The parent contributes the Java 17 compiler settings (`-parameters` in particular, required by the metadata model) and the
`flatten-maven-plugin` that makes `${revision}` publishable; the BOM contributes the Redis module versions.

## Overriding A Version

If you must try a newer snapshot of one module, pin it explicitly — a direct `<version>` in the dependency beats the BOM:

```xml

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
        <version>0.2.15-SNAPSHOT</version>
    </dependency>
</dependencies>
```

Verify the result with:

```bash
mvn dependency:tree -Dincludes=io.github.microsphere-projects
mvn help:effective-pom
```

Mixing versions across the family is discouraged: `microsphere-redis-spring` expects the `microsphere-redis-core` API of
the same release line, and the metadata YAML it loads is generated for a specific Spring Data Redis version.

## Version & Compatibility

| BOM Version | Manages | Spring Data Redis | Spring Boot | JDK |
|-------------|---------|-------------------|-------------|-----|
| `0.2.14` (0.2.x line) | the six artifacts listed above, at `0.2.14` | 3.0.0 – 3.5.x | 3.x | 17+ |
| `0.2.15-SNAPSHOT` | same, at the development tip of `release` | 3.0.0 – 3.5.x | 3.x | 17+ |
| `0.1.14` (legacy 0.1.x line) | the same artifact names on the `release-1.x` branch | 2.1.0 – 2.7.x | 2.x | 8+ |

See the [1.x user guide](../1.x/microsphere-redis-dependencies.md) for the legacy BOM. The BOM version always equals the
versions of the modules it manages; there is no separate BOM release train.

## Related Modules

- [microsphere-redis-parent](./microsphere-redis-parent.md) — parent POM with build conventions.
- [microsphere-redis-core](./microsphere-redis-core.md) — foundation module managed by this BOM.
- [microsphere-redis-spring](./microsphere-redis-spring.md) — main integration module.
- [microsphere-redis-generator](./microsphere-redis-generator.md) — deliberately *not* managed here.
- Developer Guide: [microsphere-redis-dependencies internals](../../developer-guide/current/microsphere-redis-dependencies.md)
- User Guide index: [../README.md](../README.md)

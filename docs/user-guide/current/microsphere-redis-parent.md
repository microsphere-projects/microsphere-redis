# microsphere-redis-parent

| Item | Value |
|------|-------|
| Module | `microsphere-redis-parent` |
| Git branch | `release` |
| Version line | 0.2.x (latest release `0.2.14`, branch tip `0.2.15-SNAPSHOT`) |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-parent` |
| Packaging | `pom` |
| Role | Parent POM: build conventions + platform version management |
| Compatibility | Products it builds run on Spring Data Redis 3.0.0 – 3.5.x / Spring Boot 3.x / JDK 17+ |

## Overview

`microsphere-redis-parent` is the POM that every runtime module of Microsphere Redis
([core](./microsphere-redis-core.md), [spring](./microsphere-redis-spring.md),
[spring-test](./microsphere-redis-spring-test.md), [replicator-spring](./microsphere-redis-replicator-spring.md),
[spring-boot](./microsphere-redis-spring-boot.md), [spring-cloud](./microsphere-redis-spring-cloud.md),
[generator](./microsphere-redis-generator.md)) inherits. It exists so that a module — inside or outside this repository —
can be compiled, versioned, flattened, sourced, javadoc-ed and signed with exactly the Microsphere conventions, without
copying plugin configuration.

Its content is deliberately small:

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis</artifactId>
    <version>${revision}</version>
    <relativePath>../pom.xml</relativePath>
</parent>

<artifactId>microsphere-redis-parent</artifactId>
<packaging>pom</packaging>

<properties>
    <!-- BOM versions -->
    <microsphere-spring-cloud.version>0.2.26</microsphere-spring-cloud.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-spring-cloud-dependencies</artifactId>
            <version>${microsphere-spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

So the module contributes one thing of its own: the import of the **`microsphere-spring-cloud-dependencies` 0.2.26** BOM,
which aligns Spring Boot, Spring Cloud, Spring Cloud Alibaba, JUnit, Testcontainers and the Microsphere Spring/Java
libraries. Everything else comes from the inheritance chain:

| Level | Coordinates | What it adds |
|-------|-------------|--------------|
| 1 | `microsphere-redis-parent` (this module) | Import of `microsphere-spring-cloud-dependencies` |
| 2 | `io.github.microsphere-projects:microsphere-redis` (root aggregator) | `revision` = `0.2.15-SNAPSHOT`, `java.version` = `17`, module list, SCM/license/developer metadata |
| 3 | `microsphere-spring-cloud-parent` 0.2.26 | BOM imports (`spring-boot-dependencies`, `spring-cloud-dependencies`, `spring-cloud-alibaba-dependencies`, `microsphere-spring-boot-dependencies` 0.2.34, `junit-bom` 6.1.3, `testcontainers-bom` 2.0.5) and the `spring-cloud-*` profiles |
| 4 | `microsphere-spring-cloud` 0.2.26 | Aggregator metadata |
| 5 | `microsphere-build` 0.3.14 | Compiler/source/javadoc/flatten plugin management, profiles for JDK, GPG/release, checkstyle, JaCoCo |

**When do you need it?** When you build your *own* modules on top of the framework (a private extension of the
interception layer, a custom starter, an internal library that must stay aligned with the same Spring Boot line) and want
identical build behaviour. Applications that merely consume the framework should use the
[BOM](./microsphere-redis-dependencies.md) instead.

## Installation

Declare the parent with the released version — never with `${revision}`:

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.2.14</version>
    <relativePath/> <!-- resolve from the repository, not from disk -->
</parent>

<groupId>com.example</groupId>
<artifactId>my-redis-extension</artifactId>
<version>${revision}</version>
<packaging>jar</packaging>

<properties>
    <revision>1.0.0-SNAPSHOT</revision>
</properties>

<dependencyManagement>
    <dependencies>
        <!-- Redis module versions are NOT inherited from the parent: import the BOM -->
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
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>
</dependencies>
```

Key point: inheriting the parent gives you the **platform** versions (Spring Boot, Spring Cloud, Microsphere Spring
libraries) and the build configuration, but **not** the `microsphere-redis-*` module versions — those are managed only by
[microsphere-redis-dependencies](./microsphere-redis-dependencies.md).

## What the Parent Brings

| Concern | Effective setting | Notes |
|---------|-------------------|-------|
| Java level | `maven.compiler.source`/`target` = `${java.version}` = `17`; `maven.compiler.release` = `17` on JDK 9+ | Set by the root POM property and the `microsphere-build` JDK profile. |
| Compiler | `maven-compiler-plugin` 3.15.0 with `<parameters>true</parameters>` | Mandatory for the metadata model: `RedisCommandUtils.buildParameterMetadataList(...)` reads real parameter names. |
| CI-friendly versions | `flatten-maven-plugin` 1.7.3, `flattenMode=resolveCiFriendliesOnly`, `updatePomFile=true`, execution bound to `process-resources` | Rewrites `${revision}` into a concrete version in the installed/deployed POM; `parent` is expanded, `properties` kept, `dependencyManagement` interpolated. |
| Sources & Javadoc | `maven-source-plugin`, `maven-javadoc-plugin` 3.12.0 (`attach-javadocs` execution, `detectJavaApiLink=false`) | Javadoc jars are attached in the `release`-type profiles. |
| Resources | `src/main/java/**/*.{properties,xml}` and `src/main/resources/**/*` filtered, delimiter `@` | Use `@project.version@`-style tokens, not `${...}`, inside filtered resources. |
| Encoding | UTF-8 (`project.build.sourceEncoding`, `project.reporting.outputEncoding`) | |
| Testing | `junit-bom` 6.1.3, `testcontainers-bom` 2.0.5, surefire `testcontainers` profile setting `testcontainers.enabled=true` | Testcontainers-backed tests are opt-in through the profile. |

### Spring Cloud profiles

Inherited from `microsphere-spring-cloud-parent` 0.2.26; select one with `-P`:

| Profile | Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|---------|-------------|--------------|----------------------|
| `spring-cloud-2022` | 3.0.13 | 2022.0.5 | 2022.0.0.2 |
| `spring-cloud-2023` | 3.3.13 | 2023.0.6 | 2023.0.3.4 |
| `spring-cloud-2024` | 3.4.13 | 2024.0.3 | 2023.0.3.4 |
| `spring-cloud-2025` (default, `activeByDefault`) | 3.5.15 | 2025.0.3 | 2025.0.0.0 |
| `spring-cloud-2025.1` | 4.0.8 | 2025.1.3 | 2025.1.0.0 |

The 0.2.x line of Microsphere Redis targets Spring Boot 3.x, so `spring-cloud-2022` … `spring-cloud-2025` are the
supported selections; `spring-cloud-2025.1` switches to Spring Boot 4 and is not part of the compatibility contract for
this release line.

## The `${revision}` Pattern

`microsphere-redis-parent`, its sibling modules and the root aggregator all declare
`<version>${revision}</version>` and rely on the property defined in the root POM
(`<revision>0.2.15-SNAPSHOT</revision>`):

- Building the reactor never requires editing the version in every module POM; it is bumped in one place.
- A child POM inside the repository also needs `<relativePath>` pointing at `../microsphere-redis-parent/pom.xml`, as
  the framework modules do.
- The flattened POM (generated as `.flattened-pom.xml` and installed with `updatePomFile=true`) is what lands in the
  repository, so consumers never see an unresolvable `${revision}`.
- If you inherit the parent in your own project, you must define your own `revision` property (see the snippet above);
  otherwise `${revision}` stays unresolved.

## Version & Compatibility

| Parent Version | Build Produces For | Spring Boot Profiles | JDK to Build | Notes |
|----------------|--------------------|----------------------|--------------|-------|
| `0.2.14` (0.2.x line) | Spring Data Redis 3.0.0 – 3.5.x artifacts | `spring-cloud-2022` … `spring-cloud-2025` | 17+ | `release` branch; the branch working tree pins `microsphere-spring-cloud-dependencies` 0.2.26. |
| `0.2.15-SNAPSHOT` | same | same | 17+ | Development tip of `release`. |
| `0.1.14` (legacy 0.1.x line) | Spring Data Redis 2.1.0 – 2.7.x artifacts | Spring Boot 2.x line | 8+ | `release-1.x` branch; see the [1.x user guide](../1.x/microsphere-redis-parent.md). |

Released POMs pin the platform BOM version at release time: the published `microsphere-redis-parent:0.2.13` POM imports
`microsphere-spring-cloud-dependencies:0.2.25`, while the `release` branch working tree imports `0.2.26`. Check the POM of
the exact release you consume before aligning your own build.

## Related Modules

- [microsphere-redis-dependencies](./microsphere-redis-dependencies.md) — the BOM that manages the module versions.
- [microsphere-redis-core](./microsphere-redis-core.md) — foundation module built with this parent.
- [microsphere-redis-spring](./microsphere-redis-spring.md) — main integration module.
- [microsphere-redis-generator](./microsphere-redis-generator.md) — build-time metadata tooling module.
- Developer Guide: [microsphere-redis-parent internals](../../developer-guide/current/microsphere-redis-parent.md)
- User Guide index: [../README.md](../README.md)

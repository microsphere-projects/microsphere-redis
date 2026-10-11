# microsphere-redis-dependencies

| 项目 | 值 |
|------|-----|
| 模块 | `microsphere-redis-dependencies` |
| Git 分支 | `release` |
| 版本线 | 0.2.x（最新发布版本 `0.2.14`，分支顶端 `0.2.15-SNAPSHOT`） |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-dependencies` |
| 打包方式 | `pom`（Bill of Materials） |
| 定位 | 为全部已发布的 Microsphere Redis 模块提供版本管理 |
| 兼容范围 | 它所管理的产物运行于 Spring Data Redis 3.0.0 – 3.5.x / Spring Boot 3.x / JDK 17+ |

## 概述

`microsphere-redis-dependencies` 是本项目的 **BOM**。它只包含一个 `<dependencyManagement>` 块，把全部六个可被消费的
Microsphere Redis 模块锁定到同一版本，应用因此无需为每个模块书写（也无需手工保持同步）版本号：

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-core</artifactId>
            <version>${revision}</version>
        </dependency>
        <!-- microsphere-redis-spring、-spring-test、-replicator-spring、
             -spring-boot、-spring-cloud：同样的 groupId 与 ${revision} -->
    </dependencies>
</dependencyManagement>
```

发布时 `${revision}` 由 `flatten-maven-plugin` 解析，因此部署到仓库中的 POM 携带的是具体版本。下面是已发布 `0.2.13`
BOM 的节选，用以说明发布后的形态：

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

**什么场景需要它？** 只要应用使用 *任何* 一个 Microsphere Redis 模块就该用它——多数应用会同时使用两个及以上（例如
`microsphere-redis-spring-boot` 加上 `microsphere-redis-spring-test`）。即便只消费单个模块，它也是最稳妥的方式，因为它能
保证传递进来的 `microsphere-redis-core` 版本与你显式引入的模块一致。

如果你是要 *构建扩展该框架的模块*，并且希望同时继承它的构建配置，请改为继承
[microsphere-redis-parent](./microsphere-redis-parent.md)（可以只继承，也可以与导入本 BOM 并用）：父 POM 提供插件管理与
平台 BOM 导入，但 **不** 提供 `microsphere-redis-*` 的版本号。

## 安装

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

说明：

- 被管理条目的 `scope` 与 `version` 由 BOM 提供；依赖本身仍需声明，并在需要处写明 `<scope>test</scope>`。
- 在 Spring Boot 项目中，如果你希望本 BOM 的坐标在（几乎不会发生的）冲突中胜出，请把 Microsphere Redis 的导入放在其它
  BOM 导入 **之前**；Maven 在 `dependencyManagement` 中保留最先声明的那一个。
- 导入 BOM 永远不会新增依赖：模块仍需显式声明。

## 被管理的产物

| 产物 | 应用中的默认 scope | 用途 |
|----------|--------------------------------|---------|
| `microsphere-redis-core` | `compile`（通常是传递依赖） | Redis 命令元数据模型与工具类，见 [microsphere-redis-core](./microsphere-redis-core.md)。 |
| `microsphere-redis-spring` | `compile` | 命令拦截、事件、模板包装器、序列化器，见 [microsphere-redis-spring](./microsphere-redis-spring.md)。 |
| `microsphere-redis-spring-test` | `test` | 带真实 Redis 连接的测试基类，见 [microsphere-redis-spring-test](./microsphere-redis-spring-test.md)。 |
| `microsphere-redis-replicator-spring` | `compile` | 基于 Kafka 的 Redis 命令复制，见 [microsphere-redis-replicator-spring](./microsphere-redis-replicator-spring.md)。 |
| `microsphere-redis-spring-boot` | `compile` | 自动配置与 `@ConditionalOnRedis*` 条件注解，见 [microsphere-redis-spring-boot](./microsphere-redis-spring-boot.md)。 |
| `microsphere-redis-spring-cloud` | `compile` | Spring Cloud 配置属性传递，见 [microsphere-redis-spring-cloud](./microsphere-redis-spring-cloud.md)。 |

本 BOM 不管理的内容：

| 产物 | 原因 |
|----------|-----|
| `microsphere-redis-generator` | 本仓库的构建期工具，应用不应依赖它，见 [microsphere-redis-generator](./microsphere-redis-generator.md)。 |
| `spring-data-redis`、`lettuce-core`、`snakeyaml`、Spring Boot / Spring Cloud 相关产物 | 由 `spring-boot-dependencies` / `spring-cloud-dependencies` 决定版本；请在你自己的项目中声明这些 BOM（或继承一个已导入它们的父 POM）。 |
| `microsphere-java-core`、`microsphere-annotation-processor`、`microsphere-spring-*` | 由 Microsphere Spring Cloud / Spring Boot 的依赖链决定版本，不由本 BOM 管理。 |

## 与父 POM 配合使用

扩展 Microsphere Redis 的库模块通常会把两者结合使用：

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

父 POM 提供 Java 17 编译配置（尤其是元数据模型所必需的 `-parameters`）以及让 `${revision}` 可发布的
`flatten-maven-plugin`；BOM 提供各 Redis 模块的版本号。

## 覆盖某个版本

如果确实要试用某个模块更新的快照，请显式锁定版本——依赖中直接写出的 `<version>` 优先于 BOM：

```xml

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
        <version>0.2.15-SNAPSHOT</version>
    </dependency>
</dependencies>
```

用以下命令确认结果：

```bash
mvn dependency:tree -Dincludes=io.github.microsphere-projects
mvn help:effective-pom
```

不建议在同一版本线内混用不同版本：`microsphere-redis-spring` 期望与自身发布线一致的 `microsphere-redis-core` API，
而它所加载的元数据 YAML 是针对特定 Spring Data Redis 版本生成的。

## 版本与兼容性

| BOM 版本 | 管理内容 | Spring Data Redis | Spring Boot | JDK |
|-------------|---------|-------------------|-------------|-----|
| `0.2.14`（0.2.x 版本线） | 上表六个产物，版本均为 `0.2.14` | 3.0.0 – 3.5.x | 3.x | 17+ |
| `0.2.15-SNAPSHOT` | 同上，取 `release` 分支开发尖端 | 3.0.0 – 3.5.x | 3.x | 17+ |
| `0.1.14`（历史 0.1.x 版本线） | `release-1.x` 分支上的同名产物 | 2.1.0 – 2.7.x | 2.x | 8+ |

参见 [1.x 用户文档](../../1.x/microsphere-redis-dependencies.md)了解历史版本线的 BOM。BOM 的版本号始终与它所管理模块的
版本号相同，不存在独立的 BOM 发布节奏。

## 相关模块

- [microsphere-redis-parent](./microsphere-redis-parent.md)——提供构建约定的父 POM。
- [microsphere-redis-core](./microsphere-redis-core.md)——由本 BOM 管理的基础模块。
- [microsphere-redis-spring](./microsphere-redis-spring.md)——核心集成模块。
- [microsphere-redis-generator](./microsphere-redis-generator.md)——有意 *未* 在此管理。
- 开发者文档：[microsphere-redis-dependencies 源码分析](../../../developer-guide/current/microsphere-redis-dependencies.md)
- 用户文档索引：[../README.md](../../README.md)

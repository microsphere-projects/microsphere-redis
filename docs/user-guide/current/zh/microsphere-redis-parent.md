# microsphere-redis-parent

| 项目 | 值 |
|------|-----|
| 模块 | `microsphere-redis-parent` |
| Git 分支 | `release` |
| 版本线 | 0.2.x（最新发布版本 `0.2.14`，分支顶端 `0.2.15-SNAPSHOT`） |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-parent` |
| 打包方式 | `pom` |
| 定位 | 父 POM：构建约定 + 平台版本管理 |
| 兼容范围 | 由它构建的产物运行于 Spring Data Redis 3.0.0 – 3.5.x / Spring Boot 3.x / JDK 17+ |

## 概述

`microsphere-redis-parent` 是 Microsphere Redis 全部运行时模块都会继承的 POM
（[core](./microsphere-redis-core.md)、[spring](./microsphere-redis-spring.md)、
[spring-test](./microsphere-redis-spring-test.md)、[replicator-spring](./microsphere-redis-replicator-spring.md)、
[spring-boot](./microsphere-redis-spring-boot.md)、[spring-cloud](./microsphere-redis-spring-cloud.md)、
[generator](./microsphere-redis-generator.md)）。它的存在使得任何一个模块——无论是否位于本仓库内——都能严格按照
Microsphere 的约定完成编译、版本管理、POM 扁平化、源码 jar、Javadoc jar 与签名，而不必复制粘贴插件配置。

它的内容刻意保持精简：

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

因此本模块自身只贡献了一件事：导入 **`microsphere-spring-cloud-dependencies` 0.2.26** BOM，由它统一对齐 Spring Boot、
Spring Cloud、Spring Cloud Alibaba、JUnit、Testcontainers 以及 Microsphere 的 Spring/Java 库。其余内容全部来自继承链：

| 层级 | 坐标 | 贡献内容 |
|-------|-------------|--------------|
| 1 | `microsphere-redis-parent`（本模块） | 导入 `microsphere-spring-cloud-dependencies` |
| 2 | `io.github.microsphere-projects:microsphere-redis`（根聚合 POM） | `revision` = `0.2.15-SNAPSHOT`、`java.version` = `17`、模块列表、SCM/许可证/开发者信息 |
| 3 | `microsphere-spring-cloud-parent` 0.2.26 | BOM 导入（`spring-boot-dependencies`、`spring-cloud-dependencies`、`spring-cloud-alibaba-dependencies`、`microsphere-spring-boot-dependencies` 0.2.34、`junit-bom` 6.1.3、`testcontainers-bom` 2.0.5）以及 `spring-cloud-*` profiles |
| 4 | `microsphere-spring-cloud` 0.2.26 | 聚合器元数据 |
| 5 | `microsphere-build` 0.3.14 | compiler/source/javadoc/flatten 插件管理、JDK profile、GPG/release、checkstyle、JaCoCo |

**什么场景需要它？** 当你在框架之上构建 *自己的* 模块（拦截层的私有扩展、自定义 starter、必须与同一 Spring Boot 版本线
对齐的内部库），并希望获得完全一致的构建行为时。仅仅消费该框架的应用应当改用
[BOM](./microsphere-redis-dependencies.md)。

## 安装

父 POM 必须声明为已发布的版本号——绝不能使用 `${revision}`：

```xml

<parent>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.2.14</version>
    <relativePath/> <!-- 从仓库解析，而不是从磁盘 -->
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
        <!-- Redis 各模块的版本不会随父 POM 继承：需要导入 BOM -->
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

关键点：继承父 POM 得到的是 **平台** 版本（Spring Boot、Spring Cloud、Microsphere Spring 系列库）与构建配置，
而 **不包含** `microsphere-redis-*` 各模块的版本——后者仅由
[microsphere-redis-dependencies](./microsphere-redis-dependencies.md) 管理。

## 父 POM 带来的内容

| 关注点 | 实际生效配置 | 说明 |
|---------|-------------------|-------|
| Java 级别 | `maven.compiler.source`/`target` = `${java.version}` = `17`；JDK 9+ 时 `maven.compiler.release` = `17` | 由根 POM 属性与 `microsphere-build` 的 JDK profile 共同决定。 |
| 编译器 | `maven-compiler-plugin` 3.15.0 且 `<parameters>true</parameters>` | 对元数据模型是必需的：`RedisCommandUtils.buildParameterMetadataList(...)` 需要读取真实参数名。 |
| CI 友好版本 | `flatten-maven-plugin` 1.7.3、`flattenMode=resolveCiFriendliesOnly`、`updatePomFile=true`，执行绑定在 `process-resources` | 把安装/发布 POM 中的 `${revision}` 改写为具体版本；`parent` 展开、`properties` 保留、`dependencyManagement` 插值。 |
| 源码与 Javadoc | `maven-source-plugin`、`maven-javadoc-plugin` 3.12.0（`attach-javadocs` 执行，`detectJavaApiLink=false`） | Javadoc jar 在 `release` 类 profile 中附加。 |
| 资源 | `src/main/java/**/*.{properties,xml}` 与 `src/main/resources/**/*` 参与过滤，分隔符为 `@` | 在被过滤的资源中使用 `@project.version@` 形式，而不是 `${...}`。 |
| 编码 | UTF-8（`project.build.sourceEncoding`、`project.reporting.outputEncoding`） | |
| 测试 | `junit-bom` 6.1.3、`testcontainers-bom` 2.0.5、surefire 的 `testcontainers` profile 设置 `testcontainers.enabled=true` | 基于 Testcontainers 的测试需要通过该 profile 显式开启。 |

### Spring Cloud profiles

由 `microsphere-spring-cloud-parent` 0.2.26 继承而来；用 `-P` 选择其一：

| Profile | Spring Boot | Spring Cloud | Spring Cloud Alibaba |
|---------|-------------|--------------|----------------------|
| `spring-cloud-2022` | 3.0.13 | 2022.0.5 | 2022.0.0.2 |
| `spring-cloud-2023` | 3.3.13 | 2023.0.6 | 2023.0.3.4 |
| `spring-cloud-2024` | 3.4.13 | 2024.0.3 | 2023.0.3.4 |
| `spring-cloud-2025`（默认，`activeByDefault`） | 3.5.15 | 2025.0.3 | 2025.0.0.0 |
| `spring-cloud-2025.1` | 4.0.8 | 2025.1.3 | 2025.1.0.0 |

Microsphere Redis 的 0.2.x 版本线面向 Spring Boot 3.x，因此受支持的选择是 `spring-cloud-2022` … `spring-cloud-2025`；
`spring-cloud-2025.1` 会切换到 Spring Boot 4，不属于本版本线的兼容性承诺范围。

## `${revision}` 模式

`microsphere-redis-parent`、它的同级模块以及根聚合 POM 都声明 `<version>${revision}</version>`，并依赖根 POM 中定义的
属性（`<revision>0.2.15-SNAPSHOT</revision>`）：

- 构建 reactor 时无需逐个修改模块 POM 的版本号，只在一处升级即可。
- 仓库内部的子 POM 同样需要把 `<relativePath>` 指向 `../microsphere-redis-parent/pom.xml`，框架自身的模块就是这样做的。
- 最终进入仓库的是扁平化 POM（生成文件为 `.flattened-pom.xml`，配合 `updatePomFile=true` 安装），因此消费者不会看到
  无法解析的 `${revision}`。
- 如果你在自己的项目中继承该父 POM，就必须定义自己的 `revision` 属性（见上文示例）；否则 `${revision}` 将无法解析。

## 版本与兼容性

| 父 POM 版本 | 构建产物的目标环境 | Spring Boot Profile | 构建所需 JDK | 说明 |
|----------------|--------------------|----------------------|--------------|-------|
| `0.2.14`（0.2.x 版本线） | Spring Data Redis 3.0.0 – 3.5.x | `spring-cloud-2022` … `spring-cloud-2025` | 17+ | `release` 分支；分支工作区将 `microsphere-spring-cloud-dependencies` 锁定为 0.2.26。 |
| `0.2.15-SNAPSHOT` | 同上 | 同上 | 17+ | `release` 分支的开发尖端。 |
| `0.1.14`（历史 0.1.x 版本线） | Spring Data Redis 2.1.0 – 2.7.x | Spring Boot 2.x 版本线 | 8+ | `release-1.x` 分支；参见 [1.x 用户文档](../../1.x/microsphere-redis-parent.md)。 |

已发布的 POM 会在发布时锁定平台 BOM 版本：已发布的 `microsphere-redis-parent:0.2.13` 导入的是
`microsphere-spring-cloud-dependencies:0.2.25`，而 `release` 分支工作区导入的是 `0.2.26`。在对齐你自己的构建之前，请以
你所消费的那个具体发布版本的 POM 为准。

## 相关模块

- [microsphere-redis-dependencies](./microsphere-redis-dependencies.md)——管理各模块版本的 BOM。
- [microsphere-redis-core](./microsphere-redis-core.md)——使用该父 POM 构建的基础模块。
- [microsphere-redis-spring](./microsphere-redis-spring.md)——核心集成模块。
- [microsphere-redis-generator](./microsphere-redis-generator.md)——构建期元数据生成工具模块。
- 开发者文档：[microsphere-redis-parent 源码分析](../../../developer-guide/current/microsphere-redis-parent.md)
- 用户文档索引：[../README.md](../../README.md)

# microsphere-redis-parent — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-parent` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-parent:0.1.14`（pom） |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-parent.md](../../current/zh/microsphere-redis-parent.md)。

## 概述

1.x 各模块的继承型父工程。它导入 `microsphere-spring-cloud-dependencies:0.1.26` BOM，
把整个 Spring Framework 5.x / Spring Boot 2.x / Spring Data Redis 2.7.x 技术栈锁定 ——
这正是 `0.1.x` 成为 "Spring Boot 2 发行线" 的原因。

## 作为 parent 使用

```xml
<parent>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-parent</artifactId>
    <version>0.1.14</version>
</parent>
```

## 作为 BOM 使用（既有 Boot 2 应用的首选）

多数应用不应替换自己的 parent；改为导入姊妹 BOM ——
[microsphere-redis-dependencies（1.x）](./microsphere-redis-dependencies.md)。

## 结构说明（1.x）

- 根 reactor（`microsphere-redis/pom.xml`）：`revision=0.1.15-SNAPSHOT`，已发布最新 `0.1.14` 之后的 tip。
- 与 0.2.x 不同，根 pom **没有声明 `java.version` 覆盖**；基线来自
  `microsphere-spring-cloud-parent:0.1.26`（Java 8 线），且 `microsphere-redis-generator` 模块
  仅在 `java9+` JDK profile 下激活。
- 模块：parent、dependencies、core、spring、spring-test、replicator-spring、spring-boot、spring-cloud
  （与 0.2.x 拓扑一致）。

## 版本与兼容

| microsphere-redis | 分支 | Spring Cloud 栈 | Spring Data Redis | Spring Boot |
|-------------------|------|-----------------|-------------------|-------------|
| `0.1.14` | `release-1.x` | microsphere-spring-cloud 0.1.26 | 2.1.0 – 2.7.x | 2.x |
| `0.2.14` | `release` | microsphere-spring-cloud 0.2.26 | 3.0.0 – 3.5.x | 3.x |

## 相关模块

- [microsphere-redis-dependencies（1.x）](./microsphere-redis-dependencies.md)
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-parent.md](../../../developer-guide/1.x/zh/microsphere-redis-parent.md)

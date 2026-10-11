# microsphere-redis-dependencies — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-dependencies` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-dependencies:0.1.14`（pom / BOM） |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-dependencies.md](../../current/zh/microsphere-redis-dependencies.md)。

## 概述

1.x 六个运行期模块的纯版本 BOM。在 `dependencyManagement` 中 import 后，所有
microsphere-redis 构件统一锁定到 `0.1.14`，且不需要改动你的 Spring Boot parent。

## 托管构件

| artifactId | 用途 |
|------------|------|
| `microsphere-redis-core` | 元数据模型 + 工具 |
| `microsphere-redis-spring` | 拦截管线 |
| `microsphere-redis-spring-test` | 测试支撑夹具 |
| `microsphere-redis-replicator-spring` | Kafka 命令复制 |
| `microsphere-redis-spring-boot` | Boot 2 条件注解 |
| `microsphere-redis-spring-cloud` | 云原生属性热刷新 |

`microsphere-redis-generator` 刻意**不托管**（构建期工具，不是运行期依赖）。

## 安装

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
        <artifactId>microsphere-redis-spring</artifactId> <!-- 无需 version -->
    </dependency>
</dependencies>
```

## 版本与兼容

- BOM 的 parent 是 `microsphere-redis-parent:0.1.14`，因此 import 本 BOM 会传递性对齐 Spring Data
  Redis 2.7.x 时代版本（生成元数据头 `version: 2.7.18` 可佐证）。
- 绝不可混用：`0.1.x` BOM 与 `0.2.x` BOM 只能二选一，并与你的 Spring Boot 大版本对应
  （2.x ↔ 0.1.x，3.x ↔ 0.2.x）。

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md) — 最可能首先添加的模块
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-dependencies.md](../../../developer-guide/1.x/zh/microsphere-redis-dependencies.md)

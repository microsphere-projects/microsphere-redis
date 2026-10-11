# microsphere-redis-dependencies — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-dependencies` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | 构建：BOM（仅 `dependencyManagement`，pom） |

## 已核实事实（`git show origin/release-1.x:microsphere-redis-dependencies/pom.xml`）

以 `${revision}` 托管同样的**六个**运行期模块，且行位置与 0.2.x 完全一致：

| artifactId | 行号 |
|------------|------|
| `microsphere-redis-core` | :26 |
| `microsphere-redis-spring` | :32 |
| `microsphere-redis-spring-test` | :38 |
| `microsphere-redis-replicator-spring` | :44 |
| `microsphere-redis-spring-boot` | :50 |
| `microsphere-redis-spring-cloud` | :56 |

`microsphere-redis-generator` 同样刻意**缺席**（构建期工具）。parent 是
`microsphere-redis-parent`（→ `microsphere-spring-cloud-dependencies:0.1.26`），因此 import
1.x BOM 会把消费者对齐到 Boot 2.x / SDR 2.7.x 时代版本。

## 版本选择指引

| 你的技术栈 | 应使用 |
|-----------|--------|
| Spring Boot 2.x / spring-data-redis 2.1.0–2.7.x / Java 8+ | `microsphere-redis-dependencies:0.1.14`（本线） |
| Spring Boot 3.x / spring-data-redis 3.0.0–3.5.x / Java 17 | `microsphere-redis-dependencies:0.2.14`（[../../current/zh/microsphere-redis-dependencies.md](../../current/zh/microsphere-redis-dependencies.md)） |

两棵依赖树中绝不可混入两个 BOM —— 元数据 YAML、servlet 命名空间与序列化器类名均不同
（见 [1.x spring 文档](./microsphere-redis-spring.md)）。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-dependencies.md](../../../user-guide/1.x/zh/microsphere-redis-dependencies.md)
- English: [../microsphere-redis-dependencies.md](../microsphere-redis-dependencies.md)

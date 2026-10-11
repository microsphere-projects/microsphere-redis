# microsphere-redis-parent — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-parent` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | 构建：继承型父工程（pom） |

## 已核实事实（来自 `git show origin/release-1.x:pom.xml` 及各模块 pom）

- 根聚合工程：`microsphere-redis`，parent 为 `microsphere-spring-cloud-parent:0.1.26`，
  `revision=0.1.15-SNAPSHOT`。
- `microsphere-redis-parent/pom.xml` 导入 BOM `microsphere-spring-cloud-dependencies:0.1.26`
  （属性 `microsphere-spring-cloud.version`，第 23 行）—— 即 **Spring Boot 2.x / Spring Data Redis
  2.7.x 时代**技术栈，这条线因此被锁定在 Boot 2。
- 1.x 根 properties 中**没有 `java.version` 覆盖**（不同于 0.2.x 的 `17`）：基线来自 0.1.26
  父链（Java 8 线）。代码层面的体现：core 不使用 record（`RawValue` 是类）、redis-spring 使用
  `javax.servlet`。
- reactor：8 个无条件模块；`microsphere-redis-generator` 仅在 `java9+` profile
  （`<jdk>[9,)</jdk>`）下加入。
- 与 0.2.x parent pom 仅 1 行差异 —— 托管栈版本（`0.1.26` vs `0.2.26`）。

## 结构

与 0.2.x 相同的双角色划分：仓库根 `pom.xml` 负责聚合；`microsphere-redis-parent` 是继承型父工程；
`microsphere-redis-dependencies` 是可消费 BOM。完整阐述见
[../../current/zh/microsphere-redis-parent.md](../../current/zh/microsphere-redis-parent.md)。

## 发布线

工作合并进 `release-1.x`；最新发布 `0.1.14`，tip 为 `0.1.15-SNAPSHOT`。该分支 README 记录了
兼容划分：main ↔ Spring Data Redis 3.0.0–3.5.x（`0.2.14`），1.x ↔ Spring Data Redis
2.1.0–2.7.x（`0.1.14`）。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-parent.md](../../../user-guide/1.x/zh/microsphere-redis-parent.md)
- English: [../microsphere-redis-parent.md](../microsphere-redis-parent.md)

# 用户文档

[English](../README.md) | 中文

用户文档面向把 Microsphere Redis 作为依赖库使用的**应用开发者**。每个 Maven 模块都有独立文档，内容包括：

- **API 解释说明** —— 所有公开类、注解、事件与拦截器的方法级说明
- **代码示例** —— 取材于项目 `src/test/java` 下的测试用例
- **版本信息** —— 构件坐标、已发布版本及各模块的版本兼容范围

## 选择发布线

| 发布线 | 文档目录 | Git 分支 | 版本 | Spring Data Redis | 典型技术栈 |
|--------|----------|----------|------|-------------------|------------|
| 当前版 | [`current/`](../current/) | `release`     | 0.2.x（`0.2.14`） | 3.0.0 – 3.5.x | Spring Boot 3.x，Java 17+ |
| 1.x 维护线 | [`1.x/`](../1.x/)     | `release-1.x` | 0.1.x（`0.1.14`） | 2.1.0 – 2.7.x | Spring Boot 2.x，Java 8+ |

## 模块文档（current / 0.2.x）

- [microsphere-redis-parent](../current/microsphere-redis-parent.md)
- [microsphere-redis-dependencies](../current/microsphere-redis-dependencies.md)
- [microsphere-redis-core](../current/microsphere-redis-core.md)
- [microsphere-redis-generator](../current/microsphere-redis-generator.md)
- [microsphere-redis-spring](../current/microsphere-redis-spring.md)
- [microsphere-redis-spring-test](../current/microsphere-redis-spring-test.md)
- [microsphere-redis-replicator-spring](../current/microsphere-redis-replicator-spring.md)
- [microsphere-redis-spring-boot](../current/microsphere-redis-spring-boot.md)
- [microsphere-redis-spring-cloud](../current/microsphere-redis-spring-cloud.md)

每篇模块文档的中文译文位于 `current/zh/` 目录，文件名与英文一致。

## 模块文档（1.x / 0.1.x）

同样九个模块的文档位于 [`1.x/`](../1.x/) 目录（中文位于 `1.x/zh/`）。注意在 1.x 发布线上，
`microsphere-redis-generator` 仅在 JDK 9+ profile 中激活。

## 推荐阅读顺序

1. [core](../current/microsphere-redis-core.md) —— 理解 Redis 命令元数据模型
2. [spring](../current/microsphere-redis-spring.md) —— 核心特性：拦截、事件、模板包装器、序列化器
3. [spring-boot](../current/microsphere-redis-spring-boot.md) / [spring-cloud](../current/microsphere-redis-spring-cloud.md) —— 自动装配入口
4. [replicator-spring](../current/microsphere-redis-replicator-spring.md) —— 基于 Kafka 的命令复制
5. [spring-test](../current/microsphere-redis-spring-test.md) —— 编写你自己的集成测试

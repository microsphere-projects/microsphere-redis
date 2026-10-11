# microsphere-redis-spring-boot — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-boot` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | Spring Boot 2 条件词汇表 |

## 1.x 状态

`git diff --stat origin/release-1.x origin/release -- microsphere-redis-spring-boot/src/main` 为**空**：
三个条件注解在两条线上完全一致 ——

- `@ConditionalOnRedisEnabled` → `@ConditionalOnProperty("microsphere.redis.enabled", matchIfMissing = true)`
  （两分支同为 ConditionalOnRedisEnabled.java:73）；
- `@ConditionalOnRedisInterceptorEnabled` → enabled + `microsphere.redis.interceptor.enabled`（:72–73）；
- `@ConditionalOnRedisAvailable` → enabled + `@ConditionalOnClass(name = {RedisConnection,
  RedisMetadataLoader, RedisMethodInterceptor})`（:57–62），`@Target(TYPE)`。

完整解析、模式表与启用流程图：
[../../current/zh/microsphere-redis-spring-boot.md](../../current/zh/microsphere-redis-spring-boot.md)。

## 1.x 特有说明

- 在 Spring Boot **2.x** 的条件机制下求值；`@ConditionalOnClass(name=...)` 语义相同，
  依赖它的应用可平滑迁移到 0.2.x。
- Boot 2 用户：姊妹 cloud 模块的 actuator `HasFeatures` 贡献（其 1.x `features.yaml`）
  引用的是 Boot 2 时代的定制器类名；0.2.x 才追加 Boot 3.1/3.4 独有条目
  （`RedisConnectionDetails`、`LettuceClientOptionsBuilderCustomizer`）。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-spring-boot.md](../../../user-guide/1.x/zh/microsphere-redis-spring-boot.md)
- English: [../microsphere-redis-spring-boot.md](../microsphere-redis-spring-boot.md)

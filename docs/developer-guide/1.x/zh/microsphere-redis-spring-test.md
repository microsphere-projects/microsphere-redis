# microsphere-redis-spring-test — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-test` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | 测试支撑库 |

## 1.x 状态

`git diff --stat origin/release-1.x origin/release -- microsphere-redis-spring-test` 结果为**空**：
该模块在两个分支上逐字节一致。`AbstractRedisTest`（默认禁用、
`@SpringJUnitConfig(RedisConfig.class)`、`SET_METHOD`/`SET_METHOD_ARGS`/
`SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE` 夹具、`assertSet` 辅助方法）与 `RedisConfig`
（static `@Bean` 三件套 + 立即校验的 `LettuceConnectionFactory("127.0.0.1", 6379)`）
在 Boot 2 线上行为完全相同。

完整源码解析、模式与流程见
[0.2.x spring-test 开发者文档](../../current/zh/microsphere-redis-spring-test.md)。

## 1.x 特有说明

- 测试依赖由 `microsphere-spring-cloud-dependencies:0.1.26` 解析（JUnit 5、spring-test 5.x、
  Lettuce 6.x 时代）。
- `release-1.x` 分支的 `docker/` compose 提供同样的本地 Redis。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-spring-test.md](../../../user-guide/1.x/zh/microsphere-redis-spring-test.md)
- English: [../microsphere-redis-spring-test.md](../microsphere-redis-spring-test.md)

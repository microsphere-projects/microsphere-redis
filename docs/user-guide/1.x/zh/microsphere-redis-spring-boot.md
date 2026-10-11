# microsphere-redis-spring-boot — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-boot` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-spring-boot:0.1.14` |
| 兼容范围 | Spring Boot `2.x`、Spring Data Redis `2.1.0 – 2.7.x`、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-spring-boot.md](../../current/zh/microsphere-redis-spring-boot.md)。
> 源码与 0.2.x 完全一致 —— 同样的三个条件注解。

## 概述

为 microsphere-redis 提供可复用的 Spring Boot **条件词汇表**。拦截本身仍由
`microsphere-redis-spring` 自动装配；本模块让*你的* Boot 2 自动配置与 bean 遵循相同的启用规则。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-boot</artifactId>
    <version>0.1.14</version>
</dependency>
```

## API 说明

| 注解 | 等价条件 | 适用场景 |
|------|----------|----------|
| `@ConditionalOnRedisEnabled` | `@ConditionalOnProperty("microsphere.redis.enabled", matchIfMissing = true)` | bean 需要 Redis 集成处于激活状态 |
| `@ConditionalOnRedisInterceptorEnabled` | enabled **且** `microsphere.redis.interceptor.enabled`（matchIfMissing） | bean 依赖拦截/事件 |
| `@ConditionalOnRedisAvailable` | enabled + classpath 上有 `RedisConnection`、`RedisMetadataLoader`、`RedisMethodInterceptor` | 守护整个配置类（`@Target(TYPE)`） |

## 使用示例

```java
@Configuration
@ConditionalOnRedisAvailable
public class AuditConfiguration {

    @Bean
    @ConditionalOnRedisInterceptorEnabled
    public RedisAuditService redisAuditService() { ... }
}
```

当 `microsphere.redis.enabled=false`（或 spring-data-redis 缺失）时整个配置消失 ——
很适合测试 profile 与特性开关。

## 版本与兼容

| microsphere-redis | Spring Boot | 备注 |
|-------------------|-------------|------|
| `0.1.14`（本线） | 2.x | 条件在 Boot 2 属性绑定机制下求值 |
| `0.2.14` | 3.x | 注解完全相同，Boot 3 坐标 |

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md) · [spring-cloud（1.x）](./microsphere-redis-spring-cloud.md)
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-spring-boot.md](../../../developer-guide/1.x/zh/microsphere-redis-spring-boot.md)

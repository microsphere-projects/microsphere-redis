# microsphere-redis-spring-boot

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-boot` |
| Git 分支 | `release`（0.2.x 版本线） |
| 最新发布版本 | `0.2.14` |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-spring-boot` |
| 兼容性 | Spring Boot 3.x（自动配置基础设施）、Spring Data Redis 3.0.0 – 3.5.x、JDK 17+ |
| 关键依赖 | `microsphere-redis-spring`（compile）、`spring-boot-starter` / `spring-boot-starter-actuator`（optional） |

## 概述

`microsphere-redis-spring-boot` 把 Microsphere Redis 的启用状态暴露为**声明式的 Spring Boot 条件注解**，
标注在你自己的 `@Configuration` 类、`@Bean` 方法或组件上，使它们只在 Redis 拦截栈确实开启时才生效：

| 注解 | 满足条件 |
|------|----------|
| `@ConditionalOnRedisEnabled` | `microsphere.redis.enabled` 为 `true`（默认 `true`，`matchIfMissing`） |
| `@ConditionalOnRedisInterceptorEnabled` | Redis 已启用，且 `microsphere.redis.interceptor.enabled` 不为 `false` |
| `@ConditionalOnRedisAvailable` | Redis 已启用，且以下类存在于类路径：`org.springframework.data.redis.connection.RedisConnection`、`io.microsphere.redis.metadata.RedisMetadataLoader`、`io.microsphere.redis.spring.interceptor.RedisMethodInterceptor` |

三者都基于 Spring Boot 的 `@ConditionalOnProperty` / `@ConditionalOnClass` 元注解组合而成，自身不携带任何
属性（`@Target(TYPE)`；`@ConditionalOnRedisEnabled` 还允许 `ANNOTATION_TYPE`）。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-spring-boot</artifactId>
</dependency>
```

## API 说明

### @ConditionalOnRedisEnabled

```java
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(name = "microsphere.redis.enabled", matchIfMissing = true)
public @interface ConditionalOnRedisEnabled {}
```

### @ConditionalOnRedisInterceptorEnabled

```java
@ConditionalOnRedisEnabled
@ConditionalOnProperty(name = "microsphere.redis.interceptor.enabled", matchIfMissing = true)
public @interface ConditionalOnRedisInterceptorEnabled {}
```

### @ConditionalOnRedisAvailable

```java
@Target(TYPE)
@ConditionalOnRedisEnabled
@ConditionalOnClass(name = {
        "org.springframework.data.redis.connection.RedisConnection",
        "io.microsphere.redis.metadata.RedisMetadataLoader",
        "io.microsphere.redis.spring.interceptor.RedisMethodInterceptor"})
public @interface ConditionalOnRedisAvailable {}
```

## 代码示例

`ConditionalOnRedisAvailableTest` / `ConditionalOnRedisEnabledTest` 验证的用法模式：

```java
@Configuration
@ConditionalOnRedisAvailable   // 整个配置类仅在 Redis 栈在类路径且已启用时存在
class MetricsConfig {

    @Bean
    @ConditionalOnRedisInterceptorEnabled
    public RedisCommandMetrics redisCommandMetrics() {
        return new RedisCommandMetrics();
    }
}
```

在测试或生产环境中的开关就是普通属性：

```properties
microsphere.redis.enabled=false                    # 关闭所有 ConditionalOnRedis* 之下的组件
microsphere.redis.interceptor.enabled=false        # 保留配置对象，关闭拦截
```

`RedisInitializerSpringBootTest` 证明在 Boot 上下文中，`microsphere-redis-spring` 的
`RedisInitializer`（`ApplicationContextInitializer`）仍会自动引导。

## 版本与兼容性

| 项目 | 值 |
|------|-----|
| 版本线 | 0.2.x（`release` 分支），最新发布 **0.2.14** |
| Spring Boot | 3.x（`jakarta` 技术栈）。Spring Boot 4 用户可依赖 `spring-cloud` 模块 `@AutoConfigureAfter` 中已预置的 Boot 4 `DataRedisAutoConfiguration` 类名 |
| JDK | 17+ |
| 遗留线 | Spring Boot 2 技术栈请使用 0.1.x 线：[../../1.x/microsphere-redis-spring-boot.md](../../1.x/microsphere-redis-spring-boot.md) |

## 相关模块

- [microsphere-redis-spring](./microsphere-redis-spring.md) —— 条件注解所守护的功能本体
- [microsphere-redis-spring-cloud](./microsphere-redis-spring-cloud.md) —— 依赖本模块
- 开发者文档：[../../developer-guide/current/microsphere-redis-spring-boot.md](../../../developer-guide/current/microsphere-redis-spring-boot.md)

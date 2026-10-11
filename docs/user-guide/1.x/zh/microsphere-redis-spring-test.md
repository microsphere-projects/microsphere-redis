# microsphere-redis-spring-test — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-test` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-spring-test:0.1.14`（test scope） |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-spring-test.md](../../current/zh/microsphere-redis-spring-test.md)。
> 本模块与 0.2.x **完全一致**（分支间无源码差异）。

## 概述

正式发布的测试支撑库：一个连接本地 Redis 的 SpringJUnit 基类，加上规范的命令夹具，
让你的拦截测试不必重复搭建 Redis 管线。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-test</artifactId>
    <version>0.1.14</version>
    <scope>test</scope>
</dependency>
```

## API 说明

| 元素 | 用途 |
|------|------|
| `AbstractRedisTest` | `@Disabled @SpringJUnitConfig(RedisConfig.class)` 基类；自动注入 `redisTemplate`、`stringRedisTemplate`、`context`；提供 `assertRedisTemplateSet/assertStringRedisTemplateSet/assertSet` |
| `AbstractRedisTest.SET_METHOD` | 反射得到的 `RedisConnection.set(byte[],byte[])` —— 与拦截器捕获结果比对的稳定标识 |
| `AbstractRedisTest.SET_METHOD_ARGS` | `["key".bytes, "value".bytes]` |
| `AbstractRedisTest.SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE` | `"redisTemplate"` |
| `config.RedisConfig` | static `@Bean` 三件套：`redisTemplate`、`stringRedisTemplate`，以及调用 `afterPropertiesSet()` + `validateConnection()` 的 `LettuceConnectionFactory("127.0.0.1", 6379)`（Redis 未启动则快速失败） |

## 代码示例

```java
// 解除基类默认禁用，测试一条被拦截的写命令
public class MyInterceptorTest extends AbstractRedisTest {

    @Test
    void setFlowsThroughInterceptor() {
        assertRedisTemplateSet("k", "v");                 // 经代理透传，语义不变
        // ... 断言你的拦截器针对 SET_METHOD/SET_METHOD_ARGS 捕获到的内容
    }
}
```

需要有本地 Redis（仓库 `docker/` compose 即可拉起）。

## 版本与兼容

| microsphere-redis | Spring Data Redis | Spring Boot | Java |
|-------------------|-------------------|-------------|------|
| `0.1.14`（本线） | 2.1.0 – 2.7.x | 2.x | 8+ |
| `0.2.14` | 3.0.0 – 3.5.x | 3.x | 17 |

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md)
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-spring-test.md](../../../developer-guide/1.x/zh/microsphere-redis-spring-test.md)

# microsphere-redis-spring-cloud — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-cloud` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-spring-cloud:0.1.14` |
| 兼容范围 | Spring Boot `2.x`、Spring Cloud 2021/2022 系 context 库、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-spring-cloud.md](../../current/zh/microsphere-redis-spring-cloud.md)。

## 概述

Spring Cloud 适配层：让每个 `microsphere.redis.*` 属性都可以通过 `/actuator/refresh` **热刷新** ——
属性变更会转化为 `RedisConfigurationPropertyChangedEvent`，由核心 `RedisConfiguration` 重新绑定，
因此拦截与命令事件暴露可以在运行期切换、无需重启。同时为 actuator `HasFeatures` 输出提供特性元数据。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring-cloud</artifactId>
    <version>0.1.14</version>
</dependency>
```

仅当 classpath 上有 `spring-cloud-context`（`EnvironmentChangeEvent`）且 `microsphere.redis.enabled`
为 true 时激活；自动配置排在 Boot 的
`org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration` 之后。
（与 0.2.x 的差异：0.2.x 还额外排在搬迁后的
`org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration` 之后以为 Boot 4 做准备。）

## API 说明

| 元素 | 用途 |
|------|------|
| `RedisCloudAutoConfiguration` | 入口；import 下面的监听器 |
| `PropagatingRedisConfigurationPropertyChangedEventApplicationListener` | 把 `EnvironmentChangeEvent` 的键过滤为 `microsphere.redis.` 前缀后重新发布为 `RedisConfigurationPropertyChangedEvent` |
| `META-INF/config/default/features.yaml` | actuator 用的声明式特性清单（抽象 Spring Data Redis API 名 + 具名 microsphere 类） |

## 使用示例

```bash
# 在运行实例上关闭拦截
curl -X POST http://host/actuator/refresh \
     -H 'Content-Type: application/json' \
     -d '{"microsphere.redis.interceptor.enabled":"false"}'
```

所有 `microsphere.redis.*` 键 —— 包括 `microsphere.redis.replicator.*` —— 自动参与。

## 版本与兼容

| microsphere-redis | Spring Boot | 排在其后的 Redis 自动配置 |
|-------------------|-------------|---------------------------|
| `0.1.14`（本线） | 2.x | `o.s.b.autoconfigure.data.redis.RedisAutoConfiguration` |
| `0.2.14` | 3.x | Boot 2.x 时代与 Boot 4.0 时代搬迁后的两个类（按名匹配） |

## 相关模块

- [spring-boot（1.x）](./microsphere-redis-spring-boot.md) — 其条件注解的基础
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-spring-cloud.md](../../../developer-guide/1.x/zh/microsphere-redis-spring-cloud.md)

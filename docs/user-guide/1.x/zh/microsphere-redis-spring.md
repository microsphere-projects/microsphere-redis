# microsphere-redis-spring — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-spring:0.1.14` |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、Java 8+（servlet API：`javax.servlet`） |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-spring.md](../../current/zh/microsphere-redis-spring.md)。

## 概述

面向纯 Spring（非 Boot）应用的集成模块：它代理从 `RedisConnectionFactory` 获取的每个
`RedisConnection`（或包装指定名字的 `RedisTemplate` bean），让每条命令流经有序拦截器链，并为每个
成功的**写**命令发布 `RedisCommandEvent`。全部运行期行为由
`META-INF/spring-data-redis-metadata.yaml` 元数据驱动（1.x 基于 Spring Data Redis 2.7.18 生成）。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-spring</artifactId>
    <version>0.1.14</version>
</dependency>
```

需要 classpath 上有 `spring-data-redis`（2.1.0–2.7.x）与 `io.microsphere:microsphere-redis-core`。

## 配置属性

| 属性 | 默认值 | 作用 |
|------|--------|------|
| `microsphere.redis.enabled` | `true` | 总开关；`false` 完全不注册 |
| `microsphere.redis.interceptor.enabled` | `true` | 拦截开/关 |
| `microsphere.redis.command-event.exposed` | `true` | 写命令发布 `RedisCommandEvent` |
| `microsphere.redis.wrapped-redis-templates` | `*` | 包装哪些模板（代替工厂代理） |
| `microsphere.redis.fail-fast` | `true` | 装配不一致时启动失败 |

## API 说明

### 启用注解

- `@EnableRedisConfiguration` —— 仅注册 `RedisConfiguration`。
- `@EnableRedisContext` —— 追加 `RedisContext`（`microsphere:redisContext`）。
- `@EnableRedisInterceptor` —— 完整管线。属性：
  - `String[] wrapRedisTemplates() default {}` —— 模板 bean 名模式；为空 = 改为代理工厂；
  - `boolean exposeCommandEvent() default true`；
  - `BeanSource[] sources()` —— 用户拦截器的发现渠道
    （`BEAN_FACTORY`、`SPRING_FACTORIES`、`JAVA_SERVICE_PROVIDER`）。

在 Spring Boot 2 应用中无需注解：`RedisInitializer` + `RedisInterceptorModuleInitializer`
经 `spring.factories` 自动注册。

### 拦截契约

```java
public interface RedisMethodInterceptor<T> extends Ordered {
    default void beforeExecute(RedisMethodContext<T> context) { }
    default void afterExecute(RedisMethodContext<T> context, Object result, Throwable failure) { }
    default void handleError(RedisMethodContext<T> context, boolean beforeExecute,
                             Object result, Object failure,
                             Object interceptionFailure) { }
}
// 绑定你关心的接口：
public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> { }
public interface RedisCommandInterceptor   extends RedisMethodInterceptor<RedisCommands> { }
```

`RedisMethodContext` 提供：`getTarget/getMethod/getArgs`、`getSourceBean/getSourceBeanName`、
`getAttribute/setAttribute`、计时（`getDuration(TimeUnit)`）、`isWriteMethod()`、参数绑定
（`getParameter(name)`）。静态 `RedisMethodContext.get()` 可在同线程取到当前调用上下文。

### 事件

`RedisCommandEvent`（仅写命令且 `failure == null`）：`getMethod`、`getMethodName`、
`getInterfaceName`、`getParameterTypes`、`getArgs/getArg(int)`、`getSourceBeanName`、
`getApplicationName`、`getTimestamp`。

### 序列化器

`io.microsphere.redis.spring.serializer.*` —— 面向值对象参数的类型化序列化器
（`Expiration`、`Boundary`、`Range`、`GeoLocation`、`Point`、`SortParameters`、ZSet range/weights…），
经 `Serializers` 查找。**1.x 命名注意：**ZSet weights 序列化器名为
`RedisZSetCommandsWeightsSerializer`（0.2.x 更名为 `WeightsSerializer`）。
`HoldingValueRedisSerializerWrapper` 记录 object↔bytes 配对，使拦截器可以经 `ValueHolder`
从序列化形态还原原始参数对象。

### DynamicRedisConnectionFactory

多实例场景的线程级路由工厂：`DynamicRedisConnectionFactory.switchTarget(beanName)` /
`clearTarget()` 按线程生效；1.x 的请求级清理监听器基于 **`javax.servlet`** 的
`ServletRequestListener`（0.2.x 使用 `jakarta.servlet`）。

## 代码示例

```java
// 1) 纯 Spring：显式启用拦截
@Configuration
@EnableRedisInterceptor
public class RedisInterceptConfig { }

// 2) 耗时/审计拦截器（模式来自 redis-spring 拦截器测试）
public class SlowCommandInterceptor implements RedisCommandInterceptor {
    @Override
    public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) {
        if (failure == null && context.isWriteMethod() && context.getDuration(MILLISECONDS) > 100) {
            log.warn("slow write {} from bean {} took {}ms",
                    context.getMethod().getName(), context.getSourceBeanName(),
                    context.getDuration(MILLISECONDS));
        }
    }
}

// 3) 响应写命令
@EventListener
public void onRedisCommand(RedisCommandEvent event) {
    byte[] key = (byte[]) event.getArg(0);   // SET key value ...
}
```

## 版本与兼容

| microsphere-redis | 分支 | Spring Data Redis | Spring Boot | Java | Servlet |
|-------------------|------|-------------------|-------------|------|---------|
| `0.1.x`（本文档，最新 `0.1.14`） | `release-1.x` | 2.1.0 – 2.7.x | 2.x | 8+ | javax |
| `0.2.x`（最新 `0.2.14`） | `release` | 3.0.0 – 3.5.x | 3.x | 17 | jakarta |

两条线的 API 面（注解、属性键、拦截契约、事件）刻意保持一致；差异只在坐标与 JDK 层面
（javax→jakarta、core 的 record 化、序列化器更名）。

## 相关模块

- [core（1.x）](./microsphere-redis-core.md) · [generator（1.x）](./microsphere-redis-generator.md) · [spring-test（1.x）](./microsphere-redis-spring-test.md)
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-spring.md](../../../developer-guide/1.x/zh/microsphere-redis-spring.md)

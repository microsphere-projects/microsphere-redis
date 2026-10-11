# microsphere-redis-spring

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring`（旗舰模块） |
| Git 分支 | `release`（0.2.x 版本线） |
| 最新发布版本 | `0.2.14` |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-spring` |
| 兼容性 | Spring Data Redis 3.0.0 – 3.5.x、Spring Framework 6.x、JDK 17+ |
| 关键依赖 | `microsphere-redis-core`（compile）、`spring-data-redis`（optional）、`snakeyaml`、`jakarta.servlet-api`（optional） |

## 概述

`microsphere-redis-spring` 为 Spring 应用提供**非侵入式的 Redis 操作拦截**能力：

- 监控经 `RedisTemplate`、`StringRedisTemplate` 或裸 `RedisConnection` 执行的每一条 Redis 命令，无需修改业务代码
- 为每条被拦截的命令发布 Spring `RedisCommandEvent`（审计日志、指标、复制）
- 通过 `RedisCommandInterceptor` / `RedisConnectionInterceptor` SPI 插入自定义拦截器
- 包装模板，使序列化后的值保留回原始 Java 对象的联系（"holding value"）
- 动态路由到不同的 `RedisConnectionFactory` bean
- 通过一整套 `RedisSerializer` 实现，恢复被 Spring Data Redis 序列化掉的原始命令参数

## 安装

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>0.2.14</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-spring</artifactId>
    </dependency>

    <!-- Redis 客户端栈需要自带 -->
    <dependency>
        <groupId>org.springframework.data</groupId>
        <artifactId>spring-data-redis</artifactId>
    </dependency>
    <dependency>
        <groupId>io.lettuce</groupId>
        <artifactId>lettuce-core</artifactId>
    </dependency>
</dependencies>
```

模块中 `spring-data-redis` 声明为 `optional`，**不会**传递引入。

## 快速开始

```java
@Configuration
@EnableRedisInterceptor                      // 对所有模板（"*"）启用拦截
public class RedisInterceptionConfig {
}
```

在上下文任意位置消费命令事件：

```java
@Component
class AuditListener {

    @EventListener
    public void onRedisCommandEvent(RedisCommandEvent event) {
        System.out.printf("%s %s %s -> %s%n",
                event.getSourceBeanName(),      // 例如 "stringRedisTemplate"
                event.getInterfaceName(),       // 例如 RedisStringCommands
                event.getMethodName(),          // 例如 "set"
                Arrays.toString(event.getArgs()));
    }
}
```

## 启用注解

### @EnableRedisInterceptor

被 `@EnableRedisContext` 元注解标注，并 `@Import(RedisInterceptorBeanDefinitionRegistrar.class)`。

| 属性 | 类型 | 默认值 | 含义 |
|------|------|--------|------|
| `wrapRedisTemplates` | `String[]` | `{}`（空 = 通过连接工厂代理拦截所有 bean） | 需要替换为包装 bean 的 `RedisTemplate`/`StringRedisTemplate` bean 名（支持 `${...}` 占位符与 `*` 通配） |
| `exposeCommandEvent` | `boolean` | `true` | 是否发布 `RedisCommandEvent`（同时受属性 `microsphere.redis.command-event.exposed` 控制） |
| `sources` | `BeanSource[]` | — | 拦截器的发现来源：`BEAN_FACTORY`、`SPRING_FACTORIES`、`JAVA_SERVICE_PROVIDER` |

### @EnableRedisContext

注册 `RedisContext`（并隐含 `@EnableRedisConfiguration`）。`@EnableRedisInterceptor` 也会自动触发它。

### @EnableRedisConfiguration

注册绑定 `microsphere.redis.*` 环境属性的 `RedisConfiguration` bean。

## 配置属性

定义于 `io.microsphere.redis.spring.util.RedisConstants`：

| 属性 | 默认值 | 用途 |
|------|--------|------|
| `microsphere.redis.enabled` | `true` | 整个框架的总开关 |
| `microsphere.redis.interceptor.enabled` | `true` | 启用/禁用拦截 |
| `microsphere.redis.command-event.exposed` | `true` | 是否发布 `RedisCommandEvent` |
| `microsphere.redis.wrapped-redis-templates` | `*` | 需要包装的模板 bean 名 |
| `microsphere.redis.fail-fast` | `true` | Redis 不可用时快速失败 |

## 拦截器 SPI

两个姐妹接口继承通用回调契约 `RedisMethodInterceptor<T>`：

```java
public interface RedisMethodInterceptor<T> extends Ordered {
    default void beforeExecute(RedisMethodContext<T> context) throws Throwable { ... }
    default void beforeExecute(T target, Method method, Object[] args, String sourceBeanName) throws Throwable { ... }
    default void afterExecute(RedisMethodContext<T> context, Object result, Throwable failure) throws Throwable { ... }
    default void afterExecute(T target, Method method, Object[] args, String sourceBeanName,
                              Object result, Throwable failure) throws Throwable { ... }
    default void handleError(RedisMethodContext<T> context, boolean before, Object result,
                             Throwable failure, Throwable interceptionFailure) { ... }
    // isEnabled() 与 getOrder() 控制是否参与及顺序
}

public interface RedisConnectionInterceptor extends RedisMethodInterceptor<RedisConnection> {}
public interface RedisCommandInterceptor  extends RedisMethodInterceptor<RedisCommands> {}
```

`RedisMethodContext<T>` 向拦截器提供本次调用的全部信息：

- `getTarget()`、`getMethod()`、`getArgs()`、`getSourceBean()`、`getSourceBeanName()`
- `getRedisContext()`、`getRedisConfiguration()`、`getEnvironment()`、`getApplicationContext()`
- 带元数据的参数访问：`getParameters()`、`getParameter(String name)`、`getParameter(int index)`
- 命令分类：`isWriteMethod()`、`isCommandEventExposed()`、`isSourceFromRedisTemplate()`
- 计时：`start()` / `stop()` / `getDurationNanos()` / `getDuration(TimeUnit)`
- 每次调用可变的属性袋：`setAttribute(name, value)` / `getAttribute(name)`
- 线程绑定：`RedisMethodContext.set(...)`、`get()`、`clear()`

拦截器异常不会中断业务调用——统一路由到 `handleError` 并记录日志。

### 注册拦截器

1. **Java SPI** —— 在 `META-INF/services/io.microsphere.redis.spring.interceptor.RedisCommandInterceptor`
   （及/或 `...RedisConnectionInterceptor`）中写入实现类全名，`sources` 需包含 `JAVA_SERVICE_PROVIDER`
   （默认注册器三种来源全开）。
2. **Spring bean** —— 把拦截器声明为 `@Component`，通过 `sources = BEAN_FACTORY` 发现。
3. **spring.factories** —— key 为 `io.microsphere.redis.spring.interceptor.RedisCommandInterceptor`
   （框架内置的 `EventPublishingRedisCommandInterceptor` 即用此法）。

## 事件

| 事件 | 父类 | 关键访问器 |
|------|------|------------|
| `RedisOperationEvent` | `ApplicationEvent` | 持有方法/参数元数据的基类 |
| `RedisCommandEvent` | `ApplicationEvent` | `getMethod()`、`getInterfaceName()`、`getMethodName()`、`getParameterTypes()`、`getParameterCount()`、`getArgs()`、`getArg(int)`、`getApplicationName()`、`getSourceBeanName()`、`getSerializationVersion()`/`setSerializationVersion(byte)`（复制器使用的线格式版本标记） |
| `RedisConfigurationPropertyChangedEvent` | `ApplicationContextEvent` | `getEnvironment()`、`getPropertyNames()`、`hasProperty(name)`、`getRedisConfiguration()` |

`EventPublishingRedisCommandInterceptor` 是内置的 `RedisCommandInterceptor`，把每条被拦截的命令转换为
`RedisCommandEvent`（受 order 控制，仅在命令事件开启时生效）。

## 模板包装器与 Holding-Value 序列化器

当 `wrapRedisTemplates` 选中 bean 时，`RedisTemplateWrapperBeanPostProcessor` 将其替换为：

- `RedisTemplateWrapper<K,V>`（继承 `RedisTemplate`）与 `StringRedisTemplateWrapper`（继承 `StringRedisTemplate`）

两者都覆写 `preProcessConnection(...)`，把 `RedisConnection` 包进拦截代理，并实现 `DelegatingWrapper`
（`getDelegate()` 返回原始连接/模板）。

`WrapperProcessors` 会对包装器执行可插拔的 `WrapperProcessor` bean，其中最重要的是：

- `HoldingValueRedisTemplateWrapperProcessor` / `HoldingValueStringRedisTemplateWrapperProcessor` —— 把每个
  `RedisSerializer` 替换为 `HoldingValueRedisSerializerWrapper`，它在委托序列化的同时把 `对象 ↔ byte[]`
  对应关系记录到线程绑定的 `ValueHolder` 中。拦截器随后可以调用 `ValueHolder.getValue(bytes)`
  从已序列化的命令参数中**还原原始 Java 对象**。

若未指定任何需要包装的模板，`RedisConnectionFactoryProxyBeanPostProcessor` 会代理每个
`RedisConnectionFactory`——因此经 `template.execute(connectionCallback)` 获取的连接同样被拦截。
工具入口：`newProxyRedisConnection(...)`、`getRawRedisConnectionFactory(...)`。

## 序列化器（`io.microsphere.redis.spring.serializer`）

`Serializers` 是一组静态实例的注册表，覆盖 Spring Data Redis 中缺省没有序列化器的值类型——用于恢复原始命令参数
以及序列化 `RedisCommandEvent` 载荷：

| 序列化器 | 目标类型 |
|----------|----------|
| `Serializers.DEFAULT_SERIALIZER`（`JdkSerializationRedisSerializer`）、`STRING_SERIALIZER`（`StringRedisSerializer`） | 基线 |
| `BooleanSerializer` / `ByteArraySerializer` / `DoubleSerializer` / `IntegerSerializer` / `LongSerializer` / `ShortSerializer` | 基本类型 |
| `EnumSerializer` | 枚举（紧凑序号式编码） |
| `ExpirationSerializer` | `RedisStringCommands.SetOption`/过期时间 |
| `BoundarySerializer`、`RangeSerializer`（+`RangeModel`）、`RedisZSetCommandsRangeSerializer` | ZSET 区间/边界 |
| `GeoLocationSerializer`、`PointSerializer` | GEO 类型 |
| `SortParametersSerializer`（内含嵌套 `RangeSerializer`） | `SortParameters` |
| `WeightsSerializer` | `RedisZSetCommands.Weights` |
| `RedisCommandEventSerializer` | 把完整的 `RedisCommandEvent` 序列化为 `byte[]`（在模块 `META-INF/spring.factories` 中以 `RedisSerializer` SPI 注册） |

所有定型序列化器都继承 `AbstractSerializer<T>`（模板方法模式：`serialize/deserialize` 为 final，委派给
`doSerialize`/`doDeserialize`；定宽类型实现 `calcBytesLength()`）。

## DynamicRedisConnectionFactory

`io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory` 是一个委托式
`RedisConnectionFactory`，按**线程**选择目标工厂：

```java
DynamicRedisConnectionFactory.switchTarget("anotherFactoryBeanName"); // 绑定当前线程
try {
    redisTemplate.opsForValue().set("k", "v");                        // 发往该集群
} finally {
    DynamicRedisConnectionFactory.clearTarget();
}
```

- `setDefaultRedisConnectionFactoryBeanName(String)` —— 未设置目标时的回退
- `getTargetBeanName()` —— 当前线程的目标
- `DynamicRedisConnectionFactoryCleanerListener`（`ServletRequestListener`）在 HTTP 请求销毁时清除线程变量，
  防止池化线程泄漏（需要 `jakarta.servlet-api`）

## 代码示例

### 自定义连接拦截器（来自 `LoggingRedisConnectionInterceptor`，经 `META-INF/services/io.microsphere.redis.spring.interceptor.RedisConnectionInterceptor` 注册）

```java
public class LoggingRedisConnectionInterceptor implements RedisConnectionInterceptor {

    @Override
    public void beforeExecute(RedisMethodContext context) throws Throwable {
        logger.info("beforeExecute - context: {}", context);
    }

    @Override
    public void afterExecute(RedisMethodContext<RedisConnection> context, Object result, Throwable failure) {
        logger.info("afterExecute - context: {} , result: {}", context, result);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

### 完整注解配置（来自 `EnableRedisInterceptorTest`）

```java
@ContextConfiguration(classes = {LoggingRedisCommandInterceptor.class, MyConfig.class})
@TestPropertySource(properties = {
        "microsphere.redis.enabled=true",
        "microsphere.redis.wrapped-rest-templates=redisTemplate",
})
@EnableRedisInterceptor(wrapRedisTemplates = {
        "${microsphere.redis.wrapped-rest-templates}",   // 占位符
        " redisTemplate , stringRedisTemplate"            // 逗号分隔的 bean 名
}, sources = {
        BEAN_FACTORY,          // Spring bean 中声明的拦截器
        SPRING_FACTORIES,      // spring.factories 中声明的拦截器
        JAVA_SERVICE_PROVIDER  // META-INF/services 中声明的拦截器
})
class MyInterceptionTest extends AbstractRedisCommandEventTest {
}
```

### 模板写入的同时消费命令事件（来自 `AbstractRedisCommandEventTest`）

```java
context.addApplicationListener((ApplicationListener<RedisCommandEvent>) event -> {
    // 记录线上观测到的 key -> value
    data.put(event.getArg(0), event.getArg(1));
});

stringRedisTemplate.opsForValue().set("Key-1", "Value-1");
// 裸连接执行同样会被拦截：
stringRedisTemplate.execute(c -> {
    c.execute("SET", keySerializer.serialize("Key-3"), valueSerializer.serialize("Value-3"));
    return c;
}, true);
```

### 关闭命令事件（来自 `EnableRedisInterceptorWithoutExposeCommandEventTest`）

```java
@EnableRedisInterceptor(exposeCommandEvent = false)
class QuietConfig {
}
```

## 版本与兼容性

| 项目 | 值 |
|------|-----|
| 版本线 | 0.2.x（`release` 分支），最新发布 **0.2.14** |
| Spring Data Redis | **3.0.0 – 3.5.x**（元数据 YAML 针对 3.5.x 重新生成；`version:` 头记录生成源版本，如 `3.5.10`） |
| Spring Boot / Framework | 3.x / 6.x（`jakarta.*` 命名空间） |
| JDK | 17+ |
| 遗留线 | Spring Data Redis 2.1.0 – 2.7.x / Spring Boot 2.x 请使用 0.1.x 线 —— [../../1.x/microsphere-redis-spring.md](../../1.x/microsphere-redis-spring.md) |

## 相关模块

- [microsphere-redis-core](./microsphere-redis-core.md) —— 本模块从 `META-INF/spring-data-redis-metadata.yaml` 加载元数据（经 `SpringRedisMetadataLoader` SPI + `SpringRedisMetadataRepository`）
- [microsphere-redis-spring-boot](./microsphere-redis-spring-boot.md) —— `@ConditionalOnRedis*` 条件注解
- [microsphere-redis-replicator-spring](./microsphere-redis-replicator-spring.md) —— 消费 `RedisCommandEvent`
- 开发者文档：[../../developer-guide/current/zh/microsphere-redis-spring.md](../../../developer-guide/current/zh/microsphere-redis-spring.md)

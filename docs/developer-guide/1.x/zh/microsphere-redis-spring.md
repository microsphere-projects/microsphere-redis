# microsphere-redis-spring — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | Spring 核心集成 |
| 基线 | Java 8、`javax.servlet`、Spring Data Redis 2.x |

> 拦截架构与 [0.2.x spring 开发者文档](../../current/zh/microsphere-redis-spring.md)**完全相同** ——
> 同样的引导链（`RedisInitializer` → `RedisModuleInitializer` → registrar → `RedisContext`）、
> 同样的代理/包装策略、同样的拦截契约、同样的元数据仓库三键缓存、同样的属性键。
> 本文记录已核实的 1.x 差异。

## 已核实的源码差异 vs 0.2.x（`git diff --stat release-1.x release -- redis-spring/src/main`）

仅 8 个 Java 文件存在差异（annotation、context、interceptor、event、beans、metadata、util 包全部不变）：

| 文件 | 1.x 行为 |
|------|----------|
| `serializer/RedisZSetCommandsWeightsSerializer.java` | 仅 1.x 存在（58 行）—— 以长名序列化 `RedisZSetCommands.Weights`；0.2.x 更名为 `WeightsSerializer` |
| `serializer/WeightsSerializer.java` | 0.2.x 对应文件（37 行） |
| `serializer/Serializers.java` | 1.x 注册表接的是 `RedisZSetCommandsWeightsSerializer`；其余为同一静态单例目录（86 行差异主要由更名与注册调整驱动） |
| `serializer/AbstractSerializer.java` | 10 行差异：Java 8 友好的泛型/工具，无 record |
| `serializer/ByteArraySerializer.java`、`EnumSerializer.java` | 各 1 行差异（import/注解外观） |
| `serializer/HoldingValueRedisSerializerWrapper.java` | 6 行差异，object↔bytes 持有契约相同 |
| `serializer/RedisZSetCommandsRangeSerializer.java` | 2 行差异 |
| `beans/WrapperProcessors.java` | 4 行差异（处理器列表组成） |
| `connection/dynamic/web/DynamicRedisConnectionFactoryCleanerListener.java` | **`javax.servlet`** 的 `ServletRequestListener`（0.2.x：`jakarta.servlet`） |
| `util/SpringRedisCommandUtils.java` | 57 行差异 —— 面向 SDR 2.x 命令面的辅助（无 3.x 独有接口） |
| `resources/META-INF/spring-data-redis-metadata.yaml` | `version: 2.7.18` vs `3.5.10`（306 行差异 = SDR 2.x 与 3.x 的方法集） |

1.x 的 `spring.factories` 注册与 0.2.x **相同的三个条目**：

```properties
org.springframework.context.ApplicationContextInitializer=\
io.microsphere.redis.spring.context.RedisInitializer
io.microsphere.redis.spring.context.RedisModuleInitializer=\
io.microsphere.redis.spring.context.RedisInterceptorModuleInitializer
org.springframework.data.redis.serializer.RedisSerializer=\
io.microsphere.redis.spring.serializer.RedisCommandEventSerializer
```

外加 `META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader` SPI 文件。

## 实践含义

- **API 兼容**：`@EnableRedisInterceptor` 属性（`wrapRedisTemplates`、`exposeCommandEvent`、`sources`）、
  `RedisMethodContext`、拦截器接口、`RedisCommandEvent`、属性键 —— 均已核实两线一致。
  1.x 上编写的拦截器代码可在 0.2.x 原样编译。
- **元数据边界**：只存在于 SDR 3.x 的命令在 1.x 没有 `MethodMetadata`，
  `isWriteMethod()`/index 查找无法解析 —— 不要跨线复制。
- **Servlet 命名空间**：本线的 `DynamicRedisConnectionFactory` 线程清理依赖 `javax.servlet`；
  Tomcat 10 / Boot 3 环境请使用 0.2.x。
- **序列化器更名陷阱**：迁移到 0.2.x 时，
  `io.microsphere.redis.spring.serializer.RedisZSetCommandsWeightsSerializer`（1.x）的 bean/类引用
  需改为 `WeightsSerializer`。

## 设计模式 / 关键流程

全部十种模式（JDK 代理、AOP 工厂代理、模板装饰器、拦截器链、上下文对象、观察者、模板方法、
注册表、SPI、BeanDefinition 变更、线程局部会话状态）与三张流程图原样适用 —— 见
[../../current/zh/microsphere-redis-spring.md](../../current/zh/microsphere-redis-spring.md)。

## 测试策略

测试集形态与 0.2.x 相同（注解/上下文测试 + 借助 spring-test 模块对本地 Redis 的集成测试）；
1.x 构建运行在 0.1.26 栈的 Boot 2.x 依赖线上。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-spring.md](../../../user-guide/1.x/zh/microsphere-redis-spring.md)
- English: [../microsphere-redis-spring.md](../microsphere-redis-spring.md)

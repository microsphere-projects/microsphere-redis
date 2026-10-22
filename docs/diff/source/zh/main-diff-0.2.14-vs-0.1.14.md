# 源码差异（src/main）：`0.2.14` 对比 `0.1.14`

本文逐 Maven 模块对比 Microsphere Redis 最新两个 tag 的 `src/main` 内容，仅比较在两个 tag 中**同名**的模块。

- **`0.2.14`** —— 0.2.x 版本线：**Java 17**、**Jakarta EE**、Spring Data Redis **3.5.10**（Spring Boot 3.x）。
- **`0.1.14`** —— 0.1.x 版本线：**Java 8**、**Java EE（`javax`）**、Spring Data Redis **2.7.18**（Spring Boot 2.x）。

对比**排除**以下属于两条版本线设计内、因而不列入表格的差异类别：

1. **Java 8 与 Java 17 语言/API 语法差异** —— `record`、模式匹配 `instanceof`、`switch` 表达式，及其直接连带改动（如 `getData()` → `data()`）。
2. **Java EE 与 Jakarta EE API 差异** —— 如 `javax.servlet.*` → `jakarta.servlet.*` 的包名迁移。

两个 tag 中名称匹配不上的模块予以忽略。

## 模块覆盖情况

| Maven 模块 | 两边均有 `src/main`？ | 排除后的实质差异 |
|---|---|---|
| `microsphere-redis-core` | 是 | 无（仅 Java 17 的 `record`/`instanceof` 改动，已排除） |
| `microsphere-redis-dependencies` | 是 | 无 |
| `microsphere-redis-generator` | 是 | 无（仅删除空行） |
| `microsphere-redis-parent` | 是 | 无 |
| `microsphere-redis-replicator-spring` | 是 | 有 |
| `microsphere-redis-spring` | 是 | 有 |
| `microsphere-redis-spring-boot` | 是 | 无 |
| `microsphere-redis-spring-cloud` | 是 | 有 |
| `microsphere-redis-spring-test` | 是 | 无 |

---

## `microsphere-redis-core`

仅两个文件不同，且均为纯 Java 17 语言特性，已排除：

- `metadata/RedisMetadata.java` —— `equals()` 改用模式匹配 `instanceof RedisMetadata that`。
- `util/RawValue.java` —— 由 class 改为 `record`；`util/ValueHolder.java` 随之 `getData()` → `data()`。

**无功能性差异。**

---

## `microsphere-redis-replicator-spring`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `kafka/producer/KafkaProducerRedisCommandEventListener` | `kafkaTemplate.send(...).completable()` | `kafkaTemplate.send(...)` | **API 兼容** —— 较新的 Spring for Apache Kafka 中 `KafkaTemplate.send()` 已直接返回 `CompletableFuture`，无需 `.completable()`。 |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration` | 先用 `instanceof ApplicationContextAware`/`SmartInitializingSingleton`/`DisposableBean` 判型再调用 | 直接调用 `setApplicationContext()`、`afterSingletonsInstantiated()`、`destroy()`；新增 `setObservationEnabled(true)` | **API 兼容 + 功能需要** —— `KafkaTemplate` 现已原生实现这些生命周期接口；并启用 observation 以适配新的 micrometer-observation 体系。 |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration` | `destroy...() throws Exception` | `destroy...()`（去掉受检异常） | **API 兼容** —— `KafkaTemplate.destroy()` 不再声明受检异常。 |

---

## `microsphere-redis-spring`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `beans/WrapperProcessors` | 辅助方法 `getResolvableType((AbstractBeanDefinition) beanDefinition)` | `beanDefinition.getResolvableType()` | **API 兼容** —— Spring 的 `BeanDefinition` 现已直接暴露 `getResolvableType()`，无需强转 + 辅助方法。 |
| `serializer/AbstractSerializer` | 静态辅助 `resolveTargetType(this)`，并按 `AbstractSerializer`/`RedisSerializer` 分支解析 | 私有 `resolvableType()`，始终 `as(RedisSerializer.class)` | **API 兼容** —— `RedisSerializer` 本身已提供目标类型解析，3.x 下多余分支不再需要。 |
| `serializer/AbstractSerializer`、`ByteArraySerializer`、`EnumSerializer` | `getTargetType()` 未覆盖任何接口方法 | `getTargetType()` 加 `@Override` | **API 兼容** —— Spring Data Redis 3.x 中 `getTargetType()` 已成为 `RedisSerializer` 接口方法。 |
| `serializer/HoldingValueRedisSerializerWrapper` | 通过反射辅助 `Serializers.canSerialize(...)` / `Serializers.getTargetType(...)` 委托 | 直接 `this.delegate.canSerialize(...)` / `getTargetType()` | **API 兼容** —— 接口方法已原生提供，反射兜底被移除。 |
| `serializer/Serializers` | 用 `MethodHandle` 反射实现 `canSerialize`/`getTargetType`/`doGetTargetType` 以兼容 2.2 之前版本；有 `doRegister(...)`；导入 `RedisZSetCommands.Aggregate`/`Weights` | 删除反射辅助，改用 `RedisSerializer` 原生方法；导入 `connection.zset.Aggregate`/`Weights`；`register()` 内联并借 `serializer.getTargetType()` 打印日志 | **API 兼容** —— 基线升至 Spring Data Redis 3.x，向后兼容的反射垫片被删除。 |
| `serializer/RedisZSetCommandsWeightsSerializer`（**删除**）→ `serializer/WeightsSerializer`（**新增**） | 序列化 `RedisZSetCommands.Weights` | 序列化 `connection.zset.Weights` | **API 重组织** —— `Weights` 在 Spring Data Redis 3.x 迁至 `zset` 子包，序列化器随之改名。（注：新文件缺少 Apache 许可证头。） |
| `serializer/RedisZSetCommandsRangeSerializer` | 普通类 | 标注 `@Deprecated` | **API 重组织** —— `RedisZSetCommands.Range` 被 `org.springframework.data.domain.Range` 取代，故该序列化器废弃。 |
| `util/SpringRedisCommandUtils` | 旧式 `switch`：TX/PubSub/Connection/default/stream 均返回 `redisConnection`；stream 落到 `redisConnection` | `getRedisCommands()` 对 TX/PubSub/Connection/default 返回 `redisConnection.commands()`，对 stream 接口返回 `redisConnection.streamCommands()` | **API 兼容 + 功能需要** —— Spring Data Redis 3.x 新增 `RedisCommandsProvider.commands()` 与 `RedisConnection.streamCommands()`，从而返回正确的子接口而非连接本身。（`switch` → `switch` 表达式的改写作为 Java 17 语法已排除。） |
| `resources/META-INF/spring-data-redis-metadata.yaml` | `version: 2.7.18` | `version: 3.5.10` | **功能需要** —— 针对 Spring Data Redis 3.5.10 重新生成命令元数据，反映上游 API 变化：`RedisZSetCommands.Range`→`domain.Range`、`RedisZSetCommands.Limit`→`connection.Limit`、`Aggregate`/`Weights`→`connection.zset.*`；`slaveOf`/`slaveOfNoOne`→`replicaOf`/`replicaOfNoOne`（`SLAVEOF`→`REPLICAOF`）；`bgWriteAof`→`bgReWriteAof`；新增 `ReactiveStringCommands.setGet` / `RedisStringCommands.setGet` 重载。 |

---

## `microsphere-redis-spring-cloud`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `autoconfigure/RedisCloudAutoConfiguration` | `@AutoConfigureAfter` 仅列 `...data.redis.RedisAutoConfiguration`（Boot `[2.0, 4.0)`） | 增加 `org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration`（Boot `[4.0,)`） | **功能需要** —— 适配 Spring Boot 4.x 对 Redis 自动配置类的迁移，保证正确的自动配置顺序。 |
| `resources/META-INF/config/default/features.yaml` | 无连接工厂；仅 `RedisZSetCommandsRangeSerializer` | 增加 `RedisConnectionFactory` / `ReactiveRedisConnectionFactory`；增加 Boot 3.4 `LettuceClientOptionsBuilderCustomizer` 与 Boot 3.1 `RedisConnectionDetails`；注册 `WeightsSerializer` | **功能需要** —— 特性探测清单扩展以覆盖更新的 Spring Boot Redis 定制器及改名后的序列化器。 |

---

## 已排除差异说明

以下差异存在于两个 tag 之间，但**有意排除**、不列入上表：

| 位置 | 改动 | 排除类别 |
|---|---|---|
| `core/RedisMetadata` | `equals()` 使用模式匹配 `instanceof` | Java 8 与 Java 17 语法 |
| `core/RawValue`、`core/ValueHolder` | class → `record`，`getData()` → `data()` | Java 8 与 Java 17 语法 |
| `spring/DynamicRedisConnectionFactoryCleanerListener` | `javax.servlet.*` → `jakarta.servlet.*` | Java EE 与 Jakarta EE API |
| `spring/SpringRedisCommandUtils` | `switch` 语句 → `switch` 表达式 | Java 8 与 Java 17 语法 |

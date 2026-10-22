# 源码差异（src/test）：`0.2.14` 对比 `0.1.14`

本文逐 Maven 模块对比 Microsphere Redis 最新两个 tag 的 `src/test` 内容，仅比较在两个 tag 中**同名**的模块。

- **`0.2.14`** —— 0.2.x 版本线：**Java 17**、**Jakarta EE**、Spring Data Redis **3.5.10**（Spring Boot 3.x）。
- **`0.1.14`** —— 0.1.x 版本线：**Java 8**、**Java EE（`javax`）**、Spring Data Redis **2.7.18**（Spring Boot 2.x）。

排除规则与 [`main-diff-0.2.14-vs-0.1.14.md`](./main-diff-0.2.14-vs-0.1.14.md) 一致：
Java 8/17 语法差异与 Java EE/Jakarta EE API 差异不列入。

## 模块覆盖情况

| Maven 模块 | 两边均有 `src/test`？ | 排除后的实质差异 |
|---|---|---|
| `microsphere-redis-core` | 是 | 无（record 访问器改动，已排除） |
| `microsphere-redis-generator` | 是 | 无 |
| `microsphere-redis-replicator-spring` | 是 | 有 |
| `microsphere-redis-spring` | 是 | 有 |
| `microsphere-redis-spring-boot` | 是 | 无 |
| `microsphere-redis-spring-cloud` | 是 | 有 |
| `microsphere-redis-spring-test` | 是 | 无 |

---

## `microsphere-redis-core`

- `util/RawValueTest` —— `rawValue.getData()` → `rawValue.data()`。

为 `RawValue` → `record` 迁移的连带改动；作为 Java 17 语法差异**已排除**。**无功能性差异。**

---

## `microsphere-redis-replicator-spring`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `kafka/KafkaRedisReplicatorModuleInitializerTest` | 先通过 `instanceof` 将 context 转为 `DefaultResourceLoader` 再调用 `setClassLoader(...)` | 直接 `this.context.setClassLoader(...)` | **API 兼容** —— 测试 context 类型现已原生暴露 `setClassLoader(...)`，无需 `instanceof`/强转兜底。 |

---

## `microsphere-redis-spring`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `AbstractRedisCommandEventTest` | 未知命令断言 `assertThrows(RuntimeException.class, ...)` | 收紧为 `assertThrows(RedisSystemException.class, ...)` | **API 兼容** —— Spring Data Redis 3.x 抛出更具体的 `RedisSystemException`，故收紧断言。 |
| `serializer/AbstractSerializerTest` | 使用反射辅助 `getTargetType(serializer)` / `canSerialize(serializer, type)`；泛型经 `forType(getClass()).as(AbstractSerializerTest.class)` 解析 | 直接 `serializer.getTargetType()` / `serializer.canSerialize(type)`；泛型经 `forType(getClass()).getSuperType()` 解析 | **API 兼容** —— 与生产代码一致：用原生 `RedisSerializer` 方法替代被删除的反射辅助。 |
| `serializer/SerializersTest` | 测试 `canSerialize`、`getTargetType`、`doGetTargetType`；使用 `RedisZSetCommands.Weights` + `REDIS_ZSET_COMMANDS_WEIGHTS_SERIALIZER` | 删除相应测试（辅助方法已删）；改用 `connection.zset.Weights` + `WEIGHTS_SERIALIZER` | **API 兼容 / 重组织** —— 跟随 `Serializers` 清理与 `Weights`→`zset` 迁移。 |
| `serializer/RedisZSetCommandsWeightsSerializerTest`（**删除**）→ `serializer/WeightsSerializerTest`（**新增**） | 测试针对 `RedisZSetCommands.Weights` 的序列化器 | 测试针对 `connection.zset.Weights` 的序列化器 | **API 重组织** —— 与被改名序列化器对应的测试改名。 |
| `util/SpringRedisCommandUtilsTest` | 反射加载 stream 接口（`loadClass(...)` 判空兜底），以 `assertSameType(...)`（比较 `getClass()`）断言，方法 id 期望 `RedisStringCommands.set(...)` | 直接断言 `RedisStreamCommands`/`ReactiveStreamCommands`，新增 `RedisCommandsProvider` 负例，改用 `assertSame(...)` 对应 `redisConnection.commands()` / `streamCommands()`，方法 id 期望 `DefaultedRedisConnection.set(...)` | **API 兼容 + 功能需要** —— Spring Data Redis 3.x 始终提供 stream 子接口，并将默认接口方法经 `DefaultedRedisConnection` / `RedisCommandsProvider` 路由，故“可选反射兜底”改为直接断言。 |

---

## `microsphere-redis-spring-cloud`

| 文件 | `0.1.14` | `0.2.14` | 原因 |
|---|---|---|---|
| `event/PropagatingRedisConfigurationPropertyChangedEventApplicationListenerTest` | 注册 listener 但从未移除 | 结尾新增 `this.context.removeApplicationListener(listener)` | **功能 / 测试规范** —— 注销 listener 以避免跨测试泄漏，并补全文件结尾换行。 |

---

## 已排除差异说明

| 位置 | 改动 | 排除类别 |
|---|---|---|
| `core/RawValueTest` | `getData()` → `data()`（record 访问器） | Java 8 与 Java 17 语法 |
| `spring/RedisMethodContextTest` | 去掉 `param.isNamePresent()` 兜底，因 0.2.x 以 `-parameters` 编译（参数名始终可得） | Java 8 与 Java 17 构建/语言行为 |
| `spring/EnumSerializerTest` | `calcBytesLength(SpringVersion.values())` 期望值 `2` → `1` | 依赖导致的取值变化（Spring `SpringVersion` 枚举数量），并非本项目的实现差异 |

# microsphere-redis-core — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-core` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-core:0.1.14` |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、Java 8+ |

> 本文档面向 **1.x** 版本线。0.2.x 请见[../current/zh/microsphere-redis-core.md](../../current/zh/microsphere-redis-core.md)。

## 概述

`microsphere-redis-core` 是 microsphere-redis 与框架无关的基础层：Redis 命令元数据模型、元数据加载器 SPI、
命令目录，以及原始值（`byte[]` ↔ 对象）持有机制。它**不依赖 Spring**，因此也可独立用于任何工具场景中做
Redis 命令推理（例如"`GETSET` 是不是写命令？"）。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-core</artifactId>
    <version>0.1.14</version>
</dependency>
```

（也可通过 `microsphere-redis-dependencies` BOM —— 见其 [1.x 文档](./microsphere-redis-dependencies.md)。）

## API 说明

### 元数据模型（`io.microsphere.redis.metadata`）

| 类型 | 用途 |
|------|------|
| `RedisMetadata` | `{version, List<MethodMetadata>}`，由 YAML 加载；`merge(RedisMetadata)` 折叠各加载器贡献（对方版本生效、methods 追加） |
| `MethodMetadata` | 单个命令方法：`index`、`interfaceName`、`methodName`、`parameterNames`、`parameterTypes`、`commands`、`write` |
| `MethodInfo` | 反射 `Method` + `MethodMetadata` + 参数元数据的运行期三元组 |
| `ParameterMetadata` / `Parameter` | 参数描述（index/type/name）与运行期绑定（`value`、`rawValue`） |
| `RedisMetadataLoader` | SPI；`loadAll()` 经 `ServiceLoader` 发现实现并逐一合并 |

### 工具类（`io.microsphere.redis.util`）

| 类型 | 用途 |
|------|------|
| `RawValue` | `byte[]` 的不可变包装，基于内容的 `equals/hashCode`（1.x：final 字段类，带 `RawValue.of(bytes)` 工厂与构造器 —— 0.2.x 改为 `record`） |
| `ValueHolder` | 线程级（`ThreadLocal`）双向缓存：Java 对象 ↔ 序列化字节；`set/getValue/getRawValue/get/clear` |
| `RedisCommandUtils` | 读取 `META-INF/redis-commands`、`META-INF/redis-write-commands` 目录；`isRedisCommand(name)`、`isRedisWriteCommand(name)`；`buildMethodId/Signature/Index` 规范 id 语法 |
| `RedisUtils` | classpath 资源读取 `loadResource`/`loadResources` |

## 代码示例

```java
// 服务端命令是否为写命令？（模式来自 RedisCommandUtilsTest）
assertTrue(isRedisWriteCommand("SET"));
assertFalse(isRedisWriteCommand("GET"));

// 规范方法标识 —— 与 YAML 生成器相同的语法
int index = RedisCommandUtils.buildMethodIndex(
        "org.springframework.data.redis.connection.KeyCommands", "expire", "[B");

// 原始值回环：从序列化字节还原原始对象
byte[] bytes = "value".getBytes(UTF_8);
ValueHolder holder = ValueHolder.get();
holder.set("value", bytes);                       // redis-spring 的序列化器会自动做这一步
Object original = ValueHolder.getValue(bytes);    // -> "value"
ValueHolder.clear();                              // 线程结束必须清除
```

## 版本与兼容

| microsphere-redis | 分支 | Spring Data Redis | Spring Boot | Java |
|-------------------|------|-------------------|-------------|------|
| `0.1.x`（本文档，最新 `0.1.14`） | `release-1.x` | 2.1.0 – 2.7.x | 2.x | 8+ |
| `0.2.x`（最新 `0.2.14`） | `release` | 3.0.0 – 3.5.x | 3.x | 17 |

1.x 的 `RawValue` 早于 record 改写；本模块其余 API 在两条版本线之间源码兼容，针对它编写的代码
通常两条线都可编译。

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md) — 运行期消费者
- 开发者视角：[../../developer-guide/1.x/zh/microsphere-redis-core.md](../../../developer-guide/1.x/zh/microsphere-redis-core.md)

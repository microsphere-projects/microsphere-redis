# microsphere-redis-replicator-spring — 开发者文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-replicator-spring` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| 层次 | 可选 Kafka 复制附加模块 |
| 基线 | spring-kafka 2.x、Spring Boot 2.x、Java 8 |

> 管线架构、属性模型、引导 SPI 链与重放机制均与
> [0.2.x replicator 开发者文档](../../current/zh/microsphere-redis-replicator-spring.md)一致 ——
> 经分支 diff 核实，只有 Kafka 侧三个文件不同。

## 已核实的源码差异 vs 0.2.x

| 文件 | 1.x 行为 |
|------|----------|
| `kafka/KafkaRedisReplicatorConfiguration.java` | 仅 1 行 import 差异 |
| `kafka/producer/KafkaProducerRedisCommandEventListener.java` | 4 行差异（import 外观）；监听→按域扇出→异步发送流程一致 |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration.java` | **spring-kafka 2.x 兼容垫片**：`KafkaTemplate` 经 `instanceof ApplicationContextAware` / `SmartInitializingSingleton` / `DisposableBean` 守卫式调用而非直接调用，且**没有 `setObservationEnabled(true)`**（该 API 属 spring-kafka 3.x） |

其余部分 —— `RedisReplicatorConfiguration`（全部 `microsphere.redis.replicator.*` 键、默认值、
域映射、热加载监听）、消费者（`Redis-Replicator-` 组前缀、`poll-timeout`、并发 =
`max(topic 数, 配置值)`）、`RedisCommandReplicatedEvent`、`RedisComandEventPartitioner`、
`RedisCommandReplicator` 的原始连接重放，以及两条 `spring.factories` SPI 条目 —— **无差异**。

## 1.x 特有说明

- 事件字节由 **1.x** 的 `RedisCommandEventSerializer` 基于 **1.x** 元数据 YAML
  （`version: 2.7.18`）产出；index 语法只在本线内匹配 → 生产者与消费者必须同线部署。
- 无 Kafka metrics/observation 集成（Boot 2 micrometer 时代）；发送成败日志（`onComplete`）
  是生产者唯一的反馈渠道。

## 设计模式 / 关键流程

观察者链、每域执行器、按 key 分区、解包重放 —— 与 0.2.x 文档一致：
[../../current/zh/microsphere-redis-replicator-spring.md](../../current/zh/microsphere-redis-replicator-spring.md)。

## 链接

- 用户文档：[../../../user-guide/1.x/zh/microsphere-redis-replicator-spring.md](../../../user-guide/1.x/zh/microsphere-redis-replicator-spring.md)
- English: [../microsphere-redis-replicator-spring.md](../microsphere-redis-replicator-spring.md)

# microsphere-redis-replicator-spring — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-replicator-spring` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-replicator-spring:0.1.14` |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`、Spring Boot `2.x`、spring-kafka 2.x、Java 8+ |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-replicator-spring.md](../../current/zh/microsphere-redis-replicator-spring.md)。
> 源码与 0.2.x 基本一致（仅外观性差异）—— 属性与流程完全相同。

## 概述

通过 Kafka 在应用之间复制 Redis **写命令**。源应用把每条被拦截的 `RedisCommandEvent` 变成一条
Kafka 记录（每个复制"域"一个 topic）；开启消费者角色的目标应用反序列化后，通过反射在自己的
Redis 上重放 —— 不需要配置 Redis 主从复制的应用层复制。

## 安装

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-replicator-spring</artifactId>
    <version>0.1.14</version>
</dependency>
```

只要 `microsphere-redis-spring` 处于激活状态，本模块即通过 `spring.factories` 自动装配 ——
无需 `@Enable`。生产者角色需要 classpath 上有 `spring-kafka`。

## 配置属性

| 属性 | 默认值 | 作用 |
|------|--------|------|
| `microsphere.redis.replicator.enabled` | `true` | replicator 总开关（前提是 Redis 已启用） |
| `microsphere.redis.replicator.consumer.enabled` | `false` | 目标应用的重放角色 |
| `microsphere.redis.replicator.domains` | `default` | 复制域（事件按域扇出） |
| `microsphere.redis.replicator.domains.{domain}.redis-templates` | *（空）* | 属于该域的模板 bean 名 |
| `microsphere.redis.replicator.kafka.bootstrap.servers` | `${spring.kafka.bootstrap-servers:127.0.0.1:9092}` | broker 列表 |
| `microsphere.redis.replicator.kafka.topic-prefix` | `redis-replicator-event-topic-` | topic = 前缀 + 域名 |
| `microsphere.redis.replicator.kafka.listener.poll-timeout` | `10000` | 消费者轮询超时（毫秒） |
| `microsphere.redis.replicator.kafka.listener.concurrency` | `1` | 生效值：`max(topic 数, 配置值)` |

消费者组 id 前缀为 `Redis-Replicator-`。

## API 说明

- `KafkaProducerRedisCommandEventListener` —— `ApplicationListener<RedisCommandEvent>`；在每域一个的
  固定线程池上异步发送；Kafka 记录 **key = Redis 的 key**（arg 0，`RedisCommands.execute` 取 arg 1），
  **时间戳 = 原始执行时刻**，value 为 `RedisCommandEventSerializer` 产出的字节。
- `RedisComandEventPartitioner`（可选 bean）—— `Integer partition(RedisCommandEvent)`；返回分区可让
  同 key 命令串行；返回 `null` 交给 Kafka 按 key 哈希。
- `RedisCommandReplicator`（`microsphere:redisCommandReplicator`）—— 在**原始**连接（已解包，重放不会
  再次触发拦截）上解析命令子对象（`keyCommands()` 等）后反射执行。
- `RedisCommandReplicatedEvent` —— 每条消费记录发布一次；需要审计/否决重放时可自行监听。

## 使用示例

```properties
# 源应用
microsphere.redis.replicator.domains = dc-a,dc-b
microsphere.redis.replicator.domains.dc-a.redis-templates = redisTemplate,stringRedisTemplate

# 目标应用
microsphere.redis.replicator.consumer.enabled = true
microsphere.redis.replicator.domains = dc-a
microsphere.redis.replicator.kafka.bootstrap.servers = kafka:9092
```

之后源应用通过 `redisTemplate` 的一次写入，会在目标应用的 Redis 上被执行一次。

## 版本与兼容

| microsphere-redis | 分支 | Spring Boot | spring-kafka | Spring Data Redis |
|-------------------|------|-------------|--------------|-------------------|
| `0.1.14` | `release-1.x` | 2.x | 2.x | 2.1.0 – 2.7.x |
| `0.2.14` | `release` | 3.x | 3.x | 3.0.0 – 3.5.x |

重放保真度依赖本线的元数据 YAML（1.x 基于 Spring Data Redis 2.7.18）；`0.1.x` 生产者复制到
`0.2.x` 消费者（或反之）**不受支持** —— 事件序列化/index 语法只在同版本线内匹配。

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md) — 事件来源
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-replicator-spring.md](../../../developer-guide/1.x/zh/microsphere-redis-replicator-spring.md)

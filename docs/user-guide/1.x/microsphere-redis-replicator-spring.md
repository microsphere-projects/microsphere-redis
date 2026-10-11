# microsphere-redis-replicator-spring — User Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-replicator-spring` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Maven coordinates | `io.microsphere:microsphere-redis-replicator-spring:0.1.14` |
| Compatibility | Spring Data Redis `2.1.0 – 2.7.x`, Spring Boot `2.x`, spring-kafka 2.x, Java 8+ |

> 1.x version line. For 0.2.x, see [../current/microsphere-redis-replicator-spring.md](../current/microsphere-redis-replicator-spring.md).
> Source is effectively identical to 0.2.x apart from cosmetic diffs — same properties, same flow.

## Overview

Replicates Redis **write commands** between applications through Kafka. The source app turns every
intercepted `RedisCommandEvent` into a Kafka record (one topic per replication "domain"); a target app
with the consumer enabled deserializes records and replays them against its own Redis via reflection —
application-level replication that doesn't require Redis master/replica configuration.

## Installation

```xml
<dependency>
    <groupId>io.microsphere</groupId>
    <artifactId>microsphere-redis-replicator-spring</artifactId>
    <version>0.1.14</version>
</dependency>
```

Auto-wires through `spring.factories` once `microsphere-redis-spring` is active — no `@Enable` needed.
`spring-kafka` must be on the classpath for the producer role.

## Configuration Properties

| Property | Default | Effect |
|----------|---------|--------|
| `microsphere.redis.replicator.enabled` | `true` | replicator master switch (requires Redis enabled) |
| `microsphere.redis.replicator.consumer.enabled` | `false` | target-app replay role |
| `microsphere.redis.replicator.domains` | `default` | replication domains (fan-out per event) |
| `microsphere.redis.replicator.domains.{domain}.redis-templates` | *(empty)* | template bean names belonging to a domain |
| `microsphere.redis.replicator.kafka.bootstrap.servers` | `${spring.kafka.bootstrap-servers:127.0.0.1:9092}` | broker list |
| `microsphere.redis.replicator.kafka.topic-prefix` | `redis-replicator-event-topic-` | topic = prefix + domain |
| `microsphere.redis.replicator.kafka.listener.poll-timeout` | `10000` | consumer poll timeout (ms) |
| `microsphere.redis.replicator.kafka.listener.concurrency` | `1` | effective: `max(topicCount, value)` |

Consumer group ids are prefixed `Redis-Replicator-`.

## API Reference

- `KafkaProducerRedisCommandEventListener` — `ApplicationListener<RedisCommandEvent>`; sends asynchronously
  on a per-domain fixed thread pool; Kafka record **key = the Redis key** (arg 0, or arg 1 for
  `RedisCommands.execute`), **timestamp = original execution instant**, value = bytes from
  `RedisCommandEventSerializer`.
- `RedisComandEventPartitioner` (optional bean) — `Integer partition(RedisCommandEvent)`; return a
  partition to serialize same-key commands; `null` lets Kafka hash by key.
- `RedisCommandReplicator` (`microsphere:redisCommandReplicator`) — replays by resolving the command
  sub-object (`keyCommands()` etc.) on the **raw** connection (unwrapped, so replay doesn't re-trigger
  interception).
- `RedisCommandReplicatedEvent` — published per consumed record; listen if you need to audit/ veto replay.

## Usage Example

```properties
# source app
microsphere.redis.replicator.domains = dc-a,dc-b
microsphere.redis.replicator.domains.dc-a.redis-templates = redisTemplate,stringRedisTemplate

# target app
microsphere.redis.replicator.consumer.enabled = true
microsphere.redis.replicator.domains = dc-a
microsphere.redis.replicator.kafka.bootstrap.servers = kafka:9092
```

Then a write through `redisTemplate` in the source app ends up executed against the target app's Redis.

## Version & Compatibility

| microsphere-redis | branch | Spring Boot | spring-kafka | Spring Data Redis |
|-------------------|--------|-------------|--------------|-------------------|
| `0.1.14` | `release-1.x` | 2.x | 2.x | 2.1.0 – 2.7.x |
| `0.2.14` | `release` | 3.x | 3.x | 3.0.0 – 3.5.x |

Replay fidelity relies on the metadata YAML of your line (1.x: generated against Spring Data Redis 2.7.18);
a `0.1.x` producer replicating to a `0.2.x` consumer (or vice versa) is **not supported** — event
serialization/index grammar matches within a line, not across the `set→2.7 / 3.x` method sets.

## Related Modules

- [microsphere-redis-spring (1.x)](./microsphere-redis-spring.md) — event source
- Developer guide: [../../developer-guide/1.x/microsphere-redis-replicator-spring.md](../../developer-guide/1.x/microsphere-redis-replicator-spring.md)

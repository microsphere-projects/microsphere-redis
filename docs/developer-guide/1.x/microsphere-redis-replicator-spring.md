# microsphere-redis-replicator-spring — Developer Guide (1.x)

| Field | Value |
|-------|-------|
| Module | `microsphere-redis-replicator-spring` |
| Git branch | `release-1.x` (1.x line, latest release `0.1.14`) |
| Layer | Optional Kafka replication add-on |
| Baseline | spring-kafka 2.x, Spring Boot 2.x, Java 8 |

> Pipeline architecture, property model, bootstrap SPI chain, and replay mechanics are identical to the
> [0.2.x replicator developer guide](../current/microsphere-redis-replicator-spring.md) — verified against
> the branch diff, only three Kafka-side files differ.

## Verified Source Diffs vs 0.2.x

| File | 1.x behavior |
|------|--------------|
| `kafka/KafkaRedisReplicatorConfiguration.java` | 1-line import diff only |
| `kafka/producer/KafkaProducerRedisCommandEventListener.java` | 4-line diff (import cosmetics); same listen→domain fan-out→async send flow |
| `kafka/producer/KafkaProducerRedisReplicatorConfiguration.java` | **spring-kafka 2.x compatibility shims**: `KafkaTemplate` is handled through `instanceof ApplicationContextAware` / `SmartInitializingSingleton` / `DisposableBean` guards instead of direct calls, and **no `setObservationEnabled(true)`** (that API is from spring-kafka 3.x) |

Everything else — `RedisReplicatorConfiguration` (all `microsphere.redis.replicator.*` keys, defaults,
domain mapping, hot-reload listener), the consumer (`Redis-Replicator-` group prefix, `poll-timeout`,
concurrency = `max(topics, configured)`), `RedisCommandReplicatedEvent`, `RedisComandEventPartitioner`,
`RedisCommandReplicator` raw-connection replay, and the two `spring.factories` SPI entries — shows **no
diff** between the branches.

## 1.x-Specific Notes

- Event bytes are produced by the **1.x** `RedisCommandEventSerializer` over the **1.x** metadata YAML
  (`version: 2.7.18`); index grammar matches only within the line → pair producer and consumer on the
  same version line.
- Kafka metrics/observation integration absent (Boot 2 micrometer era); send success/failure logging is
  the only producer feedback channel (`onComplete`).

## Design Patterns / Key Flows

Observer chain, per-domain executor, key-partitioning, unwrapped replay — as documented for 0.2.x:
[../current/microsphere-redis-replicator-spring.md](../current/microsphere-redis-replicator-spring.md).

## Links

- User guide: [../../user-guide/1.x/microsphere-redis-replicator-spring.md](../../user-guide/1.x/microsphere-redis-replicator-spring.md)
- Chinese: [../zh/microsphere-redis-replicator-spring.md](zh/microsphere-redis-replicator-spring.md)

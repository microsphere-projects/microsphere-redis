# microsphere-redis-spring-test

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-spring-test` |
| Git 分支 | `release`（0.2.x 版本线） |
| 最新发布版本 | `0.2.14` |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-spring-test` |
| 兼容性 | JUnit 5（Jupiter）、Spring Test 6.x、Spring Data Redis 3.0.0 – 3.5.x、JDK 17+ |
| 依赖作用域 | `test` —— 仅以测试依赖方式引入 |

## 概述

`microsphere-redis-spring-test` 提供一个开箱即用、连接**真实本地 Redis 服务器**的 JUnit 5 测试基类，
免除在每个集成测试里重复声明 `RedisTemplate` / `StringRedisTemplate` / 连接工厂 bean 的样板代码。

本项目自身的测试（`microsphere-redis-spring`、`microsphere-redis-replicator-spring` 等）全部继承它，
针对由仓库 `docker/` compose 配置提供的 Redis 实例运行（CI 在 `127.0.0.1:6379` 启动 Redis）。

## 安装

```xml
<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-redis-spring-test</artifactId>
    <scope>test</scope>
</dependency>
```

运行测试前需在本地启动 Redis（例如 `docker run -p 6379:6379 redis`），或使用仓库 `docker/` compose 文件。

## API 说明

### AbstractRedisTest

```java
@Disabled
@SpringJUnitConfig(classes = RedisConfig.class)
public abstract class AbstractRedisTest {

    public static final Method SET_METHOD;           // RedisConnection#set(byte[], byte[])
    public static final Object[] SET_METHOD_ARGS;    // "key"、"value" 的 UTF-8 字节
    public static final String SOURCE_BEAN_NAME_FOR_REDIS_TEMPLATE; // "redisTemplate"

    @Autowired @Qualifier("redisTemplate")
    protected RedisTemplate redisTemplate;

    @Autowired @Qualifier("stringRedisTemplate")
    protected StringRedisTemplate stringRedisTemplate;

    @Autowired
    protected ConfigurableApplicationContext context;

    protected void assertRedisTemplateSet(String key, String value);
    protected void assertStringRedisTemplateSet(String key, String value);
    public static void assertSet(RedisTemplate redisTemplate, String key, String value);
}
```

说明：

- 基类带 `@Disabled`；子类显式执行，或在 Redis 可用的 CI 环境中运行。
- `assertSet(...)` 执行 `opsForValue().set(key, value)` 后断言读回值与写入值一致。

### RedisConfig

`io.microsphere.redis.spring.test.config.RedisConfig` 声明三个静态 `@Bean`：

| Bean | 类型 | 说明 |
|------|------|------|
| `redisTemplate` | `RedisTemplate` | 注入下述连接工厂 |
| `stringRedisTemplate` | `StringRedisTemplate` | 注入下述连接工厂 |
| `redisConnectionFactory` | `LettuceConnectionFactory` | 硬编码指向 `127.0.0.1:6379`；立即调用 `afterPropertiesSet()` 与 `validateConnection()`，Redis 缺失时测试快速失败 |

## 代码示例

典型的子类——基础夹具 + `@EnableRedisInterceptor`（`AbstractRedisCommandEventTest` → `RedisCommandEventIntegrationTest` 的用法模式）：

```java
@EnableRedisInterceptor
class MyRedisCommandEventTest extends AbstractRedisTest {

    @Test
    void setPublishesCommandEvent() {
        List<RedisCommandEvent> events = new ArrayList<>();
        context.addApplicationListener((ApplicationListener<RedisCommandEvent>) events::add);

        assertStringRedisTemplateSet("key-A", "value-A");

        assertEquals(1, events.size());
        assertEquals("set", events.get(0).getMethodName());
    }
}
```

## 版本与兼容性

| 项目 | 值 |
|------|-----|
| 版本线 | 0.2.x（`release` 分支），最新发布 **0.2.14** |
| Redis 服务器 | 任何支持 RESP `SET`/`GET` 的单机实例（`127.0.0.1:6379`） |
| Spring 技术栈 | Spring Test 6.x / Spring Data Redis 3.0.0 – 3.5.x、Lettuce 客户端 |
| JDK | 17+ |
| 遗留线 | Spring Boot 2 技术栈对应 0.1.x：[../../1.x/microsphere-redis-spring-test.md](../../1.x/microsphere-redis-spring-test.md) |

## 相关模块

- [microsphere-redis-spring](./microsphere-redis-spring.md) —— 本模块最常测试的目标
- 开发者文档：[../../developer-guide/current/microsphere-redis-spring-test.md](../../../developer-guide/current/microsphere-redis-spring-test.md)

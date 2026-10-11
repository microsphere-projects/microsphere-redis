# microsphere-redis-generator — 用户文档（1.x）

| 字段 | 值 |
|------|-----|
| 模块 | `microsphere-redis-generator` |
| Git 分支 | `release-1.x`（1.x 版本线，最新发布 `0.1.14`） |
| Maven 坐标 | `io.microsphere:microsphere-redis-generator:0.1.x`（仅构建期，不在 BOM 中） |
| 兼容范围 | Spring Data Redis `2.1.0 – 2.7.x`（1.x 元数据基于 **2.7.18** 生成）、JDK **9+** |

> 1.x 版本线。0.2.x 请见[../current/zh/microsphere-redis-generator.md](../../current/zh/microsphere-redis-generator.md)。

## 概述

构建期工具：用自定义 JDK `Doclet` 解析 `spring-data-redis` 的 **sources jar**，把每个
`Redis*Commands` 接口方法与 Redis 服务端命令的映射生成到 `microsphere-redis-spring` 的
`META-INF/spring-data-redis-metadata.yaml`。

**与 0.2.x 的不同：**1.x 根 pom 中该模块只在 `java9+` profile
（`<activation><jdk>[9,)</jdk></activation>`）下加入 reactor —— 因为 1.x 运行线面向 Java 8，
而 doclet 需要 JDK 9+ API。在 JDK 8 上构建时，模块直接随包携带已生成好的 YAML。

## 用法

通常无需手动调用 —— 构建该模块即自动执行：

```bash
# 在仓库根目录、release-1.x 分支、JDK 9+ 下
mvn -pl microsphere-redis-generator -am package
# 1) maven-dependency-plugin 解包 spring-data-redis:2.7.x 源码（仅 *Commands.java）
# 2) exec-maven-plugin 运行 SpringDataRedisMetadataGenerator
#    -> microsphere-redis-spring/src/main/resources/META-INF/spring-data-redis-metadata.yaml
```

手动调用（签名相同）：

```bash
java -classpath <generator classpath> --add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
  io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator \
  <source-path> <target-metadata.yaml>
```

## 输出契约

```yaml
version: 2.7.18
methods:
- index: 498782566
  interfaceName: org.springframework.data.redis.connection.RedisHyperLogLogCommands
  methodName: pfAdd
  parameterNames: [key, values]
  parameterTypes: ['[B', '[[B']
  commands: [PFADD]
  write: true
```

文件可加载进 `microsphere-redis-core` 的 `RedisMetadata`；`index` 遵循与运行期相同的
`RedisCommandUtils.buildMethodIndex` 语法，因此任意 Spring Data Redis 2.x 版本生成的 YAML
都与 1.x 元数据仓库自洽。

## 版本与兼容

| microsphere-redis | 解析的 Spring Data Redis | JDK 要求 |
|-------------------|--------------------------|----------|
| `0.1.14`（本线） | 2.1.0 – 2.7.x（随包 YAML：2.7.18） | 构建需 9+，运行线是 Java 8 |
| `0.2.14` | 3.0.0 – 3.5.x（随包 YAML：3.5.10） | 17（reactor 无条件包含） |

## 相关模块

- [microsphere-redis-spring（1.x）](./microsphere-redis-spring.md) — 消费该 YAML
- [microsphere-redis-core（1.x）](./microsphere-redis-core.md) — 提供生成器校验用的命令目录
- 开发者文档：[../../developer-guide/1.x/zh/microsphere-redis-generator.md](../../../developer-guide/1.x/zh/microsphere-redis-generator.md)

# microsphere-redis-core

| 项目 | 值 |
|------|-----|
| 模块 | `microsphere-redis-core` |
| Git 分支 | `release` |
| 版本线 | 0.2.x（最新发布版本 `0.2.14`，分支顶端 `0.2.15-SNAPSHOT`） |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-core` |
| 打包方式 | `jar` |
| 兼容范围 | Spring Data Redis 3.0.0 – 3.5.x · Spring Boot 3.x · JDK 17+ |
| 运行时依赖 | 无强制依赖； classpath 上需要 `microsphere-java-core`（本模块中声明为 `optional`） |

## 概述

`microsphere-redis-core` 是 Microsphere Redis 的基础模块。它对 **Redis 命令元数据** 进行建模——即 Spring Data Redis
命令接口中的哪个 Java 方法对应哪个 Redis 命令、该命令是否属于写命令——同时提供上层构建所依赖的工具类与值持有器。本模块
**不依赖 Spring**，仅由纯 Java 代码和少量 classpath 资源组成，因此可以在非 Spring 的工具场景中独立使用。

它对外提供两个包：

| 包 | 包含内容 | 用途 |
|---------|----------|------|
| `io.microsphere.redis.metadata` | `RedisMetadata`、`MethodMetadata`、`ParameterMetadata`、`Parameter`、`MethodInfo`、`RedisMetadataLoader` | 元数据对象模型及其基于 `ServiceLoader` 的 SPI |
| `io.microsphere.redis.util` | `RedisCommandUtils`、`RedisUtils`、`RawValue`、`ValueHolder` | 命令目录查询、id/索引计算、资源加载、原始值缓存 |

它同时以 classpath 资源的形式内置了两份命令目录：

| 资源 | 内容 | 来源 |
|----------|----------|--------|
| `META-INF/redis-commands` | 595 个 Redis 命令名（每行一个，`#` 注释行被忽略） | Redis 8.4.0 的 `COMMAND LIST` 命令 |
| `META-INF/redis-write-commands` | 177 个 Redis 写命令名 | Redis 8.4.0 的 `ACL CAT "write"` 命令 |

**什么场景需要直接使用本模块？**

- 绝大多数情况下是 *传递依赖*：[microsphere-redis-spring](./microsphere-redis-spring.md) 将它声明为普通（非 optional）
  的编译依赖，因此使用 Spring 集成层的应用已经在 classpath 上拥有它。
- 当你需要构建自己的、基于元数据的扩展（代码生成器、审计工具、自定义拦截器库）而又不引入 Spring 层时，请 *显式* 添加本模块。
- 实现 [RedisMetadataLoader](#redismetadataloader-spi) 以便向合并后的元数据模型贡献你自己的命令元数据。

## 安装

先导入 BOM（Bill of Materials），再声明本模块依赖：

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.microsphere-projects</groupId>
            <artifactId>microsphere-redis-dependencies</artifactId>
            <version>${microsphere-redis.version}</version> <!-- 0.2.14 -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>io.github.microsphere-projects</groupId>
        <artifactId>microsphere-redis-core</artifactId>
    </dependency>
</dependencies>
```

由于 `microsphere-java-core`（提供 `io.microsphere.util.*`、`io.microsphere.logging.*`、`io.microsphere.annotation.*`）
在本模块中被声明为 `<optional>true</optional>`，它 **不会** 传递给消费者。直接使用 `microsphere-redis-core` 的应用必须
自行添加它：

```xml

<dependency>
    <groupId>io.github.microsphere-projects</groupId>
    <artifactId>microsphere-java-core</artifactId>
</dependency>
```

其版本已经通过 BOM 的父级继承链（`microsphere-redis-parent` → `microsphere-spring-cloud-dependencies`）得到统一管理。

## API 参考

### `io.microsphere.redis.metadata`

#### RedisMetadata

顶层容器：一个 `version` 字符串加上一组 [MethodMetadata](#methodmetadata) 条目。通常由 SnakeYAML 反序列化
`META-INF/spring-data-redis-metadata.yaml` 得到。

| 方法签名 | 语义 |
|-----------|-----------|
| `public String getVersion()` | 模式/数据版本，例如生成该文件的 Spring Data Redis 版本；可能为 `null`。 |
| `public void setVersion(String version)` | 设置版本号。 |
| `public List<MethodMetadata> getMethods()` | 永不为 `null`；未设置时延迟初始化为 `LinkedList`。返回列表是可变的，可直接添加条目。 |
| `public void setMethods(List<MethodMetadata> methods)` | 替换整个列表。 |
| `public RedisMetadata merge(RedisMetadata another)` | 采用 `another` 的版本，并把它的全部方法 **追加** 到当前实例；返回 `this` 便于链式调用。不做重复检测。 |

`equals`/`hashCode` 比较 `version` 与整个 `methods` 列表；`toString` 输出形如
`RedisMetadata[version=...,methods=[...]]`。`merge` 对版本而言是“后者覆盖前者”，这正是
[loadAll()](#redismetadataloader-spi) 最终报告最后一个贡献者版本号的原因。

#### MethodMetadata

可变的 JavaBean，描述一个 Redis 命令方法；它是 SnakeYAML 绑定 YAML 中每个 `methods` 条目所使用的类型。

| 属性 | 访问器 | 语义 |
|----------|-----------|-----------|
| `index` | `int getIndex()` / `void setIndex(int)` | 方法的数字标识：`abs(methodId.hashCode())`，由 `RedisCommandUtils.buildMethodIndex(...)` 计算。在元数据文件内唯一。 |
| `interfaceName` | `String getInterfaceName()` / `void setInterfaceName(String)` | 声明该方法的命令接口全限定名，例如 `org.springframework.data.redis.connection.RedisStringCommands`。 |
| `methodName` | `String getMethodName()` / `void setMethodName(String)` | 方法简单名，例如 `set`。 |
| `parameterNames` | `String[] getParameterNames()` / `void setParameterNames(String[])` | 源码层面的参数名；当生成器无法解析时可能为 `null`。 |
| `parameterTypes` | `String[] getParameterTypes()` / `void setParameterTypes(String[])` | 参数类型的 JVM 内部名称，例如 `{"[B", "[[[B"}`（参见 `Class.getName()`）；`buildMethodId` 消费的正是该值。 |
| `commands` | `String[] getCommands()` / `void setCommands(String[])` | 该方法所实现的 Redis 命令名，例如 `{"SET"}`。对于不发出具名命令的方法（如 `RedisCommands.execute`）该字段缺失。 |
| `write` | `boolean isWrite()` / `void setWrite(boolean)` | 当解析出的命令中至少有一个是写命令（SET、DEL 等）时为 `true`，GET 这类只读命令为 `false`。 |

上述七个属性全部参与 `equals`/`hashCode`（数组按元素比较），因此只有每个属性都一致时两个实例才相等。

#### ParameterMetadata

对单个方法参数的不可变描述。

| 方法签名 | 语义 |
|-----------|-----------|
| `public ParameterMetadata(int parameterIndex, String parameterType)` | 便捷构造器；参数名自动生成，形如 `"arg" + parameterIndex`。 |
| `public ParameterMetadata(int parameterIndex, String parameterType, @Nullable String parameterName)` | 完整构造器；`parameterName` 可以为 `null`。 |
| `public int getParameterIndex()` | 在方法签名中从零开始的参数位置。 |
| `public String getParameterType()` | 全限定/JVM 类型名，例如 `java.lang.String`，`byte[]` 则为 `[B`。 |
| `@Nullable public String getParameterName()` | 解析出的参数名、自动生成的 `argN`，或 `null`。 |

相等性只依据 `parameterIndex` 与 `parameterType`——刻意不包含参数名，因此
`new ParameterMetadata(0, "java.lang.String")` 与 `new ParameterMetadata(0, "java.lang.String", "key")` 相等
（`ParameterMetadataTest` 中对此有断言）。而 `toString` 会包含参数名。

#### Parameter

运行期的参数：Java 值加上其元数据，再加上可选的序列化形式。

| 方法签名 | 语义 |
|-----------|-----------|
| `public Parameter(Object value, ParameterMetadata metadata)` | 将运行期值与其元数据绑定。 |
| `public Object getValue()` | 调用方法时传入的 Java 对象。 |
| `public ParameterMetadata getMetadata()` | 关联的元数据（实践中不应为 `null`）。 |
| `@Nullable public byte[] getRawValue()` | 序列化后的字节，若尚未序列化则为 `null`。 |
| `public void setRawValue(@Nullable byte[] rawValue)` | 保存由 `RedisSerializer` 产生的字节。 |
| `public int getParameterIndex()` | 委托给 `metadata.getParameterIndex()`。 |
| `public String getParameterType()` | 委托给 `metadata.getParameterType()`。 |

`equals`/`hashCode`/`toString` 覆盖 `value`、`metadata` 与 `rawValue`（字节数组按 `Arrays.equals` 比较内容）。

#### MethodInfo

对反射得到的 `java.lang.reflect.Method` 与其元数据的只读组合，是 Spring 层元数据仓库缓存的值类型。

| 方法签名 | 语义 |
|-----------|-----------|
| `public MethodInfo(Method method, MethodMetadata methodMetadata, List<ParameterMetadata> parameterMetadataList)` | 构造时通过 `buildMethodId(method)` 立即计算 id。 |
| `public String getId()` | 形如 `fully.qualified.ClassName.methodName(param1Type,param2Type,...)`。 |
| `public int getIndex()` | 即 `methodMetadata.getIndex()`。 |
| `public String getName()` | 即 `method.getName()`。 |
| `public Method getMethod()` | 反射方法对象。 |
| `public MethodMetadata getMethodMetadata()` | 携带接口名、参数名/类型、命令、写标志的元数据。 |
| `public List<ParameterMetadata> getParameterMetadataList()` | 按声明顺序为每个参数提供一个条目。 |

`hashCode()` 返回方法索引，`toString()` 返回方法 id；`equals` 比较 `id`、`methodMetadata` 与
`parameterMetadataList`（因此仅元数据不同的实例互不相等——参见 `MethodInfoTest`）。

#### `RedisMetadataLoader` (SPI)

```java
public interface RedisMetadataLoader {

    @Nonnull
    RedisMetadata load();

    @Nonnull
    static RedisMetadata loadAll() {
        RedisMetadata redisMetadata = new RedisMetadata();
        RedisMetadataLoader[] redisMetadataLoaders = loadServices(RedisMetadataLoader.class);
        for (RedisMetadataLoader redisMetadataLoader : redisMetadataLoaders) {
            redisMetadata.merge(redisMetadataLoader.load());
        }
        return redisMetadata;
    }
}
```

| 成员 | 语义 |
|--------|-----------|
| `RedisMetadata load()` | 返回单个提供者贡献的元数据；永不为 `null`。 |
| `static RedisMetadata loadAll()` | 通过 `java.util.ServiceLoader` 发现所有提供者，并把它们的 `load()` 结果合并为单个 `RedisMetadata`。因此实现类必须注册在 `META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader` 中。 |

`microsphere-redis-spring` 正是为该 SPI 注册了 `SpringRedisMetadataLoader`；它解析 classpath 上所有
`META-INF/spring-data-redis-metadata.yaml`（该文件的生成方式参见
[microsphere-redis-generator](./microsphere-redis-generator.md)）。

### `io.microsphere.redis.util`

#### RedisCommandUtils

`public abstract class RedisCommandUtils implements Utils`——静态工具类（私有构造器）。

常量：

| 常量 | 值 | 含义 |
|----------|-------|---------|
| `REDIS_COMMANDS_RESOURCE` | `"META-INF/redis-commands"` | 保存全部已知命令名的资源。 |
| `REDIS_WRITE_COMMANDS_RESOURCE` | `"META-INF/redis-write-commands"` | 保存写命令子集的资源。 |
| `LOAD_REDIS_COMMANDS_FUNCTION` | `ThrowableFunction<InputStream, Set<String>>` | 包可见的加载函数：逐行读取流，跳过 `#` 注释，返回不可修改的 `TreeSet`（有序）。对外暴露是为了测试以及自定义资源格式。 |

方法：

| 方法签名 | 语义 |
|-----------|-----------|
| `static Set<String> getRedisCommands()` | 在类初始化时加载的、不可变且有序的全部命令名集合。 |
| `static Set<String> getRedisWriteCommands()` | 不可变且有序的写命令名集合。 |
| `static boolean isRedisCommand(String command)` | 若该精确（区分大小写）的命令名已知则返回 `true`。 |
| `static boolean isRedisWriteCommand(String command)` | 若该命令是写命令则返回 `true`。 |
| `static String buildMethodId(Method method)` | 由声明类、方法名与参数类型生成 id。 |
| `static String buildMethodId(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)` | 显式入参版本。 |
| `static String buildMethodId(String className, String methodName, Class<?>... parameterTypes)` | 基于类名的版本。 |
| `static String buildMethodId(String className, String methodName, String... parameterTypes)` | 最终形态：`className + "." + buildMethodSignature(...)`。 |
| `static String buildMethodSignature(Method method)` | 形如 `methodName(type1,type2)`，不含类名前缀。 |
| `static String buildMethodSignature(String methodName, Class<?>... parameterTypes)` | 基于 Class 对象的变体。 |
| `static String buildMethodSignature(String methodName, String... parameterClassNames)` | 基于类型名的变体，例如 `set([B,[B)`。 |
| `static int buildMethodIndex(Method method)` | 即 `abs(buildMethodId(method).hashCode())`。 |
| `static int buildMethodIndex(Class<?> declaringClass, String methodName, Class<?>... parameterTypes)` | 显式入参变体。 |
| `static int buildMethodIndex(String className, String methodName, Class<?>... parameterTypes)` | 基于类名的变体。 |
| `static int buildMethodIndex(String className, String methodName, String... parameterTypes)` | 规范形态；生成器用它把 `index` 写入 YAML。 |
| `static List<ParameterMetadata> buildParameterMetadataList(Method method)` | 为每个声明的参数生成元数据；返回不可修改列表。 |
| `static List<ParameterMetadata> buildParameterMetadataList(Method method, Class<?>[] parameterTypes)` | 使用调用方提供的参数类型（运行期类型可能与声明类型不同），参数名仍取自 `method`。 |
| `static String[] getParameterClassNames(Class<?>... parameterTypes)` | 逐元素调用 `Class.getName()`；入参为 `null` 或空时返回空数组。 |

参数名的获取要求类以 `-parameters` 方式编译；Microsphere 构建父 POM 已为框架自身开启该选项，这正是
`RedisCommandUtilsTest` 能够断言 `declaringClass` 这类参数名的原因。

#### RedisUtils

`public abstract class RedisUtils`——classpath 资源加载助手。

| 方法签名 | 语义 |
|-----------|-----------|
| `public static final ClassLoader CLASS_LOADER` | 资源查找使用的默认类加载器（`io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader()`）。 |
| `static <T> T loadResource(String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget)` | 使用 `CLASS_LOADER` 加载该名称的第一个资源并转换其流。 |
| `static <T> T loadResource(ClassLoader classLoader, String resourceName, ThrowableFunction<InputStream, T> inputStreamToTarget)` | 显式指定类加载器的变体。 |
| `static <T> T loadResources(String resourceName, ThrowableFunction<List<InputStream>, T> inputStreamsToTarget)` | 加载同名 **全部** 资源（`ClassLoader#getResources`），把流列表交给函数处理；处理完毕后所有流都会被关闭。 |
| `static <T> T loadResources(ClassLoader classLoader, String resourceName, ThrowableFunction<List<InputStream>, T> inputStreamsToTarget)` | 显式指定类加载器的变体；当没有任何匹配资源时返回空的转换结果（`RedisUtilsTest` 断言得到空 `Set`）。 |

`loadResources(...)` 就是 `SpringRedisMetadataLoader` 合并各个 jar 所提供的元数据 YAML 的机制。

#### RawValue

```java
public record RawValue(byte[] data) {

    public static RawValue of(byte[] bytes) {
        return new RawValue(bytes);
    }
}
```

对序列化后的 Redis 值（`byte[]`）的不可变包装，使字节数组可以作为 `Map` 的键。各处统一使用工厂方法 `of(byte[])`；
规范构造器 `new RawValue(bytes)` 与它等价。需要注意：record 自动生成的 `equals`/`hashCode` 对 `data` 分量采用的是数组
语义，也就是 **引用同一性**，因为数组不会重写 `Object.equals`。因此 `ValueHolder` 只能匹配当初存入的同一个数组实例，
而这正是序列化器包装类所依赖的契约。

#### ValueHolder

线程本地的双向缓存，在 Java 对象与其原始 `byte[]` 表示之间建立映射。

| 方法签名 | 语义 |
|-----------|-----------|
| `public ValueHolder(int initialCapacity)` | 创建独立实例，内部缓存为 `new HashMap<>(initialCapacity << 1)`（为每对映射的双向条目预留空间）。 |
| `public void set(Object value, byte[] rawValue)` | 在同一个 Map 中同时写入 `value -> RawValue` 与 `RawValue -> value`。 |
| `public Object getValue(byte[] rawValue)` | 给定字节对应的原始对象，未命中时返回 `null`。 |
| `public byte[] getRawValue(Object value)` | 给定对象对应的序列化字节，未命中时返回 `null`（缓存值不是 `RawValue` 时同样返回 `null`）。 |
| `public static ValueHolder get()` | 当前线程持有的实例，首次访问时以初始容量 4 延迟创建。 |
| `public static void clear()` | 执行 `ThreadLocal.remove()`——释放全部映射；每个命令/请求结束后必须调用，以避免在线程池场景下泄漏。 |

查找过程会通过 Microsphere 日志门面以 `trace` 级别输出。在下游，
[microsphere-redis-spring](./microsphere-redis-spring.md) 中的 `HoldingValueRedisSerializerWrapper` 会把每一次
序列化/反序列化结果缓存进 `ValueHolder`，从而让拦截层能够在不做二次反序列化的情况下由原始字节还原出原始 Java 对象。

## 代码示例

以下示例改编自本模块自身的测试类，位于 `microsphere-redis-core/src/test/java/`。

### 命令目录（`RedisCommandUtilsTest`）

测试固定了目录条目数量，因此资源被意外改动会立刻被发现：

```java
Set<String> redisCommands = getRedisCommands();
assertEquals(595, redisCommands.size());

Set<String> redisWriteCommands = getRedisWriteCommands();
assertEquals(177, redisWriteCommands.size());

for (String redisCommand : redisCommands) {
    assertTrue(isRedisCommand(redisCommand));
}
assertFalse(isRedisCommand("X"));
assertFalse(isRedisWriteCommand("X"));
```

### 方法 id、索引与参数元数据（`RedisCommandUtilsTest`）

```java
Method method = findMethod(RedisCommandUtils.class, "buildMethodId", Class.class, String.class, Class[].class);

String methodId = buildMethodId(method);
assertEquals("io.microsphere.redis.util.RedisCommandUtils.buildMethodId"
        + "(java.lang.Class,java.lang.String,[Ljava.lang.Class;)", methodId);

int methodIndex = buildMethodIndex("io.microsphere.redis.util.RedisCommandUtils", "buildMethodId",
        Class.class.getName(), String.class.getName(), Class[].class.getName());
assertEquals(abs(methodId.hashCode()), methodIndex);

List<ParameterMetadata> parameterMetadataList = buildParameterMetadataList(method);
assertEquals(3, parameterMetadataList.size());
assertEquals("declaringClass", parameterMetadataList.get(0).getParameterName());
assertEquals("methodName", parameterMetadataList.get(1).getParameterName());
assertEquals("parameterTypes", parameterMetadataList.get(2).getParameterName());
```

### 原始值往返读写（`ValueHolderTest`）

```java
ValueHolder valueHolder = new ValueHolder(4);
String value = "test-value";
byte[] rawValue = "test-raw-value".getBytes();

valueHolder.set(value, rawValue);

assertEquals(value, valueHolder.getValue(rawValue));        // 原始字节 -> 对象
assertArrayEquals(rawValue, valueHolder.getRawValue(value)); // 对象 -> 原始字节

assertNull(valueHolder.getValue("non-existent".getBytes())); // 未知字节
assertNull(valueHolder.getRawValue("non-existent"));         // 未知对象

assertNotSame(get(), valueHolder); // 线程本地实例与手工创建的不是同一个
ValueHolder holder = get();
assertSame(holder, holder = get()); // 同一线程得到同一实例
clear();                             // 清理线程本地状态
```

### `RawValue` 的相等性语义（`RawValueTest`）

```java
byte[] bytes = "test-data".getBytes();
RawValue rawValue = RawValue.of(bytes);

assertArrayEquals(bytes, rawValue.data());
assertTrue(rawValue.equals(RawValue.of(bytes)));  // 同一个数组实例
assertFalse(rawValue.equals(null));
assertFalse(rawValue.equals("test-data"));         // 类型不同
```

### 通过自定义 SPI 提供者合并元数据（`RedisMetadataLoaderTest`）

测试模块在 `src/test/resources/META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader` 中注册了自己的提供者：

```
io.microsphere.redis.metadata.TestRedisMetadataLoader
```

```java
public class TestRedisMetadataLoader implements RedisMetadataLoader {

    @Override
    public RedisMetadata load() {
        return new RedisMetadata();
    }
}

RedisMetadataLoader loader = new TestRedisMetadataLoader();
assertEquals(new RedisMetadata(), loader.load());   // 单个提供者
assertEquals(new RedisMetadata(), loadAll());      // ServiceLoader 发现 + 合并
```

## 版本与兼容性

| 模块版本 | Spring Data Redis | Spring Boot | JDK | 说明 |
|----------------|-------------------|-------------|-----|-------|
| `0.2.14`（当前版本线 0.2.x） | 3.0.0 – 3.5.x | 3.x | 17+ | `release` 分支；随包发布的命令资源基于 Redis 8.4.0。 |
| `0.2.15-SNAPSHOT` | 3.0.0 – 3.5.x | 3.x | 17+ | `release` 分支的开发尖端。 |
| `0.1.14`（历史版本线 0.1.x） | 2.1.0 – 2.7.x | 2.x | 8+ | `release-1.x` 分支；参见 [1.x 用户文档](../../1.x/microsphere-redis-core.md)。 |

`microsphere-redis-core` 的 API 在 0.2.x 版本线内保持稳定：上文列出的元数据模型与工具方法签名从 `0.2.0` 到 `0.2.14`
未发生变化。

## 相关模块

- [microsphere-redis-spring](./microsphere-redis-spring.md)——消费元数据模型以实现命令拦截与事件发布。
- [microsphere-redis-generator](./microsphere-redis-generator.md)——生成本模块模型所绑定的 YAML。
- [microsphere-redis-dependencies](./microsphere-redis-dependencies.md)——管理本模块版本的 BOM。
- [microsphere-redis-parent](./microsphere-redis-parent.md)——框架构建所继承的父 POM。
- 开发者文档：[microsphere-redis-core 源码分析](../../../developer-guide/current/microsphere-redis-core.md)
- 用户文档索引：[../README.md](../../README.md)

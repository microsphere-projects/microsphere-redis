# microsphere-redis-generator

| 项目 | 值 |
|------|-----|
| 模块 | `microsphere-redis-generator` |
| Git 分支 | `release` |
| 版本线 | 0.2.x（最新发布版本 `0.2.14`，分支顶端 `0.2.15-SNAPSHOT`） |
| Maven 坐标 | `io.github.microsphere-projects:microsphere-redis-generator` |
| 打包方式 | `jar` |
| 定位 | 构建期工具——**不是** 应用的运行时依赖 |
| 兼容范围 | JDK 17+（使用 `jdk.javadoc.doclet`）；Spring Data Redis 3.0.0 – 3.5.x 源码 |

## 概述

`microsphere-redis-generator` 用于生成 **Spring Data Redis 命令元数据** 描述文件
`META-INF/spring-data-redis-metadata.yaml`。它读取 Spring Data Redis 命令接口的 Java 源码
（`org/springframework/data/redis/connection/*Commands.java`），用自定义的 `jdk.javadoc.doclet.Doclet` 解析其 Javadoc，
并输出一份 YAML 文档，其结构与 [microsphere-redis-core](./microsphere-redis-core.md) 的模型类
`RedisMetadata` / `MethodMetadata` 完全对应。

为什么要解析 Javadoc 而不是字节码？因为 Spring Data Redis 方法所实现的 Redis 命令只存在于文档中，而不是编码在字节码里：
这些接口携带形如 `@see <a href="...">Redis Documentation: SET</a>` 的标签。只有 doclet 能够把这些标签转换为框架运行期
所需的 `commands` / `write` 属性。

生成的文件被提交进 [microsphere-redis-spring](./microsphere-redis-spring.md) 的
`src/main/resources/META-INF/spring-data-redis-metadata.yaml`，由 `SpringRedisMetadataLoader` 通过
`RedisMetadataLoader` SPI 读取。应用永远不会依赖本模块；它是维护者用来针对新的 Spring Data Redis 版本刷新元数据的
reactor 模块。

流程总览：

| 步骤 | 组件 | 作用 |
|------|-----------|--------|
| 1 | `maven-dependency-plugin:unpack`（`generate-resources`） | 将 `spring-data-redis:<version>:sources` 源码 jar 按 `org/springframework/data/redis/connection/*Commands.java` 过滤后解包到 `${sourcePath}`（即 `target/classes`）。 |
| 2 | `exec-maven-plugin`（`java -classpath <project classpath>`） | 以 `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED` 启动 `SpringDataRedisMetadataGenerator.main(sourcePath, targetYaml)`。 |
| 3 | `SpringDataRedisMetadataGenerator` | 收集源路径下所有 `*.java`，构建文档工具任务，并传入 doclet 以及 `--metadata-file=<target>`。 |
| 4 | `SpringDataRedisMetadataGenerationDoclet` | 访问顶层命令接口、解析参数类型、从 Javadoc 抽取 Redis 命令、执行校验，并用 SnakeYAML 输出 YAML。 |

## 安装

**不要** 把本模块加入应用的 `<dependencies>`。它生成的元数据已经打包在 `microsphere-redis-spring` 中，而且本模块自身的
全部依赖（`microsphere-redis-core`、`spring-data-redis`、`snakeyaml`、`reactor-core`、`microsphere-lang-model`）都声明为
`optional`。

顺便说明：本模块的版本并不由 BOM 管理——`microsphere-redis-dependencies` 中列出的是
`microsphere-redis-core`、`-spring`、`-spring-test`、`-replicator-spring`、`-spring-boot` 与 `-spring-cloud`，
其中并不包含 `microsphere-redis-generator`。

如需针对其它 Spring Data Redis 版本重新生成描述文件，可把下面的配置复制到你自己的模块构建中（这正是本模块使用的配置）：

```xml

<properties>
    <sourcePath>${project.build.directory}/classes</sourcePath>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-dependency-plugin</artifactId>
            <executions>
                <execution>
                    <id>src-dependencies</id>
                    <phase>generate-resources</phase>
                    <goals>
                        <goal>unpack</goal>
                    </goals>
                    <configuration>
                        <artifactItems>
                            <artifactItem>
                                <groupId>org.springframework.data</groupId>
                                <artifactId>spring-data-redis</artifactId>
                                <classifier>sources</classifier>
                                <type>jar</type>
                                <overWrite>true</overWrite>
                                <includes>org/springframework/data/redis/connection/*Commands.java</includes>
                                <outputDirectory>${sourcePath}</outputDirectory>
                            </artifactItem>
                        </artifactItems>
                    </configuration>
                </execution>
            </executions>
        </plugin>
        <plugin>
            <groupId>org.codehaus.mojo</groupId>
            <artifactId>exec-maven-plugin</artifactId>
            <configuration>
                <executable>java</executable>
                <arguments>
                    <argument>-classpath</argument>
                    <classpath/>
                    <argument>--add-opens=java.base/java.lang.invoke=ALL-UNNAMED</argument>
                    <argument>io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator</argument>
                    <argument>${sourcePath}</argument>
                    <argument>${project.basedir}/../microsphere-redis-spring/src/main/resources/META-INF/spring-data-redis-metadata.yaml</argument>
                </arguments>
            </configuration>
        </plugin>
    </plugins>
</build>
```

关于该配置：

- `exec` 执行没有声明 `<phase>`，因此沿用该 goal 的默认绑定；同时它既依赖 `${sourcePath}` 下解包出的源码，也依赖生成器
  自身已编译的类。所以元数据重新生成属于维护动作（产物 YAML 会被提交进仓库），而不是应用构建会执行的事情。
- 需要 `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`，是因为 doclet 在 JDK 17+ 上会反射式地驱动 `JavacTask` 内部实现。
- 最快捷的重新生成方式，其实是以同样的两个参数直接在 IDE 中运行 `SpringDataRedisMetadataGenerator.main(...)`。

## API 参考

### `io.microsphere.redis.generator.doclet.SpringDataRedisMetadataGenerationDoclet`

`public class SpringDataRedisMetadataGenerationDoclet implements Doclet`

Doclet 生命周期：

| 方法签名 | 语义 |
|-----------|-----------|
| `public void init(Locale locale, Reporter reporter)` | 把 Javadoc 的 `Reporter` 包装成 `Logger`（`ReporterLoggerAdapter`），从 `microsphere-redis-core` 载入 Redis 命令目录（`getRedisCommands()` / `getRedisWriteCommands()`），初始化空的 `supportedCommands` / `supportedWriteCommands` 收集器，获取系统 `JavaCompiler`，并创建容量 256 的类缓存。 |
| `public String getName()` | 返回类的简单名。 |
| `public Set<? extends Option> getSupportedOptions()` | 只声明一个选项：`--metadata-file`（见下表）。 |
| `public SourceVersion getSupportedSourceVersion()` | 返回 `SourceVersion.latest()`——doclet 跟随当前运行的 JDK。 |
| `public boolean run(DocletEnvironment environment)` | 生成元数据并始终返回 `true`；失败以校验器抛出的 `IllegalStateException` 形式暴露。 |

选项：

| 选项名 | 参数个数 | 类型 | 描述 |
|-------------|----------------|------|-------------|
| `--metadata-file` | `1` | `STANDARD` | “The metadata file path to generate Spring Data Redis Metadata”（生成 Spring Data Redis 元数据的目标文件路径）。其值被保存为 `File` 并作为 YAML 输出目标；同时支持 `--metadata-file <path>` 与 `--metadata-file=<path>` 两种写法。 |

生成的 YAML 键（doclet 的公开常量）：

| 常量 | 字段 | YAML 键 | 取值 |
|----------|-------|----------|-------|
| `VERSION_KEY` | — | `version` | `RedisCommands.class.getPackage().getImplementationVersion()`，即被解析的 Spring Data Redis 版本。 |
| `METHODS_KEY` | — | `methods` | 方法条目序列。 |
| `INDEX_KEY` | `index` | `index` | `RedisCommandUtils.buildMethodIndex(interfaceName, methodName, parameterTypes)`——方法 id 哈希的绝对值；会被校验为唯一。 |
| `INTERFACE_NAME_KEY` | `interfaceName` | `interfaceName` | 命令接口全限定名。 |
| `METHOD_NAME_KEY` | `methodName` | `methodName` | 方法简单名。 |
| `METHOD_PARAMETER_NAMES_KEY` | — | `parameterNames` | 取自 `VariableElement#getSimpleName()` 的源码级参数名。 |
| `METHOD_PARAMETER_TYPES_KEY` | — | `parameterTypes` | `Class#getName()` 的取值（`[B`、`[[B`、`java.nio.ByteBuffer` 等）。 |
| `COMMANDS_KEY` | — | `commands` | 从 Javadoc 抽取的 Redis 命令名；当方法未声明任何命令时该键被省略。 |
| `WRITE_KEY` | — | `write` | 当最后解析出的命令属于 `META-INF/redis-write-commands` 时为 `true`；与 `commands` 一同被省略。 |

扫描与抽取规则（来自 `generateSpringDataRedisMetadata` 及其内部访问器）：

- 只访问 **顶层**（`NestingKind.TOP_LEVEL`）且至少声明了一个方法的 **接口**；嵌套类型与无方法的标记接口会被跳过。
- 每个方法产出一条包含 `index`、`interfaceName`、`methodName`、`parameterNames`、`parameterTypes` 的条目；若方法带有
  文档注释，则由 `RedisCommandMethodDocumentVisitor` 扫描其中的 `@see` 标签。
- 命令抽取：`@see` 标签文本中从 `Redis Documentation:` 到下一个 `<` 之间的内容，会用 doclet 的 locale 转为大写并成为
  `commands` 取值；`write` 标志由 core 的写命令目录决定。同一命令不会重复添加。
- 参数类型解析：先规范化源码中的类型名（去掉泛型、保留数组后缀，例如 `List<String>[]` → `List[]`），再通过文档工具的
  class path 加载。若无法加载，则用 `JavacTask` 重新解析声明该方法的 `.java` 文件，取该方法类型参数的上界
  （`deduceMethodParameterClass`），未限定的上界默认加上 `java.lang.` 前缀（例如 `T extends Number` → `java.lang.Number`）。

校验（每处失败都会抛出 `IllegalStateException`）：

| 方法 | 规则 | 失败消息模板 |
|--------|------|-------------------------|
| `void validateCommand(Map<String, Object>, String)` | 每个解析出的命令都必须存在于 `META-INF/redis-commands`。 | `The Redis command can't be resolved by the JavaDoc of {} , actual : {}` |
| `void validateIndex(List<Map<String, Object>>)` | 所有条目之间的 `index` 必须唯一。 | `The index of Redis Command Method[...] : {} is duplicated` |
| `void validateMethod(List<Map<String, Object>>)` | 每条条目都必须对应真实方法：`interfaceName` + `methodName` + `parameterTypes` 会被加载并反射查找。 | `Can't find the method[name : '{}' , parameter types : {}] in interface[name : '{}']` |

输出：`writeSpringDataRedisMetadata` 使用 SnakeYAML（`DumperOptions` 设置 `BLOCK` 流风格与 `prettyFlow`）导出该 Map，
随后再用 `yaml.loadAs(reader, RedisMetadata.class)` 回读文件做一致性校验，并把结果记入日志。

### `io.microsphere.redis.generator.doclet.StandardOption`

`public abstract class StandardOption implements Doclet.Option`——把 `Kind.STANDARD` 固定下来的基类。

| 方法签名 | 语义 |
|-----------|-----------|
| `public StandardOption(String name, int argumentCount, String parameters, String description)` | 保存选项元数据；子类负责实现 `process(String, List<String>)`。 |
| `public int getArgumentCount()` | 该选项消费的参数个数。 |
| `public String getDescription()` | `javadoc --help` 展示的可读描述。 |
| `public final Kind getKind()` | 恒为 `STANDARD`。 |
| `public List<String> getNames()` | 只含选项名的单元素列表。 |
| `public String getParameters()` | 帮助输出中的参数提示（可以为 `null`）。 |

### `io.microsphere.redis.generator.doclet.logging.ReporterLoggerAdapter`

`public class ReporterLoggerAdapter extends AbstractLogger`——把框架的 `Logger` API 适配到 Javadoc 的 `Reporter`，
各级别与 `javax.tools.Diagnostic.Kind` 的映射如下：

| Logger 方法 | `Diagnostic.Kind` |
|---------------|-------------------|
| `trace` / `debug` | `OTHER` |
| `info` | `NOTE` |
| `warn` | `WARNING` |
| `error` | `ERROR` |

所有级别恒为开启；当 `Throwable` 非 `null` 时，会以额外一条消息的形式打印堆栈。

### `io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator`

`public class SpringDataRedisMetadataGenerator`——构建所使用的 `main` 入口。

| 方法签名 | 语义 |
|-----------|-----------|
| `public static void main(String[] args) throws Exception` | `args[0]` = 待扫描的源码路径，`args[1]` = 目标 YAML 路径。参数少于两个时只记录 `Usage : SpringDataRedisMetadataGenerator <source-path> <target-file-path>` 并返回。 |
| `private static Set<File> resolveClassPaths()` | 把 `io.microsphere.util.ClassPathUtils.getClassPaths()` 转成 `File` 集合；它成为文档工具的 `CLASS_PATH` 位置，从而使 doclet 能够加载它所记录的这些接口本身。 |

`main` 还会：在静态初始化块中执行 `Locale.setDefault(ENGLISH)`（保证命令名大写结果一致），遍历源码目录树收集 `*.java`
文件（目录/文件访问失败仅记录日志并跳过），通过 `ToolProvider.getSystemDocumentationTool()` 创建
`DocumentationTask`（UTF-8、`ENGLISH` locale、标准文件管理器），传入 `--metadata-file=<target>`，并记录
`The JavaDoc generation result : {}`。

## 生成的 YAML 格式

`release` 分支上提交的 `microsphere-redis-spring/src/main/resources/META-INF/spring-data-redis-metadata.yaml`
（Spring Data Redis `3.5.10`，共 856 条方法条目）节选：

```yaml
version: 3.5.10
methods:
- index: 498782566
  interfaceName: org.springframework.data.redis.connection.RedisHyperLogLogCommands
  methodName: pfAdd
  parameterNames:
  - key
  - values
  parameterTypes:
  - '[B'
  - '[[B'
  commands:
  - PFADD
  write: true
- index: 1526869519
  interfaceName: org.springframework.data.redis.connection.RedisHyperLogLogCommands
  methodName: pfMerge
  parameterNames:
  - destinationKey
  - sourceKeys
  parameterTypes:
  - '[B'
  - '[[B'
  commands:
  - PFMERGE
  write: true
- index: 1140409136
  interfaceName: org.springframework.data.redis.connection.RedisCommands
  methodName: execute
  parameterNames:
  - command
  - args
  parameterTypes:
  - java.lang.String
  - '[[B'
- index: 3143195
  interfaceName: org.springframework.data.redis.connection.ReactiveStringCommands
  methodName: set
  parameterNames:
  - key
  - value
  parameterTypes:
  - java.nio.ByteBuffer
  - java.nio.ByteBuffer
  commands:
  - SET
  write: true
```

第四条条目（`RedisCommands.execute`）展示了不发出具名 Redis 命令的方法：既没有输出 `commands` 也没有输出 `write`，
绑定到 `RedisMetadata`/`MethodMetadata` 后它们分别为 `null`/`false`。

## 元数据如何被消费

```java
// microsphere-redis-spring: META-INF/services/io.microsphere.redis.metadata.RedisMetadataLoader
// -> io.microsphere.redis.spring.metadata.SpringRedisMetadataLoader
public class SpringRedisMetadataLoader implements RedisMetadataLoader {

    public static final String SPRING_REDIS_METADATA_RESOURCE = "META-INF/spring-data-redis-metadata.yaml";

    @Override
    public RedisMetadata load() {
        ClassLoader classLoader = getClassLoader(getClass());
        return loadResources(classLoader, SPRING_REDIS_METADATA_RESOURCE, inputStreams -> {
            RedisMetadata redisMetadata = new RedisMetadata();
            Yaml yaml = new Yaml();
            for (InputStream inputStream : inputStreams) {
                redisMetadata.merge(yaml.loadAs(inputStream, RedisMetadata.class));
            }
            return redisMetadata;
        });
    }
}
```

同模块下的 `SpringRedisMetadataRepository` 会在类初始化阶段调用一次 `RedisMetadataLoader.loadAll()`，并以索引、
`Method` 对象、方法 id 三种键缓存 `MethodInfo` 实例。

## 代码示例

生成器模块自身没有单元测试；它的产物是从消费侧进行校验的。

### 元数据覆盖全部命令方法（`SpringRedisMetadataLoaderTest`）

```java
SpringRedisMetadataLoader loader = new SpringRedisMetadataLoader();
RedisMetadata redisMetadata = loader.load();
assertTrue(redisMetadata.getMethods().size() > 1);

Set<String> configuredCommandMethodIds = new TreeSet<>();
for (MethodMetadata method : redisMetadata.getMethods()) {
    configuredCommandMethodIds.add(buildMethodId(method.getInterfaceName(), method.getMethodName(),
            method.getParameterTypes()));
}

Set<String> commandMethodIds = new TreeSet<>();
for (Method method : RedisCommands.class.getMethods()) {
    commandMethodIds.add(buildMethodId(method));
}
commandMethodIds.removeAll(configuredCommandMethodIds);
assertTrue(commandMethodIds.isEmpty()); // 不存在未被记录的 Spring Data Redis 命令方法
```

### 生成器入口（改编自 `SpringDataRedisMetadataGenerator.main`）

```java
// 重新生成属于维护动作：以两个参数调用 main()（在 IDE 中，或使用上文给出的 exec-maven-plugin 配置）。
public static void main(String[] args) {
    // args[0] = 解包 spring-data-redis 源码到其中的 target/classes
    // args[1] = META-INF/spring-data-redis-metadata.yaml 的绝对路径
    Path sourcePath = Paths.get(args[0]);
    String targetFilePath = args[1];
    Set<String> options = Set.of("--metadata-file=" + targetFilePath);
    DocumentationTask task = documentationTool.getTask(null, standardFileManager, null,
            SpringDataRedisMetadataGenerationDoclet.class, options, javaFileObjects);
    logger.info("The JavaDoc generation result : {}", task.call());
}
```

### doclet 内部的命令名校验（来自 `validateCommand`）

```java
void validateCommand(Map<String, Object> redisMethodMetadataMap, String resolvedCommand) {
    if (!redisCommands.contains(resolvedCommand)) {
        String interfaceName = (String) redisMethodMetadataMap.get(INTERFACE_NAME_KEY);
        String methodName = (String) redisMethodMetadataMap.get(METHOD_NAME_KEY);
        String[] parameterTypes = (String[]) redisMethodMetadataMap.get(METHOD_PARAMETER_TYPES_KEY);
        throwsException("The Redis command can't be resolved by the JavaDoc of {} , actual : {}",
                buildMethodId(interfaceName, methodName, parameterTypes), resolvedCommand);
    }
}
```

### 历史遗留夹具

`microsphere-redis-generator/src/test/resources/spring-data-redis-metadata.yaml` 是一份更早期、结构不同的夹具
（`org.springframework.data.redis.connection.RedisKeyCommands: methods: [hash/signature/command]`）。本模块没有任何测试
类引用它；当前 doclet 输出的是上文所示的扁平 `version` / `methods` 结构。

## 版本与兼容性

| 模块版本 | Spring Data Redis | JDK | 说明 |
|----------------|-------------------|-----|-------|
| `0.2.14`（0.2.x 版本线） | 3.0.0 – 3.5.x | 17+ | `release` 分支。JDK 17+ 是强制要求：本模块针对 `jdk.javadoc.doclet` 与 `com.sun.source.*` 编译。 |
| `0.2.15-SNAPSHOT` | 3.0.0 – 3.5.x | 17+ | `release` 分支的开发尖端。 |
| `0.1.14`（历史 0.1.x 版本线） | 2.1.0 – 2.7.x | 8+（仅 JDK 9+ 激活 doclet） | `release-1.x` 分支；在该项目线上生成器由 JDK 9+ profile 激活。参见 [1.x 用户文档](../../1.x/microsphere-redis-generator.md)。 |

随包提交的描述文件（`version: 3.5.10`）是针对 Spring Data Redis 3.5.10 生成的，也就是默认 `spring-cloud-2025` 构建
profile 引入的版本。`microsphere-redis-core` 所携带的命令名目录反映的是 Redis 8.4.0。

## 相关模块

- [microsphere-redis-core](./microsphere-redis-core.md)——doclet 写入所依据的模型与命令目录。
- [microsphere-redis-spring](./microsphere-redis-spring.md)——打包并加载生成的描述文件。
- [microsphere-redis-parent](./microsphere-redis-parent.md)——本模块的父 POM。
- [microsphere-redis-dependencies](./microsphere-redis-dependencies.md)——项目 BOM（不管理本模块）。
- 开发者文档：[microsphere-redis-generator 源码分析](../../../developer-guide/current/microsphere-redis-generator.md)
- 用户文档索引：[../README.md](../../README.md)

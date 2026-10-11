# microsphere-redis-generator

| Item | Value |
|------|-------|
| Module | `microsphere-redis-generator` |
| Git branch | `release` |
| Version line | 0.2.x (latest release `0.2.14`, branch tip `0.2.15-SNAPSHOT`) |
| Maven coordinates | `io.github.microsphere-projects:microsphere-redis-generator` |
| Packaging | `jar` |
| Role | Build-time tooling — **not** an application runtime dependency |
| Compatibility | JDK 17+ (uses `jdk.javadoc.doclet`); Spring Data Redis 3.0.0 – 3.5.x sources |

## Overview

`microsphere-redis-generator` produces the **Spring Data Redis command metadata** descriptor
`META-INF/spring-data-redis-metadata.yaml`. It reads the Java source of Spring Data Redis command interfaces
(`org/springframework/data/redis/connection/*Commands.java`), parses their Javadoc with a custom `jdk.javadoc.doclet.Doclet`,
and writes a YAML document whose shape matches the [microsphere-redis-core](./microsphere-redis-core.md) model classes
`RedisMetadata` / `MethodMetadata`.

Why Javadoc instead of bytecode? The Redis command implemented by a Spring Data Redis method is documented, not encoded:
the interfaces carry tags such as `@see <a href="...">Redis Documentation: SET</a>`. Only the doclet can turn those tags
into the `commands` / `write` properties the framework needs at runtime.

The generated file is committed into [microsphere-redis-spring](./microsphere-redis-spring.md) at
`src/main/resources/META-INF/spring-data-redis-metadata.yaml`, where `SpringRedisMetadataLoader` reads it through the
`RedisMetadataLoader` SPI. Applications never depend on this module; it is a reactor module used by the maintainers to
refresh the metadata for a new Spring Data Redis version.

Pipeline overview:

| Step | Component | Effect |
|------|-----------|--------|
| 1 | `maven-dependency-plugin:unpack` (`generate-resources`) | Extracts `spring-data-redis:<version>:sources` jar, filtered to `org/springframework/data/redis/connection/*Commands.java`, into `${sourcePath}` (= `target/classes`). |
| 2 | `exec-maven-plugin` (`java -classpath <project classpath>`) | Launches `SpringDataRedisMetadataGenerator.main(sourcePath, targetYaml)` with `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED`. |
| 3 | `SpringDataRedisMetadataGenerator` | Collects every `*.java` under the source path, builds the documentation tool task, and passes the doclet plus `--metadata-file=<target>`. |
| 4 | `SpringDataRedisMetadataGenerationDoclet` | Visits top-level command interfaces, resolves parameter types, harvests Redis commands from Javadoc, validates, and dumps YAML with SnakeYAML. |

## Installation

Do **not** add this module to `<dependencies>` of an application. The metadata it produces is already packaged in
`microsphere-redis-spring`, and the module itself declares every dependency (`microsphere-redis-core`,
`spring-data-redis`, `snakeyaml`, `reactor-core`, `microsphere-lang-model`) as `optional`.

For completeness, the version of the module is *not* managed by the BOM: `microsphere-redis-dependencies` lists
`microsphere-redis-core`, `-spring`, `-spring-test`, `-replicator-spring`, `-spring-boot` and `-spring-cloud`, but not
`microsphere-redis-generator`.

To regenerate the descriptor for a different Spring Data Redis version, copy the wiring below into the module build (this
is the configuration the module itself uses):

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

Notes on this wiring:

- The `exec` execution declares no `<phase>`, so it keeps the goal's default binding, and it needs both the unpacked
  sources under `${sourcePath}` and the generator's own compiled classes. Metadata regeneration is therefore a
  maintenance step (the resulting YAML is committed), not something an application build performs.
- `--add-opens=java.base/java.lang.invoke=ALL-UNNAMED` is required because the doclet reflectively drives `JavacTask`
  internals on JDK 17+.
- Running `SpringDataRedisMetadataGenerator.main(...)` directly from an IDE, with the same two arguments, is the quickest
  way to regenerate the file.

## API Reference

### `io.microsphere.redis.generator.doclet.SpringDataRedisMetadataGenerationDoclet`

`public class SpringDataRedisMetadataGenerationDoclet implements Doclet`

Doclet lifecycle:

| Signature | Semantics |
|-----------|-----------|
| `public void init(Locale locale, Reporter reporter)` | Wraps the Javadoc `Reporter` into a `Logger` (`ReporterLoggerAdapter`), loads the Redis command catalogues from `microsphere-redis-core` (`getRedisCommands()` / `getRedisWriteCommands()`), prepares empty `supportedCommands` / `supportedWriteCommands` accumulators, obtains the system `JavaCompiler`, and creates a 256-entry class cache. |
| `public String getName()` | Returns the simple class name. |
| `public Set<? extends Option> getSupportedOptions()` | Declares exactly one option: `--metadata-file` (see below). |
| `public SourceVersion getSupportedSourceVersion()` | `SourceVersion.latest()` — the doclet follows the running JDK. |
| `public boolean run(DocletEnvironment environment)` | Generates the metadata and always returns `true`; failures surface as `IllegalStateException` from the validators. |

Option:

| Option name | Argument count | Kind | Description |
|-------------|----------------|------|-------------|
| `--metadata-file` | `1` | `STANDARD` | "The metadata file path to generate Spring Data Redis Metadata". The value is stored as a `File` and used as the YAML output target; both `--metadata-file <path>` and `--metadata-file=<path>` forms are accepted. |

YAML keys produced (public constants of the doclet):

| Constant | Field | YAML key | Value |
|----------|-------|----------|-------|
| `VERSION_KEY` | — | `version` | `RedisCommands.class.getPackage().getImplementationVersion()`, i.e. the Spring Data Redis version being documented. |
| `METHODS_KEY` | — | `methods` | Sequence of method entries. |
| `INDEX_KEY` | `index` | `index` | `RedisCommandUtils.buildMethodIndex(interfaceName, methodName, parameterTypes)` — absolute hash of the method id; validated to be unique. |
| `INTERFACE_NAME_KEY` | `interfaceName` | `interfaceName` | Fully-qualified command interface name. |
| `METHOD_NAME_KEY` | `methodName` | `methodName` | Simple method name. |
| `METHOD_PARAMETER_NAMES_KEY` | — | `parameterNames` | Source-level parameter names taken from `VariableElement#getSimpleName()`. |
| `METHOD_PARAMETER_TYPES_KEY` | — | `parameterTypes` | `Class#getName()` values (`[B`, `[[B`, `java.nio.ByteBuffer`, …). |
| `COMMANDS_KEY` | — | `commands` | Redis command names harvested from Javadoc; omitted when a method documents no command. |
| `WRITE_KEY` | — | `write` | `true` when the last resolved command belongs to `META-INF/redis-write-commands`; omitted together with `commands`. |

Scanning and extraction rules (from `generateSpringDataRedisMetadata` and its inner visitors):

- Only **top-level** (`NestingKind.TOP_LEVEL`) **interfaces** that declare at least one method are visited; nested types
  and method-free marker interfaces are skipped.
- Each method yields one entry containing `index`, `interfaceName`, `methodName`, `parameterNames`, `parameterTypes`;
  if the method has a doc comment, `RedisCommandMethodDocumentVisitor` scans its `@see` tags.
- Command extraction: a `@see` tag whose text contains `Redis Documentation:` up to the next `<` is upper-cased with the
  doclet locale and becomes a `commands` value; the `write` flag is derived from the core write-command catalogue.
  Duplicates are not added twice.
- Parameter type resolution: the source type name is normalized (generics stripped, array suffix preserved, e.g.
  `List<String>[]` → `List[]`) and loaded through the documentation tool's class path. If it cannot be loaded, the
  declaring `.java` file is re-parsed with `JavacTask` and the method's type-parameter bound is used (`deduceMethodParameterClass`),
  defaulting unqualified bounds to `java.lang.` (e.g. `T extends Number` → `java.lang.Number`).

Validation (each failure throws `IllegalStateException`):

| Method | Rule | Failure message pattern |
|--------|------|-------------------------|
| `void validateCommand(Map<String, Object>, String)` | Every resolved command must exist in `META-INF/redis-commands`. | `The Redis command can't be resolved by the JavaDoc of {} , actual : {}` |
| `void validateIndex(List<Map<String, Object>>)` | `index` must be unique across all entries. | `The index of Redis Command Method[...] : {} is duplicated` |
| `void validateMethod(List<Map<String, Object>>)` | Each entry must resolve to a real method: `interfaceName` + `methodName` + `parameterTypes` are loaded and looked up reflectively. | `Can't find the method[name : '{}' , parameter types : {}] in interface[name : '{}']` |

Output: `writeSpringDataRedisMetadata` dumps the map with SnakeYAML (`DumperOptions` with `BLOCK` flow style and
`prettyFlow`), then re-reads the file with `yaml.loadAs(reader, RedisMetadata.class)` as a consistency check and logs the
result.

### `io.microsphere.redis.generator.doclet.StandardOption`

`public abstract class StandardOption implements Doclet.Option` — base class that fixes `Kind.STANDARD`.

| Signature | Semantics |
|-----------|-----------|
| `public StandardOption(String name, int argumentCount, String parameters, String description)` | Stores the option metadata; subclasses implement `process(String, List<String>)`. |
| `public int getArgumentCount()` | Number of arguments the option consumes. |
| `public String getDescription()` | Human-readable description shown by `javadoc --help`. |
| `public final Kind getKind()` | Always `STANDARD`. |
| `public List<String> getNames()` | Single-element list with the option name. |
| `public String getParameters()` | Parameter hint for the help output (may be `null`). |

### `io.microsphere.redis.generator.doclet.logging.ReporterLoggerAdapter`

`public class ReporterLoggerAdapter extends AbstractLogger` — adapts the framework `Logger` API onto the Javadoc
`Reporter`, mapping each level to a `javax.tools.Diagnostic.Kind`:

| Logger method | `Diagnostic.Kind` |
|---------------|-------------------|
| `trace` / `debug` | `OTHER` |
| `info` | `NOTE` |
| `warn` | `WARNING` |
| `error` | `ERROR` |

All levels are always enabled; a non-`null` `Throwable` is printed as an additional stack-trace message.

### `io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator`

`public class SpringDataRedisMetadataGenerator` — the `main` entry point used by the build.

| Signature | Semantics |
|-----------|-----------|
| `public static void main(String[] args) throws Exception` | `args[0]` = source path to scan, `args[1]` = target YAML path. With fewer than two arguments it only logs `Usage : SpringDataRedisMetadataGenerator <source-path> <target-file-path>` and returns. |
| `private static Set<File> resolveClassPaths()` | Turns `io.microsphere.util.ClassPathUtils.getClassPaths()` into `File`s; this becomes the `CLASS_PATH` location of the documentation tool, so the doclet can load the very interfaces it documents. |

`main` additionally: forces `Locale.setDefault(ENGLISH)` in a static initializer (so command names upper-case
consistently), walks the source tree collecting `*.java` files (directory/file visit failures are logged and skipped),
creates the `DocumentationTask` with `ToolProvider.getSystemDocumentationTool()` (UTF-8, `ENGLISH` locale, standard file
manager), passes `--metadata-file=<target>`, and logs `The JavaDoc generation result : {}`.

## Generated YAML Format

Excerpt of `microsphere-redis-spring/src/main/resources/META-INF/spring-data-redis-metadata.yaml` as committed on the
`release` branch (Spring Data Redis `3.5.10`, 856 method entries):

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

The fourth entry (`RedisCommands.execute`) illustrates a method that issues no named Redis command: neither `commands`
nor `write` is emitted, and `RedisMetadata`/`MethodMetadata` binding leaves them `null`/`false`.

## How the Metadata Is Consumed

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

`SpringRedisMetadataRepository` (same module) then calls `RedisMetadataLoader.loadAll()` once at class-initialization
time and caches `MethodInfo` instances keyed by index, `Method` object, and method id.

## Code Examples

The generator module itself contains no unit tests; its output is verified from the consuming side.

### Metadata covers every command method (`SpringRedisMetadataLoaderTest`)

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
assertTrue(commandMethodIds.isEmpty()); // no Spring Data Redis command method left undocumented
```

### Generator entry point (adapted from `SpringDataRedisMetadataGenerator.main`)

```java
// Regeneration is a maintenance step: invoke main() with the two arguments
// (from an IDE, or through the exec-maven-plugin configuration shown above).
public static void main(String[] args) {
    // args[0] = target/classes where the spring-data-redis sources were unpacked
    // args[1] = absolute path of META-INF/spring-data-redis-metadata.yaml
    Path sourcePath = Paths.get(args[0]);
    String targetFilePath = args[1];
    Set<String> options = Set.of("--metadata-file=" + targetFilePath);
    DocumentationTask task = documentationTool.getTask(null, standardFileManager, null,
            SpringDataRedisMetadataGenerationDoclet.class, options, javaFileObjects);
    logger.info("The JavaDoc generation result : {}", task.call());
}
```

### Command name check inside the doclet (from `validateCommand`)

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

### Legacy fixture

`microsphere-redis-generator/src/test/resources/spring-data-redis-metadata.yaml` is an older, different-shaped fixture
(`org.springframework.data.redis.connection.RedisKeyCommands: methods: [hash/signature/command]`). No test class in this
module references it; the current doclet emits the flat `version` / `methods` schema shown above.

## Version & Compatibility

| Module Version | Spring Data Redis | JDK | Notes |
|----------------|-------------------|-----|-------|
| `0.2.14` (0.2.x line) | 3.0.0 – 3.5.x | 17+ | `release` branch. JDK 17+ is mandatory: the module compiles against `jdk.javadoc.doclet` and `com.sun.source.*`. |
| `0.2.15-SNAPSHOT` | 3.0.0 – 3.5.x | 17+ | Development tip of `release`. |
| `0.1.14` (legacy 0.1.x line) | 2.1.0 – 2.7.x | 8+ (doclet active on 9+ only) | `release-1.x` branch; the generator is enabled by a JDK 9+ profile there. See the [1.x user guide](../1.x/microsphere-redis-generator.md). |

The committed descriptor (`version: 3.5.10`) was generated against Spring Data Redis 3.5.10, which is the version pulled
in by the default `spring-cloud-2025` build profile. Command name catalogues shipped by `microsphere-redis-core` reflect
Redis 8.4.0.

## Related Modules

- [microsphere-redis-core](./microsphere-redis-core.md) — the model and command catalogues the doclet writes against.
- [microsphere-redis-spring](./microsphere-redis-spring.md) — packages and loads the generated descriptor.
- [microsphere-redis-parent](./microsphere-redis-parent.md) — parent POM of this module.
- [microsphere-redis-dependencies](./microsphere-redis-dependencies.md) — project BOM (does not manage this module).
- Developer Guide: [microsphere-redis-generator internals](../../developer-guide/current/microsphere-redis-generator.md)
- User Guide index: [../README.md](../README.md)

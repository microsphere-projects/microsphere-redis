/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.redis.generator.doclet;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreeScanner;
import com.sun.source.util.DocTrees;
import io.microsphere.logging.Logger;
import io.microsphere.redis.generator.doclet.logging.ReporterLoggerAdapter;
import io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator;
import io.microsphere.redis.metadata.RedisMetadata;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import org.springframework.data.redis.connection.RedisCommands;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner9;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.sun.source.doctree.DocTree.Kind.SEE;
import static io.microsphere.annotation.processor.util.ElementUtils.isInterface;
import static io.microsphere.annotation.processor.util.MethodUtils.findDeclaredMethods;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodName;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodParameterTypeNames;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.redis.util.RedisUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisUtils.getRedisWriteCommands;
import static io.microsphere.util.StringUtils.substringBetween;
import static javax.lang.model.SourceVersion.latest;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

/**
 * The {@link Doclet} class for Spring Data Redis Metadata Generation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Doclet
 * @see SpringDataRedisMetadataGenerator
 * @since 1.0.0
 */
public class SpringDataRedisMetadataGenerationDoclet implements Doclet {

    public static final String METADATA_FILE_OPTION_NAME = "--metadata-file";

    /**
     * The metadata key of Spring Data Redis Version
     */
    public static final String VERSION_KEY = "version";

    /**
     * The metadata key of Spring Data Redis Command Methods
     */
    public static final String METHODS_KEY = "methods";

    /**
     * The metadata key of Spring Data Redis Command Interface Name
     */
    public static final String INTERFACE_NAME_KEY = "interfaceName";

    /**
     * The metadata key of Spring Data Redis Command Method Index
     */
    public static final String INDEX_KEY = "index";

    public static final String METHOD_NAME_KEY = "methodName";

    public static final String METHOD_PARAMETER_TYPES_KEY = "parameterTypes";

    public static final String SIGNATURE_KEY = "signature";

    public static final String COMMAND_KEY = "command";

    public static final String WRITE_KEY = "write";

    private Locale locale;

    private Logger logger;

    private Set<String> redisCommands;

    private Set<String> redisWriteCommands;

    private Set<String> supportedCommands;

    private Set<String> supportedWriteCommands;

    private File metadataFile;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.logger = new ReporterLoggerAdapter(getName(), reporter);
        this.redisCommands = getRedisCommands();
        this.redisWriteCommands = getRedisWriteCommands();
        this.supportedCommands = new HashSet<>();
        this.supportedWriteCommands = new HashSet<>();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return ofSet(
                new StandardOption(METADATA_FILE_OPTION_NAME, 1,
                        null,
                        "The metadata file path to generate Spring Data Redis Metadata") {
                    @Override
                    public boolean process(String option, List<String> arguments) {
                        String metadataFilePath = arguments.get(0);
                        metadataFile = new File(metadataFilePath);
                        return true;
                    }
                }
        );
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        generateSpringDataRedisMetadata(environment);
        return true;
    }

    protected void generateSpringDataRedisMetadata(DocletEnvironment environment) {
        DocTrees docTrees = environment.getDocTrees();

        Map<String, Object> springDataRedisMetadata = newLinkedHashMap();
        springDataRedisMetadata.put(VERSION_KEY, getSpringDataRedisVersion());

        List<Map<String, Object>> redisMethodMetadataMapList = new LinkedList<>();
        springDataRedisMetadata.put(METHODS_KEY, redisMethodMetadataMapList);

        RedisCommandMethodVisitor redisCommandMethodVisitor = new RedisCommandMethodVisitor(redisMethodMetadataMapList);

        Set<? extends Element> specifiedElements = environment.getSpecifiedElements();
        for (Element element : specifiedElements) {
            ElementKind kind = element.getKind();
            if (isInterface(kind)) {
                TypeMirror type = element.asType();
                if (!isTopClass(element)) {
                    continue;
                }
                if (!hasMethods(type)) {
                    continue;
                }
                element.accept(redisCommandMethodVisitor, docTrees);
            }
        }

        this.logger.info("All Redis commands(size : {} , write : {}), the supported Spring Data Redis commands(size : {} , write : {})",
                this.redisCommands.size(), this.redisWriteCommands.size(), this.supportedCommands.size(), this.supportedWriteCommands.size());
        this.logger.info("All Redis commands : {}", this.redisCommands);
        this.logger.info("All Redis write commands : {}", this.redisWriteCommands);
        this.logger.info("The supported Spring Data Redis commands : {}", this.supportedCommands);
        this.logger.info("The supported Spring Data Redis write commands : {}", this.supportedWriteCommands);

        writeSpringDataRedisMetadata(springDataRedisMetadata);
    }

    private String getSpringDataRedisVersion() {
        return RedisCommands.class.getPackage().getImplementationVersion();
    }

    private boolean isTopClass(Element element) {
        TypeElement typeElement = (TypeElement) element;
        NestingKind nestingKind = typeElement.getNestingKind();
        return TOP_LEVEL == nestingKind;
    }

    private boolean hasMethods(TypeMirror type) {
        return !findDeclaredMethods(type).isEmpty();
    }

    private void writeSpringDataRedisMetadata(Map<String, Object> springDataRedisMetadata) {
        Yaml yaml = createYaml();
        try (FileWriter writer = new FileWriter(metadataFile);
             FileReader reader = new FileReader(metadataFile)) {
            yaml.dump(springDataRedisMetadata, writer);
            // Check the consistency
            RedisMetadata redisMetadata = yaml.loadAs(reader, RedisMetadata.class);
            this.logger.info("Spring Data Redis Metadata : {}", redisMetadata);
        } catch (IOException e) {
            this.logger.error("Can't write Spring Data Redis Metadata to file : {}", metadataFile, e);
        }
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(BLOCK);
        options.setPrettyFlow(true);
        return new Yaml(options);
    }

    class RedisCommandMethodVisitor extends ElementScanner9<Void, DocTrees> {

        private final List<Map<String, Object>> redisMethodMetadataMapList;

        RedisCommandMethodVisitor(List<Map<String, Object>> redisMethodMetadataMapList) {
            this.redisMethodMetadataMapList = redisMethodMetadataMapList;
        }

        @Override
        public Void scan(Element e, DocTrees docTrees) {
            ElementKind kind = e.getKind();
            if (METHOD.equals(kind)) {
                ExecutableElement methodElement = (ExecutableElement) e;
                DocCommentTree dcTree = docTrees.getDocCommentTree(e);
                String interfaceName = methodElement.getEnclosingElement().toString();
                String signature = methodElement.toString();
                Integer index = buildIndex(interfaceName, signature);
                String methodName = getMethodName(methodElement);
                String[] methodParameterTypeNames = getMethodParameterTypeNames(methodElement);

                Map<String, Object> redisMethodMetadataMap = newLinkedHashMap();
                redisMethodMetadataMap.put(INDEX_KEY, index);
                redisMethodMetadataMap.put(INTERFACE_NAME_KEY, interfaceName);
                redisMethodMetadataMap.put(METHOD_NAME_KEY, methodName);
                redisMethodMetadataMap.put(METHOD_PARAMETER_TYPES_KEY, methodParameterTypeNames);

                if (dcTree != null) {
                    dcTree.accept(new RedisCommandMethodDocumentVisitor(redisMethodMetadataMap), methodElement);
                }

                this.redisMethodMetadataMapList.add(redisMethodMetadataMap);
                logger.info("Redis Method Metadata : {}", redisMethodMetadataMap);
            }
            return null;
        }

//        private void initInterfaceMetadata(Map<String, Object> metadataMap, String interfaceName) {
//            int index = interfaceName.indexOf(DOT);
//
//            if (index == -1) {
//                metadataMap.put(interfaceName, newLinkedHashMap());
//            } else {
//                String key = interfaceName.substring(0, index);
//                String remaining = interfaceName.substring(index + 1);
//                Map<String, Object> subMap = (Map) metadataMap.computeIfAbsent(key, k -> newLinkedHashMap());
//                initInterfaceMetadata(subMap, remaining);
//            }
//        }
//
//
//        private Map<String, Object> getInterfaceMap(String interfaceName) {
//            String[] parts = split(interfaceName, DOT);
//            Map<String, Object> interfaceMap = this.methodsMetadataMap;
//            for (int i = 0; i < parts.length; i++) {
//                String part = parts[i];
//                interfaceMap = (Map<String, Object>) interfaceMap.get(part);
//            }
//            return interfaceMap;
//        }

        private Integer buildIndex(String interfaceName, String signature) {
            return (interfaceName + DOT + signature).hashCode();
            // return toHexString(hashCode);
        }
    }

    class RedisCommandMethodDocumentVisitor extends DocTreeScanner<Void, ExecutableElement> {

        private static final String DOC_OPEN = "Redis Documentation:";

        private static final String DOC_CLOSE = "<";

        private final Map<String, Object> redisMethodMetadataMap;

        RedisCommandMethodDocumentVisitor(Map<String, Object> redisMethodMetadataMap) {
            this.redisMethodMetadataMap = redisMethodMetadataMap;
        }

        @Override
        public Void scan(DocTree t, ExecutableElement methodElement) {
            DocTree.Kind kind = t.getKind();
            if (SEE.equals(kind)) {
                String see = t.toString();
                String reference = substringBetween(see, DOC_OPEN, DOC_CLOSE);
                if (reference != null) {
                    String interfaceName = (String) this.redisMethodMetadataMap.get(INTERFACE_NAME_KEY);
                    String signature = methodElement.toString();
                    String resolvedCommand = reference.trim().toUpperCase(locale);
                    if (isCommand(interfaceName, signature, resolvedCommand)) {
                        boolean write = redisWriteCommands.contains(resolvedCommand);

                        this.redisMethodMetadataMap.put(COMMAND_KEY, resolvedCommand);
                        this.redisMethodMetadataMap.put(WRITE_KEY, write);

                        supportedCommands.add(resolvedCommand);
                        if (write) {
                            supportedWriteCommands.add(resolvedCommand);
                        }
                    }
                }
            }
            return super.scan(t, methodElement);
        }

        boolean isCommand(String interfaceName, String signature, String resolvedCommand) {
            boolean isCommand = redisCommands.contains(resolvedCommand);
            if (!isCommand) {
                logger.warn("The Redis command can't be resolved by the JavaDoc of {}#{} , actual : {}", interfaceName, signature, resolvedCommand);
            }
            return isCommand;
        }
    }
}
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
import io.microsphere.redis.generator.metadata.SpringDataRedisMetadataGenerator;
import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;

import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.util.ElementScanner9;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.sun.source.doctree.DocTree.Kind.SEE;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodName;
import static io.microsphere.annotation.processor.util.MethodUtils.getMethodParameterTypeNames;
import static io.microsphere.annotation.processor.util.TypeUtils.getTypeName;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.util.RedisUtils.getRedisCommands;
import static io.microsphere.redis.util.RedisUtils.getRedisWriteCommands;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.StringUtils.substringBetween;
import static java.util.Set.of;
import static javax.lang.model.SourceVersion.latest;
import static javax.lang.model.element.ElementKind.METHOD;

/**
 * The {@link Doclet} class for Spring Data Redis Metadata Generation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Doclet
 * @see SpringDataRedisMetadataGenerator
 * @since 1.0.0
 */
public class SpringDataRedisMetadataGenerationDoclet implements Doclet {

    private static final Logger logger = getLogger(SpringDataRedisMetadataGenerationDoclet.class);

    private Locale locale;

    private Reporter reporter;

    private Set<String> redisCommands;

    private Set<String> redisWriteCommands;

    private Set<String> allSupportedCommands;

    private Set<String> allSupportedWriteCommands;

    @Override
    public void init(Locale locale, Reporter reporter) {
        this.locale = locale;
        this.reporter = reporter;
        this.redisCommands = getRedisCommands();
        this.redisWriteCommands = getRedisWriteCommands();
        this.allSupportedCommands = new HashSet<>();
        this.allSupportedWriteCommands = new HashSet<>();
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return of();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return latest();
    }

    @Override
    public boolean run(DocletEnvironment environment) {
        DocTrees docTrees = environment.getDocTrees();

        RedisCommandMethodVisitor redisCommandMethodVisitor = new RedisCommandMethodVisitor();

        Set<? extends Element> specifiedElements = environment.getSpecifiedElements();
        for (Element element : specifiedElements) {
            element.accept(redisCommandMethodVisitor, docTrees);
        }

        logger.info("{} supported commands : {} ", allSupportedCommands.size(), allSupportedCommands);
        logger.info("{} supported write commands : {} ", allSupportedWriteCommands.size(), allSupportedWriteCommands);

        return true;
    }

    class RedisCommandMethodVisitor extends ElementScanner9<Void, DocTrees> {

        @Override
        public Void scan(Element e, DocTrees docTrees) {
            ElementKind kind = e.getKind();
            if (METHOD.equals(kind)) {
                DocCommentTree dcTree = docTrees.getDocCommentTree(e);
                if (dcTree != null) {
                    ExecutableElement methodElement = (ExecutableElement) e;
                    dcTree.accept(new RedisCommandMethodDocumentVisitor(), methodElement);
                }
            }
            return null;
        }
    }

    class RedisCommandMethodDocumentVisitor extends DocTreeScanner<Void, ExecutableElement> {

        @Override
        public Void scan(DocTree t, ExecutableElement methodElement) {
            DocTree.Kind kind = t.getKind();
            if (SEE.equals(kind)) {
                String see = t.toString();
                String reference = substringBetween(see, "Redis Documentation:", "<");
                if (reference != null) {
                    Element declaredClass = methodElement.getEnclosingElement();
                    String methodSignature = methodElement.toString();
                    String className = getTypeName(declaredClass.asType());
                    String methodName = getMethodName(methodElement);
                    String[] parameterTypes = getMethodParameterTypeNames(methodElement);
                    String command = reference.trim().toUpperCase(locale);
                    boolean isCommand = redisCommands.contains(command);
                    boolean isWriteCommand = redisWriteCommands.contains(command);
                    logger.info("className : {} , method name : '{}' , parameter types : '{}' , method signature : '{}' , command : '{}' , isCommand : {} , isWriteCommand : {}",
                            className, methodName, arrayToString(parameterTypes), methodSignature, command, isCommand, isWriteCommand);
                    if (isCommand) {
                        allSupportedCommands.add(command);
                        if (isWriteCommand) {
                            allSupportedWriteCommands.add(command);
                        }
                    }
                }
            }
            return super.scan(t, methodElement);
        }
    }
}
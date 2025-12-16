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

package io.microsphere.redis.generator.metadata;


import io.microsphere.logging.Logger;
import io.microsphere.redis.generator.doclet.SpringDataRedisMetadataGenerationDoclet;

import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.StandardJavaFileManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassPathUtils.getClassPaths;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.Files.walk;
import static java.nio.file.Files.walkFileTree;
import static java.nio.file.Paths.get;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.setDefault;
import static java.util.Set.of;
import static java.util.stream.Collectors.toSet;
import static javax.tools.StandardLocation.CLASS_PATH;
import static javax.tools.ToolProvider.getSystemDocumentationTool;

/**
 * The Spring Data Redis Metadata Generator
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringDataRedisMetadataGenerationDoclet
 * @since 1.0.0
 */
public class SpringDataRedisMetadataGenerator {

    static {
        setDefault(ENGLISH);
    }

    private static final Logger logger = getLogger(SpringDataRedisMetadataGenerator.class);

    public static void main(String[] args) throws Exception {
        int length = args.length;

        if (length < 1) {
            logger.warn("Usage : SpringDataRedisMetadataGenerator <source-path> <lib-path>(optional)");
        }

        Path sourcePath = get(args[0]);

        Set<File> classPaths = resolve(args);

        List<Path> sourceFiles = new LinkedList<>();

        walkFileTree(sourcePath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                if (exc != null) {
                    logger.warn("The directory visit failed for {}", dir, exc);
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                if (exc != null) {
                    logger.warn("The directory file failed for {}", file, exc);
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.toString().endsWith(".java")) {
                    sourceFiles.add(file);
                }
                return CONTINUE;
            }
        });

        DocumentationTool documentationTool = getSystemDocumentationTool();

        StandardJavaFileManager standardFileManager = documentationTool.getStandardFileManager(null, null, UTF_8);

        // set class-paths
        standardFileManager.setLocation(CLASS_PATH, classPaths);

        logger.info("Class-Paths : {}", classPaths);

        var docUtils = standardFileManager.getJavaFileObjects(sourceFiles.toArray(new Path[0]));

        Set<String> options = of();

        DocumentationTask docTask = documentationTool.getTask(null, standardFileManager, null,
                SpringDataRedisMetadataGenerationDoclet.class, options, docUtils);

        boolean result = docTask.call();

        logger.info("The JavaDoc generation result : {}", result);
    }

    private static Set<File> resolve(String[] args) throws IOException {
        if (args.length < 2) {
            return getClassPaths().stream().map(File::new).collect(toSet());
        }
        Path libraryPath = get(args[1]);
        Stream<Path> libs = walk(libraryPath, 1);
        return libs.map(Path::toFile).collect(toSet());
    }
}
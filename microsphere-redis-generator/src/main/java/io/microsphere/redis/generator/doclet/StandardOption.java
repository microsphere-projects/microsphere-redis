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

import jdk.javadoc.doclet.Doclet.Option;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static jdk.javadoc.doclet.Doclet.Option.Kind.STANDARD;

/**
 * Abstract {@link Option} with Standard
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Option
 * @since 1.0.0
 */
public abstract class StandardOption implements Option {

    private final String name;

    private final int argumentCount;

    private final String description;

    private final String parameters;

    public StandardOption(String name, int argumentCount, String parameters, String description) {
        this.name = name;
        this.argumentCount = argumentCount;
        this.parameters = parameters;
        this.description = description;
    }

    @Override
    public int getArgumentCount() {
        return argumentCount;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public final Kind getKind() {
        return STANDARD;
    }

    @Override
    public List<String> getNames() {
        return ofList(name);
    }

    @Override
    public String getParameters() {
        return parameters;
    }
}

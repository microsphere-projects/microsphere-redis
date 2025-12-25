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
package io.microsphere.redis.metadata;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;

/**
 * Redis Metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodMetadata
 * @since 1.0.0
 */
public class RedisMetadata {

    private String version;

    private List<MethodMetadata> methods;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<MethodMetadata> getMethods() {
        List<MethodMetadata> methods = this.methods;
        if (methods == null) {
            methods = new LinkedList<>();
            setMethods(methods);
        }
        return methods;
    }

    public void setMethods(List<MethodMetadata> methods) {
        this.methods = methods;
    }

    public RedisMetadata merge(RedisMetadata another) {
        List<MethodMetadata> methods = getMethods();
        // Merge the version
        this.version = another.getVersion();
        // Add another methods
        methods.addAll(another.getMethods());
        return this;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof RedisMetadata)) {
            return false;
        }

        RedisMetadata that = (RedisMetadata) o;
        return Objects.equals(this.getVersion(), that.getVersion())
                && Objects.equals(this.getMethods(), that.getMethods());
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(this.getVersion());
        result = 31 * result + Objects.hashCode(this.getMethods());
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, RedisMetadata.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("version=" + this.getVersion())
                .add("methods=" + this.getMethods()).toString()
                ;
    }
}
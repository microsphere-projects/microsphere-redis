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
 * Top-level container for Redis metadata that groups a version string and a list of
 * {@link MethodMetadata} instances.  Typically deserialized from YAML resources located at
 * {@code META-INF/spring-data-redis-metadata.yaml} on the classpath.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   RedisMetadata metadata = new RedisMetadata();
 *   metadata.setVersion("1.0.0");
 *
 *   MethodMetadata method = new MethodMetadata();
 *   method.setMethodName("set");
 *   method.setCommands(new String[]{"SET"});
 *   method.setWrite(true);
 *   metadata.getMethods().add(method);
 *
 *   System.out.println(metadata.getVersion());        // "1.0.0"
 *   System.out.println(metadata.getMethods().size()); // 1
 *
 *   // Merge two instances
 *   RedisMetadata other = new RedisMetadata();
 *   other.setVersion("2.0.0");
 *   metadata.merge(other);
 *   System.out.println(metadata.getVersion());        // "2.0.0"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodMetadata
 * @since 1.0.0
 */
public class RedisMetadata {

    private String version;

    private List<MethodMetadata> methods;

    /**
     * Returns the schema/data version string of this metadata (e.g. {@code "1.0.0"}).
     *
     * @return version string, may be {@code null} if not set
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version string.
     *
     * @param version the version string to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the list of {@link MethodMetadata} entries, lazily initializing it to an empty
     * {@link java.util.LinkedList} if not yet set.
     *
     * @return non-null mutable list of {@link MethodMetadata}
     */
    public List<MethodMetadata> getMethods() {
        List<MethodMetadata> methods = this.methods;
        if (methods == null) {
            methods = new LinkedList<>();
            setMethods(methods);
        }
        return methods;
    }

    /**
     * Sets the list of {@link MethodMetadata}.
     *
     * @param methods the list of method metadata to set
     */
    public void setMethods(List<MethodMetadata> methods) {
        this.methods = methods;
    }

    /**
     * Merges another {@link RedisMetadata} into this instance by adopting the other's version
     * string and appending all of its method metadata entries to this instance's list.
     *
     * @param another the {@link RedisMetadata} to merge into this instance
     * @return this instance (for chaining)
     */
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
        if (!(o instanceof RedisMetadata that)) {
            return false;
        }

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
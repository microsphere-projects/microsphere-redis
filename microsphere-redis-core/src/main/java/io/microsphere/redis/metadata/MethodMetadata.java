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

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

import static io.microsphere.constants.SymbolConstants.COMMA;
import static io.microsphere.constants.SymbolConstants.LEFT_SQUARE_BRACKET;
import static io.microsphere.constants.SymbolConstants.RIGHT_SQUARE_BRACKET;
import static io.microsphere.util.ArrayUtils.arrayEquals;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static java.util.Objects.hash;

/**
 * Method metadata that describes a Redis command method, including its declaring interface,
 * method name, parameter names and types, associated Redis commands, and whether it is a
 * write command.  Instances are typically deserialized from YAML resources bundled on the
 * classpath (e.g. {@code META-INF/spring-data-redis-metadata.yaml}).
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   MethodMetadata metadata = new MethodMetadata();
 *   metadata.setIndex(0);
 *   metadata.setInterfaceName("org.springframework.data.redis.connection.RedisStringCommands");
 *   metadata.setMethodName("set");
 *   metadata.setParameterNames(new String[]{"key", "value"});
 *   metadata.setParameterTypes(new String[]{"[B", "[B"});
 *   metadata.setCommands(new String[]{"SET"});
 *   metadata.setWrite(true);
 *
 *   System.out.println(metadata.getMethodName()); // "set"
 *   System.out.println(metadata.isWrite());       // true
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodMetadata {

    private int index;

    private String interfaceName;

    private String methodName;

    private String[] parameterNames;

    private String[] parameterTypes;

    private String[] commands;

    private boolean write;

    /**
     * Returns the numeric index that uniquely identifies this method (absolute hash of its id).
     *
     * @return the method index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the numeric index for this method metadata.
     *
     * @param index the method index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Returns the fully-qualified name of the Redis command interface that declares this method.
     *
     * @return interface class name, e.g. {@code "org.springframework.data.redis.connection.RedisStringCommands"}
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets the fully-qualified interface class name for this method.
     *
     * @param interfaceName the declaring interface class name
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns the simple name of the method.
     *
     * @return method name, e.g. {@code "set"}
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the method name.
     *
     * @param methodName the method name
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the parameter names of the method.
     *
     * @return array of parameter names, may be {@code null} if not resolved
     */
    public String[] getParameterNames() {
        return parameterNames;
    }

    /**
     * Sets the parameter names for the method.
     *
     * @param parameterNames array of parameter names
     */
    public void setParameterNames(String[] parameterNames) {
        this.parameterNames = parameterNames;
    }

    /**
     * Returns the fully-qualified class names of the method parameter types.
     *
     * @return array of parameter type names, e.g. {@code {"[B", "[B"}} for {@code byte[], byte[]}
     */
    public String[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * Sets the parameter type class names.
     *
     * @param parameterTypes array of fully-qualified parameter type names
     */
    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * Returns the Redis command names associated with this method (e.g. {@code {"SET"}}).
     *
     * @return array of Redis command name strings
     */
    public String[] getCommands() {
        return commands;
    }

    /**
     * Sets the Redis command names associated with this method.
     *
     * @param commands array of Redis command name strings
     */
    public void setCommands(String[] commands) {
        this.commands = commands;
    }

    /**
     * Returns {@code true} if this method is a Redis write command.
     *
     * @return {@code true} for write commands (e.g. SET, DEL), {@code false} for read-only commands (e.g. GET)
     */
    public boolean isWrite() {
        return write;
    }

    /**
     * Sets whether this method is a Redis write command.
     *
     * @param write {@code true} if this is a write command
     */
    public void setWrite(boolean write) {
        this.write = write;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodMetadata)) {
            return false;
        }
        MethodMetadata that = (MethodMetadata) o;
        return this.index == that.index
                && this.write == that.write
                && Objects.equals(this.interfaceName, that.interfaceName)
                && Objects.equals(this.methodName, that.methodName)
                && arrayEquals(this.parameterNames, that.parameterNames)
                && arrayEquals(this.parameterTypes, that.parameterTypes)
                && arrayEquals(this.commands, that.commands);
    }

    @Override
    public int hashCode() {
        int result = hash(this.index, this.interfaceName, this.methodName, this.write);
        result = 31 * result + Arrays.hashCode(this.parameterTypes);
        result = 31 * result + Arrays.hashCode(this.commands);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, MethodMetadata.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("id=" + this.index)
                .add("interfaceName='" + this.interfaceName + "'")
                .add("methodName='" + this.methodName + "'")
                .add("parameterNames=" + arrayToString(this.parameterNames))
                .add("parameterTypes=" + arrayToString(this.parameterTypes))
                .add("commands=" + arrayToString(this.commands))
                .add("write=" + this.write)
                .toString();
    }
}
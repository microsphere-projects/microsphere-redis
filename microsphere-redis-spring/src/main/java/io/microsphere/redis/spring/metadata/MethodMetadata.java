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
package io.microsphere.redis.spring.metadata;

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
 * Method metadata
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodMetadata {

    private short index;

    private String interfaceName;

    private String methodName;

    private String[] parameterTypes;

    private boolean write;

    public short getIndex() {
        return index;
    }

    public void setIndex(short index) {
        this.index = index;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public boolean isWrite() {
        return write;
    }

    public void setWrite(boolean write) {
        this.write = write;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MethodMetadata)) {
            return false;
        }
        MethodMetadata that = (MethodMetadata) o;
        return index == that.index &&
                write == that.write &&
                Objects.equals(interfaceName, that.interfaceName) &&
                Objects.equals(methodName, that.methodName) &&
                arrayEquals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        int result = hash(index, interfaceName, methodName, write);
        result = 31 * result + Arrays.hashCode(parameterTypes);
        return result;
    }

    @Override
    public String toString() {
        return new StringJoiner(COMMA, MethodMetadata.class.getSimpleName() + LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET)
                .add("id=" + index)
                .add("interfaceName='" + interfaceName + "'")
                .add("methodName='" + methodName + "'")
                .add("parameterTypes=" + arrayToString(parameterTypes))
                .add("write=" + write)
                .toString();
    }
}
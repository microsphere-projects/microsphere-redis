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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import static io.microsphere.redis.util.RedisCommandUtils.buildMethodId;

/**
 * The {@link Method} Information class that combines a reflected {@link Method} with its associated
 * {@link MethodMetadata} and a list of {@link ParameterMetadata}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Find a method via reflection
 *   Method method = String.class.getMethod("toUpperCase", Locale.class);
 *
 *   // Build MethodMetadata
 *   MethodMetadata methodMetadata = new MethodMetadata();
 *   methodMetadata.setIndex(buildMethodIndex(method));
 *   methodMetadata.setMethodName(method.getName());
 *   methodMetadata.setCommands(new String[]{"SET"});
 *   methodMetadata.setWrite(true);
 *
 *   // Build ParameterMetadata list
 *   List<ParameterMetadata> parameterMetadataList = buildParameterMetadataList(method);
 *
 *   // Construct MethodInfo
 *   MethodInfo methodInfo = new MethodInfo(method, methodMetadata, parameterMetadataList);
 *
 *   String id = methodInfo.getId();       // "java.lang.String.toUpperCase(java.util.Locale)"
 *   int index = methodInfo.getIndex();    // absolute hash of the id
 *   String name = methodInfo.getName();   // "toUpperCase"
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Method
 * @see MethodMetadata
 * @since 1.0.0
 */
public class MethodInfo {

    private final String id;

    private final Method method;

    private final MethodMetadata methodMetadata;

    private final List<ParameterMetadata> parameterMetadataList;

    /**
     * Constructs a {@link MethodInfo} from the given {@link Method}, {@link MethodMetadata}, and parameter metadata list.
     *
     * @param method                 the reflected {@link Method}
     * @param methodMetadata         the associated {@link MethodMetadata}
     * @param parameterMetadataList  the list of {@link ParameterMetadata} for the method parameters
     */
    public MethodInfo(Method method, MethodMetadata methodMetadata, List<ParameterMetadata> parameterMetadataList) {
        this.id = buildMethodId(method);
        this.method = method;
        this.methodMetadata = methodMetadata;
        this.parameterMetadataList = parameterMetadataList;
    }

    /**
     * Returns the unique identifier for this method, formed as
     * {@code "fully.qualified.ClassName.methodName(param1Type,param2Type,...)"}.
     *
     * @return non-null method id string
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the numeric index of this method derived from {@link MethodMetadata#getIndex()}.
     *
     * @return method index (absolute value of the id's hash code)
     */
    public int getIndex() {
        return this.methodMetadata.getIndex();
    }

    /**
     * Returns the simple name of the underlying {@link Method}.
     *
     * @return method name, e.g. {@code "toUpperCase"}
     */
    public String getName() {
        return this.method.getName();
    }

    /**
     * Returns the underlying reflected {@link Method}.
     *
     * @return non-null {@link Method}
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the associated {@link MethodMetadata} that carries YAML/generated metadata
     * (interface name, parameter names, Redis commands, write flag, etc.).
     *
     * @return the {@link MethodMetadata}
     */
    public MethodMetadata getMethodMetadata() {
        return methodMetadata;
    }

    /**
     * Returns the immutable list of {@link ParameterMetadata} for each method parameter.
     *
     * @return non-null list of {@link ParameterMetadata}
     */
    public List<ParameterMetadata> getParameterMetadataList() {
        return parameterMetadataList;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MethodInfo)) {
            return false;
        }

        MethodInfo that = (MethodInfo) o;
        return Objects.equals(this.id, that.id)
                && Objects.equals(this.methodMetadata, that.methodMetadata)
                && Objects.equals(this.parameterMetadataList, that.parameterMetadataList);
    }

    @Override
    public int hashCode() {
        return this.getIndex();
    }

    @Override
    public String toString() {
        return this.id;
    }
}
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
 * The {@link Method} Information class
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

    public MethodInfo(Method method, MethodMetadata methodMetadata, List<ParameterMetadata> parameterMetadataList) {
        this.id = buildMethodId(method);
        this.method = method;
        this.methodMetadata = methodMetadata;
        this.parameterMetadataList = parameterMetadataList;
    }

    public String getId() {
        return id;
    }

    public int getIndex() {
        return this.methodMetadata.getIndex();
    }

    public String getName() {
        return this.method.getName();
    }

    public Method getMethod() {
        return method;
    }

    public MethodMetadata getMethodMetadata() {
        return methodMetadata;
    }

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
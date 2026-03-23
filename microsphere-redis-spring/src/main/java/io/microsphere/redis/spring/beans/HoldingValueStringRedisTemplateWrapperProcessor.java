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
package io.microsphere.redis.spring.beans;

import io.microsphere.lang.WrapperProcessor;
import io.microsphere.redis.spring.serializer.HoldingValueRedisSerializerWrapper;

import static io.microsphere.redis.spring.serializer.HoldingValueRedisSerializerWrapper.wrap;

/**
 * {@link WrapperProcessor} for {@link StringRedisTemplateWrapper} that installs
 * {@link HoldingValueRedisSerializerWrapper}s on the key, value, hash-key, and hash-value
 * serializers of the template so that original Java objects and their serialized byte arrays
 * are kept in the thread-local {@link io.microsphere.redis.util.ValueHolder} during command
 * execution.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Registered automatically via WrapperProcessors; can be applied manually in tests:
 *   HoldingValueStringRedisTemplateWrapperProcessor processor =
 *           new HoldingValueStringRedisTemplateWrapperProcessor();
 *   StringRedisTemplateWrapper wrapper = ...;
 *   wrapper = processor.process(wrapper);
 *   // The wrapper's serializers now record raw bytes in ValueHolder during serialization
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HoldingValueRedisSerializerWrapper
 * @since 1.0.0
 */
public class HoldingValueStringRedisTemplateWrapperProcessor implements WrapperProcessor<StringRedisTemplateWrapper> {

    /**
     * Wraps all serializers of the given {@link StringRedisTemplateWrapper} with
     * {@link HoldingValueRedisSerializerWrapper} instances and returns the (unmodified) wrapper.
     *
     * @param wrapper the {@link StringRedisTemplateWrapper} whose serializers will be wrapped
     * @return the same {@code wrapper} instance after serializer wrapping
     */
    @Override
    public StringRedisTemplateWrapper process(StringRedisTemplateWrapper wrapper) {
        wrap(wrapper);
        return wrapper;
    }
}

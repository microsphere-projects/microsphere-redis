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
package io.microsphere.redis.util;

import io.microsphere.logging.Logger;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.lang.ThreadLocal.withInitial;

/**
 * Value Holder
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ValueHolder {

    private static final Logger logger = getLogger(ValueHolder.class);

    private static final ThreadLocal<ValueHolder> holder = withInitial(() -> new ValueHolder(4));

    private final Map<Object, Object> cache;

    public ValueHolder(int initialCapacity) {
        this.cache = new HashMap<>(initialCapacity << 1);
    }

    public void set(Object value, byte[] rawValue) {
        RawValue rawValueObject = RawValue.of(rawValue);
        put(value, rawValueObject);
        put(rawValueObject, value);
    }

    private void put(Object key, Object value) {
        Object oldValue = cache.put(key, value);
        logger.trace("Put key[{}] and value[{}] into cache, old value : {}", key, value, oldValue);
    }

    public Object getValue(byte[] rawValue) {
        RawValue rawValueObject = RawValue.of(rawValue);
        return cache.get(rawValueObject);
    }

    public byte[] getRawValue(Object value) {
        Object rawValue = cache.get(value);
        return rawValue instanceof RawValue ? ((RawValue) rawValue).data() : null;
    }

    public static ValueHolder get() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }

}

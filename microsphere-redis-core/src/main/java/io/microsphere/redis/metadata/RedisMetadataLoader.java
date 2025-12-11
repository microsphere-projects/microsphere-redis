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

import io.microsphere.annotation.Nonnull;

import static io.microsphere.util.ServiceLoaderUtils.loadServices;

/**
 * The loader class of {@link RedisMetadata}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMetadata
 * @since 1.0.0
 */
public interface RedisMetadataLoader {

    /**
     * Load {@link RedisMetadata}
     *
     * @return {@link RedisMetadata}
     */
    @Nonnull
    RedisMetadata load();

    /**
     * Load all {@link RedisMetadata}
     *
     * @return non-null
     */
    @Nonnull
    static RedisMetadata loadAll() {
        RedisMetadata redisMetadata = new RedisMetadata();
        RedisMetadataLoader[] redisMetadataLoaders = loadServices(RedisMetadataLoader.class);
        for (RedisMetadataLoader redisMetadataLoader : redisMetadataLoaders) {
            redisMetadata.merge(redisMetadataLoader.load());
        }
        return redisMetadata;
    }
}
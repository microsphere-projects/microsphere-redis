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
 * SPI interface for loading {@link RedisMetadata}. Implementations are discovered via
 * {@link java.util.ServiceLoader} from the classpath and contribute individual
 * {@link RedisMetadata} instances that are merged together by {@link #loadAll()}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Load all available RedisMetadata from all registered loaders on the classpath
 *   RedisMetadata allMetadata = RedisMetadataLoader.loadAll();
 *   System.out.println(allMetadata.getVersion());
 *   System.out.println(allMetadata.getMethods().size());
 *
 *   // Implement a custom loader (registered via META-INF/services)
 *   public class MyRedisMetadataLoader implements RedisMetadataLoader {
 *       public RedisMetadata load() {
 *           RedisMetadata metadata = new RedisMetadata();
 *           metadata.setVersion("1.0.0");
 *           return metadata;
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMetadata
 * @since 1.0.0
 */
public interface RedisMetadataLoader {

    /**
     * Loads the {@link RedisMetadata} provided by this particular implementation.
     *
     * @return non-null {@link RedisMetadata} instance
     */
    @Nonnull
    RedisMetadata load();

    /**
     * Discovers all {@link RedisMetadataLoader} implementations via {@link java.util.ServiceLoader},
     * invokes each loader's {@link #load()} method, and merges the results into a single
     * {@link RedisMetadata} instance.
     *
     * @return merged non-null {@link RedisMetadata} containing data from all registered loaders
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
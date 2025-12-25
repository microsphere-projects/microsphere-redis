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

import io.microsphere.redis.metadata.RedisMetadata;
import io.microsphere.redis.metadata.RedisMetadataLoader;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

import static io.microsphere.redis.util.RedisUtils.loadResources;
import static io.microsphere.util.ClassLoaderUtils.getClassLoader;

/**
 * {@link RedisMetadataLoader} for The Spring Data Redis
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisMetadataLoader
 * @since 1.0.0
 */
public class SpringRedisMetadataLoader implements RedisMetadataLoader {

    public static final String SPRING_REDIS_METADATA_RESOURCE = "META-INF/spring-data-redis-metadata.yaml";

    @Override
    public RedisMetadata load() {
        ClassLoader classLoader = getClassLoader(getClass());
        return loadResources(classLoader, SPRING_REDIS_METADATA_RESOURCE, inputStreams -> {
            RedisMetadata redisMetadata = new RedisMetadata();
            Yaml yaml = new Yaml();
            for (InputStream inputStream : inputStreams) {
                RedisMetadata metadata = yaml.loadAs(inputStream, RedisMetadata.class);
                redisMetadata.merge(metadata);
            }
            return redisMetadata;
        });
    }
}

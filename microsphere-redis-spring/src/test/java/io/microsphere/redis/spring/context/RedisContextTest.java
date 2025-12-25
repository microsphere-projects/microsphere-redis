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

package io.microsphere.redis.spring.context;


import io.microsphere.redis.spring.AbstractRedisTest;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.config.RedisContextConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.context.ContextConfiguration;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.spring.context.RedisContext.get;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RedisContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisContext
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        RedisContextTest.class,
        RedisContextConfig.class
})
class RedisContextTest extends AbstractRedisTest {

    @Autowired
    private ConfigurableEnvironment environment;

    @Test
    void test() {
        RedisContext redisContext = get(this.context);
        RedisConfiguration redisConfiguration = redisContext.getRedisConfiguration();
        assertSame(RedisConfiguration.get(this.context), redisConfiguration);
        assertSame(this.context.getBeanFactory(), redisContext.getBeanFactory());
        assertSame(this.context, redisContext.getApplicationContext());
        assertSame(this.context.getClassLoader(), redisContext.getClassLoader());
        assertSame(this.environment, redisContext.getEnvironment());
        assertFalse(redisContext.isEnabled());
        assertSame(this.redisTemplate, redisContext.getRedisTemplate("redisTemplate"));
        assertSame(this.stringRedisTemplate, redisContext.getRedisTemplate("stringRedisTemplate"));
        assertNull(redisContext.getRedisTemplate("not-found"));
        assertEquals(ofSet("redisTemplate", "stringRedisTemplate"), redisContext.getRedisTemplateBeanNames());
        assertEquals(ofSet("redisConnectionFactory"), redisContext.getRedisConnectionFactoryBeanNames());
        assertEquals(redisConfiguration.isCommandEventExposed(), redisContext.isCommandEventExposed());
        assertEquals(redisConfiguration.getApplicationName(), redisContext.getApplicationName());
        assertEquals(emptyList(), redisContext.getRedisConnectionInterceptors());
        assertEquals(emptyList(), redisContext.getRedisCommandInterceptors());
    }
}
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

package io.microsphere.redis.spring.boot.autoconfigure;

import io.microsphere.redis.spring.annotation.EnableRedisInterceptor;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.context.RedisInitializer;
import io.microsphere.redis.spring.interceptor.RedisCommandInterceptor;
import io.microsphere.redis.spring.test.AbstractRedisTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.reflect.Method;
import java.util.Map;

import static io.microsphere.collection.MapUtils.newHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link RedisInitializer} Spring Boot Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisInitializer
 * @see EnableRedisInterceptor
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        RedisInitializerSpringBootTest.TestInterceptor.class
})
@EnableAutoConfiguration
class RedisInitializerSpringBootTest extends AbstractRedisTest {

    @Autowired
    private RedisConfiguration redisConfiguration;

    @Autowired
    private RedisContext redisContext;

    @Autowired
    private TestInterceptor testInterceptor;

    @Test
    void test() {
        assertNotNull(this.redisConfiguration);
        assertNotNull(this.redisContext);

        String key = "a";
        String value = "1";
        assertStringRedisTemplateSet(key, value);

        Map<Object, Object> data = testInterceptor.data;
        assertEquals(1, data.size());
    }

    static class TestInterceptor implements RedisCommandInterceptor {

        Map<Object, Object> data = newHashMap();

        @Override
        public void afterExecute(RedisCommands target, Method method, Object[] args, String sourceBeanName, Object result, Throwable failure) throws Throwable {
            String methodName = method.getName();
            if ("set".equals(methodName) && args.length == 2) {
                this.data.put(args[0], args[1]);
            }
        }

        @Override
        public int getOrder() {
            return 0;
        }
    }
}

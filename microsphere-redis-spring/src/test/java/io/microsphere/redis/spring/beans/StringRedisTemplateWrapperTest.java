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


import io.microsphere.redis.spring.config.RedisContextConfig;
import io.microsphere.redis.spring.context.RedisContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link StringRedisTemplateWrapper} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see StringRedisTemplateWrapper
 * @since 1.0.0
 */
@SpringJUnitConfig(classes = {
        RedisContextConfig.class,
        StringRedisTemplateWrapperTest.class
})
@TestPropertySource(properties = {
        "microsphere.redis.enabled=false"
})
class StringRedisTemplateWrapperTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisContext redisContext;

    private StringRedisTemplateWrapper stringRedisTemplateWrapper;

    @BeforeEach
    void setUp() {
        this.stringRedisTemplateWrapper = new StringRedisTemplateWrapper("stringRedisTemplate", this.stringRedisTemplate, this.redisContext);
        this.stringRedisTemplateWrapper.afterPropertiesSet();
    }

    @Test
    void test() {
        ValueOperations<String, String> opsForValue = this.stringRedisTemplateWrapper.opsForValue();
        String key = "foo";
        String value = "bar";
        opsForValue.set(key, value);
        assertEquals(value, opsForValue.get(key));

        assertEquals("stringRedisTemplate", this.stringRedisTemplateWrapper.getBeanName());
        assertSame(this.redisContext, this.stringRedisTemplateWrapper.getRedisContext());
        assertSame(this.stringRedisTemplate, this.stringRedisTemplateWrapper.getDelegate());
    }
}
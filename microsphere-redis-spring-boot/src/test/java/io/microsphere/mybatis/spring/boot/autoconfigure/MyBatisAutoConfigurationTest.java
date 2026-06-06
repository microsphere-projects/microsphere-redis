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

import io.microsphere.redis.executor.LoggingExecutorFilter;
import io.microsphere.redis.executor.LoggingExecutorInterceptor;
import io.microsphere.redis.plugin.InterceptingExecutorInterceptor;
import io.microsphere.redis.spring.annotation.EnableMyBatis;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import static io.microsphere.redis.spring.annotation.MyBatisBeanDefinitionRegistrar.SQL_SESSION_FACTORY_BEAN_NAME;
import static io.microsphere.redis.spring.annotation.MyBatisBeanDefinitionRegistrar.SQL_SESSION_TEMPLATE_BEAN_NAME;
import static io.microsphere.redis.spring.annotation.MyBatisExtensionBeanDefinitionRegistrar.INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link MyBatisAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MyBatisAutoConfiguration
 * @see EnableMyBatis
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        LoggingExecutorFilter.class,
        LoggingExecutorInterceptor.class,
        MyBatisAutoConfigurationTest.class
})
@EnableAutoConfiguration
class MyBatisAutoConfigurationTest {

    @Autowired
    @Qualifier(SQL_SESSION_FACTORY_BEAN_NAME)
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    @Qualifier(SQL_SESSION_TEMPLATE_BEAN_NAME)
    private SqlSessionTemplate sqlSessionTemplate;

    @Autowired
    @Qualifier(INTERCEPTING_EXECUTOR_INTERCEPTOR_BEAN_NAME)
    private InterceptingExecutorInterceptor interceptingExecutorInterceptor;

    @Test
    void test() {
        assertNotNull(sqlSessionFactory);
        assertNotNull(sqlSessionTemplate);
        assertNotNull(interceptingExecutorInterceptor);
    }
}

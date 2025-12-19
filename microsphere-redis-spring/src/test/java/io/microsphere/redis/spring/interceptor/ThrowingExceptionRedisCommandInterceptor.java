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

package io.microsphere.redis.spring.interceptor;

import io.microsphere.logging.Logger;
import org.springframework.data.redis.connection.RedisCommands;

import java.lang.reflect.Method;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link RedisCommandInterceptor} for throwing exception.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandInterceptor
 * @since 1.0.0
 */
public class ThrowingExceptionRedisCommandInterceptor implements RedisCommandInterceptor {

    private static final Logger logger = getLogger(ThrowingExceptionRedisCommandInterceptor.class);

    @Override
    public void beforeExecute(RedisCommands target, Method method, Object[] args, String sourceBeanName) throws Throwable {
        throw new Throwable("For Testing...");
    }

    @Override
    public void afterExecute(RedisCommands target, Method method, Object[] args, String sourceBeanName, Object result, Throwable failure) throws Throwable {
        throw failure;
    }

    @Override
    public void handleError(RedisMethodContext<RedisCommands> context, boolean before, Object result, Throwable failure, Throwable error) {
        logger.error("context : {} , before : {} , result : {} , failure : {}", context, before, result, failure, error);
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
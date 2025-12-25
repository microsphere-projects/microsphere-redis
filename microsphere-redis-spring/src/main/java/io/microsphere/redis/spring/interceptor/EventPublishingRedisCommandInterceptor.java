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
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.data.redis.connection.RedisCommands;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link RedisCommandInterceptor} publishes {@link RedisCommandEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisCommandInterceptor
 * @see RedisMethodContext
 * @see RedisCommandEvent
 * @see RedisConfiguration
 * @see RedisContext
 * @since 1.0.0
 */
public class EventPublishingRedisCommandInterceptor implements RedisCommandInterceptor, ApplicationEventPublisherAware {

    private static final Logger logger = getLogger(EventPublishingRedisCommandInterceptor.class);

    public static final String BEAN_NAME = "microsphere:eventPublishingRedisCommendInterceptor";

    private final RedisConfiguration redisConfiguration;

    private ApplicationEventPublisher applicationEventPublisher;

    public EventPublishingRedisCommandInterceptor(RedisConfiguration redisConfiguration) {
        this.redisConfiguration = redisConfiguration;
    }

    public boolean isEnabled() {
        return this.redisConfiguration.isCommandEventExposed();
    }

    @Override
    public void afterExecute(RedisMethodContext<RedisCommands> context, Object result, Throwable failure) {
        if (isEnabled() && failure == null) {
            if (context.isWriteMethod(true)) { // The current method is a Redis write command
                // Publish Redis Command Event
                publishRedisCommandEvent(context);
            }
        }
        logger.trace("afterExecute - context : {} , result : {} , failure : {}", context, result, failure);
    }

    private void publishRedisCommandEvent(RedisMethodContext<RedisCommands> context) {
        // Event handling allows exceptions to be thrown
        applicationEventPublisher.publishEvent(new RedisCommandEvent(context));
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
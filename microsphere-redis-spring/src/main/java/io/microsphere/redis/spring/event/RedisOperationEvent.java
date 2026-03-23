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
package io.microsphere.redis.spring.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.data.redis.core.RedisOperations;

/**
 * Base Spring {@link ApplicationEvent} for high-level Redis operations executed through
 * {@link RedisOperations}.  Subclasses can carry more specific operation details.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Publishing a Redis operation event:
 *   applicationContext.publishEvent(new RedisOperationEvent(redisTemplate));
 *
 *   // Listening for Redis operation events:
 *   @Component
 *   public class MyOperationListener implements ApplicationListener<RedisOperationEvent> {
 *       @Override
 *       public void onApplicationEvent(RedisOperationEvent event) {
 *           System.out.println("Redis operation source: " + event.getSource());
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisOperationEvent extends ApplicationEvent {

    /**
     * Creates a new {@link RedisOperationEvent}.
     *
     * @param source the object on which the event initially occurred (e.g. a {@link RedisOperations} instance)
     */
    public RedisOperationEvent(Object source) {
        super(source);
    }
}

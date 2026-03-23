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

import io.microsphere.redis.spring.config.RedisConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.Environment;

import java.util.Set;

/**
 * Spring {@link ApplicationContextEvent} fired when one or more Microsphere Redis configuration
 * properties (prefixed with {@code microsphere.redis.}) have changed at runtime, typically as
 * a result of a Spring Cloud {@link org.springframework.cloud.context.environment.EnvironmentChangeEvent}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Listen for Redis configuration changes:
 *   @Component
 *   public class MyRedisConfigListener
 *           implements ApplicationListener<RedisConfigurationPropertyChangedEvent> {
 *
 *       @Override
 *       public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
 *           if (event.hasProperty("microsphere.redis.enabled")) {
 *               boolean enabled = event.getRedisConfiguration().isEnabled();
 *               System.out.println("Redis interceptor enabled: " + enabled);
 *           }
 *       }
 *   }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisConfigurationPropertyChangedEvent extends ApplicationContextEvent {

    private final Environment environment;

    private final Set<String> propertyNames;

    /**
     * Creates a new {@link RedisConfigurationPropertyChangedEvent}.
     *
     * @param source        the application context in which the change occurred
     * @param propertyNames the set of changed property names (prefixed with {@code microsphere.redis.})
     */
    public RedisConfigurationPropertyChangedEvent(ConfigurableApplicationContext source, Set<String> propertyNames) {
        super(source);
        this.environment = source.getEnvironment();
        this.propertyNames = propertyNames;
    }

    @Override
    public ConfigurableApplicationContext getSource() {
        return (ConfigurableApplicationContext) super.getSource();
    }

    /**
     * Returns the Spring {@link Environment} at the time the change event was fired.
     *
     * @return the environment; never {@code null}
     */
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * Returns the set of Microsphere Redis property names that changed.
     *
     * @return property names; never {@code null}, may be empty
     */
    public Set<String> getPropertyNames() {
        return propertyNames;
    }

    /**
     * Returns {@code true} if the given property name is among the changed properties.
     *
     * @param propertyName the property name to check (e.g. {@code "microsphere.redis.enabled"})
     * @return {@code true} if the property was changed
     */
    public boolean hasProperty(String propertyName) {
        return propertyNames.contains(propertyName);
    }

    /**
     * Retrieves the {@link RedisConfiguration} bean from the event's application context source.
     *
     * @return the {@link RedisConfiguration}; never {@code null}
     */
    public RedisConfiguration getRedisConfiguration() {
        return RedisConfiguration.get(getSource());
    }

    @Override
    public String toString() {
        return "RedisConfigurationPropertyChangedEvent{" +
                "environment = " + environment +
                ", propertyNames = " + propertyNames +
                ", source = " + source +
                ", timestamp = " + getTimestamp() +
                '}';
    }
}

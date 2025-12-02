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

package io.microsphere.redis.replicator.spring.config;


import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.context.RedisContext;
import io.microsphere.redis.spring.event.RedisConfigurationPropertyChangedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.BEAN_NAME;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.CONSUMER_ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.CONSUMER_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_CONSUMER_ENABLED;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_CONSUMER_ENABLED_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_DOMAIN;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_DOMAINS;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_ENABLED;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DEFAULT_ENABLED_PROPERTY_VALUE;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DOMAINS_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_SUFFIX;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.ENABLED_PROPERTY_NAME;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.PROPERTY_NAME_PREFIX;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.get;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link RedisReplicatorConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisReplicatorConfiguration
 * @since 1.0.0
 */
class RedisReplicatorConfigurationTest {

    public static final String TEST_PROPERTY_SOURCE_RESOURCE_NAME = "/META-INF/test-redis-replicator.properties";

    public static final String TEST_PROPERTY_SOURCE_LOCATION = "classpath:" + TEST_PROPERTY_SOURCE_RESOURCE_NAME;

    @Test
    void testConstants() {
        assertEquals("microsphere:redisReplicatorConfiguration", BEAN_NAME);
        assertEquals("microsphere.redis.replicator.", PROPERTY_NAME_PREFIX);
        assertEquals("true", DEFAULT_ENABLED_PROPERTY_VALUE);
        assertEquals(true, DEFAULT_ENABLED);
        assertEquals("microsphere.redis.replicator.enabled", ENABLED_PROPERTY_NAME);

        assertEquals("microsphere.redis.replicator.consumer.", CONSUMER_PROPERTY_NAME_PREFIX);
        assertEquals("false", DEFAULT_CONSUMER_ENABLED_PROPERTY_VALUE);
        assertEquals(false, DEFAULT_CONSUMER_ENABLED);
        assertEquals("microsphere.redis.replicator.consumer.enabled", CONSUMER_ENABLED_PROPERTY_NAME);

        assertEquals("default", DEFAULT_DOMAIN);
        assertEquals(ofList("default"), DEFAULT_DOMAINS);
        assertEquals("microsphere.redis.replicator.domains", DOMAINS_PROPERTY_NAME);

        assertEquals("microsphere.redis.replicator.domains.", DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_PREFIX);
        assertEquals(".redis-templates", DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_SUFFIX);
    }

    @Test
    void testOnDefault() {
        testInSpringContainer((context, environment) -> {
            RedisReplicatorConfiguration configuration = assertDefaultConfig(context, "default");
            assertEquals(emptyList(), configuration.getDomainRedisTemplateBeanNames(environment, "default"));
        }, DefaultRedisReplicationConfig.class);
    }

    @Test
    void testOnDomainRedisTemplateBeans() {
        testInSpringContainer((context, environment) -> {
            RedisReplicatorConfiguration configuration = assertDefaultConfig(context, "default", "test", "fixed", "duplicated");
            assertDomainRedisTemplateBeanNames(configuration, environment, "default");
            assertDomainRedisTemplateBeanNames(configuration, environment, "test");
        }, FullRedisReplicationConfig.class);
    }

    @Test
    void testOnRedisConfigurationPropertyChangedEvent() {
        testInSpringContainer((context, environment) -> {
            RedisReplicatorConfiguration configuration = assertDefaultConfig(context, "default");
            DefaultPropertySourceFactory propertySourceFactory = new DefaultPropertySourceFactory();

            RedisConfigurationPropertyChangedEvent event = new RedisConfigurationPropertyChangedEvent(context, ofSet("not-found"));
            context.publishEvent(event);

            EncodedResource resource = new EncodedResource(new ClassPathResource(TEST_PROPERTY_SOURCE_RESOURCE_NAME), "UTF-8");
            org.springframework.core.env.PropertySource<?> propertySource = propertySourceFactory.createPropertySource("test", resource);

            MutablePropertySources propertySources = environment.getPropertySources();
            propertySources.addLast(propertySource);

            Map<String, Object> properties = getSubProperties(propertySources, PROPERTY_NAME_PREFIX);
            Set<String> propertyNames = properties.keySet()
                    .stream()
                    .map(k -> PROPERTY_NAME_PREFIX + k)
                    .collect(toSet());

            event = new RedisConfigurationPropertyChangedEvent(context, propertyNames);
            context.publishEvent(event);

            assertDomainRedisTemplateBeanNames(configuration, environment, "default");
            assertDomainRedisTemplateBeanNames(configuration, environment, "test");
        }, DefaultRedisReplicationConfig.class);
    }

    RedisReplicatorConfiguration assertDefaultConfig(ConfigurableApplicationContext context, String... domains) {
        RedisReplicatorConfiguration configuration = get(context);
        assertSame(context.getBean(RedisContext.BEAN_NAME), configuration.getRedisContext());
        assertSame(context.getBean(RedisConfiguration.BEAN_NAME), configuration.getRedisConfiguration());

        assertTrue(configuration.isEnabled());
        assertFalse(configuration.isConsumerEnabled());
        assertEquals(ofList(domains), configuration.getDomains());
        assertEquals(configuration.getDomains(), configuration.getDomains("not-found"));
        return configuration;
    }

    void assertDomainRedisTemplateBeanNames(RedisReplicatorConfiguration configuration, ConfigurableEnvironment environment, String domain) {
        List<String> domainRedisTemplateBeanNames = configuration.getDomainRedisTemplateBeanNames(environment, domain);
        assertEquals(ofList(domain + "RedisTemplate", domain + "StringRedisTemplate"), domainRedisTemplateBeanNames);
    }
}
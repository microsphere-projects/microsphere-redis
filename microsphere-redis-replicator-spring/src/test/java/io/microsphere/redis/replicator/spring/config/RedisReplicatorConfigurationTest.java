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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
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
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static java.util.Collections.emptyList;
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

    @BeforeEach
    void setUp() {
    }

    @Test
    void testConstants() {
        assertEquals("redisReplicatorConfiguration", BEAN_NAME);
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
        }, DefaultConfig.class);
    }

    @Test
    void testOnDomainRedisTemplateBeans() {
        testInSpringContainer((context, environment) -> {
            RedisReplicatorConfiguration configuration = assertDefaultConfig(context, "default", "test", "fixed");

            assertDomainRedisTemplateBeanNames(configuration, environment, "default");
            assertDomainRedisTemplateBeanNames(configuration, environment, "test");
        }, FullConfig.class);
    }

    @Test
    void testOnRedisConfigurationPropertyChangedEvent() {

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

    static class DefaultConfig {

        @Bean(BEAN_NAME)
        public RedisReplicatorConfiguration redisReplicatorConfiguration(ConfigurableApplicationContext context) {
            return new RedisReplicatorConfiguration(context);
        }

        @Bean(RedisContext.BEAN_NAME)
        public RedisContext redisContext() {
            return new RedisContext();
        }

        @Bean(RedisConfiguration.BEAN_NAME)
        public RedisConfiguration redisConfiguration() {
            return new RedisConfiguration();
        }
    }

    @PropertySource(
            "classpath:/META-INF/test-redis-replicator.properties"
    )
    static class FullConfig extends DefaultConfig {

        @Bean
        public RedisTemplate defaultRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return newRedisTemplate(redisConnectionFactory);
        }

        @Bean
        public StringRedisTemplate defaultStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return newStringRedisTemplate(redisConnectionFactory);
        }

        @Bean
        public RedisTemplate testRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return newRedisTemplate(redisConnectionFactory);
        }

        @Bean
        public StringRedisTemplate testStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            return newStringRedisTemplate(redisConnectionFactory);
        }

        RedisTemplate newRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            RedisTemplate redisTemplate = new RedisTemplate();
            redisTemplate.setConnectionFactory(redisConnectionFactory);
            return redisTemplate;
        }

        StringRedisTemplate newStringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            StringRedisTemplate stringRedisTemplate = new StringRedisTemplate();
            stringRedisTemplate.setConnectionFactory(redisConnectionFactory);
            return stringRedisTemplate;
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory("127.0.0.1", 6379);
            redisConnectionFactory.afterPropertiesSet();
            redisConnectionFactory.validateConnection();
            return redisConnectionFactory;
        }
    }
}
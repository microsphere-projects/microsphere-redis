package io.microsphere.redis.replicator.spring.kafka.producer;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.RedisReplicatorInitializer;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * Redis Replicator Kafka producer configuration (loaded by {@link RedisReplicatorInitializer})
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see KafkaRedisReplicatorConfiguration
 * @see RedisReplicatorConfiguration
 * @see RedisReplicatorInitializer
 * @since 1.0.0
 */
public class KafkaProducerRedisReplicatorConfiguration extends KafkaRedisReplicatorConfiguration implements ApplicationContextAware {

    private static final Logger logger = getLogger(KafkaProducerRedisReplicatorConfiguration.class);

    public static final String KAFKA_PRODUCER_PROPERTY_NAME_PREFIX = KAFKA_PROPERTY_NAME_PREFIX + "producer.";

    public static final String DEFAULT_KAFKA_PRODUCER_KEY_PREFIX = "RPE-";

    /**
     * The Spring property name for Kafka Producer key prefix.
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_KAFKA_PRODUCER_KEY_PREFIX,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + "key-prefix";

    /**
     * Key Prefix
     */
    private String keyPrefix;

    private Map<String, Object> producerConfigs;

    private KafkaTemplate<byte[], byte[]> redisReplicatorKafkaTemplate;

    private ApplicationContext context;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        initKeyPrefix();
        initProducerConfigs();
        initRedisReplicatorKafkaTemplate();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        this.destroyProducerFactory();
        this.destroyRedisReplicatorKafkaTemplate();
    }

    /**
     * @return Redis Replicator {@link KafkaTemplate} (For internal use)
     */
    protected KafkaTemplate<byte[], byte[]> getRedisReplicatorKafkaTemplate() {
        return this.redisReplicatorKafkaTemplate;
    }

    protected String getKeyPrefix() {
        return this.keyPrefix;
    }

    private void initKeyPrefix() {
        this.keyPrefix = this.environment.getProperty(KAFKA_PRODUCER_KEY_PREFIX_PROPERTY_NAME, DEFAULT_KAFKA_PRODUCER_KEY_PREFIX);
        logger.trace("The Kafka key prefix : '{}'", this.keyPrefix);
    }

    private void initProducerConfigs() {
        Map<String, Object> producerConfigs = new HashMap<>();
        producerConfigs.put(BOOTSTRAP_SERVERS_CONFIG, this.brokerList);
        // Kafka Common properties
        producerConfigs.putAll(getSubProperties(this.environment, KAFKA_PROPERTY_NAME_PREFIX));
        // Kafka Producer properties
        producerConfigs.putAll(getSubProperties(this.environment, KAFKA_PRODUCER_PROPERTY_NAME_PREFIX));
        this.producerConfigs = producerConfigs;
        logger.trace("The Kafka Producer configs : {}", producerConfigs);
    }

    private void initRedisReplicatorKafkaTemplate() {
        this.redisReplicatorKafkaTemplate = new KafkaTemplate<>(redisReplicatorProducerFactory());
        this.redisReplicatorKafkaTemplate.setObservationEnabled(true);
        this.redisReplicatorKafkaTemplate.setApplicationContext(context);
        this.redisReplicatorKafkaTemplate.afterSingletonsInstantiated();
    }

    private void destroyProducerFactory() {
        KafkaTemplate<byte[], byte[]> kafkaTemplate = this.getRedisReplicatorKafkaTemplate();
        if (kafkaTemplate != null) {
            ProducerFactory producerFactory = kafkaTemplate.getProducerFactory();
            DefaultKafkaProducerFactory defaultKafkaProducerFactory = (DefaultKafkaProducerFactory) producerFactory;
            defaultKafkaProducerFactory.reset();
        }
    }

    private void destroyRedisReplicatorKafkaTemplate() {
        KafkaTemplate<byte[], byte[]> kafkaTemplate = this.getRedisReplicatorKafkaTemplate();
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }

    private ProducerFactory<byte[], byte[]> redisReplicatorProducerFactory() {
        DefaultKafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory<>(getRedisReplicatorProducerConfigs());
        producerFactory.setKeySerializer(new ByteArraySerializer());
        producerFactory.setValueSerializer(new ByteArraySerializer());
        return producerFactory;
    }

    private Map<String, Object> getRedisReplicatorProducerConfigs() {
        return this.producerConfigs;
    }
}
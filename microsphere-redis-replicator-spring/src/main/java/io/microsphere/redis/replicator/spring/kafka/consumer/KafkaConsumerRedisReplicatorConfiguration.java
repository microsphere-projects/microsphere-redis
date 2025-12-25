package io.microsphere.redis.replicator.spring.kafka.consumer;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.RedisReplicatorInitializer;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import io.microsphere.redis.replicator.spring.event.RedisCommandReplicatedEvent;
import io.microsphere.redis.replicator.spring.kafka.KafkaRedisReplicatorConfiguration;
import io.microsphere.redis.spring.config.RedisConfiguration;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchAcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.serializer.Serializers.deserialize;
import static io.microsphere.redis.spring.util.RedisSpringUtils.getBoolean;
import static io.microsphere.reflect.FieldUtils.getStaticFieldValue;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static io.microsphere.util.ArrayUtils.length;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;

/**
 * Kafka Consumer {@link KafkaRedisReplicatorConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see KafkaRedisReplicatorConfiguration
 * @see RedisReplicatorConfiguration
 * @see RedisReplicatorInitializer
 * @since 1.0.0
 */
public class KafkaConsumerRedisReplicatorConfiguration extends KafkaRedisReplicatorConfiguration implements ApplicationEventPublisherAware {

    private static final Logger logger = getLogger(KafkaConsumerRedisReplicatorConfiguration.class);

    private static final String KAFKA_GROUP_ID_CONFIG = getStaticFieldValue(CommonClientConfigs.class, "GROUP_ID_CONFIG");

    static final String DEFAULT_KAFKA_CONSUMER_ENABLED_PROPERTY_VALUE = "true";

    static final String DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_VALUE = "10000";

    static final String DEFAULT_KAFKA_LISTENER_CONCURRENCY_PROPERTY_VALUE = "1";

    static final String GROUP_ID_CONFIG = KAFKA_GROUP_ID_CONFIG == null ? "group.id" : KAFKA_GROUP_ID_CONFIG;

    public static final String KAFKA_CONSUMER_PROPERTY_NAME_PREFIX = KafkaRedisReplicatorConfiguration.KAFKA_PROPERTY_NAME_PREFIX + "consumer.";

    public static final String KAFKA_LISTENER_PROPERTY_NAME_PREFIX = KafkaRedisReplicatorConfiguration.KAFKA_PROPERTY_NAME_PREFIX + "listener.";

    /**
     * The property name for Kafka Consumer enabled status
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_KAFKA_CONSUMER_ENABLED_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_CONSUMER_ENABLED_PROPERTY_NAME = KAFKA_CONSUMER_PROPERTY_NAME_PREFIX + "enabled";

    /**
     * The property name for Kafka Listener poll timeout
     */
    @ConfigurationProperty(
            type = int.class,
            defaultValue = DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME = KAFKA_LISTENER_PROPERTY_NAME_PREFIX + "poll-timeout";

    /**
     * The property name for Kafka Listener concurrency
     */
    @ConfigurationProperty(
            type = int.class,
            defaultValue = DEFAULT_KAFKA_LISTENER_CONCURRENCY_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME = KAFKA_LISTENER_PROPERTY_NAME_PREFIX + "concurrency";

    public static final String KAFKA_CONSUMER_GROUP_ID_PREFIX = "Redis-Replicator-";

    public static final boolean DEFAULT_KAFKA_CONSUMER_ENABLED = parseBoolean(DEFAULT_KAFKA_CONSUMER_ENABLED_PROPERTY_VALUE);

    public static final int DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT = parseInt(DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_VALUE);

    public static final int DEFAULT_KAFKA_LISTENER_CONCURRENCY = parseInt(DEFAULT_KAFKA_LISTENER_CONCURRENCY_PROPERTY_VALUE);

    private volatile Map<String, Object> consumerConfigs;

    /**
     * Listener connection timeout
     */
    int listenerPollTimeOut;

    /**
     * Number of listener connection threads
     */
    int listenerConcurrency;

    private ApplicationEventPublisher applicationEventPublisher;

    public static boolean isKafkaConsumerEnabled(ApplicationContext context) {
        return getBoolean(context.getEnvironment(), KAFKA_CONSUMER_ENABLED_PROPERTY_NAME, DEFAULT_KAFKA_CONSUMER_ENABLED,
                "Kafka Consumer", "enabled");
    }

    @Bean
    public ConcurrentMessageListenerContainer<byte[], byte[]> redisReplicatorConcurrentMessageListenerContainer() {
        String[] topics = getTopics();
        ContainerProperties containerProperties = new ContainerProperties(topics);
        containerProperties.setPollTimeout(this.listenerPollTimeOut);
        ConsumerFactory<byte[], byte[]> redisReplicatorConsumerFactory = redisReplicatorConsumerFactory();
        ConcurrentMessageListenerContainer<byte[], byte[]> listenerContainer = new ConcurrentMessageListenerContainer<>(redisReplicatorConsumerFactory, containerProperties);
        listenerContainer.setConcurrency(getConcurrency(topics));
        listenerContainer.setupMessageListener(batchAcknowledgingMessageListener());
        return listenerContainer;
    }

    private int getConcurrency(String[] topics) {
        int topicCount = topics.length;
        return max(topicCount, listenerConcurrency);
    }

    private BatchAcknowledgingMessageListener<byte[], byte[]> batchAcknowledgingMessageListener() {
        return (data, acknowledgment) -> {
            int size = data.size();
            for (int i = 0; i < size; i++) {
                ConsumerRecord<byte[], byte[]> consumerRecord = data.get(i);
                consumeRecord(consumerRecord);
            }
        };
    }

    void consumeRecord(ConsumerRecord<byte[], byte[]> consumerRecord) {
        byte[] key = consumerRecord.key();
        byte[] value = consumerRecord.value();
        int partition = consumerRecord.partition();
        try {
            RedisCommandEvent redisCommandEvent = deserialize(value, RedisCommandEvent.class);
            RedisCommandReplicatedEvent redisCommandReplicatedEvent = createRedisCommandReplicatedEvent(redisCommandEvent, consumerRecord);
            applicationEventPublisher.publishEvent(redisCommandReplicatedEvent);
            logger.trace("[Redis-Replicator-Kafka-C-S] Topic: {}, key: {}, data size: {} bytes, partition: {}", consumerRecord.topic(), key, length(value), partition);
        } catch (Throwable e) {
            logger.warn("[Redis-Replicator-Kafka-C-F] Topic: {}, key: {}, data size: {} bytes, partition: {}", consumerRecord.topic(), key, length(value), partition, e);
        }
    }

    private RedisCommandReplicatedEvent createRedisCommandReplicatedEvent(RedisCommandEvent redisCommandEvent, ConsumerRecord<byte[], byte[]> consumerRecord) {
        String topic = consumerRecord.topic();
        String domain = getDomain(topic);
        return new RedisCommandReplicatedEvent(redisCommandEvent, domain);
    }

    private ConsumerFactory<byte[], byte[]> redisReplicatorConsumerFactory() {
        DefaultKafkaConsumerFactory<byte[], byte[]> kafkaConsumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerConfigs());
        kafkaConsumerFactory.setKeyDeserializer(new ByteArrayDeserializer());
        kafkaConsumerFactory.setValueDeserializer(new ByteArrayDeserializer());
        return kafkaConsumerFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        initConsumerConfigs();
        initListenerConfigs();
        logger.trace("Redis Replicator Kafka consumer configuration has been initialized.");
    }

    private void initConsumerConfigs() {
        Map<String, Object> consumerConfigs = new HashMap<>();

        // Kafka bootstrap servers
        consumerConfigs.put(BOOTSTRAP_SERVERS_CONFIG, brokerList);
        // Kafka consumer group id
        consumerConfigs.put(GROUP_ID_CONFIG, getConsumerGroupId());

        // Kafka Common properties
        consumerConfigs.putAll(getSubProperties(environment, KAFKA_PROPERTY_NAME_PREFIX));

        // Kafka Consumer properties
        consumerConfigs.putAll(getSubProperties(environment, KAFKA_CONSUMER_PROPERTY_NAME_PREFIX));
        this.consumerConfigs = consumerConfigs;
    }

    private void initListenerConfigs() {
        this.listenerPollTimeOut = environment.getProperty(KAFKA_LISTENER_POLL_TIMEOUT_PROPERTY_NAME, int.class, DEFAULT_KAFKA_LISTENER_POLL_TIMEOUT);
        this.listenerConcurrency = environment.getProperty(KAFKA_LISTENER_CONCURRENCY_PROPERTY_NAME, int.class, DEFAULT_KAFKA_LISTENER_CONCURRENCY);
    }

    private Map<String, Object> getConsumerConfigs() {
        return consumerConfigs;
    }

    private String getConsumerGroupId() {
        RedisConfiguration redisConfiguration = redisReplicatorConfiguration.getRedisConfiguration();
        return KAFKA_CONSUMER_GROUP_ID_PREFIX + redisConfiguration.getApplicationName();
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void destroy() throws Exception {
        super.destroy();
        logger.trace("Redis Replicator Kafka consumer configuration is being destroyed.");
    }
}
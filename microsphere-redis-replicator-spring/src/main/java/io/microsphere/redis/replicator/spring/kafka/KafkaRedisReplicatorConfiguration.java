package io.microsphere.redis.replicator.spring.kafka;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.List;

import static io.microsphere.annotation.ConfigurationProperty.APPLICATION_SOURCE;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration.REDIS_REPLICATOR_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.StringUtils.substringAfter;
import static org.apache.kafka.clients.CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;


/**
 * Kafka {@link RedisReplicatorConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisReplicatorConfiguration
 * @since 1.0.0
 */
public class KafkaRedisReplicatorConfiguration implements EnvironmentAware, InitializingBean, DisposableBean {

    private static final Logger logger = getLogger(KafkaRedisReplicatorConfiguration.class);

    /**
     * The default value of Kafka Broker list
     */
    public static final String DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE = "127.0.0.1:9092";

    /**
     * The Microsphere Property name of Kafka Broker list
     */
    @ConfigurationProperty(
            type = String[].class,
            defaultValue = DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME = "spring.kafka.bootstrap-servers";

    public static final String SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER = "${" + SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME + ":" + DEFAULT_SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_VALUE + "}";

    public static final String KAFKA_PROPERTY_NAME_PREFIX = REDIS_REPLICATOR_PROPERTY_NAME_PREFIX + "kafka.";

    /**
     * The Microsphere Property name of Kafka Broker list
     */
    @ConfigurationProperty(
            type = String[].class,
            defaultValue = SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + BOOTSTRAP_SERVERS_CONFIG;

    public static final String KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER = "${" + KAFKA_BOOTSTRAP_SERVERS_PROPERTY_NAME + ":" + SPRING_KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER + "}";

    public static final String DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE = "redis-replicator-event-topic-";

    /*
     * The Spring Property name of Kafka Topic Prefix
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE,
            source = APPLICATION_SOURCE
    )
    public static final String KAFKA_TOPIC_PREFIX_PROPERTY_NAME = KAFKA_PROPERTY_NAME_PREFIX + "topic-prefix";

    /**
     * Node ip Port address (reusing application configurations)
     */
    protected String brokerList;

    protected String topicPrefix;

    protected ConfigurableEnvironment environment;

    @Autowired
    protected RedisReplicatorConfiguration redisReplicatorConfiguration;

    protected String[] topics;

    public String createTopic(String domain) {
        return this.topicPrefix + domain;
    }

    public String getDomain(String topic) {
        return substringAfter(topic, this.topicPrefix);
    }

    public String[] getTopics() {
        return topics;
    }

    @Override
    public final void setEnvironment(Environment environment) {
        this.environment = asConfigurableEnvironment(environment);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initBrokerList();
        initTopicPrefix();
        initTopics();
    }

    private void initBrokerList() {
        String brokerList = this.environment.resolvePlaceholders(KAFKA_BOOTSTRAP_SERVERS_PROPERTY_PLACEHOLDER);
        logger.trace("Kafka Broker list : {}", brokerList);
        this.brokerList = brokerList;
    }

    private void initTopicPrefix() {
        String topicPrefix = this.environment.getProperty(KAFKA_TOPIC_PREFIX_PROPERTY_NAME, DEFAULT_KAFKA_TOPIC_PREFIX_PROPERTY_VALUE);
        logger.trace("Kafka Topic prefix : {}", topicPrefix);
        this.topicPrefix = topicPrefix;
    }

    private void initTopics() {
        List<String> domains = this.redisReplicatorConfiguration.getDomains();
        int size = domains.size();
        String[] topics = new String[size];
        for (int i = 0; i < size; i++) {
            String domain = domains.get(i);
            String topic = createTopic(domain);
            topics[i] = topic;
        }
        logger.trace("The Kafka topics for Redis Replicator : {}", arrayToString(topics));
        this.topics = topics;
    }

    @Override
    public void destroy() throws Exception {
    }
}
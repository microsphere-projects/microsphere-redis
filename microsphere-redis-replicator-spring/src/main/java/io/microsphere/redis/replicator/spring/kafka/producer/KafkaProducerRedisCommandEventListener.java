package io.microsphere.redis.replicator.spring.kafka.producer;

import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.redis.spring.serializer.Serializers.serialize;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * {@link ApplicationListener} listens to {@link RedisCommandEvent} implementation -
 * Transfers {@link RedisCommandEvent} objects using Kafka messages
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class KafkaProducerRedisCommandEventListener implements ApplicationListener<RedisCommandEvent>,
        SmartInitializingSingleton, ApplicationContextAware, DisposableBean {

    private static final Logger logger = getLogger(KafkaProducerRedisCommandEventListener.class);

    private KafkaTemplate<byte[], byte[]> redisReplicatorKafkaTemplate;

    private ApplicationContext context;

    private RedisReplicatorConfiguration redisReplicatorConfiguration;

    private KafkaProducerRedisReplicatorConfiguration kafkaProducerRedisReplicatorConfiguration;

    private ExecutorService executor;

    @Override
    public void onApplicationEvent(RedisCommandEvent event) {
        onRedisCommandEvent(event);
    }

    private void initRedisReplicatorConfiguration(ApplicationContext context) {
        this.redisReplicatorConfiguration = RedisReplicatorConfiguration.get(context);
    }

    private void initRedisReplicatorKafkaProducerConfiguration(ApplicationContext context) {
        this.kafkaProducerRedisReplicatorConfiguration = context.getBean(KafkaProducerRedisReplicatorConfiguration.class);
    }

    private void initRedisReplicatorKafkaTemplate(KafkaProducerRedisReplicatorConfiguration kafkaProducerRedisReplicatorConfiguration) {
        this.redisReplicatorKafkaTemplate = kafkaProducerRedisReplicatorConfiguration.getRedisReplicatorKafkaTemplate();
    }

    private void initExecutor() {
        List<String> domains = this.redisReplicatorConfiguration.getDomains();
        int size = domains.size();
        this.executor = newFixedThreadPool(size, new CustomizableThreadFactory(domains.toString()));
    }

    private void onRedisCommandEvent(RedisCommandEvent event) {
        String beanName = event.getSourceBeanName();
        List<String> domains = this.redisReplicatorConfiguration.getDomains(beanName);
        for (String domain : domains) {
            executor.execute(() -> sendRedisReplicatorKafkaMessage(domain, event));
        }
    }

    private void sendRedisReplicatorKafkaMessage(String domain, RedisCommandEvent event) {
        String topic = this.kafkaProducerRedisReplicatorConfiguration.createTopic(domain);
        byte[] key = generateKafkaKey(event);
        byte[] value = serialize(event);
        Integer partition = calcPartition(event);
        // Use a timestamp of the event
        long timestamp = event.getTimestamp();

        CompletableFuture<SendResult<byte[], byte[]>> future = this.redisReplicatorKafkaTemplate.send(topic, partition, timestamp, key, value);

        future.whenComplete(this::onComplete);
    }

    void onComplete(SendResult<byte[], byte[]> result, Throwable failure) {
        if (failure == null) {
            logger.trace("[Redis-Replicator-Kafka-P-S] Kafka message sending operation succeeds: {}", result);
        } else {
            logger.warn("[Redis-Replicator-Kafka-P-F] Kafka message sending operation failed: {}", result, failure);
        }
    }

    /**
     * Generate Kafka message keys, using Redis Key as part of Kafka
     *
     * @param event {@link RedisCommandEvent}
     * @return Generate Kafka message keys
     */
    private byte[] generateKafkaKey(RedisCommandEvent event) {
        // Almost all RedisCommands interface methods take the first argument as Key
        return (byte[]) event.getArg(0);
    }

    private Integer calcPartition(RedisCommandEvent event) {
        // TODO Future computing partition
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = context;
    }

    @Override
    public void afterSingletonsInstantiated() {
        initRedisReplicatorConfiguration(this.context);
        initRedisReplicatorKafkaProducerConfiguration(this.context);
        initRedisReplicatorKafkaTemplate(this.kafkaProducerRedisReplicatorConfiguration);
        initExecutor();
    }

    @Override
    public void destroy() throws Exception {
        executor.shutdown();
    }
}
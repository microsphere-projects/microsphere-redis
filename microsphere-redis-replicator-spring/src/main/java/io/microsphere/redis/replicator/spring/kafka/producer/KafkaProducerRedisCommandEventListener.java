package io.microsphere.redis.replicator.spring.kafka.producer;

import io.microsphere.logging.Logger;
import io.microsphere.redis.replicator.spring.config.RedisReplicatorConfiguration;
import io.microsphere.redis.spring.event.RedisCommandEvent;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.SmartApplicationListener;
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
public class KafkaProducerRedisCommandEventListener implements SmartApplicationListener, DisposableBean {

    private final Logger logger = getLogger(getClass());

    private KafkaTemplate<byte[], byte[]> redisReplicatorKafkaTemplate;

    private ApplicationContext context;

    private RedisReplicatorConfiguration redisReplicatorConfiguration;

    private KafkaProducerRedisReplicatorConfiguration kafkaProducerRedisReplicatorConfiguration;

    private ExecutorService executor;

    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return RedisCommandEvent.class.equals(eventType) ||
                ContextRefreshedEvent.class.equals(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof ContextRefreshedEvent) {
            onContextRefreshedEvent((ContextRefreshedEvent) event);
        } else if (event instanceof RedisCommandEvent) {
            onRedisCommandEvent((RedisCommandEvent) event);
        }
    }

    private void onContextRefreshedEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initApplicationContext(context);
        initRedisReplicatorConfiguration(context);
        initRedisReplicatorKafkaProducerConfiguration(context);
        initRedisReplicatorKafkaTemplate(kafkaProducerRedisReplicatorConfiguration);
        initExecutor();
    }

    private void initApplicationContext(ApplicationContext context) {
        this.context = context;
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
        List<String> domains = redisReplicatorConfiguration.getDomains();
        int size = domains.size();
        this.executor = newFixedThreadPool(size, new CustomizableThreadFactory(domains.toString()));
    }

    private void onRedisCommandEvent(RedisCommandEvent event) {
        try {
            String beanName = event.getSourceBeanName();
            List<String> domains = redisReplicatorConfiguration.getDomains(beanName);
            for (String domain : domains) {
                executor.execute(() -> sendRedisReplicatorKafkaMessage(domain, event));
            }
        } catch (Throwable e) {
            logger.warn("[Redis-Replicator-Kafka-P-F] Failed to perform Redis Replicator Kafka message sending operation.", e);
        }
    }

    private void sendRedisReplicatorKafkaMessage(String domain, RedisCommandEvent event) {
        String topic = kafkaProducerRedisReplicatorConfiguration.createTopic(domain);
        // Almost all RedisCommands interface methods take the first argument as Key
        byte[] key = generateKafkaKey(event);
        byte[] value = serialize(event);
        Integer partition = calcPartition(event);
        // Use a timestamp of the event
        long timestamp = event.getTimestamp();

        CompletableFuture<SendResult<byte[], byte[]>> future = redisReplicatorKafkaTemplate.send(topic, partition, timestamp, key, value);

        future.whenComplete((result, failure) -> {
            if (failure == null) {
                logger.debug("[Redis-Replicator-Kafka-P-S] Kafka message sending operation succeeds. Topic: '{}', key: '{}', data size: {} bytes, event: {}",
                        topic, key, value.length, event);
            } else {
                logger.warn("[Redis-Replicator-Kafka-P-F] Kafka message sending operation failed. Topic: '{}', key: '{}', data size: {} bytes",
                        topic, key, value.length, failure);
            }
        });
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
    public void destroy() throws Exception {
        executor.shutdown();
    }
}

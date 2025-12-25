package io.microsphere.redis.spring.connection.dynamic.web;

import io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

import static io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory.clearTarget;

/**
 * {@link DynamicRedisConnectionFactory} {@link ThreadLocal} state cleaner
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DynamicRedisConnectionFactory
 * @see ServletRequestListener
 * @since 1.0.0
 */
@WebListener
public class DynamicRedisConnectionFactoryCleanerListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // Clear ThreadLocal
        clearTarget();
    }
}
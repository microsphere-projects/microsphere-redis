package io.microsphere.redis.spring.connection.dynamic.web;

import io.microsphere.redis.spring.connection.dynamic.DynamicRedisConnectionFactory;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

/**
 * {@link DynamicRedisConnectionFactory} State cleaner
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@WebListener
public class DynamicRedisConnectionFactoryCleanerListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // 清除 ThreadLocal
        DynamicRedisConnectionFactory.clearTarget();
    }
}

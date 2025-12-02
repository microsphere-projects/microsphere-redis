package io.microsphere.redis.spring.beans;

import io.microsphere.lang.Wrapper;
import io.microsphere.redis.spring.context.RedisContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Collection;
import java.util.Set;

import static io.microsphere.collection.SetUtils.ofSet;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;

/**
 * {@link BeanPostProcessor} implements Wrapper {@link RedisTemplate} and {@link StringRedisTemplate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisTemplateWrapper
 * @see StringRedisTemplateWrapper
 * @see BeanPostProcessor
 * @since 1.0.0
 */
public class RedisTemplateWrapperBeanPostProcessor implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    public static final String BEAN_NAME = "redisTemplateWrapperBeanPostProcessor";

    private ConfigurableApplicationContext context;

    private RedisContext redisContext;

    private final Set<String> wrappedRedisTemplateBeanNames;

    private WrapperProcessors wrapperProcessors;

    public RedisTemplateWrapperBeanPostProcessor(Collection<String> wrappedRedisTemplateBeanNames) {
        this.wrappedRedisTemplateBeanNames = ofSet(wrappedRedisTemplateBeanNames);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (wrappedRedisTemplateBeanNames.contains(beanName)) {
            Class<?> beanClass = ultimateTargetClass(bean);
            if (StringRedisTemplate.class.equals(beanClass)) {
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) bean;
                return process(new StringRedisTemplateWrapper(beanName, stringRedisTemplate, redisContext));
            } else if (RedisTemplate.class.equals(beanClass)) {
                RedisTemplate redisTemplate = (RedisTemplate) bean;
                return process(new RedisTemplateWrapper(beanName, redisTemplate, redisContext));
            }
            // TODO Support for more custom RedisTemplate types
        }
        return bean;
    }

    private <W extends Wrapper> W process(W wrapper) {
        return wrapperProcessors.process(wrapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = asConfigurableApplicationContext(context);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.redisContext = RedisContext.get(context);
        this.wrapperProcessors = context.getBean(WrapperProcessors.BEAN_NAME, WrapperProcessors.class);
    }

    public Set<String> getWrappedRedisTemplateBeanNames() {
        return wrappedRedisTemplateBeanNames;
    }
}

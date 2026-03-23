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
import static io.microsphere.redis.spring.context.RedisContext.get;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;

/**
 * {@link BeanPostProcessor} that replaces specific {@link RedisTemplate} and
 * {@link StringRedisTemplate} beans (identified by name) with intercepting wrapper
 * counterparts ({@link RedisTemplateWrapper} / {@link StringRedisTemplateWrapper}).
 * After creating the wrapper it applies all registered {@link WrapperProcessors}.
 *
 * <p>Registered by {@link io.microsphere.redis.spring.annotation.RedisInterceptorBeanDefinitionRegistrar}
 * when at least one template bean name is supplied to
 * {@link io.microsphere.redis.spring.annotation.EnableRedisInterceptor#wrapRedisTemplates()}.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 *   // Applied automatically by @EnableRedisInterceptor; manual construction for tests:
 *   RedisTemplateWrapperBeanPostProcessor processor =
 *           new RedisTemplateWrapperBeanPostProcessor(Set.of("redisTemplate", "stringRedisTemplate"));
 *
 *   Set<String> wrappedNames = processor.getWrappedRedisTemplateBeanNames();
 *   // wrappedNames == {"redisTemplate", "stringRedisTemplate"}
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisTemplateWrapper
 * @see StringRedisTemplateWrapper
 * @see BeanPostProcessor
 * @since 1.0.0
 */
public class RedisTemplateWrapperBeanPostProcessor implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    public static final String BEAN_NAME = "microsphere:redisTemplateWrapperBeanPostProcessor";

    private ConfigurableApplicationContext context;

    private RedisContext redisContext;

    private final Set<String> wrappedRedisTemplateBeanNames;

    private WrapperProcessors wrapperProcessors;

    /**
     * Creates a processor that will wrap only the {@link RedisTemplate} beans whose names
     * are contained in the given collection.
     *
     * @param wrappedRedisTemplateBeanNames the Spring bean names of the templates to intercept
     */
    public RedisTemplateWrapperBeanPostProcessor(Collection<String> wrappedRedisTemplateBeanNames) {
        this.wrappedRedisTemplateBeanNames = ofSet(wrappedRedisTemplateBeanNames);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (this.wrappedRedisTemplateBeanNames.contains(beanName)) {
            Class<?> beanClass = ultimateTargetClass(bean);
            if (StringRedisTemplate.class.equals(beanClass)) {
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) bean;
                return process(new StringRedisTemplateWrapper(beanName, stringRedisTemplate, this.redisContext));
            } else if (RedisTemplate.class.equals(beanClass)) {
                RedisTemplate redisTemplate = (RedisTemplate) bean;
                return process(new RedisTemplateWrapper(beanName, redisTemplate, this.redisContext));
            }
            // TODO Support for more custom RedisTemplate types
        }
        return bean;
    }

    private <W extends Wrapper> W process(W wrapper) {
        return this.wrapperProcessors.process(wrapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.context = asConfigurableApplicationContext(context);
    }

    @Override
    public void afterPropertiesSet() {
        this.redisContext = get(this.context);
        this.wrapperProcessors = WrapperProcessors.get(this.context);
    }

    /**
     * Returns the unmodifiable set of {@link RedisTemplate} bean names that this processor wraps.
     *
     * @return the set of wrapped template bean names; never {@code null}
     */
    public Set<String> getWrappedRedisTemplateBeanNames() {
        return this.wrappedRedisTemplateBeanNames;
    }
}
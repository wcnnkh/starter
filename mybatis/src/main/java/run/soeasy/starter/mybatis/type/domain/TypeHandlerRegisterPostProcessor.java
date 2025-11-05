package run.soeasy.starter.mybatis.type.domain;

import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import lombok.extern.slf4j.Slf4j;

/**
 * 注册默认 TypeHandler 的 BeanPostProcessor 拦截 MyBatis 的 Configuration，添加自定义
 * TypeHandler
 */
@Slf4j
public class TypeHandlerRegisterPostProcessor implements BeanPostProcessor {

	// 要注册的默认 TypeHandler（可添加多个）
	private final Class<?>[] defaultTypeHandlers = { BoxTypeHandler.class, MeasuredValueTypeHandler.class,
			OptionTypeHandler.class };

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		// 拦截 MyBatis 的 Configuration 或 MyBatis-Plus 的 MybatisConfiguration
		if (bean instanceof Configuration) {
			Configuration mybatisConfig = (Configuration) bean;
			registerDefaultTypeHandlers(mybatisConfig);
		}
		return bean;
	}

	/**
	 * 注册默认 TypeHandler 到 MyBatis 配置
	 */
	// 优化后的 registerDefaultTypeHandlers 方法
	private void registerDefaultTypeHandlers(Configuration configuration) {
		for (Class<?> typeHandlerClass : defaultTypeHandlers) {
			try {
				TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
				// 判断是否已注册（通过 TypeHandler 类名）
				boolean isRegistered = registry.getTypeHandlers().stream()
						.anyMatch(handler -> typeHandlerClass.isInstance(handler));

				if (!isRegistered) {
					registry.register(typeHandlerClass);
					log.info("默认 TypeHandler 注册成功：" + typeHandlerClass.getName());
				} else {
					log.info("TypeHandler 已存在，跳过注册：" + typeHandlerClass.getName());
				}
			} catch (Exception e) {
				throw new RuntimeException("注册默认 TypeHandler 失败：" + typeHandlerClass.getName(), e);
			}
		}
	}
}
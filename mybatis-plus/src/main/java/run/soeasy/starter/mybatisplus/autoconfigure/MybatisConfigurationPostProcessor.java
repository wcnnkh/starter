package run.soeasy.starter.mybatisplus.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.github.pagehelper.PageInterceptor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis-Plus 集成 PageHelper
 */
@Configuration
@ConditionalOnBean(value = { PageInterceptor.class })
@RequiredArgsConstructor
@Slf4j
public class MybatisConfigurationPostProcessor implements BeanPostProcessor {
	@NonNull
	private final PageInterceptor pageInterceptor;

	@Bean
	@ConditionalOnMissingBean(MybatisConfiguration.class)
	public MybatisConfiguration mybatisConfiguration() {
		MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
		mybatisConfiguration.addInterceptor(pageInterceptor);
		return mybatisConfiguration;
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
		if (bean instanceof MybatisConfiguration) {
			MybatisConfiguration mybatisConfiguration = (MybatisConfiguration) bean;
			// 先判断拦截器是否已存在，避免重复添加
			if (!containsPageInterceptor(mybatisConfiguration)) {
				mybatisConfiguration.addInterceptor(pageInterceptor);
				// 可选：打印日志，方便调试
				log.info("PageInterceptor 已成功添加到 MybatisConfiguration 拦截器链");
			}
		}
		return bean;
	}

	// 辅助方法：判断拦截器是否已存在（避免重复添加导致分页失效或报错）
	private boolean containsPageInterceptor(MybatisConfiguration configuration) {
		return configuration.getInterceptors().stream().anyMatch(interceptor -> interceptor instanceof PageInterceptor);
	}
}
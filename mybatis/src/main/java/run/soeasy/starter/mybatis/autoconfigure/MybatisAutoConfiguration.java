package run.soeasy.starter.mybatis.autoconfigure;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.pagehelper.PageInterceptor;

import lombok.NonNull;
import run.soeasy.starter.mybatis.entity.MybatisEntityFiller;
import run.soeasy.starter.mybatis.pagehelper.PageInterceptorRegisterPostProcessor;
import run.soeasy.starter.mybatis.plugin.FillMybatisEntityInterceptor;
import run.soeasy.starter.mybatis.type.domain.TypeHandlerRegisterPostProcessor;

@Configuration
public class MybatisAutoConfiguration {
	@Bean(name = "pageHelperProperties")
	@ConditionalOnMissingBean(name = "pageHelperProperties")
	@ConfigurationProperties(prefix = "pagehelper")
	public Properties pageHelperProperties() {
		return new Properties();
	}

	@Bean
	@ConditionalOnBean(name = "pageHelperProperties")
	@ConditionalOnMissingBean(PageInterceptor.class)
	public PageInterceptor pageHelperInterceptor() {
		PageInterceptor pageInterceptor = new PageInterceptor();
		pageInterceptor.setProperties(pageHelperProperties());
		return pageInterceptor;
	}

	@Bean
	public TypeHandlerRegisterPostProcessor typeHandlerRegisterPostProcessor() {
		return new TypeHandlerRegisterPostProcessor();
	}

	@Bean
	public PageInterceptorRegisterPostProcessor pageInterceptorRegisterPostProcessor() {
		return new PageInterceptorRegisterPostProcessor();
	}

	@Bean
	@ConditionalOnBean(MybatisEntityFiller.class)
	public FillMybatisEntityInterceptor fillMybatisEntityInterceptor(
			@NonNull MybatisEntityFiller fillMybatisEntityFactory) {
		return new FillMybatisEntityInterceptor(fillMybatisEntityFactory);
	}
}

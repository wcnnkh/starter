package run.soeasy.starter.mybatis.autoconfigure;

import java.util.Properties;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.pagehelper.PageInterceptor;

@Configuration
public class PageInterceptorAutoConfiguration {

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
}
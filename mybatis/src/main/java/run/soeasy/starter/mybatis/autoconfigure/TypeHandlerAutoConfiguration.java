package run.soeasy.starter.mybatis.autoconfigure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import run.soeasy.starter.mybatis.type.domain.TypeHandlerRegisterPostProcessor;

@Configuration
public class TypeHandlerAutoConfiguration {
	@Bean
	public TypeHandlerRegisterPostProcessor typeHandlerRegisterPostProcessor() {
		return new TypeHandlerRegisterPostProcessor();
	}
}

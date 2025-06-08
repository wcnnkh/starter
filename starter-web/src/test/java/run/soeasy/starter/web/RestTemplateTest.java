package run.soeasy.starter.web;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RestTemplateTest {
	@Test
	public void baidu() {
		String host = "https://baidu.com";
		RestTemplate restTemplate = new RestTemplate();
		try {
			String message = restTemplate.getForObject(host, String.class);
			log.info(message);
		} catch (Exception e) {
			log.error(host, e);
		}
	}
}

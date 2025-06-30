package run.soeasy.starter.commons.web;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import run.soeasy.framework.json.JsonConverter;
import run.soeasy.framework.messaging.convert.support.QueryStringFormat;
import run.soeasy.starter.commons.jackson.JsonFormat;

/**
 * 增强型 HTTP 客户端，扩展 Spring RestTemplate 功能
 * 
 * <p>
 * 作为 RESTful API 调用的核心组件，提供：
 * <ul>
 * <li>智能参数处理：自动转换 GET 请求参数为查询字符串</li>
 * <li>统一日志追踪：通过 UUID 关联全链路请求/响应日志</li>
 * <li>灵活 JSON 处理：支持自定义序列化/反序列化策略</li>
 * <li>安全通信支持：SSL/TLS 证书配置与管理</li>
 * <li>类型安全响应：基于泛型的响应体自动转换</li>
 * </ul>
 * 
 * <p>
 * 典型使用场景：
 * 
 * <pre>
 * // 创建客户端实例
 * HttpTemplate httpClient = new HttpTemplate();
 * 
 * // 配置 SSL 证书（可选）
 * httpClient.loadKeyMaterial(new ClassPathResource("client-cert.p12"), "storePassword", "keyPassword");
 * 
 * // 执行 GET 请求
 * HttpResponseEntity&lt;User&gt; response = httpClient.getJson("/api/users/{id}", Map.of("id", 123), User.class);
 * User user = response.getBody();
 * 
 * // 执行 POST 请求
 * HttpResponseEntity&lt;LoginResponse&gt; loginResponse = httpClient.postJson("/api/login", null,
 * 		new LoginRequest("username", "password"), LoginResponse.class);
 * </pre>
 * 
 * @author soeasy.run
 * @see RestTemplate
 * @see HttpClientExecutor
 * @see HttpResponseEntity
 */
@Getter
@Setter
public class HttpTemplate extends RestTemplate implements HttpClientExecutor {
	/** JSON 转换器，默认使用框架提供的默认实现 */
	@NonNull
	private JsonConverter jsonConverter = JsonFormat.DEFAULT;

	/** URL 查询参数格式化器，默认使用 UTF-8 编码 */
	@NonNull
	private QueryStringFormat queryStringFormat = QueryStringFormat.getFormat(StandardCharsets.UTF_8);

	/** JSON 请求的媒体类型，默认使用 UTF-8 编码的 APPLICATION_JSON */
	@NonNull
	private MediaType jsonMediaType = DEFAULT_JSON_MEDIA_TYPE;

	/**
	 * 执行 HTTP 请求并返回类型安全的响应实体
	 * 
	 * <p>
	 * 该方法通过 Spring 的 ParameterizedTypeReference 实现泛型类型安全， 支持复杂响应类型（如
	 * List&lt;User&gt;）的正确解析。
	 * 
	 * @param <S>           请求体类型
	 * @param <T>           响应体类型
	 * @param requestEntity 完整的请求实体
	 * @param responseType  响应类型的反射表示
	 * @return 类型安全的响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	@Override
	public <S, T> ResponseEntity<T> executeRequest(RequestEntity<S> requestEntity, Type responseType)
			throws HttpClientException {
		return exchange(requestEntity, new ParameterizedTypeReference<T>() {
			@Override
			public Type getType() {
				return responseType;
			}
		});
	}

	/**
	 * 设置 SSL 上下文，启用 HTTPS 通信
	 * 
	 * <p>
	 * 该方法会创建新的 {@link SimpleClientHttpsRequestFactory}， 并将其设置为 RestTemplate
	 * 的请求工厂，从而应用 SSL 配置。
	 * 
	 * @param sslContext 要设置的 SSL 上下文，可为 null（重置为默认配置）
	 * @throws HttpClientException 当 SSL 配置失败时抛出
	 */
	@Override
	public void setSSLContext(SSLContext sslContext) throws HttpClientException {
		SimpleClientHttpsRequestFactory httpsRequestFactory = new SimpleClientHttpsRequestFactory();
		httpsRequestFactory.setSslSocketFactory(sslContext == null ? null : sslContext.getSocketFactory());
		setRequestFactory(httpsRequestFactory);
	}
}
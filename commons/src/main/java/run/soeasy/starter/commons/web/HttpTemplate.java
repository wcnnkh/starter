package run.soeasy.starter.commons.web;

import java.lang.reflect.Type;

import javax.net.ssl.SSLContext;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.starter.commons.jackson.JsonFormat;
import run.soeasy.starter.commons.jackson.XmlFormat;

/**
 * 增强型HTTP客户端实现类，继承Spring RestTemplate并实现HttpClientExecutor接口
 * 
 * <p>该类整合了Spring RestTemplate的基础能力与自定义HTTP客户端契约，提供：
 * <ul>
 *   <li>类型安全的HTTP请求/响应处理</li>
 *   <li>内置JSON/XML数据格式转换支持</li>
 *   <li>灵活的SSL/TLS配置能力</li>
 *   <li>与HttpClientExecutor接口定义的标准化操作兼容</li>
 * </ul>
 * 
 * <p>典型应用场景包括微服务间通信、第三方API调用等需要可靠HTTP客户端支持的场景。
 * 通过泛型类型引用实现复杂响应类型的自动转换，无需手动处理JSON/XML序列化逻辑。
 * 
 * @see RestTemplate
 * @see HttpClientExecutor
 * @see ParameterizedTypeReference
 */
@Getter
@Setter
public class HttpTemplate extends RestTemplate implements HttpClientExecutor {
	/** 
	 * JSON 转换器，默认使用框架提供的默认实现（JsonFormat.DEFAULT）
	 * <p>用于请求体JSON序列化和响应体JSON反序列化
	 */
	@NonNull
	private Converter jsonConverter = JsonFormat.DEFAULT;

	/** 
	 * XML 转换器，默认使用框架提供的默认实现（XmlFormat.DEFAULT）
	 * <p>用于请求体XML序列化和响应体XML反序列化
	 */
	@NonNull
	private Converter xmlConverter = XmlFormat.DEFAULT;

	/** 
	 * JSON 请求的媒体类型，默认值为 application/json;charset=UTF-8
	 * <p>用于设置JSON格式请求的Content-Type头信息
	 */
	@NonNull
	private MediaType jsonMediaType = DEFAULT_JSON_MEDIA_TYPE;

	/** 
	 * XML 请求的媒体类型，默认值为 application/xml;charset=UTF-8
	 * <p>用于设置XML格式请求的Content-Type头信息
	 */
	private MediaType xmlMediaType = DEFAULT_XML_MEDIA_TYPE;

	/**
	 * 执行HTTP请求并返回类型安全的响应实体
	 * 
	 * <p>核心实现方法，通过Spring的ParameterizedTypeReference保留泛型类型信息，
	 * 解决了Java泛型类型擦除导致的复杂类型转换问题。支持如List&lt;T&gt;、Map&lt;K,V&gt;等
	 * 复杂响应类型的正确解析。
	 * 
	 * @param <S> 请求体类型
	 * @param <T> 响应体类型
	 * @param requestEntity 封装完整请求信息的实体对象，包含URL、方法、头信息和请求体
	 * @param responseType 响应数据的目标类型（支持泛型）
	 * @return 包含响应状态码、响应头和转换后响应体的ResponseEntity对象
	 * @throws HttpClientException 当请求执行失败（如网络异常、状态码错误等）时抛出
	 */
	@Override
	public <S, T> ResponseEntity<T> doRequest(RequestEntity<S> requestEntity, Type responseType)
			throws HttpClientException {
		return exchange(requestEntity, new ParameterizedTypeReference<T>() {
			@Override
			public Type getType() {
				return responseType;
			}
		});
	}

	/**
	 * 设置SSL上下文，配置HTTPS通信的安全参数
	 * 
	 * <p>该方法通过创建自定义的{@link SimpleClientHttpsRequestFactory}替换默认请求工厂，
	 * 实现SSL/TLS安全通信配置。适用于需要客户端证书认证或自定义证书验证策略的场景。
	 * 当sslContext为null时，将使用默认的SSL配置。
	 * 
	 * @param sslContext 要应用的SSL上下文，包含加密套件、证书等安全信息
	 * @throws HttpClientException 当SSL上下文配置失败或请求工厂创建异常时抛出
	 */
	@Override
	public void setSSLContext(SSLContext sslContext) throws HttpClientException {
		SimpleClientHttpsRequestFactory httpsRequestFactory = new SimpleClientHttpsRequestFactory();
		httpsRequestFactory.setSslSocketFactory(sslContext == null ? null : sslContext.getSocketFactory());
		setRequestFactory(httpsRequestFactory);
	}
}
    
package run.soeasy.starter.commons.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.messaging.convert.support.QueryStringFormat;

/**
 * 函数式接口：定义HTTP请求执行的核心抽象，继承媒体类型转换器工厂以支持数据格式转换。
 * 
 * <p>
 * 作为HTTP客户端的基础契约，整合了请求处理、数据转换和安全配置能力，主要提供：
 * <ul>
 * <li>标准化请求-响应模型：基于Spring HTTP组件（RequestEntity/ResponseEntity）</li>
 * <li>灵活的数据转换：通过媒体类型（MediaType）自动匹配转换器，支持多种数据格式</li>
 * <li>参数与请求体处理：自动转换查询参数和请求体，简化API调用逻辑</li>
 * <li>安全通信支持：SSL/TLS证书加载与配置，保障HTTPS通信安全</li>
 * <li>默认实现便捷方法：如{@link #get(String, Object, HttpMethod, MediaType, Type, HttpHeaders)}和{@link #post(String, Object, HttpMethod, MediaType, Type, HttpHeaders, MediaType, Object)}，支持请求头合并，减少重复代码</li>
 * </ul>
 * 
 * <p>
 * 实现类应专注于底层通信细节（如连接池管理、超时控制、重试机制等），本接口则定义通用操作语义，
 * 与Spring生态无缝集成，兼容RestTemplate等组件的扩展。
 * 
 * @see MediaTypeConverterFactory
 * @see org.springframework.http.RequestEntity
 * @see org.springframework.http.ResponseEntity
 * @see HttpResponseEntity
 */
@FunctionalInterface
public interface HttpClientExecutor extends MediaTypeConverterFactory {

	/**
	 * 根据媒体类型获取对应的转换器，从系统转换器注册表中查找。
	 * <p>
	 * 实现{@link MediaTypeConverterFactory}接口的核心方法，用于根据请求/响应的Content-Type
	 * 自动匹配合适的转换器（如JSON/XML转换器），支持数据的序列化与反序列化。
	 * 
	 * @param mediaType 媒体类型（如application/json、application/xml），不可为null
	 * @return 匹配的转换器，若未找到则可能返回null（取决于注册表实现）
	 */
	@Override
	default Converter getConverter(@NonNull MediaType mediaType) {
		return MediaTypeConverterRegistry.system().getConverter(mediaType);
	}

	/**
	 * 执行HTTP请求的核心方法，是函数式接口的唯一抽象方法。
	 * <p>
	 * 实现类需提供具体的HTTP通信逻辑，包括请求发送、响应接收、异常处理等，
	 * 并根据指定的响应类型{@code responseType}完成响应体的类型转换。
	 * 
	 * @param <S>           请求体的类型
	 * @param <T>           响应体的类型
	 * @param requestEntity 封装完整请求信息的对象，包含URI、HTTP方法、请求头、请求体等，不可为null
	 * @param responseType  响应体的目标类型（支持泛型，如List&amp;lt;User&amp;gt;.class），不可为null
	 * @return 包含响应状态码、响应头和转换后响应体的ResponseEntity对象
	 * @throws HttpClientException 当请求执行失败时抛出（如网络异常、转换失败、状态码错误等）
	 */
	<S, T> ResponseEntity<T> doRequest(@NonNull RequestEntity<S> requestEntity, @NonNull Type responseType)
			throws HttpClientException;

	/**
	 * 通用HTTP请求执行方法，支持自定义转换器和请求体类型，简化参数与请求体处理。
	 * <p>
	 * 提供一站式请求构建能力，自动处理：
	 * <ol>
	 * <li>查询参数转换：将对象/Map转换为URL查询字符串（支持自定义字符集）</li>
	 * <li>请求体序列化：根据指定转换器或Content-Type自动序列化请求体</li>
	 * <li>响应转换器关联：为返回的HttpResponseEntity绑定合适的转换器，便于后续处理</li>
	 * </ol>
	 * 
	 * @param <S>          请求体目标类型泛型（通常为String，用于序列化后的请求体）
	 * @param <T>          响应体类型泛型
	 * @param uri          请求的URI地址（支持占位符，如/api/users/{id}），不可为null
	 * @param httpMethod   HTTP请求方法（如GET/POST/PUT/DELETE），不可为null
	 * @param responseType 响应体的目标类型，不可为null
	 * @param params       URL查询参数（支持字符串、Map或普通对象，null表示无参数）
	 * @param httpHeaders  请求头信息（null表示使用默认头信息）
	 * @param body         请求体内容（null表示无请求体）
	 * @param bodyClass    请求体序列化后的目标类型（通常为String）
	 * @param converter    用于请求体序列化的转换器（null则根据Content-Type自动获取）
	 * @return 包含响应信息的HttpResponseEntity对象，已关联合适的转换器
	 * @throws HttpClientException 当参数转换失败、请求执行异常或响应处理错误时抛出
	 */
	default <S, T> HttpResponseEntity<T> doRequest(@NonNull String uri, @NonNull HttpMethod httpMethod,
			@NonNull Type responseType, Object params, HttpHeaders httpHeaders, Object body, Class<S> bodyClass,
			Converter converter) throws HttpClientException {
		MediaType contentType = httpHeaders == null ? null : httpHeaders.getContentType();
		if (params != null) {
			String queryString;
			if (params instanceof String) {
				// 直接使用字符串作为查询参数（适用于已手动拼接好的场景）
				queryString = (String) params;
			} else {
				// 根据请求头字符集获取对应的查询字符串转换器
				Converter queryStringConverter;
				if (contentType != null && contentType.getCharset() != null) {
					queryStringConverter = QueryStringFormat.getFormat(contentType.getCharset());
				} else {
					queryStringConverter = QueryStringFormat.getFormat(StandardCharsets.UTF_8);
				}

				// 将非Map类型参数转换为Map（优先使用指定转换器，其次使用内容类型对应的转换器）
				if (!(params instanceof Map)) {
					if (converter != null && converter.canConvert(params.getClass(), Map.class)) {
						params = converter.convert(params, Map.class);
					} else if (contentType != null) {
						Converter paramsConverter = getConverter(contentType);
						if (paramsConverter != null && paramsConverter.canConvert(params.getClass(), Map.class)) {
							params = paramsConverter.convert(params, Map.class);
						}
					}
				}
				queryString = queryStringConverter.convert(params, String.class);
			}
			// 拼接查询字符串到URI
			uri = UriComponentsBuilder.fromUriString(uri).query(queryString).build().toUriString();
		}

		// 使用指定转换器或内容类型对应的转换器序列化请求体
		if (body != null && bodyClass != null && !bodyClass.isInstance(body)) {
			if (converter != null && converter.canConvert(body.getClass(), bodyClass)) {
				body = converter.convert(body, bodyClass);
			} else if (contentType != null) {
				Converter bodyConverter = getConverter(contentType);
				if (bodyConverter != null && bodyConverter.canConvert(body.getClass(), bodyClass)) {
					body = bodyConverter.convert(body, bodyClass);
				}
			}
		}

		// 构建请求实体并执行请求
		RequestEntity<Object> requestEntity = RequestEntity.method(httpMethod, URI.create(uri)).headers(httpHeaders)
				.body(body);
		ResponseEntity<T> responseEntity = doRequest(requestEntity, responseType);
		HttpResponseEntity<T> response = new HttpResponseEntity<>(responseEntity);

		// 为响应绑定转换器（优先使用指定转换器，其次使用响应内容类型对应的转换器）
		if (converter != null) {
			response.setConverter(converter);
		} else {
			MediaType responseContentType = response.getHeaders().getContentType();
			if (responseContentType != null) {
				Converter responseConverter = getConverter(responseContentType);
				if (responseConverter != null) {
					response.setConverter(responseConverter);
				}
			}
		}
		return response;
	}

	/**
	 * 发送获取数据的请求（适用于GET、HEAD等无请求体的方法），支持请求头合并与自动响应转换。
	 * <p>
	 * 请求头处理逻辑：
	 * <ul>
	 * <li>创建新的{@link HttpHeaders}实例作为基础</li>
	 * <li>若传入{@code httpHeaders}不为null，将其所有头信息复制到新实例中（实现自定义头与默认头的合并）</li>
	 * <li>设置默认头：Accept为{@code acceptMediaType}（声明期望响应格式），Content-Type为{@link MediaType#APPLICATION_FORM_URLENCODED}（适配表单参数）</li>
	 * </ul>
	 * 响应处理：将响应体字符串自动转换为{@code responseType}指定的类型（基于响应Content-Type匹配转换器）。
	 * 
	 * @param <T>             响应体类型泛型
	 * @param uri             请求的URI地址，不可为null
	 * @param params          URL查询参数（支持字符串、Map或普通对象，null表示无参数）
	 * @param httpMethod      HTTP请求方法（通常为GET），不可为null
	 * @param acceptMediaType 期望的响应媒体类型（如application/json），不可为null
	 * @param responseType    响应体的目标类型（支持泛型，如List&amp;lt;User&amp;gt;），不可为null
	 * @param httpHeaders     自定义请求头（可包含Authorization等信息，null则仅使用默认头）
	 * @return 转换后的响应体对象（类型为{T}）
	 * @throws HttpClientException 当请求执行失败或响应转换异常时抛出
	 */
	default <T> T get(@NonNull String uri, Object params, @NonNull HttpMethod httpMethod,
			@NonNull MediaType acceptMediaType, @NonNull Type responseType, HttpHeaders httpHeaders) {
		HttpHeaders headers = new HttpHeaders();
		if (httpHeaders != null) {
			headers.putAll(httpHeaders);
		}
		headers.setAccept(Arrays.asList(acceptMediaType));
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		HttpResponseEntity<String> response = doRequest(uri, httpMethod, String.class, params, headers, null, null,
				null);
		return response.getBody(responseType);
	}

	/**
	 * 发送包含数据的请求（适用于POST、PUT等有请求体的方法），支持请求头合并、自动序列化与响应转换。
	 * <p>
	 * 请求头处理逻辑：
	 * <ul>
	 * <li>创建新的{@link HttpHeaders}实例作为基础</li>
	 * <li>若传入{@code httpHeaders}不为null，将其所有头信息复制到新实例中（实现自定义头与默认头的合并）</li>
	 * <li>设置默认头：Accept为{@code acceptMediaType}（声明期望响应格式），Content-Type为{@code contentType}（声明请求体格式）</li>
	 * </ul>
	 * 数据处理：根据{@code contentType}匹配转换器序列化{@code content}；响应体自动转换为{@code responseType}指定类型。
	 * 
	 * @param <T>             响应体类型泛型
	 * @param uri             请求的URI地址，不可为null
	 * @param params          URL查询参数（支持字符串、Map或普通对象，null表示无参数）
	 * @param httpMethod      HTTP请求方法（通常为POST），不可为null
	 * @param acceptMediaType 期望的响应媒体类型（如application/json），不可为null
	 * @param responseType    响应体的目标类型（支持泛型，如Result&amp;lt;User&amp;gt;），不可为null
	 * @param httpHeaders     自定义请求头（可包含Authorization等信息，null则仅使用默认头）
	 * @param contentType     请求体的媒体类型（如application/json），不可为null
	 * @param content         请求体内容（需序列化的数据对象）
	 * @return 转换后的响应体对象（类型为{T}）
	 * @throws HttpClientException 当请求执行失败、请求体序列化或响应转换异常时抛出
	 */
	default <T> T post(@NonNull String uri, Object params, @NonNull HttpMethod httpMethod,
			@NonNull MediaType acceptMediaType, @NonNull Type responseType, HttpHeaders httpHeaders,
			@NonNull MediaType contentType, Object content) {
		HttpHeaders headers = new HttpHeaders();
		if (httpHeaders != null) {
			headers.putAll(httpHeaders);
		}
		headers.setAccept(Arrays.asList(acceptMediaType));
		headers.setContentType(contentType);
		HttpResponseEntity<String> response = doRequest(uri, httpMethod, String.class, params, headers, content,
				String.class, null);
		return response.getBody(responseType);
	}

	/**
	 * 加载SSL密钥材料（如客户端证书），用于配置HTTPS通信的安全上下文。
	 * <p>
	 * 支持加载.p12/.jks等格式的密钥库，包含私钥和证书链，适用于需要客户端认证的场景。处理流程：
	 * <ol>
	 * <li>验证资源有效性：检查密钥材料资源是否存在且可读</li>
	 * <li>构建SSL上下文：根据资源类型（文件/URL）加载密钥库，创建SSLContext</li>
	 * <li>应用配置：通过{@link #setSSLContext(SSLContext)}将SSL上下文应用到HTTP客户端</li>
	 * </ol>
	 * 
	 * @param keyMaterialResource 密钥材料资源（包含私钥和证书链的密钥库），不可为null
	 * @param storePassword       密钥库密码（用于解锁密钥库），不可为null
	 * @param keyPassword         私钥密码（用于解锁私钥，可与密钥库密码相同），不可为null
	 * @throws HttpClientException 当资源无效、密码错误、密钥库加载失败或SSL上下文创建异常时抛出
	 */
	default void loadKeyMaterial(@NonNull Resource keyMaterialResource, @NonNull String storePassword,
			@NonNull String keyPassword) throws HttpClientException {
		// 验证资源有效性
		if (keyMaterialResource == null || !keyMaterialResource.exists()) {
			throw new IllegalArgumentException("Key material resource not found: " + keyMaterialResource);
		}

		// 构建 SSL 上下文
		SSLContext sslContext;
		try {
			if (keyMaterialResource.isFile()) {
				sslContext = SSLContexts.custom().loadKeyMaterial(keyMaterialResource.getFile(),
						storePassword.toCharArray(), keyPassword.toCharArray()).build();
			} else {
				sslContext = SSLContexts.custom().loadKeyMaterial(keyMaterialResource.getURL(),
						storePassword.toCharArray(), keyPassword.toCharArray()).build();
			}
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			throw new HttpClientException(
					"Failed to load SSL key material from " + keyMaterialResource.getDescription(), e);
		}

		// 应用 SSL 上下文
		setSSLContext(sslContext);
	}

	/**
	 * 设置SSL上下文，配置HTTPS通信的安全参数。
	 * <p>
	 * SSL上下文包含加密套件、证书验证策略等安全配置，设置后HTTP客户端将使用该上下文进行HTTPS通信。
	 * 默认实现抛出不支持操作异常，需要具体实现类重写以提供实际逻辑（如配置请求工厂的SSL套接字工厂）。
	 * 
	 * @param sslContext 要应用的SSL上下文（null表示使用默认安全配置）
	 * @throws HttpClientException 当SSL上下文配置失败时抛出（如不支持的SSL协议、无效配置等）
	 */
	default void setSSLContext(SSLContext sslContext) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}
}
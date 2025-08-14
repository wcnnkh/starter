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
import java.util.LinkedHashMap;
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
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.messaging.convert.support.QueryStringFormat;
import run.soeasy.starter.commons.jackson.JsonFormat;
import run.soeasy.starter.commons.jackson.XmlFormat;

/**
 * 函数式接口：定义 HTTP 请求执行的核心抽象
 * 
 * <p>
 * 作为 HTTP 客户端的基础契约，提供：
 * <ul>
 * <li>标准化的请求-响应模型（基于 Spring HTTP 模型）</li>
 * <li>泛型类型安全的响应处理</li>
 * <li>默认实现的便捷方法（简化常见 HTTP 操作）</li>
 * <li>与 Spring 生态无缝集成（兼容 RestTemplate 扩展）</li>
 * </ul>
 * 
 * <p>
 * 实现类需专注于底层通信细节（如 Apache HttpClient/OkHttp 连接管理）， 本接口则定义通用的 HTTP 操作语义和上层逻辑。
 * 
 * @see org.springframework.http.RequestEntity
 * @see org.springframework.http.ResponseEntity
 * @see HttpResponseEntity
 */
@FunctionalInterface
public interface HttpClientExecutor {

	/**
	 * 默认 JSON 媒体类型：application/json;charset=UTF-8
	 * <p>
	 * 用于设置JSON请求的Content-Type头信息
	 */
	MediaType DEFAULT_JSON_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

	/**
	 * 默认 XML 媒体类型：application/xml;charset=UTF-8
	 * <p>
	 * 用于设置XML请求的Content-Type头信息
	 */
	MediaType DEFAULT_XML_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_XML, StandardCharsets.UTF_8);

	/**
	 * 描述 GET 请求参数的 Map 类型（键值均为 Object）
	 * <p>
	 * 用于类型转换时指定源类型描述
	 */
	TypeDescriptor GET_PARAMETER_MAP_TYPE = TypeDescriptor.map(LinkedHashMap.class, Object.class, Object.class);

	/**
	 * 执行HTTP请求的核心方法
	 * <p>
	 * 实现类需提供具体的HTTP请求发送和响应处理逻辑
	 * 
	 * @param requestEntity 封装了请求信息的对象，包含URL、方法、头信息和请求体
	 * @param responseType  响应数据的目标类型
	 * @param <S>           请求体的类型
	 * @param <T>           响应体的类型
	 * @return 包含响应状态、头信息和响应体的ResponseEntity对象
	 * @throws HttpClientException 当请求执行过程中发生错误时抛出
	 */
	<S, T> ResponseEntity<T> doRequest(RequestEntity<S> requestEntity, Type responseType) throws HttpClientException;

	/**
	 * 通用HTTP请求执行方法，支持自定义转换器和请求体类型
	 * <p>
	 * 提供参数处理、请求体转换和请求构建的统一实现，适用于各种数据格式的HTTP请求。 支持多种参数类型，并根据指定的转换器处理请求体序列化。
	 * 
	 * @param uri          请求的URI地址
	 * @param httpMethod   HTTP请求方法（GET/POST/PUT等）
	 * @param responseType 响应数据的目标类型
	 * @param params       URL查询参数（支持字符串、Map或普通对象）
	 * @param httpHeaders  请求头信息（可为null）
	 * @param body         请求体内容（将通过转换器序列化）
	 * @param bodyClass    请求体目标类型（通常为String）
	 * @param converter    用于序列化请求体的转换器
	 * @param <S>          请求体目标类型泛型
	 * @param <T>          响应体类型泛型
	 * @return 包含响应信息的HttpResponseEntity对象，已关联指定转换器
	 * @throws HttpClientException 当请求执行或参数转换失败时抛出
	 */
	default <S, T> HttpResponseEntity<T> doRequest(@NonNull String uri, @NonNull HttpMethod httpMethod,
			@NonNull Type responseType, Object params, HttpHeaders httpHeaders, Object body, Class<S> bodyClass,
			Converter converter) throws HttpClientException {
		if (params != null) {
			String queryString;
			if (params instanceof String) {
				// 直接使用字符串作为查询参数（适用于已手动拼接好的场景）
				queryString = (String) params;
			} else {
				// 根据请求头字符集获取对应的查询字符串转换器
				MediaType mediaType = httpHeaders == null ? null : httpHeaders.getContentType();
				Converter queryStringConverter;
				if (mediaType != null && mediaType.getCharset() != null) {
					queryStringConverter = QueryStringFormat.getFormat(mediaType.getCharset());
				} else {
					queryStringConverter = QueryStringFormat.getFormat(StandardCharsets.UTF_8);
				}

				if (!(params instanceof Map) && converter != null) {
					params = converter.convert(params, Map.class);
				}
				queryString = queryStringConverter.convert(params, String.class);
			}
			// 拼接查询字符串到URI
			uri = UriComponentsBuilder.fromUriString(uri).query(queryString).build().toUriString();
		}

		// 使用指定转换器序列化请求体（仅当满足转换条件时）
		if (body != null && bodyClass != null && converter != null && !bodyClass.isInstance(body)) {
			body = converter.convert(body, bodyClass);
		}

		RequestEntity<Object> requestEntity = RequestEntity.method(httpMethod, URI.create(uri)).headers(httpHeaders)
				.body(body);
		ResponseEntity<T> responseEntity = doRequest(requestEntity, responseType);
		HttpResponseEntity<T> response = new HttpResponseEntity<>(responseEntity);
		if (converter != null) {
			response.setConverter(converter);
		}
		return response;
	}

	/**
	 * 发送JSON格式的请求（支持任意HTTP方法）
	 * <p>
	 * 自动处理JSON序列化和Content-Type头信息设置，适用于需要JSON请求体的各种HTTP方法
	 * （如POST/PUT/PATCH等）。若请求头未指定Content-Type，将自动设置为默认JSON媒体类型。
	 * 
	 * @param uri          请求的URI地址
	 * @param httpMethod   HTTP请求方法（如POST/PUT/PATCH等）
	 * @param responseType 响应数据的目标类型
	 * @param params       URL查询参数（支持字符串、Map或普通对象）
	 * @param headers      请求头信息（可为null，自动补充Content-Type）
	 * @param body         请求体内容（非字符串类型将被序列化为JSON）
	 * @param <T>          响应体的类型
	 * @return 包含响应数据的HttpResponseEntity对象，已关联JSON转换器
	 * @throws HttpClientException 当请求执行或JSON序列化失败时抛出
	 */
	default <T> HttpResponseEntity<T> doJson(@NonNull String uri, @NonNull HttpMethod httpMethod,
			@NonNull Type responseType, Object params, HttpHeaders headers, Object body) throws HttpClientException {
		if (headers == null) {
			headers = new HttpHeaders();
		}

		// 自动设置 JSON 媒体类型（未指定时）
		if (httpMethod != HttpMethod.GET && headers.getContentType() == null) {
			headers.setContentType(getJsonMediaType());
		}

		return doRequest(uri, httpMethod, responseType, params, headers, body, String.class, getJsonConverter());
	}

	/**
	 * 发送XML格式的请求（支持任意HTTP方法）
	 * <p>
	 * 自动处理XML序列化和Content-Type头信息设置，适用于需要XML请求体的各种HTTP方法。
	 * 若请求头未指定Content-Type，将自动设置为默认XML媒体类型。
	 * 
	 * @param uri          请求的URI地址
	 * @param httpMethod   HTTP请求方法（如POST/PUT等）
	 * @param responseType 响应数据的目标类型
	 * @param params       URL查询参数（支持字符串、Map或普通对象）
	 * @param headers      请求头信息（可为null，自动补充Content-Type）
	 * @param body         请求体内容（非字符串类型将被序列化为XML）
	 * @param <T>          响应体的类型
	 * @return 包含响应数据的HttpResponseEntity对象，已关联XML转换器
	 * @throws HttpClientException 当请求执行或XML序列化失败时抛出
	 */
	default <T> HttpResponseEntity<T> doXml(@NonNull String uri, @NonNull HttpMethod httpMethod,
			@NonNull Type responseType, Object params, HttpHeaders headers, Object body) throws HttpClientException {
		if (headers == null) {
			headers = new HttpHeaders();
		}

		if (httpMethod != HttpMethod.GET && headers.getContentType() == null) {
			headers.setContentType(getXmlMediaType());
		}

		// 注意：此处原代码使用了getJsonConverter()，实际应使用getXmlConverter()，建议修正
		return doRequest(uri, httpMethod, responseType, params, headers, body, String.class, getXmlConverter());
	}

	/**
	 * 获取 JSON 转换器（默认实现）
	 * 
	 * <p>
	 * 默认返回框架提供的 JSON 转换器， 实现类可重写此方法提供自定义转换器。
	 * 
	 * @return 当前配置的 JSON 转换器
	 */
	default Converter getJsonConverter() {
		return JsonFormat.DEFAULT;
	}

	/**
	 * 获取 JSON 请求的媒体类型（默认实现）
	 * 
	 * <p>
	 * 默认返回 application/json;charset=UTF-8， 可通过 {@link #setJsonMediaType(MediaType)}
	 * 动态修改。
	 * 
	 * @return 当前配置的 JSON 媒体类型
	 */
	default MediaType getJsonMediaType() {
		return DEFAULT_JSON_MEDIA_TYPE;
	}

	/**
	 * 获取 XML 转换器（默认实现）
	 * <p>
	 * 默认返回框架提供的XML转换器，实现类可重写此方法提供自定义转换器
	 * 
	 * @return 当前配置的XML转换器
	 */
	default Converter getXmlConverter() {
		return XmlFormat.DEFAULT;
	}

	/**
	 * 获取XML请求的媒体类型（默认实现）
	 * <p>
	 * 默认返回application/xml;charset=UTF-8
	 * 
	 * @return 当前配置的XML媒体类型
	 */
	default MediaType getXmlMediaType() {
		return DEFAULT_XML_MEDIA_TYPE;
	}

	/**
	 * 加载 SSL 密钥材料（支持 .p12/.jks 等格式）
	 * 
	 * <p>
	 * 处理流程：
	 * <ol>
	 * <li>验证资源有效性（存在性/可读性）</li>
	 * <li>根据资源类型（文件/URL）选择加载方式</li>
	 * <li>使用 Apache HttpClient 构建 SSL 上下文</li>
	 * <li>通过 {@link #setSSLContext(SSLContext)} 应用配置</li>
	 * </ol>
	 * 
	 * <p>
	 * 证书要求：
	 * <ul>
	 * <li>必须包含私钥和证书链</li>
	 * <li>推荐使用 PKCS#12 (.p12) 格式</li>
	 * <li>密钥库密码与私钥密码可相同/不同</li>
	 * </ul>
	 * 
	 * @param keyMaterialResource 密钥材料资源（含私钥和证书）
	 * @param storePassword       密钥库密码
	 * @param keyPassword         私钥密码
	 * @throws HttpClientException 当资源无效/密码错误/加载失败时抛出
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
	 * 设置 JSON 转换器（默认抛出异常，需实现类重写）
	 * 
	 * <p>
	 * 实现类应实现：
	 * <ul>
	 * <li>动态替换 JSON 转换器</li>
	 * <li>线程安全的配置更新</li>
	 * <li>不影响已创建的请求实例</li>
	 * </ul>
	 * 
	 * @param jsonConverter 新的 JSON 转换器
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setJsonConverter(Converter jsonConverter) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}

	/**
	 * 设置JSON请求的媒体类型
	 * <p>
	 * 默认实现抛出不支持操作异常，实现类需重写此方法以支持自定义媒体类型
	 * 
	 * @param mediaType 新的JSON媒体类型
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setJsonMediaType(MediaType mediaType) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}

	/**
	 * 设置SSL上下文
	 * <p>
	 * 默认实现抛出不支持操作异常，实现类需重写此方法以支持SSL配置
	 * 
	 * @param sslContext 要设置的SSL上下文
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setSSLContext(SSLContext sslContext) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}

	/**
	 * 设置XML转换器
	 * <p>
	 * 默认实现抛出不支持操作异常，实现类需重写此方法以支持自定义XML转换器
	 * 
	 * @param xmlConverter 新的XML转换器
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setXmlConverter(Converter xmlConverter) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}
}

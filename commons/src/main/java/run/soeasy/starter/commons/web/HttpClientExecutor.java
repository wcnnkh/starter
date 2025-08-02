package run.soeasy.starter.commons.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;

import org.apache.http.ssl.SSLContexts;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.NonNull;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.core.convert.strings.StringFormat;
import run.soeasy.framework.json.JsonConverter;
import run.soeasy.framework.logging.LogManager;
import run.soeasy.framework.logging.Logger;
import run.soeasy.framework.messaging.convert.support.QueryStringFormat;
import run.soeasy.starter.commons.jackson.JsonFormat;

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

	/** 默认 JSON 媒体类型：application/json;charset=UTF-8 */
	MediaType DEFAULT_JSON_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8);

	/** 描述 GET 请求参数的 Map 类型（键值均为 Object） */
	TypeDescriptor GET_PARAMETER_MAP_TYPE = TypeDescriptor.map(LinkedHashMap.class, Object.class, Object.class);

	/**
	 * 执行 HTTP 请求并返回响应实体（核心抽象方法）
	 * 
	 * <p>
	 * 作为函数式接口的单一抽象方法（SAM），定义 HTTP 通信的核心操作：
	 * <ul>
	 * <li>支持任意 HTTP 方法（GET/POST/PUT/DELETE 等）</li>
	 * <li>通过 RequestEntity 封装完整请求（URL/方法/头/体）</li>
	 * <li>通过 Type 参数支持复杂泛型响应类型</li>
	 * </ul>
	 * 
	 * <p>
	 * 实现类需处理：
	 * <ul>
	 * <li>底层网络连接与通信</li>
	 * <li>HTTP 状态码校验（2xx 成功，其他转换为异常）</li>
	 * <li>响应体类型转换（基于 Spring 类型转换体系）</li>
	 * <li>网络异常与重试策略（由实现类自定义）</li>
	 * </ul>
	 * 
	 * @param <S>           请求体类型
	 * @param <T>           响应体类型
	 * @param requestEntity 完整请求实体（包含 URL/方法/头/体）
	 * @param responseType  响应类型的反射表示（支持泛型类型引用）
	 * @return 包含响应体和响应头的 ResponseEntity
	 * @throws HttpClientException 当请求失败时抛出（如连接超时/状态码错误）
	 */
	<S, T> ResponseEntity<T> executeRequest(RequestEntity<S> requestEntity, Type responseType)
			throws HttpClientException;

	/**
	 * 执行 HTTP 请求并返回增强型响应实体（默认实现）
	 * 
	 * <p>
	 * 功能特性：
	 * <ul>
	 * <li>自动拼接 GET 请求参数（Map/对象转查询字符串）</li>
	 * <li>请求/响应日志记录（含 UUID 唯一标识追踪）</li>
	 * <li>转换器驱动的请求体/响应体处理</li>
	 * <li>返回增强型响应实体（支持 JSON 便捷转换）</li>
	 * </ul>
	 * 
	 * <p>
	 * 参数处理逻辑：
	 * <ul>
	 * <li>GET 请求：content 转查询字符串，URL 自动拼接</li>
	 * <li>非 GET 请求：content 直接作为请求体（需自行序列化）</li>
	 * </ul>
	 * 
	 * @param <T>              响应体类型
	 * @param url              请求 URL
	 * @param httpMethod       HTTP 方法
	 * @param headers          请求头（可为 null，自动补全基础头）
	 * @param content          请求体内容（GET 请求时作为查询参数）
	 * @param requestConverter 转换器（处理请求/响应体转换）
	 * @param responseType     响应类型（支持泛型类型）
	 * @return 增强型 HTTP 响应实体（封装 ResponseEntity）
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	@SuppressWarnings("unchecked")
	default <T> HttpResponseEntity<T> executeRequest(@NonNull String url, @NonNull HttpMethod httpMethod,
			HttpHeaders headers, Object content, @NonNull Converter requestConverter, @NonNull Type responseType)
			throws HttpClientException {
		// GET 请求参数自动拼接逻辑
		if (httpMethod == HttpMethod.GET && content != null && (content instanceof Map
				|| requestConverter.canConvert(TypeDescriptor.forObject(content), GET_PARAMETER_MAP_TYPE))) {
			Map<Object, Object> uriVariables = content instanceof Map ? ((Map<Object, Object>) content)
					: ((Map<Object, Object>) requestConverter.convert(content, GET_PARAMETER_MAP_TYPE));
			String queryString = getQueryStringFormat().convert(uriVariables, String.class);
			url = UriComponentsBuilder.fromUriString(url).query(queryString).build().toUriString();
			content = null;
		}

		// 生成请求唯一标识并记录日志
		String uuid = UUID.randomUUID().toString().replace("-", "");
		HttpEntity<?> httpEntity = new HttpEntity<>(content, headers);
		getLogger().info("[{}] {} {} request entity {}", uuid, httpMethod, url, httpEntity);

		RequestEntity<Object> requestEntity = RequestEntity.method(httpMethod, url).headers(headers).body(content);
		ResponseEntity<T> responseEntity = executeRequest(requestEntity, responseType);
		getLogger().info("[{}] {} {} response entity {}", uuid, httpMethod, url, responseEntity);

		// 封装响应实体并设置 JSON 转换器
		HttpResponseEntity<T> httpResponseEntity = new HttpResponseEntity<>(responseEntity);
		httpResponseEntity.setJsonConverter(getJsonConverter());
		return httpResponseEntity;
	}

	/**
	 * 使用默认 JSON 转换器执行 HTTP 请求（便捷方法）
	 * 
	 * <p>
	 * 等效于调用：
	 * 
	 * <pre>
	 * executeRequest(url, httpMethod, headers, content, getJsonConverter(), responseType)
	 * </pre>
	 * 
	 * @param <T>          响应体类型
	 * @param url          请求 URL
	 * @param httpMethod   HTTP 方法
	 * @param headers      请求头（可为 null）
	 * @param content      请求体内容
	 * @param responseType 响应类型
	 * @return 增强型 HTTP 响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	default <T> HttpResponseEntity<T> executeRequest(@NonNull String url, @NonNull HttpMethod httpMethod,
			HttpHeaders headers, Object content, @NonNull Type responseType) throws HttpClientException {
		return executeRequest(url, httpMethod, headers, content, getJsonConverter(), responseType);
	}

	/**
	 * 执行 GET 请求并自动处理 JSON 响应（默认转换器）
	 * 
	 * <p>
	 * 处理流程：
	 * <ol>
	 * <li>request 参数转 URL 查询字符串</li>
	 * <li>自动添加 Accept: application/json 头</li>
	 * <li>使用默认 JSON 转换器解析响应体</li>
	 * </ol>
	 * 
	 * @param <T>          响应体类型
	 * @param uri          请求 URI（不含查询参数）
	 * @param headers      请求头（可为 null）
	 * @param request      请求参数（转查询字符串）
	 * @param responseType 响应类型
	 * @return 增强型 HTTP 响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	default <T> HttpResponseEntity<T> getJson(String uri, HttpHeaders headers, Object request, Type responseType)
			throws HttpClientException {
		return getJson(uri, headers, request, responseType, getJsonConverter());
	}

	/**
	 * 执行 GET 请求并自动处理 JSON 响应（自定义转换器）
	 * 
	 * <p>
	 * 处理流程：
	 * <ol>
	 * <li>request 参数转 URL 查询字符串</li>
	 * <li>自动添加 Accept: application/json 头</li>
	 * <li>使用指定 JSON 转换器解析响应体</li>
	 * </ol>
	 * 
	 * @param <T>           响应体类型
	 * @param url           请求 URL
	 * @param headers       请求头（可为 null）
	 * @param request       请求参数（转查询字符串）
	 * @param responseType  响应类型
	 * @param jsonConverter 自定义 JSON 转换器
	 * @return 增强型 HTTP 响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	default <T> HttpResponseEntity<T> getJson(String url, HttpHeaders headers, Object request, Type responseType,
			@NonNull JsonConverter jsonConverter) throws HttpClientException {
		HttpResponseEntity<T> responseEntity = executeRequest(url, HttpMethod.GET, headers, request, jsonConverter,
				responseType);
		responseEntity.setJsonConverter(jsonConverter);
		return responseEntity;
	}

	/**
	 * 获取 JSON 转换器（默认实现）
	 * 
	 * <p>
	 * 默认返回框架提供的 JSON 转换器， 实现类可重写此方法提供自定义转换器。
	 * 
	 * @return 当前配置的 JSON 转换器
	 */
	default JsonConverter getJsonConverter() {
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
	 * 获取日志记录器（默认实现）
	 * 
	 * <p>
	 * 日志格式建议包含：
	 * <ul>
	 * <li>请求 UUID（用于链路追踪）</li>
	 * <li>HTTP 方法与 URL</li>
	 * <li>请求/响应体摘要</li>
	 * </ul>
	 * 
	 * @return 当前实现类的日志记录器
	 */
	default Logger getLogger() {
		return LogManager.getLogger(getClass());
	}

	/**
	 * 获取查询字符串格式化器（默认实现）
	 * 
	 * <p>
	 * 用于将 Map 转换为 URL 查询字符串， 默认使用 UTF-8 编码，支持：
	 * <ul>
	 * <li>嵌套对象展开（如 {a:{b:c}} → a.b=c）</li>
	 * <li>集合类型序列化（如 [a,b] → a&amp;b）</li>
	 * <li>特殊字符转义（如空格→%20）</li>
	 * </ul>
	 * 
	 * @return 查询字符串格式化器
	 */
	default StringFormat<Object> getQueryStringFormat() {
		return QueryStringFormat.getFormat(StandardCharsets.UTF_8);
	}

	/**
	 * 执行 POST 请求并发送 JSON 参数（默认转换器）
	 * 
	 * <p>
	 * 处理流程：
	 * <ol>
	 * <li>自动设置 Content-Type: application/json</li>
	 * <li>使用默认 JSON 转换器序列化请求体</li>
	 * <li>使用默认 JSON 转换器解析响应体</li>
	 * </ol>
	 * 
	 * @param <T>          响应体类型
	 * @param url          请求 URL
	 * @param headers      请求头（可为 null，自动补全 JSON 头）
	 * @param content      请求 JSON 内容（对象转字符串）
	 * @param responseType 响应类型
	 * @return 增强型 HTTP 响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	default <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType) throws HttpClientException {
		return postJson(url, headers, content, responseType, getJsonConverter());
	}

	/**
	 * 执行 POST 请求并发送 JSON 参数（自定义转换器）
	 * 
	 * <p>
	 * 处理流程：
	 * <ol>
	 * <li>自动设置 Content-Type: application/json</li>
	 * <li>使用指定 JSON 转换器序列化请求体</li>
	 * <li>使用指定 JSON 转换器解析响应体</li>
	 * </ol>
	 * 
	 * @param <T>           响应体类型
	 * @param url           请求 URL
	 * @param headers       请求头（可为 null，自动补全 JSON 头）
	 * @param content       请求 JSON 内容（对象转字符串）
	 * @param responseType  响应类型
	 * @param jsonConverter 自定义 JSON 转换器
	 * @return 增强型 HTTP 响应实体
	 * @throws HttpClientException 当请求执行失败时抛出
	 */
	default <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType, @NonNull JsonConverter jsonConverter) throws HttpClientException {
		if (headers == null) {
			headers = new HttpHeaders();
		}

		// 自动设置 JSON 媒体类型
		if (!MediaType.APPLICATION_JSON.isCompatibleWith(headers.getContentType())) {
			headers.setContentType(getJsonMediaType());
		}

		// 序列化请求体为 JSON 字符串
		if (content != null) {
			content = jsonConverter.convert(content, String.class);
		}
		HttpResponseEntity<T> responseEntity = executeRequest(url, HttpMethod.POST, headers, content, jsonConverter,
				responseType);
		responseEntity.setJsonConverter(jsonConverter);
		return responseEntity;
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
	default void setJsonConverter(JsonConverter jsonConverter) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}

	/**
	 * 设置 JSON 请求的媒体类型（默认抛出异常，需实现类重写）
	 * 
	 * <p>
	 * 常见配置值：
	 * <ul>
	 * <li>application/json</li>
	 * <li>application/hal+json</li>
	 * <li>application/vnd.api+json</li>
	 * </ul>
	 * 
	 * @param mediaType 新的媒体类型
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setJsonMediaType(MediaType mediaType) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}

	/**
	 * 设置 SSL 上下文（默认抛出异常，需实现类重写）
	 * 
	 * <p>
	 * 实现类应实现：
	 * <ul>
	 * <li>将 SSL 上下文应用到底层客户端</li>
	 * <li>支持动态证书轮换</li>
	 * <li>确保多线程环境下的安全性</li>
	 * </ul>
	 * 
	 * @param sslContext 新的 SSL 上下文
	 * @throws HttpClientException 当配置失败时抛出
	 */
	default void setSSLContext(SSLContext sslContext) throws HttpClientException {
		throw new UnsupportedOperationException("Not supported in default implementation, please override");
	}
}
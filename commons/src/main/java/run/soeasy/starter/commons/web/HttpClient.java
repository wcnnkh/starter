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

import javax.net.ssl.SSLSocketFactory;

import org.apache.http.ssl.SSLContexts;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.json.JsonConverter;
import run.soeasy.framework.messaging.convert.support.QueryStringFormat;
import run.soeasy.starter.commons.jackson.JsonFormat;

/**
 * HTTP客户端工具类<br>
 * 封装{@link RestTemplate}实现RESTful API请求，支持JSON参数序列化、URL参数拼接、SSL证书加载等功能
 * 
 * @author soeasy.run
 */
@Getter
@Setter
@Slf4j
public class HttpClient extends RestTemplate {
	private static final TypeDescriptor GET_PARAMETER_MAP_TYPE = TypeDescriptor.map(LinkedHashMap.class, Object.class,
			Object.class);
	private static final MediaType DEFAULT_JSON_MEDIA_TYPE = new MediaType(MediaType.APPLICATION_JSON,
			StandardCharsets.UTF_8);

	/** JSON转换器，默认使用{@link JsonFormat#DEFAULT} */
	@NonNull
	private JsonConverter jsonConverter = JsonFormat.DEFAULT;
	
	/** 资源加载器，默认使用{@link DefaultResourceLoader} */
	@NonNull
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	
	/** URL查询参数格式化器 */
	@NonNull
	private QueryStringFormat queryStringFormat = QueryStringFormat.getFormat(StandardCharsets.UTF_8);
	
	/** JSON媒体类型，默认使用UTF-8编码的APPLICATION_JSON */
	@NonNull
	private MediaType jsonMediaType = DEFAULT_JSON_MEDIA_TYPE;

	/**
	 * 执行HTTP请求
	 * 
	 * @param url 请求URL
	 * @param httpMethod HTTP方法（GET/POST等）
	 * @param headers 请求头
	 * @param content 请求体内容
	 * @param requestConverter 请求参数转换器
	 * @param responseType 响应类型
	 * @return 封装的HTTP响应实体
	 * @throws IllegalArgumentException 当参数不合法时抛出
	 */
	@SuppressWarnings("unchecked")
	public <T> HttpResponseEntity<T> request(@NonNull String url, @NonNull HttpMethod httpMethod, HttpHeaders headers,
			Object content, @NonNull Converter requestConverter, @NonNull Type responseType) {
		if (httpMethod == HttpMethod.GET && content != null && (content instanceof Map
				|| requestConverter.canConvert(TypeDescriptor.forObject(content), GET_PARAMETER_MAP_TYPE))) {
			Map<Object, Object> uriVariables = content instanceof Map ? ((Map<Object, Object>) content)
					: ((Map<Object, Object>) requestConverter.convert(content, GET_PARAMETER_MAP_TYPE));
			String queryString = queryStringFormat.convert(uriVariables, String.class);
			url = UriComponentsBuilder.fromUriString(url).query(queryString).build().toUriString();
			content = null;
		}

		String uuid = UUID.randomUUID().toString().replace("-", "");
		HttpEntity<?> httpEntity = new HttpEntity<>(content, headers);
		log.info("[{}] {} {} request entity {}", uuid, httpMethod, url, httpEntity);
		ResponseEntity<T> responseEntity = exchange(url, httpMethod, httpEntity, new ParameterizedTypeReference<T>() {
			@Override
			public Type getType() {
				return responseType;
			}
		});
		log.info("[{}] {} {} request entity {}", uuid, httpMethod, url, responseEntity);
		HttpResponseEntity<T> httpResponseEntity = new HttpResponseEntity<>(responseEntity);
		httpResponseEntity.setJsonConverter(jsonConverter);
		return httpResponseEntity;
	}

	/**
	 * 使用默认JSON转换器执行HTTP请求
	 * 
	 * @param url 请求URL
	 * @param httpMethod HTTP方法
	 * @param headers 请求头
	 * @param content 请求体
	 * @param responseType 响应类型
	 * @return 封装的HTTP响应实体
	 */
	public final <T> HttpResponseEntity<T> request(@NonNull String url, @NonNull HttpMethod httpMethod,
			HttpHeaders headers, Object content, @NonNull Type responseType) {
		return request(url, httpMethod, headers, content, this.jsonConverter, responseType);
	}

	/**
	 * 执行GET请求并解析JSON响应
	 * 
	 * @param uri 请求URI
	 * @param headers 请求头
	 * @param request 请求参数
	 * @param responseType 响应类型
	 * @return 封装的HTTP响应实体
	 */
	public final <T> HttpResponseEntity<T> getJson(String uri, HttpHeaders headers, Object request, Type responseType) {
		return getJson(uri, headers, request, responseType, this.jsonConverter);
	}

	/**
	 * 执行GET请求并解析JSON响应（指定JSON转换器）
	 * 
	 * @param url 请求URL
	 * @param headers 请求头
	 * @param request 请求参数
	 * @param responseType 响应类型
	 * @param jsonConverter JSON转换器
	 * @return 封装的HTTP响应实体
	 */
	public final <T> HttpResponseEntity<T> getJson(String url, HttpHeaders headers, Object request, Type responseType,
			@NonNull JsonConverter jsonConverter) {
		HttpResponseEntity<T> responseEntity = request(url, HttpMethod.GET, headers, request, jsonConverter,
				responseType);
		responseEntity.setJsonConverter(jsonConverter);
		return responseEntity;
	}

	/**
	 * 加载SSL密钥材料（.p12/.jks等格式）
	 * 
	 * @param keyMaterialResource 密钥材料资源
	 * @param storePassword 密钥库密码
	 * @param keyPassword 密钥密码
	 * @throws IllegalArgumentException 当资源不存在时抛出
	 */
	public void loadKeyMaterial(@NonNull Resource keyMaterialResource, @NonNull String storePassword,
			@NonNull String keyPassword) {
		if (keyMaterialResource == null || !keyMaterialResource.exists()) {
			throw new IllegalArgumentException("Key material resource not found: " + keyMaterialResource);
		}

		SSLSocketFactory sslSocketFactory;
		try {
			if (keyMaterialResource.isFile()) {
				sslSocketFactory = SSLContexts.custom().loadKeyMaterial(keyMaterialResource.getFile(),
						storePassword.toCharArray(), keyPassword.toCharArray()).build().getSocketFactory();
			} else {
				sslSocketFactory = SSLContexts.custom().loadKeyMaterial(keyMaterialResource.getURL(),
						storePassword.toCharArray(), keyPassword.toCharArray()).build().getSocketFactory();
			}
		} catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException
				| CertificateException | IOException e) {
			logger.error(keyMaterialResource.getDescription(), e);
			return;
		}
		setSSLSocketFactory(sslSocketFactory);
	}

	/**
	 * 通过资源路径加载SSL密钥材料
	 * 
	 * @param keyMaterialLocation 密钥材料路径（支持classpath:/、file:等前缀）
	 * @param storePassword 密钥库密码
	 * @param keyPassword 密钥密码
	 */
	public void loadKeyMaterial(@NonNull String keyMaterialLocation, @NonNull String storePassword,
			@NonNull String keyPassword) {
		Resource resource = resourceLoader.getResource(keyMaterialLocation);
		loadKeyMaterial(resource, storePassword, keyPassword);
	}

	/**
	 * 执行POST请求并发送JSON参数
	 * 
	 * @param url 请求URL
	 * @param headers 请求头
	 * @param content 请求JSON内容
	 * @param responseType 响应类型
	 * @return 封装的HTTP响应实体
	 */
	public final <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType) {
		return postJson(url, headers, content, responseType, this.jsonConverter);
	}

	/**
	 * 执行POST请求并发送JSON参数（指定JSON转换器）
	 * 
	 * @param url 请求URL
	 * @param headers 请求头
	 * @param content 请求JSON内容
	 * @param responseType 响应类型
	 * @param jsonConverter JSON转换器
	 * @return 封装的HTTP响应实体
	 */
	public final <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType, @NonNull JsonConverter jsonConverter) {
		if (headers == null) {
			headers = new HttpHeaders();
		}

		if (!MediaType.APPLICATION_JSON.isCompatibleWith(headers.getContentType())) {
			headers.setContentType(jsonMediaType);
		}

		if (content != null) {
			content = jsonConverter.convert(content, String.class);
		}
		HttpResponseEntity<T> responseEntity = request(url, HttpMethod.POST, headers, content, jsonConverter,
				responseType);
		responseEntity.setJsonConverter(jsonConverter);
		return responseEntity;
	}

	/**
	 * 设置SSL套接字工厂
	 * 
	 * @param sslSocketFactory SSL套接字工厂
	 */
	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		SimpleClientHttpsRequestFactory httpsRequestFactory = new SimpleClientHttpsRequestFactory();
		httpsRequestFactory.setSslSocketFactory(sslSocketFactory);
		setRequestFactory(httpsRequestFactory);
	}
}
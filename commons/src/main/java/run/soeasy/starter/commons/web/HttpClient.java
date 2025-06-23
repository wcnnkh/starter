package run.soeasy.starter.commons.web;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.starter.commons.json.JacksonFormat;
import run.soeasy.starter.commons.json.JsonFormat;

@Getter
@Setter
@Slf4j
public class HttpClient extends RestTemplate {
	private static final TypeDescriptor GET_PARAMETER_MAP_TYPE = TypeDescriptor.map(LinkedHashMap.class, String.class,
			Object.class);

	@NonNull
	private JsonFormat jsonFormat = JacksonFormat.DEFAULT;
	@NonNull
	private ResourceLoader resourceLoader = new DefaultResourceLoader();

	/**
	 * 扩展exchange
	 * 
	 * @param <T>
	 * @param url
	 * @param httpMethod
	 * @param headers
	 * @param content
	 * @param requestConverter
	 * @param responseType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> HttpResponseEntity<T> exchange(@NonNull String url, @NonNull HttpMethod httpMethod, HttpHeaders headers,
			Object content, @NonNull Converter requestConverter, @NonNull Type responseType) {
		if (httpMethod == HttpMethod.GET && content != null
				&& requestConverter.canConvert(TypeDescriptor.forObject(content), GET_PARAMETER_MAP_TYPE)) {
			Map<String, String> uriVariables = (Map<String, String>) requestConverter.convert(content,
					GET_PARAMETER_MAP_TYPE);
			MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
			if (uriVariables != null) {
				for (Entry<String, String> entry : uriVariables.entrySet()) {
					multiValueMap.set(entry.getKey(), entry.getValue());
				}
			}

			url = UriComponentsBuilder.fromUriString(url).queryParams(multiValueMap).build().toUriString();
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
		httpResponseEntity.setJsonFormat(jsonFormat);
		return httpResponseEntity;
	}

	/**
	 * 扩展exchange
	 * 
	 * @param <T>
	 * @param url
	 * @param httpMethod
	 * @param headers
	 * @param content
	 * @param responseType
	 * @return
	 */
	public final <T> HttpResponseEntity<T> exchange(@NonNull String url, @NonNull HttpMethod httpMethod,
			HttpHeaders headers, Object content, @NonNull Type responseType) {
		return exchange(url, httpMethod, headers, content, this.jsonFormat, responseType);
	}

	public final <T> HttpResponseEntity<T> getJson(String uri, HttpHeaders headers, Object request, Type responseType) {
		return getJson(uri, headers, request, responseType, this.jsonFormat);
	}

	public final <T> HttpResponseEntity<T> getJson(String url, HttpHeaders headers, Object request, Type responseType,
			@NonNull JsonFormat jsonFormat) {
		HttpResponseEntity<T> responseEntity = exchange(url, HttpMethod.GET, headers, request, jsonFormat,
				responseType);
		responseEntity.setJsonFormat(jsonFormat);
		return responseEntity;
	}

	public void loadKeyMaterial(@NonNull Resource keyMaterialResource, @NonNull String storePassword,
			@NonNull String keyPassword) {
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

	public void loadKeyMaterial(@NonNull String KeyMaterialLocation, @NonNull String storePassword,
			@NonNull String keyPassword) {
		Resource resource = resourceLoader.getResource(KeyMaterialLocation);
		if (resource != null && resource.exists()) {
			loadKeyMaterial(resource, storePassword, keyPassword);
		}
	}

	public final <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType) {
		return postJson(url, headers, content, responseType, this.jsonFormat);
	}

	public final <T> HttpResponseEntity<T> postJson(@NonNull String url, HttpHeaders headers, Object content,
			@NonNull Type responseType, @NonNull JsonFormat jsonFormat) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (content != null) {
			content = jsonFormat.convert(content, String.class);
		}
		HttpResponseEntity<T> responseEntity = exchange(url, HttpMethod.POST, headers, content, jsonFormat,
				responseType);
		responseEntity.setJsonFormat(jsonFormat);
		return responseEntity;
	}

	public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
		JacksonFormat jacksonFormat = JacksonFormat.DEFAULT.copy();
		jacksonFormat.setPropertyNamingStrategy(propertyNamingStrategy);
		setJsonFormat(jacksonFormat);
	}

	public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
		SimpleClientHttpsRequestFactory httpsRequestFactory = new SimpleClientHttpsRequestFactory();
		httpsRequestFactory.setSslSocketFactory(sslSocketFactory);
		setRequestFactory(httpsRequestFactory);
	}
}

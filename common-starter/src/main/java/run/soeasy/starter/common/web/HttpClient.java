package run.soeasy.starter.common.web;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.starter.common.json.JsonFormat;

@Getter
@Setter
@Slf4j
public class HttpClient {
	private static final RestOperations REST_OPERATIONS = new RestTemplate();
	@NonNull
	private RestOperations restOperations = REST_OPERATIONS;
	private JsonFormat jsonFormat = JsonFormat.JACKSON;

	@SuppressWarnings("unchecked")
	public <T> ResponseEntity<T> exchange(@NonNull String uri, @NonNull HttpMethod httpMethod, HttpHeaders headers,
			Object content, @NonNull Type responseType) {
		if (httpMethod == HttpMethod.GET && content != null) {
			Map<String, String> uriVariables = (Map<String, String>) jsonFormat.convert(content,
					TypeDescriptor.map(LinkedHashMap.class, String.class, Object.class));
			MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
			if (uriVariables != null) {
				for (Entry<String, String> entry : uriVariables.entrySet()) {
					multiValueMap.set(entry.getKey(), entry.getValue());
				}
			}

			uri = UriComponentsBuilder.fromUriString(uri).queryParams(multiValueMap).build().toUriString();
			content = null;
		}

		String uuid = UUID.randomUUID().toString().replace("-", "");
		HttpEntity<?> httpEntity = new HttpEntity<>(content, headers);
		log.info("[{}] {} {} request entity {}", uuid, httpMethod, uri, httpEntity);
		ResponseEntity<T> responseEntity = restOperations.exchange(uri, httpMethod, httpEntity,
				new ParameterizedTypeReference<T>() {
					@Override
					public Type getType() {
						return responseType;
					}
				});
		log.info("[{}] {} {} request entity {}", uuid, httpMethod, uri, responseEntity);
		return responseEntity;
	}

	public final <T> ResponseEntity<T> getJson(String uri, HttpHeaders headers, Object request, Type responseType) {
		ResponseEntity<String> responseEntity = exchange(uri, HttpMethod.GET, headers, request, String.class);
		return toJsonResponseEntity(responseEntity, responseType);
	}

	@SuppressWarnings("unchecked")
	private <T> ResponseEntity<T> toJsonResponseEntity(ResponseEntity<String> responseEntity, Type responseType) {
		if (responseEntity.getStatusCode().isError() || !responseEntity.hasBody()) {
			return new ResponseEntity<>(null, responseEntity.getHeaders(), responseEntity.getStatusCodeValue());
		}
		T responseBody = (T) jsonFormat.convert(responseEntity.getBody(), TypeDescriptor.valueOf(responseType));
		return new ResponseEntity<T>(responseBody, responseEntity.getHeaders(), responseEntity.getStatusCodeValue());
	}

	public final <T> ResponseEntity<T> postJson(String uri, HttpHeaders headers, Object content, Type responseType) {
		if (headers == null) {
			headers = new HttpHeaders();
		}
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (content != null) {
			content = jsonFormat.convert(content, String.class);
		}
		ResponseEntity<String> responseEntity = exchange(uri, HttpMethod.POST, headers, content, String.class);
		return toJsonResponseEntity(responseEntity, responseType);
	}
}

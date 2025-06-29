package run.soeasy.starter.commons.web;

import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.json.JsonConverter;
import run.soeasy.starter.commons.json.JacksonConverter;

@Getter
@Setter
public class HttpResponseEntity<T> extends ResponseEntity<T> {
	@NonNull
	private JsonConverter jsonFormat = JacksonConverter.DEFAULT;

	public HttpResponseEntity(ResponseEntity<T> responseEntity) {
		this(responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCodeValue());
	}

	public HttpResponseEntity(T body, MultiValueMap<String, String> headers, int rawStatus) {
		super(body, headers, rawStatus);
	}

	@SuppressWarnings("unchecked")
	public <R> HttpResponseEntity<R> map(@NonNull Function<? super T, ? extends R> mapper) {
		if (getStatusCode().isError() || !hasBody()) {
			return (HttpResponseEntity<R>) this;
		}
		HttpResponseEntity<R> response = new HttpResponseEntity<>(mapper.apply(getBody()), getHeaders(),
				getStatusCodeValue());
		response.jsonFormat = this.jsonFormat;
		return response;
	}

	@SuppressWarnings("unchecked")
	public <R> HttpResponseEntity<R> map(@NonNull TypeDescriptor bodyTypeDescriptor, @NonNull Converter converter) {
		return map((body) -> (R) converter.convert(body, bodyTypeDescriptor));
	}

	public <R> HttpResponseEntity<R> map(@NonNull Class<R> bodyClass, @NonNull Converter converter) {
		return map(TypeDescriptor.valueOf(bodyClass), converter);
	}

	public <R> HttpResponseEntity<R> toJSON(Class<R> bodyClass) {
		return map(bodyClass, jsonFormat);
	}

	public <R> HttpResponseEntity<R> toJSON(TypeDescriptor bodyTypeDescriptor) {
		return map(bodyTypeDescriptor, jsonFormat);
	}
}

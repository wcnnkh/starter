package run.soeasy.starter.commons.web;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.core.convert.support.SystemConversionService;
import run.soeasy.framework.core.type.ResolvableType;
import run.soeasy.framework.json.JsonConverter;

/**
 * HTTP 响应实体封装类<br>
 * 继承自 {@link ResponseEntity}，增强响应数据的转换和处理能力，支持函数式映射、类型转换和 JSON 解析
 * 
 * <p>
 * 核心特性：
 * <ul>
 * <li>函数式映射：通过 {@code map} 方法实现响应数据的转换</li>
 * <li>类型安全转换：支持基于 {@link Converter} 的类型转换</li>
 * <li>JSON 自动解析：集成 {@link JsonConverter} 实现 JSON 响应解析</li>
 * <li>状态码便捷判断：保留原始响应状态码和头部信息</li>
 * </ul>
 * 
 * @param <T> 响应体原始类型
 * @author soeasy.run
 * @see ResponseEntity
 * @see JsonConverter
 */
@Getter
@Setter
public class HttpResponseEntity<T> extends ResponseEntity<T> {
	@NonNull
	private Converter converter = SystemConversionService.getInstance();

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
		response.converter = this.converter;
		return response;
	}

	@SuppressWarnings("unchecked")
	public <R> HttpResponseEntity<R> map(@NonNull TypeDescriptor bodyTypeDescriptor, @NonNull Converter converter) {
		return map((body) -> (R) converter.convert(body, bodyTypeDescriptor));
	}

	public <R> HttpResponseEntity<R> map(@NonNull Class<R> bodyClass, @NonNull Converter converter) {
		return map(TypeDescriptor.valueOf(bodyClass), converter);
	}

	public <R> R getBody(Class<R> requriedType) {
		return (R) converter.convert(getBody(), requriedType);
	}

	public final <R> R getBody(Type requriedType) {
		TypeDescriptor typeDescriptor = new TypeDescriptor(ResolvableType.forType(requriedType), null);
		return getBody(typeDescriptor);
	}

	@SuppressWarnings("unchecked")
	public <R> R getBody(TypeDescriptor requriedTypeDescriptor) {
		return (R) converter.convert(getBody(), requriedTypeDescriptor);
	}
}
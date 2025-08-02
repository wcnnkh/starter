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
import run.soeasy.starter.commons.jackson.JsonFormat;

/**
 * HTTP 响应实体封装类<br>
 * 继承自 {@link ResponseEntity}，增强响应数据的转换和处理能力，支持函数式映射、类型转换和 JSON 解析
 * 
 * <p>核心特性：
 * <ul>
 *     <li>函数式映射：通过 {@code map} 方法实现响应数据的转换</li>
 *     <li>类型安全转换：支持基于 {@link Converter} 的类型转换</li>
 *     <li>JSON 自动解析：集成 {@link JsonConverter} 实现 JSON 响应解析</li>
 *     <li>状态码便捷判断：保留原始响应状态码和头部信息</li>
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
    private JsonConverter jsonConverter = JsonFormat.DEFAULT;

    /**
     * 基于原生 ResponseEntity 创建封装实例
     * @param responseEntity 原生响应实体
     */
    public HttpResponseEntity(ResponseEntity<T> responseEntity) {
        this(responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCodeValue());
    }

    /**
     * 构造完整响应实体（含 body、headers、状态码）
     * @param body 响应体内容
     * @param headers 响应头集合
     * @param rawStatus 原始状态码（如 200、404）
     */
    public HttpResponseEntity(T body, MultiValueMap<String, String> headers, int rawStatus) {
        super(body, headers, rawStatus);
    }

    /**
     * 函数式映射响应体（支持错误状态短路）
     * 
     * <p>执行逻辑：
     * <ul>
     *     <li>若响应状态为错误或无响应体，直接返回当前实例</li>
     *     <li>否则使用函数转换响应体并创建新实例</li>
     * </ul>
     * 
     * @param mapper 转换函数（输入：原始类型，输出：目标类型）
     * @param <R> 目标类型
     * @return 转换后的响应实体
     */
    @SuppressWarnings("unchecked")
    public <R> HttpResponseEntity<R> map(@NonNull Function<? super T, ? extends R> mapper) {
        if (getStatusCode().isError() || !hasBody()) {
            return (HttpResponseEntity<R>) this;
        }
        HttpResponseEntity<R> response = new HttpResponseEntity<>(mapper.apply(getBody()), getHeaders(),
                getStatusCodeValue());
        response.jsonConverter = this.jsonConverter;
        return response;
    }

    /**
     * 基于转换器映射响应体（支持泛型类型转换）
     * 
     * @param bodyTypeDescriptor 目标类型描述符
     * @param converter 类型转换器
     * @param <R> 目标类型
     * @return 转换后的响应实体
     */
    @SuppressWarnings("unchecked")
    public <R> HttpResponseEntity<R> map(@NonNull TypeDescriptor bodyTypeDescriptor, @NonNull Converter converter) {
        return map((body) -> (R) converter.convert(body, bodyTypeDescriptor));
    }

    /**
     * 基于类类型映射响应体（简化类型描述符创建）
     * 
     * @param bodyClass 目标类型类
     * @param converter 类型转换器
     * @param <R> 目标类型
     * @return 转换后的响应实体
     */
    public <R> HttpResponseEntity<R> map(@NonNull Class<R> bodyClass, @NonNull Converter converter) {
        return map(TypeDescriptor.valueOf(bodyClass), converter);
    }

    /**
     * 使用 JSON 转换器解析响应体（默认转换器）
     * 
     * @param bodyClass 目标类型类
     * @param <R> 目标类型
     * @return JSON 解析后的响应实体
     */
    public <R> HttpResponseEntity<R> toJSON(Class<R> bodyClass) {
        return map(bodyClass, jsonConverter);
    }

    /**
     * 使用 JSON 转换器解析响应体（基于类型描述符）
     * 
     * @param bodyTypeDescriptor 目标类型描述符
     * @param <R> 目标类型
     * @return JSON 解析后的响应实体
     */
    public <R> HttpResponseEntity<R> toJSON(TypeDescriptor bodyTypeDescriptor) {
        return map(bodyTypeDescriptor, jsonConverter);
    }
}
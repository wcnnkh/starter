package run.soeasy.starter.commons.web;

import java.lang.reflect.Type;
import java.util.function.Function;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import lombok.NonNull;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.core.convert.support.SystemConversionService;
import run.soeasy.framework.core.type.ResolvableType;

/**
 * 扩展Spring {@link ResponseEntity} 的响应实体接口，核心增强类型转换与转换器工厂管理能力
 * 
 * <p>该接口在保留Spring原生响应核心信息（响应体、状态码、响应头）的基础上，提供三大核心能力：
 * <ul>
 * <li>{@code cast} 类型转换：支持将响应体转换为指定Class/泛型Type（解决泛型擦除场景）</li>
 * <li>{@code map} 函数式转换：通过Lambda表达式灵活处理响应体，或基于媒体类型（ContentType）自动匹配转换器</li>
 * <li>{@code assignableFrom} 转换器工厂切换：动态替换用于类型转换的{@link MediaTypeConverterFactory}，适配不同数据格式</li>
 * </ul>
 * 适用于需要在响应处理链路中动态适配数据类型、灵活切换转换策略的场景，与框架的类型转换体系（{@link Converter}、{@link SystemConversionService}）深度集成。
 *
 * @param <T> 响应体的原始数据类型
 * @see MediaTypeConverterFactory 媒体类型转换器工厂，用于匹配ContentType对应的转换器
 * @see MappedHttpResponseEntity 函数式转换的实现类
 * @see AssignableHttpResponseEntity 转换器工厂管理的实现类
 * @see org.springframework.http.ResponseEntity Spring原生响应实体
 */
public interface HttpResponseEntity<T> {

    /**
     * 静态工厂方法：创建支持指定转换器工厂的{@link HttpResponseEntity}实例
     * 
     * <p>将Spring原生{@link ResponseEntity}包装为扩展接口实例，并绑定自定义的{@link MediaTypeConverterFactory}，
     * 后续通过{@code cast}或{@code map}转换时将优先使用该工厂的转换器。
     *
     * @param responseEntity        Spring原生响应实体（不可为null，提供原始响应数据）
     * @param mediaTypeConverterFactory 用于类型转换的媒体类型转换器工厂（不可为null）
     * @param <V>                   响应体类型
     * @return 绑定了转换器工厂的{@link HttpResponseEntity}实例（具体为{@link StandardHttpResponseEntity}）
     */
    public static <V> HttpResponseEntity<V> assignableFrom(@NonNull ResponseEntity<V> responseEntity,
                                                           MediaTypeConverterFactory mediaTypeConverterFactory) {
        return new StandardHttpResponseEntity<>(responseEntity, mediaTypeConverterFactory);
    }

    /**
     * 获取响应体内容
     *
     * @return 响应体对象（类型为<T>），可能为null（需通过{@link #hasBody()}判断是否存在）
     */
    T getBody();

    /**
     * 获取HTTP响应头信息
     *
     * @return 包含所有响应头的{@link HttpHeaders}对象（非null，空响应头返回空对象）
     */
    HttpHeaders getHeaders();

    /**
     * 获取HTTP状态码（枚举形式）
     *
     * @return 表示响应状态的{@link HttpStatus}枚举（非null）
     */
    HttpStatus getStatusCode();

    /**
     * 获取HTTP状态码的数值形式
     *
     * @return 状态码整数（如200表示成功、404表示资源未找到）
     */
    int getStatusCodeValue();

    /**
     * 判断响应是否包含非空响应体
     *
     * @return true：响应体存在且非null；false：响应体为null或无响应体（如204 No Content）
     */
    boolean hasBody();

    /**
     * 将响应体转换为指定泛型类型（支持复杂类型如List<T>、Map<K,V>）
     * 
     * <p>委托给{@link #map(Type, MediaTypeConverterFactory)}实现，默认不指定转换器工厂（null），
     * 转换逻辑将自动按优先级查找转换器（系统注册表→默认转换服务）。
     *
     * @param bodyType 目标泛型类型的{@link Type}对象（如通过{@code new TypeToken<List<User>>(){}.getType()}获取，不可为null）
     * @param <R>      转换后的响应体类型
     * @return 包含转换后响应体的新{@link HttpResponseEntity}实例
     */
    default <R> HttpResponseEntity<R> cast(Type bodyType) {
        return map(bodyType, null);
    }

    /**
     * 将响应体转换为指定普通Class类型（非泛型）
     * 
     * <p>委托给{@link #map(Type, MediaTypeConverterFactory)}实现，逻辑与泛型转换一致，
     * 适用于简单类型（如String、User、Integer）的转换。
     *
     * @param bodyType 目标类型的{@link Class}对象（不可为null）
     * @param <R>      转换后的响应体类型
     * @return 包含转换后响应体的新{@link HttpResponseEntity}实例
     */
    default <R> HttpResponseEntity<R> cast(Class<R> bodyType) {
        return map(bodyType, null);
    }

    /**
     * 切换当前响应实体使用的媒体类型转换器工厂
     * 
     * <p>创建{@link AssignableHttpResponseEntity}实例，绑定新的转换器工厂，后续通过{@code cast}或{@code map}转换时，
     * 将优先使用该工厂的转换器（替代原有的转换器逻辑）。
     *
     * @param mediaTypeConverterFactory 新的媒体类型转换器工厂（不可为null）
     * @return 绑定了新转换器工厂的{@link HttpResponseEntity}实例（具体为{@link AssignableHttpResponseEntity}）
     */
    default HttpResponseEntity<T> assignableFrom(@NonNull MediaTypeConverterFactory mediaTypeConverterFactory) {
        return new AssignableHttpResponseEntity<>(this, mediaTypeConverterFactory);
    }

    /**
     * 通过函数式接口将响应体转换为目标类型
     * 
     * <p>基于{@link MappedHttpResponseEntity}实现，直接使用传入的Lambda表达式处理原始响应体，
     * 不依赖转换器工厂，适用于自定义转换逻辑（如简单数据裁剪、字段映射）。
     *
     * @param mapper 转换函数（接收原始类型<T>，返回目标类型<R>，不可为null）
     * @param <R>    转换后的响应体类型
     * @return 包含转换后响应体的新{@link HttpResponseEntity}实例（具体为{@link MappedHttpResponseEntity}）
     */
    default <R> HttpResponseEntity<R> map(@NonNull Function<? super T, ? extends R> mapper) {
        return new MappedHttpResponseEntity<>(this, mapper);
    }

    /**
     * 根据目标类型和转换器工厂，自动完成响应体转换（核心转换逻辑）
     * 
     * <p>转换优先级与流程：
     * 1. <b>非泛型类型检查</b>：若目标类型无泛型参数，且原始响应体可直接强转为目标类型，则直接强转（避免冗余转换）；
     * 2. <b>转换器查找</b>：根据响应头的ContentType，按以下顺序找转换器：
     *    - 优先使用传入的{@code converterFactory}获取转换器；
     *    - 工厂无匹配时，从{@link MediaTypeConverterRegistry}系统注册表查找；
     *    - 注册表无匹配时，使用{@link SystemConversionService}默认转换服务；
     * 3. <b>执行转换</b>：通过找到的转换器将原始响应体转换为目标类型。
     *
     * @param bodyType         目标类型（支持泛型，不可为null）
     * @param converterFactory 用于查找转换器的工厂（可为null，null时跳过该优先级）
     * @param <R>              转换后的响应体类型
     * @return 包含转换后响应体的新{@link HttpResponseEntity}实例
     */
    @SuppressWarnings("unchecked")
    default <R> HttpResponseEntity<R> map(@NonNull Type bodyType, MediaTypeConverterFactory converterFactory) {
        return map((e) -> {
            // 将目标Type转换为可解析类型，便于处理泛型参数
            ResolvableType resolvableType = ResolvableType.forType(bodyType);
            // 处理非泛型类型：直接强转（若类型兼容）
            if (!resolvableType.hasActualTypeArguments()) {
                if (resolvableType.getRawType().isInstance(e)) {
                    return (R) resolvableType.getRawType().cast(e);
                }
            }

            // 从响应头获取ContentType，用于匹配转换器
            MediaType contentType = getHeaders().getContentType();
            Converter converter = null;
            if (contentType != null) {
                // 1. 优先使用传入的转换器工厂
                converter = converterFactory == null ? null : converterFactory.getConverter(contentType);
                // 2. 工厂无匹配时，从系统注册表获取
                if (converter == null) {
                    converter = MediaTypeConverterRegistry.system().getConverter(contentType);
                }
            }

            // 3. 无匹配转换器时，使用默认系统转换服务
            if (converter == null) {
                converter = SystemConversionService.getInstance();
            }

            // 执行转换：基于可解析类型的TypeDescriptor
            return (R) converter.convert(e, TypeDescriptor.forType(resolvableType));
        });
    }

    /**
     * 将当前扩展响应实体转换为Spring原生{@link ResponseEntity}
     * 
     * <p>用于与Spring原生API（如RestTemplate、Controller返回值）交互，保留所有核心响应信息：
     * - 响应体：若存在（{@link #hasBody()}为true）则返回原始响应体，否则为null；
     * - 响应头：直接复用当前响应头；
     * - 状态码：使用数值形式构建原生状态码。
     *
     * @return 与当前响应信息一致的Spring原生{@link ResponseEntity}实例
     */
    default ResponseEntity<T> shared() {
        return new ResponseEntity<T>(hasBody() ? getBody() : null, getHeaders(), getStatusCodeValue());
    }
}
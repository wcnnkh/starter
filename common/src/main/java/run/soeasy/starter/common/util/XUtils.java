package run.soeasy.starter.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import run.soeasy.framework.core.RandomUtils;
import run.soeasy.framework.core.collection.CollectionUtils;

/**
 * 通用工具类集合
 * 
 * @author soeasy.run
 *
 */
@UtilityClass
public class XUtils {

	private static final TypeFilter INCLUDE_ALL_TYPE_FILTER = (a, b) -> true;

	/**
	 * 大写英文字母常量池，包含 A-Z 共 26 个字符。
	 */
	public static final String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	/**
	 * 小写英文字母常量池，通过 {@link #CAPITAL_LETTERS} 转小写生成，包含 a-z 共 26 个字符。
	 */
	public static final String LOWERCASE_LETTERS = CAPITAL_LETTERS.toLowerCase();

	/**
	 * 数字字符常量池，包含 0-9 共 10 个字符。
	 */
	public static final String NUMBERIC_CHARACTER = "0123456789";

	/**
	 * 易区分字符常量池，排除了视觉上易混淆的字符（如 0/O、1/I、l 等），特别适用于验证码、临时密码等用户可视化场景。
	 * <p>
	 * 字符集组成：{@link #NUMBERIC_CHARACTER} + 小写易区分字母（acdefhkmnprstvwy） +
	 * 大写易区分字母（ABCEFGHKMNRSTVWY）。
	 */
	public static final CharSequence EASY_TO_DISTINGUISH = NUMBERIC_CHARACTER + "acdefhkmnprstvwyABCEFGHKMNRSTVWY";

	/**
	 * 全字符常量池，包含所有大小写英文字母和数字，共 62 个字符 (26+26+10)。
	 */
	public static final String ALL = CAPITAL_LETTERS + LOWERCASE_LETTERS + NUMBERIC_CHARACTER;

	/**
	 * 生成一个包含大小写字母和数字的随机字符串（基于 {@link #ALL} 字符集）。
	 *
	 * @param length 随机字符串的目标长度，必须大于等于 0。若长度为 0，则返回空字符串。
	 * @return 一个长度为 {@code length} 的随机字符串。
	 * @see RandomUtils#random(CharSequence, int)
	 */
	public static String randomString(int length) {
		return RandomUtils.random(ALL, length);
	}

	/**
	 * 生成一个纯数字的随机字符串（基于 {@link #NUMBERIC_CHARACTER} 字符集）。
	 * <p>
	 * 适用于生成短信验证码、邮箱验证码、随机编号等场景。请注意，生成的字符串可能以'0'开头。
	 *
	 * @param length 随机数字字符串的目标长度，必须大于等于 0。若长度为 0，则返回空字符串。
	 * @return 一个长度为 {@code length} 的纯数字字符串。
	 */
	public static String randomNumber(int length) {
		return RandomUtils.random(NUMBERIC_CHARACTER, length);
	}

	/**
	 * 生成一个由易区分字符组成的随机字符串（基于 {@link #EASY_TO_DISTINGUISH} 字符集）。
	 * <p>
	 * 该方法生成的字符串排除了易混淆字符，能有效避免用户在肉眼识别时产生错误，非常适合用于：
	 * <ul>
	 * <li>图形验证码、扫码登录验证码</li>
	 * <li>临时登录密码、设备激活码</li>
	 * </ul>
	 *
	 * @param length 随机字符串的目标长度，必须大于等于 0。若长度为 0，则返回空字符串。
	 * @return 一个长度为 {@code length} 的易区分随机字符串。
	 */
	public static String randomCode(int length) {
		return RandomUtils.random(EASY_TO_DISTINGUISH, length);
	}

	/**
	 * 在指定的基础包路径下扫描所有类，并返回它们的 {@link BeanDefinition}。
	 * <p>
	 * 此方法使用默认的类加载器和一个匹配所有类的过滤器。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @return 一个包含所有扫描到的 {@link BeanDefinition} 的 {@link Set}。
	 * @see #scanBeanDefinitions(String, ClassLoader, TypeFilter)
	 */
	public static Set<BeanDefinition> scanBeanDefinitions(@NonNull String basePackage) {
		return scanBeanDefinitions(basePackage, null);
	}

	/**
	 * 使用指定的类加载器，在指定的基础包路径下扫描所有类，并返回它们的 {@link BeanDefinition}。
	 * <p>
	 * 此方法使用一个匹配所有类的过滤器。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @param classLoader 用于加载资源的类加载器，可以为 {@code null}，此时将使用默认的类加载器。
	 * @return 一个包含所有扫描到的 {@link BeanDefinition} 的 {@link Set}。
	 * @see #scanBeanDefinitions(String, ClassLoader, TypeFilter)
	 */
	public static Set<BeanDefinition> scanBeanDefinitions(@NonNull String basePackage, ClassLoader classLoader) {
		return scanBeanDefinitions(basePackage, classLoader, INCLUDE_ALL_TYPE_FILTER);
	}

	/**
	 * 使用指定的类加载器和类型过滤器，在指定的基础包路径下扫描类，并返回符合条件的 {@link BeanDefinition}。
	 * <p>
	 * 这是组件扫描功能最灵活的实现，允许完全定制扫描过程。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @param classLoader 用于加载资源的类加载器，可以为 {@code null}。
	 * @param typeFilter  用于筛选类的 {@link TypeFilter}，不能为空。
	 * @return 一个包含所有符合条件的 {@link BeanDefinition} 的 {@link Set}。
	 */
	public static Set<BeanDefinition> scanBeanDefinitions(@NonNull String basePackage, ClassLoader classLoader,
			@NonNull TypeFilter typeFilter) {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(true);
		if (classLoader != null) {
			scanner.setResourceLoader(new DefaultResourceLoader(classLoader));
		}
		scanner.addIncludeFilter(typeFilter);
		return scanner.findCandidateComponents(basePackage);
	}

	/**
	 * 将一组 {@link BeanDefinition} 转换为对应的 {@link Class} 对象集合。
	 * <p>
	 * 此方法会遍历 {@link BeanDefinition}，并尝试加载其指定的类。任何加载失败的类都会被静默忽略。
	 *
	 * @param beanDefinitions 要转换的 {@link BeanDefinition} 集合。
	 * @param classLoader     用于加载类的类加载器，可以为 {@code null}。
	 * @return 一个包含成功加载的 {@link Class} 对象的 {@link Set}。
	 */
	private static Set<Class<?>> toClassSet(Set<BeanDefinition> beanDefinitions, ClassLoader classLoader) {
		return beanDefinitions.stream().map((def) -> {
			try {
				return ClassUtils.forName(def.getBeanClassName(), classLoader);
			} catch (Throwable e) {
				// ignore
			}
			return null;
		}).filter((e) -> e != null).collect(Collectors.toSet());
	}

	/**
	 * 使用指定的类加载器和类型过滤器，在指定的基础包路径下扫描类，并返回符合条件的 {@link Class} 对象集合。
	 * <p>
	 * 这是 {@link #scanBeanDefinitions(String, ClassLoader, TypeFilter)}
	 * 的便捷方法，直接返回加载好的 Class 对象。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @param classLoader 用于加载类的类加载器，可以为 {@code null}。
	 * @param typeFilter  用于筛选类的 {@link TypeFilter}，不能为空。
	 * @return 一个包含所有符合条件的 {@link Class} 对象的 {@link Set}。
	 */
	public static Set<Class<?>> scanClasses(@NonNull String basePackage, ClassLoader classLoader,
			@NonNull TypeFilter typeFilter) {
		return toClassSet(scanBeanDefinitions(basePackage, classLoader, typeFilter), classLoader);
	}

	/**
	 * 使用指定的类加载器，在指定的基础包路径下扫描所有类，并返回它们的 {@link Class} 对象集合。
	 * <p>
	 * 此方法使用一个匹配所有类的过滤器。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @param classLoader 用于加载类的类加载器，可以为 {@code null}。
	 * @return 一个包含所有扫描到的 {@link Class} 对象的 {@link Set}。
	 */
	public static Set<Class<?>> scanClasses(@NonNull String basePackage, ClassLoader classLoader) {
		return toClassSet(scanBeanDefinitions(basePackage, classLoader), classLoader);
	}

	/**
	 * 在指定的基础包路径下扫描所有类，并返回它们的 {@link Class} 对象集合。
	 * <p>
	 * 此方法使用默认的类加载器和一个匹配所有类的过滤器。
	 *
	 * @param basePackage 要扫描的基础包路径，不能为空。
	 * @return 一个包含所有扫描到的 {@link Class} 对象的 {@link Set}。
	 */
	public static Set<Class<?>> scanClasses(@NonNull String basePackage) {
		return toClassSet(scanBeanDefinitions(basePackage), null);
	}
	
	public static <S, T> Set<T> mapToNonNullSet(Collection<? extends S> source,
			@NonNull Function<? super S, ? extends T> mapper) {
		if (CollectionUtils.isEmpty(source)) {
			return Collections.emptySet();
		}

		return source.stream().filter((e) -> e != null).map(mapper).filter((e) -> e != null)
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}
}
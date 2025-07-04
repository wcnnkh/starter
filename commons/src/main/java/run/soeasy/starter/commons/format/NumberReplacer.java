package run.soeasy.starter.commons.format;

import java.math.BigDecimal;

import lombok.NonNull;
import run.soeasy.framework.codec.Codec;
import run.soeasy.framework.codec.DecodeException;
import run.soeasy.framework.codec.EncodeException;
import run.soeasy.framework.core.Assert;
import run.soeasy.framework.core.StringUtils;
import run.soeasy.framework.core.math.NumberUtils;

public final class NumberReplacer implements Codec<BigDecimal, String> {
	private static final String[] CHINESE_NUMBERS = new String[] { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
	private static final String[] CAPITALIZE_CHINESE_NUMBERS = new String[] { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒",
			"捌", "玖", "拾" };

	private final String[] mapping;
	private final boolean stripTrailingZeros;

	/**
	 * 小写
	 */
	public static final NumberReplacer LOWERCASE = new NumberReplacer(false, true);
	/**
	 * 大写
	 */
	public static final NumberReplacer CAPITALIZE = new NumberReplacer(true, true);

	public NumberReplacer(boolean capitalize, boolean stripTrailingZeros) {
		this(capitalize ? CAPITALIZE_CHINESE_NUMBERS : CHINESE_NUMBERS, stripTrailingZeros);
	}

	public NumberReplacer(@NonNull String[] mapping, boolean stripTrailingZeros) {
		this.mapping = mapping;
		this.stripTrailingZeros = stripTrailingZeros;
	}

	public String[] getMapping() {
		return mapping.clone();
	}

	public boolean isStripTrailingZeros() {
		return stripTrailingZeros;
	}

	@Override
	public String encode(BigDecimal source) throws EncodeException {
		if (source == null) {
			return null;
		}

		String value = (stripTrailingZeros ? NumberUtils.stripTrailingZeros(source) : source).toPlainString();
		return encode(value);
	}

	public String encode(CharSequence source) throws EncodeException {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < source.length(); i++) {
			char v = source.charAt(i);
			if (Character.isDigit(v)) {
				sb.append(format(v));
				continue;
			}
			sb.append(v);
		}
		return sb.toString();
	}

	@Override
	public BigDecimal decode(String source) throws DecodeException {
		if (source == null) {
			return null;
		}

		String value = source;
		for (int i = 0; i < mapping.length; i++) {
			value = value.replaceAll(mapping[i], i + "");
		}

		BigDecimal number = new BigDecimal(value);
		return stripTrailingZeros ? NumberUtils.stripTrailingZeros(number) : number;
	}

	public String format(char source) {
		Assert.isTrue(Character.isDigit(source), "Source must be a digital");
		// 48-57是0-9的ASCII值
		return mapping[Character.getNumericValue(source)];
	}

	/**
	 * @param source
	 * @return 返回-1说明不存在
	 */
	public int parse(String source) {
		if (StringUtils.isEmpty(source)) {
			return -1;
		}

		for (int i = 0; i < mapping.length; i++) {
			if (StringUtils.equals(source, mapping[i])) {
				return i;
			}
		}
		return -1;
	}

	public boolean contains(String source) {
		return parse(source) != -1;
	}

	public boolean exists(String source) {
		if (StringUtils.isEmpty(source)) {
			return false;
		}

		for (String v : mapping) {
			if (source.indexOf(v) != -1) {
				return true;
			}
		}
		return false;
	}
}

package run.soeasy.starter.commons.format;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.NonNull;
import run.soeasy.framework.codec.Codec;
import run.soeasy.framework.codec.DecodeException;
import run.soeasy.framework.codec.EncodeException;
import run.soeasy.framework.core.collection.ArrayUtils;
import run.soeasy.framework.core.math.NumberUnit;
import run.soeasy.framework.core.math.NumberUtils;

public final class RmbFormat implements Codec<BigDecimal, String> {
	/**
	 * 大写
	 * 
	 * @see NumberReplacer#CAPITALIZE
	 */
	public static final RmbFormat CAPITALIZE = new RmbFormat(NumberReplacer.CAPITALIZE);

	/**
	 * 小写
	 * 
	 * @see NumberReplacer#LOWERCASE
	 */
	public static final RmbFormat LOWERCASE = new RmbFormat(NumberReplacer.LOWERCASE);

	private final Codec<BigDecimal, String> numberCodec;
	private final NumberUnit[] integerUnits;
	private final NumberUnit[] decimalUnits;

	public RmbFormat(Codec<BigDecimal, String> numberCodec) {
		this(numberCodec,
				new NumberUnit[] { MoneyUnit.亿, MoneyUnit.万, MoneyUnit.仟, MoneyUnit.佰, MoneyUnit.拾, MoneyUnit.元 },
				new NumberUnit[] { MoneyUnit.角, MoneyUnit.分 });
	}

	public RmbFormat(@NonNull Codec<BigDecimal, String> numberCodec, @NonNull NumberUnit[] integerUnits,
			@NonNull NumberUnit[] decimalUnits) {
		this.numberCodec = numberCodec;
		this.integerUnits = integerUnits;
		this.decimalUnits = decimalUnits;
	}

	public Codec<BigDecimal, String> getNumberCodec() {
		return numberCodec;
	}

	@Override
	public String encode(BigDecimal money) throws EncodeException {
		StringBuilder sb = new StringBuilder();
		BigDecimal number = money.abs();
		BigDecimal[] decimals = number.divideAndRemainder(BigDecimal.ONE);
		if (decimals[0].compareTo(BigDecimal.ZERO) == 0) {
			sb.append(numberCodec.encode(BigDecimal.ZERO));
			sb.append("元");
		} else {
			sb.append(NumberUtils.format(decimals[0], numberCodec::encode, integerUnits));
		}

		decimals[1] = decimals[1].setScale(2, RoundingMode.HALF_UP);
		if (decimals[1].compareTo(BigDecimal.ZERO) == 0) {
			// 整数
			sb.append("整");
		} else {
			sb.append(NumberUtils.format(decimals[1], numberCodec::encode, decimalUnits));
		}
		return sb.toString();
	}

	@Override
	public BigDecimal decode(String money) throws DecodeException {
		int index = money.indexOf("整");
		if (index == -1) {
			// 不是整数
			index = money.indexOf("元");
			if (index == -1) {
				// 内嵌的
				return NumberUtils.parse(money, numberCodec::decode, ArrayUtils.merge(integerUnits, decimalUnits));
			} else {
				return NumberUtils.parse(money.substring(0, index), this::decode, integerUnits)
						.add(NumberUtils.parse(money.substring(index + 1), this::decode, decimalUnits));
			}
		} else {
			// 是整数
			return NumberUtils.parse(money.substring(0, index), this::decode, integerUnits);
		}
	}

	public String encode(long money) {
		return encode(new BigDecimal(money));
	}
}

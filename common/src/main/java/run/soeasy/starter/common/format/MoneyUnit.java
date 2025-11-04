package run.soeasy.starter.common.format;

import java.math.BigDecimal;

import lombok.Getter;
import run.soeasy.framework.core.math.NumberUnit;

@Getter
public enum MoneyUnit implements NumberUnit{
	分("分", "0.01"),
	角("角", "0.1"),
	元("元", "1"),
	拾("拾", "10"),
	佰("佰", "100"),
	仟("仟", "1000"),
	万("万", "10000"),
	亿("亿", "100000000")
	;
	private final String name;
	private final BigDecimal radix;

	MoneyUnit(String name, String radix) {
		this.name = name;
		this.radix = new BigDecimal(radix);
	}
}

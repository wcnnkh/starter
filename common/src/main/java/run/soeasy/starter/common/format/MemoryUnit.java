package run.soeasy.starter.common.format;

import java.math.BigDecimal;

import lombok.Getter;
import run.soeasy.framework.core.math.NumberUnit;


@Getter
public enum MemoryUnit implements NumberUnit {
	B("B", 1l),
	KB("KB", 1024L),
	MB("MB", 1024L * 1024L),
	GB("GB", 1024L * 1024L * 1024L),
	TB("TB", 1024L * 1024L * 1024L * 1024L)
	;
	
	private final String name;
	private final BigDecimal radix;

	MemoryUnit(String name, long radix) {
		this.name = name;
		this.radix = BigDecimal.valueOf(radix);
	}
}

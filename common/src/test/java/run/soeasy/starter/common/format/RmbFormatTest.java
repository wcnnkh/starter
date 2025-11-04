package run.soeasy.starter.common.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.TypeDescriptor;

public class RmbFormatTest {
	@Test
	public void test() {
		TypeDescriptor typeDescriptor = TypeDescriptor.map(LinkedHashMap.class, TypeDescriptor.valueOf(String.class),
				TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(String.class)));
		System.out.println(typeDescriptor.getMapValueTypeDescriptor());
		assertTrue(RmbFormat.CAPITALIZE.encode(1112).equals("壹仟壹佰壹拾贰元整"));
		assertTrue(RmbFormat.CAPITALIZE.encode(new BigDecimal("0.12")).equals("零元壹角贰分"));
		assertTrue(RmbFormat.CAPITALIZE.encode(new BigDecimal("123456789.12")).equals("壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元壹角贰分"));
		assertTrue(RmbFormat.CAPITALIZE.decode("壹亿贰仟叁佰肆拾伍万陆仟柒佰捌拾玖元壹角贰分").equals(new BigDecimal("123456789.12")));
		assertEquals(RmbFormat.CAPITALIZE.encode(new BigDecimal("0.125")), "零元壹角叁分");
	}
}

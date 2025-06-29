package run.soeasy.starter.commons.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NumberReplacerTest {
	@Test
	public void test() {
		assertEquals("零一二三四五六七八九", NumberReplacer.LOWERCASE.encode("0123456789"));
		assertEquals("零壹贰叁肆伍陆柒捌玖", NumberReplacer.CAPITALIZE.encode("0123456789"));
	}
}

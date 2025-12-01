package run.soeasy.starter.common.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.Test;

import run.soeasy.starter.commons.util.XUtils;

public class XUtilsTest {

	@Test
	public void testScan() {
		// 1. 获取当前测试类所在的包名
		String packageName = this.getClass().getPackage().getName();
		System.out.println("开始扫描包: " + packageName);

		// 2. 执行扫描
		Set<Class<?>> classSet = XUtils.scanClasses(packageName);

		// 3. 打印扫描结果
		System.out.println("扫描到的类数量: " + classSet.size());

		// 4. 断言：至少应该扫描到当前测试类 XUtilsTest 本身
		// 注意：如果你的 XUtils.scan 方法在扫描时排除了测试类，这个断言会失败。
		// 但根据我们当前的配置 (useDefaultFilters = false)，它应该能扫描到。
		assertTrue(classSet.size() > 0, "扫描结果为空，请检查配置或包路径。");
	}

	@Test
	public void getResources() throws IOException {
		assert XUtils.getResources(getClass().getPackage().getName().replace(".", "/") + "/**.*").length > 0;
	}
}
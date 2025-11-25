package run.soeasy.starter.redis.sequences;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.LongSupplier;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import lombok.NonNull;
import run.soeasy.framework.core.StringUtils;
import run.soeasy.starter.common.util.XUtils;

/**
 * 基于Spring Data Redis {@link StringRedisTemplate} 的Redis序列实现。
 */
public class StringRedisTemplateSequence extends AbstractRedisSequence {

	@NonNull
	private final StringRedisTemplate stringRedisTemplate;

	// 预编译的Redis脚本对象，提高执行效率
	private static final DefaultRedisScript<Long> NEXT_VALUE_REDIS_SCRIPT = new DefaultRedisScript<>();

	static {
		String location = AbstractRedisSequence.class.getPackage().getName().replace(".", "/")
				+ "/stringRedisTemplate.lua";
		String script;
		try {
			script = XUtils.getResource(location).toCharSequence().toString();
		} catch (IOException e) {
			throw new IllegalStateException("无法获取脚本资源:" + location);
		}
		NEXT_VALUE_REDIS_SCRIPT.setScriptText(script);
		NEXT_VALUE_REDIS_SCRIPT.setResultType(Long.class);
	}

	/**
	 * 构造函数。
	 *
	 * @param key                  序列在Redis中的键
	 * @param initialValueSupplier 序列初始值提供者
	 * @param stringRedisTemplate  StringRedisTemplate实例
	 */
	public StringRedisTemplateSequence(String key, LongSupplier initialValueSupplier,
			StringRedisTemplate stringRedisTemplate) {
		super(key, initialValueSupplier);
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Override
	protected Long executeNextValue(String key, Long step, Long defaultValue) {
		return stringRedisTemplate.execute(NEXT_VALUE_REDIS_SCRIPT, Arrays.asList(key), step.toString(), defaultValue == null? null:defaultValue.toString());
	}

	@Override
	protected Long getKeyAsLong(String key) {
		String value = stringRedisTemplate.opsForValue().get(key);
		if (StringUtils.isEmpty(value)) {
			return null;
		}
		return Long.parseLong(value);
	}

	@Override
	protected void setKey(String key, long value) {
		stringRedisTemplate.opsForValue().set(key, String.valueOf(value));
	}

	@Override
	protected void deleteKey(String key) {
		stringRedisTemplate.delete(key);
	}
}
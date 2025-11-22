package run.soeasy.starter.redis.sequences;

import java.util.List;
import java.util.function.LongSupplier;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import lombok.NonNull;

/**
 * 基于Spring Data Redis {@link StringRedisTemplate} 的Redis序列实现。
 */
public class StringRedisTemplateSequence extends AbstractRedisSequence {

	@NonNull
	private final StringRedisTemplate stringRedisTemplate;

	// 预编译的Redis脚本对象，提高执行效率
	private static final DefaultRedisScript<Long> NEXT_VALUE_REDIS_SCRIPT = new DefaultRedisScript<>();

	static {
		NEXT_VALUE_REDIS_SCRIPT.setScriptText(NEXT_VALUE_LUA_SCRIPT);
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
	protected Long executeNextValueScript(List<String> keys, List<Object> args) {
		return stringRedisTemplate.execute(NEXT_VALUE_REDIS_SCRIPT, keys, args.toArray());
	}

	@Override
	protected Long getKeyAsLong(String key) {
		String value = stringRedisTemplate.opsForValue().get(key);
		if (value == null) {
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
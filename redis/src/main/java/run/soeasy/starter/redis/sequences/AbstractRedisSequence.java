package run.soeasy.starter.redis.sequences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.LongSupplier;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import run.soeasy.starter.common.sequences.PersistentSequence;

/**
 * 基于Redis的分布式序列生成器抽象基类。 统一封装公共逻辑（Lua脚本、参数校验等），具体Redis操作由子类实现。
 *
 * @author soeasy.run
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public abstract class AbstractRedisSequence implements PersistentSequence {

	@NonNull
	protected final String key;
	@NonNull
	protected final LongSupplier initialValueSupplier;

	/**
	 * 原子获取下一个序列值的Lua脚本。 逻辑：存在则自增步长返回新值；不存在则用默认值初始化并返回；无默认值则返回null。
	 */
	protected static final String NEXT_VALUE_LUA_SCRIPT = "local current = redis.call('get', KEYS[1])\n"
			+ "if current then\n" + "    current = tonumber(current)\n" + "    local newValue = current + ARGV[1]\n"
			+ "    redis.call('set', KEYS[1], newValue)\n" + "    return newValue\n" + "else\n"
			+ "    local defaultValue = ARGV[2]\n" + "    if defaultValue then\n"
			+ "        redis.call('set', KEYS[1], defaultValue)\n" + "        return tonumber(defaultValue)\n"
			+ "    else\n" + "        return nil\n" + "    end\n" + "end";

	/**
	 * {@inheritDoc}
	 * <p>
	 * 公共逻辑：检查键是否存在，存在则获取值，不存在则调用初始值提供者。
	 */
	@Override
	public long getAsLong() {
		Long currentValue = getKeyAsLong(key);
		if (currentValue != null) {
			return currentValue;
		}
		return initialValueSupplier.getAsLong();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * 公共逻辑：参数校验 + 构造脚本参数 + 调用子类脚本执行方法。
	 */
	@Override
	public Long next(@NonNull Long step, Long defaultValue) {
		if (step <= 0) {
			throw new IllegalArgumentException("步长必须是大于0的正整数");
		}

		List<String> keys = Collections.singletonList(key);
		List<Object> args = new ArrayList<>();
		args.add(step.toString()); // 步长转字符串（适配所有客户端）
		args.add(defaultValue != null ? defaultValue.toString() : null); // 默认值转字符串或null

		Long result = executeNextValueScript(keys, args);

		if (result != null && log.isTraceEnabled()) {
			log.trace("序列[{}]生成下一个值：{}", key, result);
		}
		return result;
	}

	/**
	 * 重置（删除）当前序列。
	 * <p>
	 * 调用后，下一次调用{@link #next(Long, Long)}将会重新初始化序列。
	 */
	public void reset() {
		deleteKey(key);
		log.info("序列[{}]已重置（删除键）", key);
	}

	/**
	 * 强制设置序列的当前值。
	 * <p>
	 * 此方法会覆盖任何现有值，谨慎使用。
	 *
	 * @param value 要设置的新值。
	 */
	public void setValue(long value) {
		setKey(key, value);
		log.info("序列[{}]已强制设置值：{}", key, value);
	}

	/**
	 * 获取当前序列的当前值。
	 *
	 * @return 当前序列值，如果键不存在或值非法则返回0。
	 */
	public long getCurrentValue() {
		Long value = getKeyAsLong(key);
		return value != null ? value : 0;
	}

	// ------------------------------ 抽象方法（子类实现）------------------------------

	/**
	 * 执行获取下一个序列值的Lua脚本。
	 *
	 * @param keys 脚本KEYS参数（序列键）
	 * @param args 脚本ARGV参数（步长字符串、默认值字符串）
	 * @return 序列值（null表示未初始化且无默认值）
	 */
	protected abstract Long executeNextValueScript(List<String> keys, List<Object> args);

	/**
	 * 获取指定键的长整型值。
	 *
	 * @param key 键
	 * @return 长整型值；如果键不存在或值非法，则返回null。
	 */
	protected abstract Long getKeyAsLong(String key);

	/**
	 * 设置指定键的长整型值。
	 *
	 * @param key   键
	 * @param value 长整型值
	 */
	protected abstract void setKey(String key, long value);

	/**
	 * 删除指定键。
	 *
	 * @param key 键
	 */
	protected abstract void deleteKey(String key);
}
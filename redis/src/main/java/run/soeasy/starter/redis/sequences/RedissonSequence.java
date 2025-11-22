package run.soeasy.starter.redis.sequences;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongSupplier;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RScript.Mode;
import org.redisson.api.RScript.ReturnType;
import org.redisson.api.RedissonClient;

import lombok.NonNull;

/**
 * 基于Redisson客户端的Redis序列实现。
 */
public class RedissonSequence extends AbstractRedisSequence {

	@NonNull
	private final RedissonClient redissonClient;

	/**
	 * 构造函数。
	 *
	 * @param key                  序列在Redis中的键
	 * @param initialValueSupplier 序列初始值提供者
	 * @param redissonClient       Redisson客户端实例
	 */
	public RedissonSequence(String key, LongSupplier initialValueSupplier, RedissonClient redissonClient) {
		super(key, initialValueSupplier);
		this.redissonClient = redissonClient;
	}

	@Override
	protected Long executeNextValueScript(List<String> keys, List<Object> args) {
		return redissonClient.getScript().eval(Mode.READ_WRITE, NEXT_VALUE_LUA_SCRIPT, ReturnType.INTEGER,
				Arrays.asList(keys.toArray()), args.toArray());
	}

	@Override
	protected Long getKeyAsLong(String key) {
		RAtomicLong atomicLong = redissonClient.getAtomicLong(key);
		// isExists() 是一个额外的网络调用。如果确定键值总是合法的长整数，可以简化为 return atomicLong.get()
		// 但为了严格遵守 getKeyAsLong 的契约（值非法时返回null），这里保持简洁实现。
		// 对于 Redisson，当键不存在时，atomicLong.get() 返回 0，这可能造成歧义。
		// 因此，最佳实践是先判断存在性。
		if (atomicLong.isExists()) {
			return atomicLong.get();
		}
		return null;
	}

	@Override
	protected void setKey(String key, long value) {
		redissonClient.getAtomicLong(key).set(value);
	}

	@Override
	protected void deleteKey(String key) {
		redissonClient.getAtomicLong(key).delete();
	}
}
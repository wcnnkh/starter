package run.soeasy.starter.redis.sequences;

import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import run.soeasy.framework.sequences.LongCounter;

/**
 * 基于 Redis 实现的高可用分布式长整型计数器。
 * <p>
 * 该计数器通过以下机制确保高可用性和数据一致性： 1. 利用 Redisson 提供的 {@link RAtomicLong} 实现原子递增。 2. 引入
 * Redisson 分布式锁，以安全地处理计数器键被意外删除或刷新后的重新初始化问题。 3. 每次调用 next() 前都会检查键是否存在，确保能感知到
 * Redis 数据的丢失。
 * <p>
 * 此实现仅依赖 Redisson 客户端，代码更加简洁和统一。
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class RedisCounter implements LongCounter {
	/**
	 * Redisson 客户端，用于所有 Redis 操作。
	 */
	@NonNull
	private final RedissonClient redissonClient;

	/**
	 * 该计数器在 Redis 中对应的键（Key）。
	 */
	@NonNull
	private final String key;

	/**
	 * 用于提供初始值的 {@link LongSupplier}。在计数器首次创建或被意外删除后，会调用此 Supplier。
	 */
	@NonNull
	private final LongSupplier initialValueSupplier;

	/**
	 * 分布式锁的键。
	 */
	private final String lockKey;

	public RedisCounter(RedissonClient redissonClient, String key, LongSupplier initialValueSupplier) {
		this(redissonClient, key, initialValueSupplier, "lock:sequence:" + key);
	}

	/**
	 * 原子地增加计数器的值，并返回增加后的新值。
	 * <p>
	 * 如果检测到 Redis 中键不存在，会尝试获取分布式锁， 在锁的保护下调用 {@code initialValueSupplier}
	 * 并设置初始值，然后执行递增。
	 *
	 * @param step 每次增加的步长。
	 * @return 增加步长后的新值。
	 * @throws IllegalArgumentException 如果步长小于或等于 0。
	 * @throws IllegalStateException    如果初始化失败或 Redis 命令执行异常。
	 */
	@Override
	public Long next(@NonNull Long step) {
		if (step <= 0) {
			throw new IllegalArgumentException("步长 (step) 必须是大于 0 的正整数。");
		}
		if (lockKey == null) {
			throw new IllegalStateException("RedisCounter 尚未初始化，请先调用 init() 方法。");
		}

		RAtomicLong atomicLong = redissonClient.getAtomicLong(key);

		// 快速路径：尝试直接递增。如果键不存在，get() 会返回 0。
		// 我们可以通过比较 incrementAndGet() 的结果来判断键是否是刚刚被创建的。
		if (atomicLong.isExists()) {
			return atomicLong.addAndGet(step);
		}

		// 慢速路径：键不存在，需要获取锁进行初始化
		RLock lock = redissonClient.getLock(lockKey);
		boolean isLocked = false;
		try {
			isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
			if (isLocked) {
				// 成功获取锁后，再次检查键是否存在（防止竞态条件）
				if (!atomicLong.isExists()) {
					log.warn("计数器 '{}' 在 Redis 中不存在，正在进行重新初始化...", key);
					long initialValue = initialValueSupplier.getAsLong();
					log.info("计数器 '{}' 初始化初始值为: {}", key, initialValue);
					// 设置初始值
					atomicLong.set(initialValue);
				}
				// 执行递增
				return atomicLong.addAndGet(step);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("获取分布式锁时线程被中断", e);
		} finally {
			if (isLocked) {
				lock.unlock();
			}
		}

		// 如果到这里还没返回，说明初始化失败
		throw new IllegalStateException(String.format("计数器 '%s' 初始化失败，请检查 Redis 连接和初始值提供器。", key));
	}

	// --- 其他辅助方法 ---

	/**
	 * 获取计数器当前的精确值。
	 *
	 * @return 当前计数值，如果键不存在则返回 0。
	 */
	public long getCurrentValue() {
		return redissonClient.getAtomicLong(key).get();
	}

	/**
	 * 将计数器重置为初始状态（删除键）。
	 */
	public void reset() {
		redissonClient.getAtomicLong(key).delete();
	}

	/**
	 * 将计数器强制设置为一个指定的值。
	 *
	 * @param value 要设置的值。
	 */
	public void setValue(long value) {
		redissonClient.getAtomicLong(key).set(value);
	}
}
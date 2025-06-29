package run.soeasy.starter.commons.jdbc;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import run.soeasy.framework.io.watch.Variable;

/**
 * 基础实体类定义
 * 
 * @author soeasy.run
 *
 * @param <U> 用户标识类型
 */
@Data
public class BaseEntity<U> implements Serializable, Variable {
	public static final String TIME_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
	private static final long serialVersionUID = 1L;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = TIME_FORMAT_PATTERN)
	private Long createTime;

	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = TIME_FORMAT_PATTERN)
	private Long updateTime;
	/**
	 * 创建者
	 */
	private U creator;
	/**
	 * 更新者
	 */
	private U updater;
	/**
	 * 数据版本号
	 */
	private Long dataVersion;

	@Override
	public long lastModified() {
		return dataVersion == null ? 0 : dataVersion;
	}
}

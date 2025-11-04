package run.soeasy.starter.common.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 一个选项的定义
 * 
 * @author soeasy.run
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Option implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 值
	 */
	private String value;
	/**
	 * 显示文本
	 */
	private String label;
}

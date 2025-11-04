package run.soeasy.starter.common.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 盒子
 * 
 * @author soeasy.run
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Box implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 长
	 */
	private MeasuredValue length;
	/**
	 * 宽
	 */
	private MeasuredValue width;
	/**
	 * 高
	 */
	private MeasuredValue height;
	/**
	 * 重
	 */
	private MeasuredValue weight;
}

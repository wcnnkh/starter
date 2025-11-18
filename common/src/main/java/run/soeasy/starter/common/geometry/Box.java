package run.soeasy.starter.common.geometry;

import java.io.Serializable;
import java.math.BigDecimal;

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
	private BigDecimal length;
	/**
	 * 宽
	 */
	private BigDecimal width;
	/**
	 * 高
	 */
	private BigDecimal height;
	/**
	 * 单位
	 */
	private String unit;
}

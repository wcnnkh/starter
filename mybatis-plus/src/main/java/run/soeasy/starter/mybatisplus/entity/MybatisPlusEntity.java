package run.soeasy.starter.mybatisplus.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;

import lombok.Data;
import run.soeasy.starter.mybatis.entity.MybatisEntity;

@Data
public class MybatisPlusEntity implements MybatisEntity, Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 逻辑删除字段
	 */
	@TableLogic(delval = "1", value = "0")
	private Integer deleted;
	/**
	 * 版本号字段
	 */
	@Version
	private Long version;
	/**
	 * 租房字段
	 */
	private Long tenantId;
}

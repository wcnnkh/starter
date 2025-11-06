package run.soeasy.starter.mybatisplus.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;

import run.soeasy.starter.mybatis.entity.MybatisEntity;

public class MybatisPlusEntity implements MybatisEntity, Serializable {
	private static final long serialVersionUID = 1L;
	@TableLogic(delval = "1", value = "0")
	private Integer deleted;
	@Version
	private Long version;
}

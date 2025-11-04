package run.soeasy.starter.mybatis.type;

import run.soeasy.starter.common.jackson.JsonFormat;

/**
 * 默认的json处理
 * 
 * @author soeasy.run
 *
 * @param <S>
 */
public class JsonTypeHandler<S> extends VarcharTypeHandler<S> {

	public JsonTypeHandler() {
		super(JsonFormat.DEFAULT);
	}

}

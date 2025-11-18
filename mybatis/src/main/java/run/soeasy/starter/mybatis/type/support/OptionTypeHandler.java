package run.soeasy.starter.mybatis.type.support;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import run.soeasy.starter.common.domain.Option;
import run.soeasy.starter.mybatis.type.JsonTypeHandler;

@MappedTypes(Option.class)
@MappedJdbcTypes({ JdbcType.VARCHAR, JdbcType.LONGVARCHAR, JdbcType.NVARCHAR, JdbcType.LONGNVARCHAR })
public class OptionTypeHandler extends JsonTypeHandler<Option> {
}

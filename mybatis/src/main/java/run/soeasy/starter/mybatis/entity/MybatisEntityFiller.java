package run.soeasy.starter.mybatis.entity;

import java.util.Iterator;
import java.util.stream.Stream;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.slf4j.Logger;

import lombok.NonNull;
import run.soeasy.framework.beans.BeanUtils;
import run.soeasy.framework.core.transform.property.PropertyAccessor;
import run.soeasy.framework.core.transform.property.PropertyMappingFilter;
import run.soeasy.framework.core.transform.property.TypedProperties;
import run.soeasy.framework.core.transform.templates.Mapper;
import run.soeasy.framework.core.transform.templates.MappingContext;

public interface MybatisEntityFiller {
	MybatisEntity getFillEntity(SqlCommandType sqlCommandType);

	default void fillArgs(@NonNull MappedStatement mappedStatement, @NonNull Stream<Object> args, Logger logger) {
		fillArgs(mappedStatement.getSqlCommandType(), args, logger);
	}

	default void fillArgs(@NonNull SqlCommandType sqlCommandType, @NonNull Stream<Object> args, Logger logger) {
		MybatisEntity fillEntity = getFillEntity(sqlCommandType);
		if (fillEntity == null) {
			return;
		}

		Iterator<Object> iterator = args.iterator();
		while (iterator.hasNext()) {
			Object arg = iterator.next();
			if (arg instanceof MybatisEntity) {
				BeanUtils.copyProperties(fillEntity, arg, new PropertyMappingFilter() {

					@Override
					public boolean doMapping(
							@NonNull MappingContext<Object, PropertyAccessor, TypedProperties> sourceContext,
							@NonNull MappingContext<Object, PropertyAccessor, TypedProperties> targetContext,
							@NonNull Mapper<Object, PropertyAccessor, TypedProperties> mapper) {
						if (sourceContext.hasKeyValue() && sourceContext.hasKeyValue()) {
							Object targetValue = targetContext.getKeyValue().getValue().get();
							if (targetValue != null) {
								return false;
							}
							Object sourceValue = sourceContext.getKeyValue().getValue().get();
							if (sourceValue == null) {
								return false;
							}
							if (logger != null && logger.isDebugEnabled()) {
								logger.debug("The data filled with {}.{} is: {}", arg.getClass().getName(),
										targetContext.getKeyValue().getKey(), sourceValue);
							}
						}
						return mapper.doMapping(sourceContext, targetContext);
					}
				});
			}
		}
	}
}

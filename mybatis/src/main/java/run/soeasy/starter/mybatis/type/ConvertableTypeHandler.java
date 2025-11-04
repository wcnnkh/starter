package run.soeasy.starter.mybatis.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.core.ResolvableType;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.function.ThrowingSupplier;

@RequiredArgsConstructor
@Getter
@Setter
public class ConvertableTypeHandler<S, T> extends BaseTypeHandler<S> {
	private Class<S> javaType;
	private Class<T> jdbcType;
	@NonNull
	private final Converter converter;

	@SuppressWarnings("unchecked")
	public Class<S> getJavaType() {
		if (this.javaType == null) {
			this.javaType = (Class<S>) super.getRawType();
		}
		return this.javaType;
	}

	@SuppressWarnings("unchecked")
	public Class<T> getJdbcType() {
		if (this.jdbcType == null) {
			this.jdbcType = (Class<T>) ResolvableType.forClass(getClass()).as(ConvertableTypeHandler.class)
					.getGeneric(1).getRawClass();
		}
		return this.jdbcType;
	}

	@Override
	public void setNonNullParameter(PreparedStatement ps, int i, S parameter, JdbcType jdbcType) throws SQLException {
		Object value = converter.convert(parameter, getJdbcType());
		ps.setObject(i, value);
	}

	private <E extends Throwable> S getResult(ThrowingSupplier<? extends T, ? extends E> resultSupplier) throws E {
		T result = resultSupplier.get();
		if (result == null) {
			return null;
		}
		return converter.convert(result, getJavaType());
	}

	@Override
	public S getNullableResult(ResultSet rs, String columnName) throws SQLException {
		return getResult(() -> rs.getObject(columnName, getJdbcType()));
	}

	@Override
	public S getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
		return getResult(() -> rs.getObject(columnIndex, getJdbcType()));
	}

	@Override
	public S getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
		return getResult(() -> cs.getObject(columnIndex, getJdbcType()));
	}

}

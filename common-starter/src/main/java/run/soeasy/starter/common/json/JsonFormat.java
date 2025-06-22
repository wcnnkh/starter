package run.soeasy.starter.common.json;

import java.lang.reflect.Type;

import lombok.NonNull;
import run.soeasy.framework.core.convert.ConversionException;
import run.soeasy.framework.core.convert.Converter;
import run.soeasy.framework.core.convert.TypeDescriptor;

public interface JsonFormat extends Converter {
	public static final JacksonFormat JACKSON = new JacksonFormat();

	@Override
	default Object convert(Object source, @NonNull TypeDescriptor sourceTypeDescriptor,
			@NonNull TypeDescriptor targetTypeDescriptor) throws ConversionException {
		String json = source instanceof String ? ((String) source) : format(source);
		if (targetTypeDescriptor.getType() == String.class) {
			return json;
		}
		return parse(json, targetTypeDescriptor.getResolvableType());
	}

	String format(Object object) throws ConversionException;

	<T> T parse(String json, Type type) throws ConversionException;
}

package run.soeasy.starter.commons.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Getter;
import run.soeasy.framework.core.convert.ConversionException;
import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.json.JsonConverter;

@Getter
public class JacksonConverter extends JsonMapper implements JsonConverter {
	private static final long serialVersionUID = 1L;

	/**
	 * 默认的
	 */
	public static final JacksonConverter DEFAULT = new JacksonConverter();
	static {
		DEFAULT.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
		DEFAULT.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		DEFAULT.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
		DEFAULT.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		DEFAULT.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		DEFAULT.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}

	/**
	 * 下划线命名方式
	 */
	public static final JacksonConverter SNAKE_CASE = new JacksonConverter();
	static {
		SNAKE_CASE.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
	}

	public JacksonConverter() {
		super();
	}

	public JacksonConverter(JsonMapper jsonMapper) {
		super(jsonMapper);
	}

	@Override
	public JacksonConverter copy() {
		_checkInvalidCopy(JacksonConverter.class);
		return new JacksonConverter(this);
	}

	@Override
	public String to(Object source, TypeDescriptor sourceTypeDescriptor, TypeDescriptor targetTypeDescriptor)
			throws ConversionException {
		try {
			return writeValueAsString(source);
		} catch (JsonProcessingException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public Object from(String source, TypeDescriptor sourceTypeDescriptor, TypeDescriptor targetTypeDescriptor)
			throws ConversionException {
		try {
			return readValue(source,
					TypeFactory.defaultInstance().constructType(targetTypeDescriptor.getResolvableType()));
		} catch (JsonProcessingException e) {
			throw new ConversionException(e);
		}
	}
}

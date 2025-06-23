package run.soeasy.starter.commons.json;

import java.lang.reflect.Type;

import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Getter;
import run.soeasy.framework.core.convert.ConversionException;

@Getter
public class JacksonFormat extends JsonMapper implements JsonFormat {
	private static final long serialVersionUID = 1L;

	/**
	 * 默认的
	 */
	public static final JacksonFormat DEFAULT = new JacksonFormat();
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
	public static final JacksonFormat SNAKE_CASE = new JacksonFormat();
	static {
		SNAKE_CASE.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
	}

	public JacksonFormat() {
		super();
	}

	public JacksonFormat(JsonMapper jsonMapper) {
		super(jsonMapper);
	}

	@Override
	public JacksonFormat copy() {
		_checkInvalidCopy(JacksonFormat.class);
		return new JacksonFormat(this);
	}

	@Override
	public String format(Object object) throws ConversionException {
		try {
			return writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public <T> T parse(String json, Type type) throws ConversionException {
		try {
			return readValue(json,
					type instanceof JavaType ? ((JavaType) type) : TypeFactory.defaultInstance().constructType(type));
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
	}
}

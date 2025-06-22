package run.soeasy.starter.common.json;

import java.lang.reflect.Type;

import org.springframework.boot.json.JsonParseException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

import lombok.Getter;
import run.soeasy.framework.core.convert.ConversionException;

@Getter
public class JacksonFormat implements JsonFormat, Cloneable {
	private static final JsonMapper DEFAULT_JSON_MAPPER = new JsonMapper();
	static {
		DEFAULT_JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略未知属性
		DEFAULT_JSON_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		DEFAULT_JSON_MAPPER.configure(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS, true);
		DEFAULT_JSON_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		DEFAULT_JSON_MAPPER.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		DEFAULT_JSON_MAPPER.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
	}

	private final JsonMapper jsonMapper;

	public JacksonFormat() {
		this(DEFAULT_JSON_MAPPER.copy());
	}

	protected JacksonFormat(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	@Override
	public JsonFormat clone() {
		return new JacksonFormat(jsonMapper.copy());
	}

	@Override
	public String format(Object object) throws ConversionException {
		try {
			return jsonMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new ConversionException(e);
		}
	}

	@Override
	public <T> T parse(String json, Type type) throws ConversionException {
		try {
			return jsonMapper.readValue(json,
					type instanceof JavaType ? ((JavaType) type) : TypeFactory.defaultInstance().constructType(type));
		} catch (JsonProcessingException e) {
			throw new JsonParseException(e);
		}
	}
}

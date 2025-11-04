package run.soeasy.starter.common.format;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import run.soeasy.framework.core.convert.TypeDescriptor;
import run.soeasy.framework.json.JsonConverter;
import run.soeasy.framework.json.JsonElement;
import run.soeasy.starter.common.jackson.JsonFormat;

public class JsonConverterTest {
	@SuppressWarnings("unchecked")
	@Test
	public void test() {
		JsonConverter jsonConverter = JsonFormat.DEFAULT;
		Map<String, Object> map = new HashMap<>();
		map.put("name", UUID.randomUUID());
		String json = jsonConverter.convert(map, String.class);
		System.out.println(json);
		map = (Map<String, Object>) jsonConverter.convert(json, TypeDescriptor.map(HashMap.class, String.class, Object.class));
		System.out.println(map);
		
		JsonElement jsonElement = JsonFormat.DEFAULT.toJsonElement(json);
		assert !jsonElement.isJsonArray();
		assert jsonElement.isJsonObject();

		JsonElement name = jsonElement.getAsJsonObject().get("name");
		System.out.println(name);
		System.out.println(name.getAsJsonPrimitive().getAsString());
		assert name.isJsonPrimitive();
	}
}

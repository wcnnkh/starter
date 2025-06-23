package run.soeasy.starter.commons.web;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Map.Entry;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import run.soeasy.framework.codec.Encoder;

@UtilityClass
public class QueryStringUtils {
	public static <K, V> String toString(@NonNull Map<? extends K, ? extends V> map,
			Encoder<String, String> valueEncoder) {
		StringBuilder builder = new StringBuilder();
		for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
			K key = entry.getKey();
			if (key == null) {
				continue;
			}

			V value = entry.getValue();
			if (value == null) {
				continue;
			}

			if (value instanceof Iterable) {
				Iterable<?> iterable = (Iterable<?>) value;
				for (Object element : iterable) {
					append(builder, key, element, valueEncoder);
				}
			} else if (value != null && value.getClass().isArray()) {
				for (int i = 0, length = Array.getLength(value); i < length; i++) {
					append(builder, key, Array.get(value, i), valueEncoder);
				}
			} else {
				append(builder, key, value, valueEncoder);
			}
		}
		return builder.toString();
	}

	private static <K, V> void append(@NonNull StringBuilder builder, K key, @NonNull V value,
			Encoder<String, String> valueEncoder) {
		if (key == null || value == null) {
			return;
		}
		if (builder.length() > 0) {
			builder.append('&');
		}
		builder.append(key);
		builder.append('=');
		String v = String.valueOf(value);
		if (valueEncoder != null) {
			v = valueEncoder.encode(v);
		}
		builder.append(v);
	}
}

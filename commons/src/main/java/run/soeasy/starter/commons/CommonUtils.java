package run.soeasy.starter.commons;

import java.util.Random;

import org.springframework.http.HttpMethod;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CommonUtils {
	public final static String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	public final static String LOWERCASE_LETTERS = CAPITAL_LETTERS.toLowerCase();

	public final static String NUMBERIC_CHARACTER = "0123456789";

	/**
	 * 放在一起容易分辨的字符
	 */
	public final static CharSequence EASY_TO_DISTINGUISH = NUMBERIC_CHARACTER + "acdefhkmnprstvwyABCEFGHKMNRSTVWY";

	public final static String ALL = CAPITAL_LETTERS + LOWERCASE_LETTERS + NUMBERIC_CHARACTER;

	public static char[] randomChars(Random random, CharSequence template, int newLength) {
		int length = template.length();
		char[] array = new char[newLength];
		for (int i = 0; i < newLength; i++) {
			int randomIndex = random.nextInt(length);
			array[i] = template.charAt(randomIndex);
		}
		return array;
	}

	/**
	 * 随机字符串
	 * 
	 * @param length
	 * @return
	 */
	public static String randomString(int length) {
		return new String(randomChars(new Random(), ALL, length));
	}

	/**
	 * 随机数字
	 * 
	 * @param length
	 * @return
	 */
	public static String randomNumber(int length) {
		return new String(randomChars(new Random(), NUMBERIC_CHARACTER, length));
	}

	/**
	 * 随机验证码
	 * 
	 * @param length
	 * @return
	 */
	public static String randomCode(int length) {
		return new String(randomChars(new Random(), EASY_TO_DISTINGUISH, length));
	}

	public static boolean isAllowedBody(@NonNull HttpMethod httpMethod) {
		return httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH
				|| httpMethod == HttpMethod.DELETE;
	}
}

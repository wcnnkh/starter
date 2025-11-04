package run.soeasy.starter.common.util;

import java.util.Random;

import org.springframework.http.HttpMethod;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * 通用工具类，提供随机字符/字符串生成、HTTP 请求方法合法性判断等基础工具能力。
 * 
 * <p>该类为 {@link UtilityClass}（工具类），不允许实例化，所有方法均为静态方法，可直接通过类名调用。
 * 核心能力包括：
 * <ul>
 * <li>多类型随机字符串生成（包含大小写字母、数字、易区分字符组合）</li>
 * <li>HTTP 请求方法是否允许携带请求体（Body）的判断</li>
 * </ul>
 * 所有随机相关方法基于 {@link Random} 实现，若需更高安全性（如密码生成），建议自行替换为 {@link java.security.SecureRandom}。
 */
@UtilityClass
public class XUtils {

    /**
     * 大写英文字母常量池，包含 A-Z 共 26 个字符
     */
    public final static String CAPITAL_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /**
     * 小写英文字母常量池，通过 {@link #CAPITAL_LETTERS} 转小写生成，包含 a-z 共 26 个字符
     */
    public final static String LOWERCASE_LETTERS = CAPITAL_LETTERS.toLowerCase();

    /**
     * 数字字符常量池，包含 0-9 共 10 个字符
     */
    public final static String NUMBERIC_CHARACTER = "0123456789";

    /**
     * 易区分字符常量池，排除易混淆字符（如 0/O、1/I、l 等），适用于验证码、临时密码等场景。
     * <p>组成：{@link #NUMBERIC_CHARACTER} + 小写易区分字母（acdefhkmnprstvwy） + 大写易区分字母（ABCEFGHKMNRSTVWY）
     */
    public final static CharSequence EASY_TO_DISTINGUISH = NUMBERIC_CHARACTER + "acdefhkmnprstvwyABCEFGHKMNRSTVWY";

    /**
     * 全字符常量池，包含所有大小写英文字母和数字，共 26+26+10=62 个字符
     */
    public final static String ALL = CAPITAL_LETTERS + LOWERCASE_LETTERS + NUMBERIC_CHARACTER;

    /**
     * 基于指定随机源和字符模板，生成固定长度的随机字符数组（核心底层方法）。
     * 
     * <p>通过循环从字符模板中随机选取字符，构建目标长度的字符数组，支持自定义随机源（如 {@link Random} 或 {@link java.security.SecureRandom}）
     * 和字符模板（如仅数字、易区分字符等），为上层随机字符串方法提供基础能力。
     *
     * @param random   随机源实例，用于生成随机索引（不可为 null，建议根据场景选择普通/安全随机源）
     * @param template 字符模板，随机字符仅从该模板中选取（不可为 null 且长度需 &gt; 0，否则会抛出 {@link IndexOutOfBoundsException}）
     * @param newLength 目标字符数组的长度（需 ≥ 0，若为 0 则返回空数组）
     * @return 固定长度的随机字符数组，元素均来自传入的字符模板
     * @throws NullPointerException     若 random 或 template 为 null
     * @throws IndexOutOfBoundsException 若 template 长度为 0（无法选取字符）
     */
    public static char[] randomChars(@NonNull Random random, @NonNull CharSequence template, int newLength) {
        int length = template.length();
        char[] array = new char[newLength];
        for (int i = 0; i < newLength; i++) {
            int randomIndex = random.nextInt(length);
            array[i] = template.charAt(randomIndex);
        }
        return array;
    }

    /**
     * 生成包含大小写字母和数字的随机字符串（基于 {@link #ALL} 字符模板）。
     * 
     * <p>默认使用新创建的 {@link Random} 实例作为随机源，适用于普通随机场景（如生成订单号后缀、临时标识等），
     * 若需防预测（如密码生成），建议使用 {@link #randomChars(Random, CharSequence, int)} 并传入 {@link java.security.SecureRandom}。
     *
     * @param length 随机字符串的长度（需 ≥ 0，若为 0 则返回空字符串）
     * @return 长度为 length 的随机字符串，字符包含大小写字母和数字
     */
    public static String randomString(int length) {
        return new String(randomChars(new Random(), ALL, length));
    }

    /**
     * 生成纯数字的随机字符串（基于 {@link #NUMBERIC_CHARACTER} 字符模板）。
     * 
     * <p>默认使用新创建的 {@link Random} 实例，适用于生成验证码（如短信验证码、邮箱验证码）、随机编号等场景，
     * 注意：生成的字符串可能包含前导零，若需避免需自行处理。
     *
     * @param length 随机数字字符串的长度（需 ≥ 0，若为 0 则返回空字符串）
     * @return 长度为 length 的纯数字字符串
     */
    public static String randomNumber(int length) {
        return new String(randomChars(new Random(), NUMBERIC_CHARACTER, length));
    }

    /**
     * 生成易区分的随机字符串（基于 {@link #EASY_TO_DISTINGUISH} 字符模板），适用于可视化场景。
     * 
     * <p>排除易混淆字符（如 0 与 O、1 与 I/l、2 与 Z 等），避免用户肉眼识别错误，典型场景包括：
     * <ul>
     * <li>图形验证码、扫码登录验证码</li>
     * <li>临时登录密码、设备激活码</li>
     * </ul>
     * 默认使用 {@link Random} 实例，若需高安全性可自行替换随机源。
     *
     * @param length 易区分随机字符串的长度（需 ≥ 0，若为 0 则返回空字符串）
     * @return 长度为 length 的易区分随机字符串，字符包含数字、特定大小写字母
     */
    public static String randomCode(int length) {
        return new String(randomChars(new Random(), EASY_TO_DISTINGUISH, length));
    }

    /**
     * 判断指定 HTTP 请求方法是否允许携带请求体（Body）。
     * 
     * <p>根据 HTTP 协议规范，以下方法允许携带 Body：
     * <ul>
     * <li>{@link HttpMethod#POST}：标准提交方法，常用于表单、JSON 数据提交</li>
     * <li>{@link HttpMethod#PUT}：全量更新资源，通常携带完整资源数据</li>
     * <li>{@link HttpMethod#PATCH}：部分更新资源，携带需修改的字段数据</li>
     * <li>{@link HttpMethod#DELETE}：删除资源（部分场景下需携带删除条件，虽非标准但实际常用）</li>
     * </ul>
     * 其他方法（如 GET、HEAD、OPTIONS 等）不建议携带 Body，多数服务器会忽略该部分数据。
     *
     * @param httpMethod HTTP 请求方法实例（不可为 null，由 {@link NonNull} 注解强制约束，传入 null 会抛出 {@link NullPointerException}）
     * @return true：允许携带请求体；false：不允许携带请求体
     * @throws NullPointerException 若 httpMethod 为 null
     */
    public static boolean isAllowedBody(@NonNull HttpMethod httpMethod) {
        return httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT || httpMethod == HttpMethod.PATCH
                || httpMethod == HttpMethod.DELETE;
    }
}
    
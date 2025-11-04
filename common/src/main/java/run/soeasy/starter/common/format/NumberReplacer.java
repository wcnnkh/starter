package run.soeasy.starter.common.format;

import java.math.BigDecimal;

import lombok.NonNull;
import run.soeasy.framework.codec.Codec;
import run.soeasy.framework.codec.CodecException;
import run.soeasy.framework.core.Assert;
import run.soeasy.framework.core.StringUtils;
import run.soeasy.framework.core.math.NumberUtils;

/**
 * 数字与中文数字（小写/大写）的双向转换器，实现 {@link Codec} 接口（泛型参数：输入为 BigDecimal，输出为 String），支持：
 * <ul>
 * <li>编码（encode）：将数字（BigDecimal/字符序列）转换为中文数字字符串（如 123 → 一二三 / 壹贰叁）</li>
 * <li>解码（decode）：将中文数字字符串还原为数字（BigDecimal，如 壹贰叁 → 123）</li>
 * <li>辅助能力：单个数字字符格式化、中文数字解析、包含性判断等</li>
 * </ul>
 * 支持自定义中文数字映射表，默认提供「小写中文数字」和「大写中文数字」两种预设实例，适用于金额、序号等场景的格式化。
 */
public final class NumberReplacer implements Codec<BigDecimal, String> {

    /**
     * 小写中文数字映射数组，索引 0-9 对应「零」到「九」，用于普通场景的数字转中文
     */
    private static final String[] CHINESE_NUMBERS = new String[] { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };

    /**
     * 大写中文数字映射数组，索引 0-9 对应「零」到「玖」，索引 10 对应「拾」，用于金额等严谨场景（防篡改）
     */
    private static final String[] CAPITALIZE_CHINESE_NUMBERS = new String[] { "零", "壹", "贰", "叁", "肆", "伍", "陆", "柒",
            "捌", "玖", "拾" };

    /**
     * 中文数字映射表，存储「数字索引→中文数字」的对应关系（不可修改，通过构造器初始化）
     */
    private final String[] mapping;

    /**
     * 是否去除数字尾部的零（如 123.00 → 123），仅在编码/解码时对 BigDecimal 生效
     */
    private final boolean stripTrailingZeros;

    /**
     * 预设实例：小写中文数字转换器，自动去除尾部零（如 456 → 四五六，100.0 → 一百）
     */
    public static final NumberReplacer LOWERCASE = new NumberReplacer(false, true);

    /**
     * 预设实例：大写中文数字转换器，自动去除尾部零（如 789 → 柒捌玖，200.00 → 贰佰），适用于金额场景
     */
    public static final NumberReplacer CAPITALIZE = new NumberReplacer(true, true);

    /**
     * 构造器：通过「是否使用大写」和「是否去尾部零」快速初始化转换器
     * 
     * <p>内部自动关联预设的中文数字映射表：
     * - 若 capitalize 为 true，使用 {@link #CAPITALIZE_CHINESE_NUMBERS}（大写）
     * - 若 capitalize 为 false，使用 {@link #CHINESE_NUMBERS}（小写）
     *
     * @param capitalize        是否使用大写中文数字（true：大写；false：小写）
     * @param stripTrailingZeros 是否去除数字尾部的零（true：去除；false：保留）
     */
    public NumberReplacer(boolean capitalize, boolean stripTrailingZeros) {
        this(capitalize ? CAPITALIZE_CHINESE_NUMBERS : CHINESE_NUMBERS, stripTrailingZeros);
    }

    /**
     * 构造器：通过自定义中文数字映射表初始化转换器（灵活支持特殊场景）
     * 
     * <p>自定义映射表需满足：索引 0-9 对应数字 0-9 的中文表述，否则可能导致编码/解码异常（如索引 0 需为「零」或对应表述）。
     *
     * @param mapping           自定义中文数字映射表（不可为 null，且长度建议 ≥10，否则无法覆盖 0-9 数字）
     * @param stripTrailingZeros 是否去除数字尾部的零（true：去除；false：保留）
     * @throws NullPointerException 若 mapping 为 null（由 {@link NonNull} 注解强制约束）
     */
    public NumberReplacer(@NonNull String[] mapping, boolean stripTrailingZeros) {
        this.mapping = mapping;
        this.stripTrailingZeros = stripTrailingZeros;
    }

    /**
     * 获取当前转换器的中文数字映射表（返回克隆数组，避免外部修改内部状态）
     *
     * @return 中文数字映射表的克隆副本，索引 0-9 对应数字 0-9 的中文表述
     */
    public String[] getMapping() {
        return mapping.clone();
    }

    /**
     * 判断当前转换器是否开启「去除数字尾部零」的功能
     *
     * @return true：开启（编码/解码时去除尾部零）；false：关闭（保留尾部零）
     */
    public boolean isStripTrailingZeros() {
        return stripTrailingZeros;
    }

    /**
     * 编码：将 BigDecimal 类型的数字转换为中文数字字符串（实现 {@link Codec} 接口的 encode 方法）
     * 
     * <p>处理流程：
     * 1. 若输入为 null，直接返回 null；
     * 2. 根据 {@link #stripTrailingZeros} 决定是否去除尾部零（通过 {@link NumberUtils#stripTrailingZeros}）；
     * 3. 将处理后的数字转为纯字符串（无科学计数法），再调用 {@link #encode(CharSequence)} 完成中文转换。
     *
     * @param source 待转换的 BigDecimal 数字（可为 null）
     * @return 中文数字字符串（如 source 为 123.45，LOWERCASE 实例返回「一二三.四五」）；若 source 为 null，返回 null
     * @throws CodecException 若转换过程中出现异常（如数字格式非法，但 BigDecimal 输入通常已保证合法性）
     */
    @Override
    public String encode(BigDecimal source) throws CodecException {
        if (source == null) {
            return null;
        }

        String value = (stripTrailingZeros ? NumberUtils.stripTrailingZeros(source) : source).toPlainString();
        return encode(value);
    }

    /**
     * 编码：将字符序列（如 String、StringBuilder）中的数字字符转换为中文数字
     * 
     * <p>仅转换字符序列中的数字字符（0-9），非数字字符（如 .、-、字母）保持原样。
     * 示例：输入「123abc-45.6」，LOWERCASE 实例返回「一二三abc-四五.六」。
     *
     * @param source 待转换的字符序列（可为 null，若为 null 则返回 null）
     * @return 数字字符替换为中文后的字符串；若 source 为 null，返回 null
     * @throws CodecException 若转换过程中出现异常（当前实现无显式异常场景，预留扩展）
     */
    public String encode(CharSequence source) throws CodecException {
        if (source == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            char v = source.charAt(i);
            if (Character.isDigit(v)) {
                sb.append(format(v));
                continue;
            }
            sb.append(v);
        }
        return sb.toString();
    }

    /**
     * 解码：将包含中文数字的字符串还原为 BigDecimal 数字（实现 {@link Codec} 接口的 decode 方法）
     * 
     * <p>处理流程：
     * 1. 若输入为 null，直接返回 null；
     * 2. 遍历 {@link #mapping}，将字符串中的中文数字替换为对应数字（如「壹」替换为 1）；
     * 3. 将替换后的纯数字字符串转为 BigDecimal；
     * 4. 根据 {@link #stripTrailingZeros} 决定是否去除尾部零。
     *
     * @param source 包含中文数字的字符串（可为 null，若为 null 则返回 null）
     * @return 还原后的 BigDecimal 数字；若 source 为 null，返回 null
     * @throws CodecException 若替换后的字符串无法转为 BigDecimal（如包含非数字/非中文数字字符）
     */
    @Override
    public BigDecimal decode(String source) throws CodecException {
        if (source == null) {
            return null;
        }

        String value = source;
        for (int i = 0; i < mapping.length; i++) {
            value = value.replaceAll(mapping[i], i + "");
        }

        try {
            BigDecimal number = new BigDecimal(value);
            return stripTrailingZeros ? NumberUtils.stripTrailingZeros(number) : number;
        } catch (NumberFormatException e) {
            throw new CodecException("Failed to decode source to BigDecimal: " + source, e);
        }
    }

    /**
     * 格式化：将单个数字字符（0-9）转换为对应的中文数字
     *
     * @param source 单个数字字符（如 '5'）
     * @return 对应的中文数字（如 source 为 '5'，LOWERCASE 实例返回「五」）
     * @throws IllegalArgumentException 若 source 不是数字字符（由 {@link Assert#isTrue} 触发）
     */
    public String format(char source) {
        Assert.isTrue(Character.isDigit(source), "Source must be a digital");
        // 48-57是0-9的ASCII值，通过 Character.getNumericValue 直接获取数字值（0-9）
        return mapping[Character.getNumericValue(source)];
    }

    /**
     * 解析：将单个中文数字字符串转为对应的数字（0-9）
     *
     * @param source 单个中文数字字符串（如「三」「柒」，可为 null 或空字符串）
     * @return 对应的数字（0-9）；若 source 为 null/空字符串，或未匹配到映射关系，返回 -1
     */
    public int parse(String source) {
        if (StringUtils.isEmpty(source)) {
            return -1;
        }

        for (int i = 0; i < mapping.length; i++) {
            if (StringUtils.equals(source, mapping[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 判断：单个字符串是否为当前映射表中的中文数字（等价于 {@link #parse(String)} != -1）
     *
     * @param source 待判断的字符串（可为 null 或空字符串）
     * @return true：source 是当前映射表中的中文数字（如「四」「玖」）；false：否则
     */
    public boolean contains(String source) {
        return parse(source) != -1;
    }

    /**
     * 判断：目标字符串中是否包含当前映射表中的任意中文数字
     *
     * @param source 待判断的目标字符串（可为 null 或空字符串）
     * @return true：source 中包含至少一个中文数字（如「abc壹def」包含「壹」）；false：source 为 null/空，或无任何中文数字
     */
    public boolean exists(String source) {
        if (StringUtils.isEmpty(source)) {
            return false;
        }

        for (String v : mapping) {
            if (source.indexOf(v) != -1) {
                return true;
            }
        }
        return false;
    }
}
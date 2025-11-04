package run.soeasy.starter.common.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通用响应结果基类（无数据载体）
 * <p>
 * 统一封装接口响应的核心状态信息（状态码、中英文提示消息、成功标识），提供静态工厂方法快速构建响应对象，
 * 支持扩展为带业务数据的 {@link DataResult} 实例。适用于微服务/应用的接口响应标准化，
 * 简化响应构建逻辑，同时便于前端统一处理状态判断和多语言消息展示。
 *
 * @author soeasy.run
 * @see DataResult 带业务数据的响应结果子类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Result implements Serializable {

    /**
     * 序列化版本号（确保对象序列化/反序列化时的兼容性）
     */
    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     * <p>
     * 约定规则：
     * - 200：操作成功（默认成功状态码）
     * - 4xx：客户端错误（如 400=参数错误、401=未授权、403=权限不足）
     * - 5xx：服务端错误（如 500=通用失败、503=服务不可用）
     * - 自定义业务码：10000+（如 10001=用户不存在、10002=订单已关闭）
     */
    private Integer code;

    /**
     * 响应提示消息（中英文组合格式）
     * <p>
     * 格式规范：中文提示(英文提示)，如"操作成功(Operation successful)"，支持前端根据场景选择展示语言
     * 成功场景：默认"操作成功(Operation successful)"，可自定义业务提示（如"数据查询成功(Data query successful)"）
     * 失败场景：描述具体错误原因（如"手机号格式不正确(Incorrect phone number format)"）
     */
    private String msg;

    /**
     * 操作成功标识
     * <p>
     * true：操作执行成功（通常对应 code=200 或自定义成功码）
     * false：操作执行失败（通常对应 code≠200）
     */
    private boolean success;

    /**
     * 拷贝构造方法
     * <p>
     * 基于已有 {@link Result} 实例的状态信息（code、msg、success）创建新实例，
     * 适用于复用已有响应状态，避免重复赋值。
     *
     * @param result 源响应实例（可为 null，null 时新实例字段均为默认值）
     */
    public Result(Result result) {
        if (result == null) {
            return;
        }
        this.code = result.code;
        this.msg = result.msg;
        this.success = result.success;
    }

    /**
     * 扩展为带业务数据的响应结果
     * <p>
     * 基于当前响应的状态信息（code、msg、success），添加业务数据后返回 {@link DataResult}，
     * 适用于需要返回具体数据的接口（如列表查询、详情查询、数据提交后返回结果）。
     *
     * @param data 业务数据（如用户详情、列表集合、统计结果等任意类型）
     * @param <T>  业务数据的泛型类型
     * @return 带数据的 {@link DataResult} 实例
     * @example
     * // 基于默认成功状态扩展数据响应
     * Result successResult = Result.SUCCESS;
     * DataResult<UserDTO> userResult = successResult.newDataResult(userDTO);
     * // 响应结果：code=200、msg="操作成功(Operation successful)"、data=用户信息
     */
    public <T> DataResult<T> newDataResult(T data) {
        DataResult<T> dataResult = new DataResult<>(this);
        dataResult.setData(data);
        return dataResult;
    }

    /**
     * 通用成功响应常量
     * <p>
     * 默认状态：code=200、msg="操作成功(Operation successful)"、success=true，
     * 适用于无需自定义提示信息和数据的成功场景（如新增、修改、删除操作）。
     */
    public static final Result SUCCESS = new Result(200, "操作成功(Operation successful)", true);

    /**
     * 通用失败响应常量
     * <p>
     * 默认状态：code=500、msg="操作失败(Operation failed)"、success=false，
     * 适用于无需自定义错误信息的通用失败场景（如未知异常、系统内部错误）。
     */
    public static final Result ERROR = new Result(500, "操作失败(Operation failed)", false);

    /**
     * 构建通用失败响应（无数据）
     * <p>
     * 复用 {@link #ERROR} 常量的配置（code=500、msg="操作失败(Operation failed)"），
     * 适用于无需明确错误原因的简单失败场景。
     *
     * @param <T> 数据泛型（失败场景通常为 null，仅为兼容 {@link DataResult} 结构）
     * @return 无数据的失败响应 {@link DataResult} 实例
     * @example
     * // 接口返回通用失败
     * if (operationFailed) {
     *     return Result.error();
     * }
     */
    public static <T> DataResult<T> error() {
        return error(ERROR.getCode(), ERROR.getMsg());
    }

    /**
     * 构建自定义状态码和错误消息的失败响应（无数据）
     * <p>
     * 适用于需要明确错误原因和自定义状态码的场景（如参数校验失败、业务逻辑错误），
     * 支持前端根据 code 做差异化处理，消息建议遵循"中文(英文)"格式。
     *
     * @param code 自定义错误状态码（如 400=参数错误、401=未授权、10001=用户不存在）
     * @param msg  具体错误提示消息（建议格式：中文提示(英文提示)，支持前端多语言展示）
     * @param <T>  数据泛型（失败场景通常为 null）
     * @return 带自定义错误信息的 {@link DataResult} 实例
     * @example
     * // 参数校验失败响应
     * if (StringUtils.isEmpty(username)) {
     *     return Result.error(400, "用户名不能为空(Username cannot be empty)");
     * }
     */
    public static <T> DataResult<T> error(Integer code, String msg) {
        DataResult<T> dataResult = new DataResult<>();
        dataResult.setCode(code);
        dataResult.setMsg(msg);
        dataResult.setSuccess(false);
        return dataResult;
    }

    /**
     * 构建自定义错误消息的失败响应（无数据）
     * <p>
     * 复用默认错误状态码（code=500），仅自定义错误提示消息，
     * 适用于无需差异化状态码，仅需说明错误原因的场景，消息建议遵循"中文(英文)"格式。
     *
     * @param msg 具体错误提示消息（如"数据库查询失败(Database query failed)"）
     * @param <T> 数据泛型（失败场景通常为 null）
     * @return 带自定义错误消息的 {@link DataResult} 实例
     * @example
     * // 业务逻辑失败响应
     * if (goodsIsOutOfStock) {
     *     return Result.error("该商品已售罄，无法下单(The product is out of stock and cannot be ordered)");
     * }
     */
    public static <T> DataResult<T> error(String msg) {
        return error(ERROR.getCode(), msg);
    }

    /**
     * 构建自定义状态码、提示消息和数据的成功响应
     * <p>
     * 适用于需要自定义成功提示和状态码的场景（如特殊业务成功状态、多状态成功响应），
     * 消息建议遵循"中文(英文)"格式。
     *
     * @param code 成功状态码（建议使用 200 系列，如 200=成功、201=创建成功）
     * @param msg  成功提示消息（如"数据新增成功(Data added successfully)"）
     * @param data 业务数据（如新增后的用户ID、查询后的列表数据）
     * @param <T>  业务数据泛型
     * @return 带数据和自定义信息的 {@link DataResult} 实例
     * @example
     * // 数据新增成功响应
     * Long userId = userService.save(user);
     * return Result.success(201, "用户注册成功(User registered successfully)", userId);
     */
    public static <T> DataResult<T> success(Integer code, String msg, T data) {
        DataResult<T> dataResult = new DataResult<>();
        dataResult.setCode(code);
        dataResult.setMsg(msg);
        dataResult.setSuccess(true);
        dataResult.setData(data);
        return dataResult;
    }

    /**
     * 构建带数据的成功响应（使用默认状态码和提示消息）
     * <p>
     * 默认状态：code=200、msg="操作成功(Operation successful)"，适用于常规数据查询、详情返回等场景。
     *
     * @param data 业务数据（如用户详情、列表数据、统计结果）
     * @param <T>  业务数据泛型
     * @return 带数据的默认成功 {@link DataResult} 实例
     * @example
     * // 查询用户详情成功响应
     * UserDTO user = userService.getById(userId);
     * return Result.success(user);
     */
    public static <T> DataResult<T> success(T data) {
        return success(SUCCESS.getCode(), SUCCESS.getMsg(), data);
    }

    /**
     * 构建无数据的成功响应（使用默认状态码和提示消息）
     * <p>
     * 默认状态：code=200、msg="操作成功(Operation successful)"，适用于无需返回数据的成功场景（如新增、修改、删除操作）。
     *
     * @param <T> 数据泛型（无数据时为 null）
     * @return 无数据的默认成功 {@link DataResult} 实例
     * @example
     * // 数据删除成功响应
     * userService.removeById(userId);
     * return Result.success();
     */
    public static <T> DataResult<T> success() {
        return success(null);
    }
}
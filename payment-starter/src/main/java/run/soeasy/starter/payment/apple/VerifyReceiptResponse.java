package run.soeasy.starter.payment.apple;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * <a href=
 * "https://developer.apple.com/documentation/appstorereceipts/responsebody">文档</a>
 * 
 * @author soeasy.run
 *
 */
@Data
public class VerifyReceiptResponse {
	/**
	 * <a href=
	 * "https://developer.apple.com/documentation/appstorereceipts/status">文档</a>
	 * 
	 * 0 SUCCESS。
	 *  21000 App Store不能读取你提供的JSON对象。
	 *  21002 receipt-data域的数据有问题。
	 *  21003 receipt无法通过验证。
	 *  21004 提供的shared secret不匹配你账号中的shared secret 
	 *  21005 receipt服务器当前不可用。
	 *  21006 receipt合法，但是订阅已过期。服务器接收到这个状态码时，receipt数据仍然会解码并一起发送。
	 *  21007 receipt是Sandbox receipt，但却发送至生产系统的验证服务 
	 *  21008 receipt是生产receipt，但却发送至Sandbox环境的验证服务
	 */
	private int status;

	/**
	 * 收据生成的环境。
	 * 
	 * 可能的值： Sandbox, Production
	 */
	private String environment;

	public boolean isSandbox() {
		return "Sandbox".equals(getEnvironment());
	}
	
	/**
	 * 发送用于验证的收据的JSON表示形式。
	 */
	private Receipt receipt;

	/**
	 * 指示在请求期间发生错误的指示器。
	 * 
	 * 值1表示暂时性问题；稍后重试对此收据进行验证。值0表示无法解决的问题；请勿重试对此收据进行验证。仅适用于状态代码21100- 21199。
	 */
	@JsonProperty("is_retryable")
	private boolean retryable;

	/**
	 * 是否可以使用is_retryable字段.
	 * 
	 *  该字段要求错误码为21100-21199。
	 */
	public boolean isUseRetryable() {
		int status = getStatus();
		return status >= 21100 && status <= 21199;
	}

	/**
	 * 最新的Base64编码的应用程序收据。
	 * 
	 *  仅针对包含自动续订的收据返回。
	 */
	private String latestReceipt;

	/**
	 * 包含所有应用内购买交易的数组。
	 * 
	 *  这不包括已被您的应用标记为完成的消耗品交易。仅针对包含自动续订的收据返回。
	 */
	private  List<LatestReceiptInfo> latestReceiptInfos;

	/**
	 * 在JSON文件中，一个数组，
	 * 
	 * In the JSON file, an array where each element contains the pending renewal
	 * information for each auto-renewable subscription identified by the
	 * product_id. Only returned for app receipts that contain auto-renewable
	 * subscriptions.
	 * 
	 *  其中每个元素包含由产品标识标识的每个自动续订订阅的挂起续订信息。仅对包含自动续订订阅的应用程序回执返回
	 */
	private List<PendingRenewalInfo> pendingRenewalInfos;

	public boolean isSuccess() {
		return getStatus() == 0;
	}

	public boolean isError() {
		return !isSuccess();
	}

	/**
	 * 运行环境错误 ，应该到沙盒模式或正式模式下尝试
	 * 
	 * @return 运行环境错误
	 * @see #getStatus()
	 */
	public boolean isOperationModeError() {
		int status = getStatus();
		return status == 21007 || status == 21003;
	}
}

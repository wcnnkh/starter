package run.soeasy.starter.payment.apple;

import org.springframework.http.ResponseEntity;

import lombok.Getter;
import lombok.Setter;
import run.soeasy.starter.commons.json.JacksonFormat;
import run.soeasy.starter.commons.web.HttpClient;

/**
 * <a href=
 * "https://developer.apple.com/documentation/appstorereceipts/verifyreceipt">文档</a>
 * 
 * @author wcnnkh
 *
 */
@Getter
@Setter
public class ApplePay {
	static final String SANDBOX_URL = "https://sandbox.itunes.apple.com/verifyReceipt";
	static final String DEV_URL = "https://buy.itunes.apple.com/verifyReceipt";
	private static HttpClient httpClient = new HttpClient();

	static {
		httpClient.setJsonFormat(JacksonFormat.SNAKE_CASE);
	}
	/**
	 * 应用程序的共享机密（十六进制字符串）。仅对包含自动续订的收据使用此字段。
	 */
	private String password;

	public VerifyReceiptResponse verifyReceipt(String host, VerifyReceiptRequest request) {
		ResponseEntity<VerifyReceiptResponse> response = httpClient.postJson(host, null, request, String.class)
				.toJSON(VerifyReceiptResponse.class);
		VerifyReceiptResponse verifyReceiptResponse = response.getBody();
		if (verifyReceiptResponse.isUseRetryable() && verifyReceiptResponse.isRetryable()) {
			return verifyReceipt(host, request);
		}
		return verifyReceiptResponse;
	}

	/**
	 * 检查凭据(自动检查是否是沙盒模式)
	 * 
	 * @param request request info
	 * @return response
	 */
	public VerifyReceiptResponse verifyReceipt(VerifyReceiptRequest request) {
		VerifyReceiptResponse res = verifyReceipt(DEV_URL, request);
		if (res.isOperationModeError()) {// 这是一个测试环境下的订单或者收据无法通过身份验证。
			res = verifyReceipt(SANDBOX_URL, request);
		}
		return res;
	}
}

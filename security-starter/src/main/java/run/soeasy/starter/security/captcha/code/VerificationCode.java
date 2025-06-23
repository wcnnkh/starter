package run.soeasy.starter.security.captcha.code;

import lombok.Data;

@Data
public class VerificationCode {
	/**
	 * 用户
	 */
	private String user;
	/**
	 * 验证码
	 */
	private String coee;
}

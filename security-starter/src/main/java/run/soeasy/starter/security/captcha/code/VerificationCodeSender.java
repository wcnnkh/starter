package run.soeasy.starter.security.captcha.code;

import run.soeasy.starter.security.captcha.CaptchaException;

/**
 * 验证码发送器
 * 
 * @author soeasy.run
 *
 */
public interface VerificationCodeSender {
	void send(VerificationCode request) throws CaptchaException;
}

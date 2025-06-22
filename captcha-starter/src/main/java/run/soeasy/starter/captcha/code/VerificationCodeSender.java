package run.soeasy.starter.captcha.code;

import run.soeasy.starter.captcha.CaptchaException;

/**
 * 验证码发送器
 * 
 * @author soeasy.run
 *
 */
public interface VerificationCodeSender {
	void send(VerificationCode request) throws CaptchaException;
}

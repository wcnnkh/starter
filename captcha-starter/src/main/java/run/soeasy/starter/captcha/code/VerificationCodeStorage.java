package run.soeasy.starter.captcha.code;

import run.soeasy.starter.captcha.CaptchaException;

/**
 * 验证码存储器
 * 
 * @author soeasy.run
 *
 */
public interface VerificationCodeStorage {
	void set(VerificationCode verificationCode) throws CaptchaException;

	void verify(VerificationCode verificationCode) throws CaptchaException;

	void remove(VerificationCode verificationCode) throws CaptchaException;
}

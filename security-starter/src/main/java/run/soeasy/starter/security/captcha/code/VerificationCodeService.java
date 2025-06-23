package run.soeasy.starter.security.captcha.code;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import run.soeasy.starter.security.captcha.CaptchaException;

/**
 * 验证码服务
 * 
 * @author soeasy.run
 *
 */
@RequiredArgsConstructor
@Getter
@Setter
public class VerificationCodeService {
	@NonNull
	private final VerificationCodeSender verificationCodeSender;
	@NonNull
	private final VerificationCodeStorage verificationCodeStorage;

	public void send(VerificationCode verificationCode) throws CaptchaException {
		verificationCodeStorage.set(verificationCode);
		verificationCodeSender.send(verificationCode);
	}

	public void verify(VerificationCode verificationCode) throws CaptchaException {
		verificationCodeStorage.verify(verificationCode);
		verificationCodeStorage.remove(verificationCode);
	}
}

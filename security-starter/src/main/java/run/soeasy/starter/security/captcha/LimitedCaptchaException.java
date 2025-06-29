package run.soeasy.starter.security.captcha;

/**
 * 已被限制的验证
 * 
 * @author soeasy.run
 *
 */
public class LimitedCaptchaException extends CaptchaException {
	private static final long serialVersionUID = 1L;

	public LimitedCaptchaException(String message) {
		super(message);
	}
}
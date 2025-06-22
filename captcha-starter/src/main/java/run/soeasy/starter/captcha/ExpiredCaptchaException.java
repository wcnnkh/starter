package run.soeasy.starter.captcha;

/**
 * 已过期的验证
 * 
 * @author soeasy.run
 *
 */
public class ExpiredCaptchaException extends CaptchaException {
	private static final long serialVersionUID = 1L;

	public ExpiredCaptchaException(String message) {
		super(message);
	}
}

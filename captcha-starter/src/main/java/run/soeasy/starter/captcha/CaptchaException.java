package run.soeasy.starter.captcha;

public class CaptchaException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CaptchaException() {
		super();
	}

	public CaptchaException(String message) {
		super(message);
	}

	public CaptchaException(String message, Throwable cause) {
		super(message, cause);
	}

	public CaptchaException(Throwable cause) {
		super(cause);
	}
}

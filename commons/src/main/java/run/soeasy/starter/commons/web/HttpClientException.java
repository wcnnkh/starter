package run.soeasy.starter.commons.web;

public class HttpClientException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public HttpClientException(String message) {
		super(message);
	}

	public HttpClientException(Throwable cause) {
		super(cause);
	}

	public HttpClientException(String message, Throwable cause) {
		super(message, cause);
	}
}

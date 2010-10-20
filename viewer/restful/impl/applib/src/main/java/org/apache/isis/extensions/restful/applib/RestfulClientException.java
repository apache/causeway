package org.apache.isis.extensions.restful.applib;

public class RestfulClientException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RestfulClientException() {
	}

	public RestfulClientException(String message) {
		super(message);
	}

	public RestfulClientException(Throwable cause) {
		super(cause);
	}

	public RestfulClientException(String message, Throwable cause) {
		super(message, cause);
	}

}

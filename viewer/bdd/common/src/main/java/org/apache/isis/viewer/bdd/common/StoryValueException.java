package org.apache.isis.viewer.bdd.common;

public class StoryValueException extends Exception {

	private static final long serialVersionUID = 1L;

	public StoryValueException() {
	}

	public StoryValueException(String message) {
		super(message);
	}

	public StoryValueException(Throwable cause) {
		super(cause);
	}

	public StoryValueException(String message, Throwable cause) {
		super(message, cause);
	}

}

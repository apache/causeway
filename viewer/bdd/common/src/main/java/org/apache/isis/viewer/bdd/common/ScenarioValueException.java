package org.apache.isis.viewer.bdd.common;

public class ScenarioValueException extends Exception {

	private static final long serialVersionUID = 1L;

	public ScenarioValueException() {
	}

	public ScenarioValueException(String message) {
		super(message);
	}

	public ScenarioValueException(Throwable cause) {
		super(cause);
	}

	public ScenarioValueException(String message, Throwable cause) {
		super(message, cause);
	}

}

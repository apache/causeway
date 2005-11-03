package org.nakedobjects.object.reflect;

import org.nakedobjects.utility.NakedObjectRuntimeException;

public class ReflectionException extends NakedObjectRuntimeException {

	public ReflectionException() {
		super();
	}

	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(Throwable cause) {
		super(cause);
	}

	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
	}

}

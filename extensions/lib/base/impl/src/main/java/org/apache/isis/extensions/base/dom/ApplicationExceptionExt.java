package org.apache.isis.extensions.base.dom;

import org.apache.isis.applib.ApplicationException;

/**
 * An unexpected application exception; will render as a stack trace to the end-user. 
 */
public class ApplicationExceptionExt extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public ApplicationExceptionExt(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public ApplicationExceptionExt(final String msg) {
        super(msg);
    }

    public ApplicationExceptionExt(final Throwable cause) {
        super(cause);
    }

    
}

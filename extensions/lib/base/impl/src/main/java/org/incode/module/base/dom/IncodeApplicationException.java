package org.incode.module.base.dom;

import org.apache.isis.applib.ApplicationException;

/**
 * An unexpected application exception; will render as a stack trace to the end-user. 
 */
public class IncodeApplicationException extends ApplicationException {

    private static final long serialVersionUID = 1L;

    public IncodeApplicationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    public IncodeApplicationException(final String msg) {
        super(msg);
    }

    public IncodeApplicationException(final Throwable cause) {
        super(cause);
    }

    
}

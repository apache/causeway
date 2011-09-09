package org.apache.isis.viewer.json.viewer;

import javax.ws.rs.WebApplicationException;

import org.apache.isis.viewer.json.applib.HttpStatusCode;

public class JsonApplicationException extends WebApplicationException {

    public static final JsonApplicationException create(HttpStatusCode httpStatusCode) {
        return new JsonApplicationException(httpStatusCode, null, null);
    }

    public static JsonApplicationException create(HttpStatusCode httpStatusCode, String message, Object... args) {
        return create(httpStatusCode, String.format(message, args));
    }

    public static JsonApplicationException create(HttpStatusCode httpStatusCode, Exception cause) {
        return create(httpStatusCode, null, cause);
    }
    
    public static JsonApplicationException create(HttpStatusCode httpStatusCode, String message, Exception cause) {
        return new JsonApplicationException(httpStatusCode, message, cause);
    }

    private static final long serialVersionUID = 1L;
    private HttpStatusCode httpStatusCode;
    private final String message;

    private JsonApplicationException(HttpStatusCode httpStatusCode, String message, Throwable ex) {
        super(ex, httpStatusCode.getStatusCode());
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }
    
    /**
     * Overridden since cannot pass up to constructor.
     */
    @Override
    public String getMessage() {
        return message;
    }
    
    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }



}

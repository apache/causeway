package org.apache.isis.viewer.json.viewer;

import javax.ws.rs.WebApplicationException;

import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;

public class JsonApplicationException extends RuntimeException {

    public static final JsonApplicationException create(HttpStatusCode httpStatusCode) {
        return create(httpStatusCode, null);
    }

    public static JsonApplicationException create(HttpStatusCode httpStatusCode, String message,  Object... args) {
        return create(httpStatusCode, null, message, args);
    }

    public static JsonApplicationException create(HttpStatusCode httpStatusCode, Exception cause) {
        return create(httpStatusCode, cause, null);
    }
    
    public static JsonApplicationException create(HttpStatusCode httpStatusCode, Exception cause, String message, Object... args) {
        return new JsonApplicationException(httpStatusCode, formatString(message, args), cause);
    }

    private static String formatString(String formatStr, Object... args) {
        return formatStr != null? String.format(formatStr, args): null;
    }

    private static final long serialVersionUID = 1L;
    private HttpStatusCode httpStatusCode;

    private JsonApplicationException(HttpStatusCode httpStatusCode, String message, Throwable ex) {
        super(message, ex);
        this.httpStatusCode = httpStatusCode;
    }
    
    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }


}

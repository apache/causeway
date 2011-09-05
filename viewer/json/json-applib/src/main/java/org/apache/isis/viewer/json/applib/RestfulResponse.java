package org.apache.isis.viewer.json.applib;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.RestfulResponse.Header;
import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class RestfulResponse<T> {

    public enum Header {
        WARNING,
        LAST_MODIFIED,
        CONTENT_TYPE,
        CACHE_CONTROL,
        X_REPRESENTATION_TYPE;

        public String getName() {
            return HeaderNameUtils.convert(name());
        }
    }

    private final Response response;
    private final HttpStatusCode httpStatusCode;
    private final Class<T> returnType;
    public static final String HTTP_CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static <T> RestfulResponse<T> of(Response response, Class<T> returnType) {
        return new RestfulResponse<T>(response, returnType);
    }

    public RestfulResponse(Response response, Class<T> returnType) {
        this.response = response;
        this.httpStatusCode = HttpStatusCode.statusFor(response.getStatus());
        this.returnType = returnType;
    }

    public HttpStatusCode getStatus() {
        return httpStatusCode;
    }

    public T getEntity() throws JsonParseException, JsonMappingException, IOException {
        return JsonMapper.instance().read(response, returnType);
    }

    public <V> V getHeader(Header header, Class<V> returnType) {
        MultivaluedMap<String, Object> metadata = response.getMetadata();
        // in spite of the always returns a String
        String value = (String) metadata.getFirst(Header.CACHE_CONTROL.getName());
        return cast(value, returnType);
    }

    @SuppressWarnings("unchecked")
    protected <V> V cast(String value, Class<V> returnType) {
        if(value == null) {
            return null;
        }
        if(returnType == String.class) {
            return (V) value;
        }
        if(returnType == int.class) {
            return (V) Integer.valueOf(value);
        }
        throw new IllegalArgumentException("requested type of " + returnType + " not supported");
    }

}

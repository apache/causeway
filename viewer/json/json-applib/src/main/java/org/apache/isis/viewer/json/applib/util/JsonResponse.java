package org.apache.isis.viewer.json.applib.util;

import java.io.IOException;

import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class JsonResponse<T> {
    
    public static final String HEADER_X_RESTFUL_OBJECTS_REASON = "X-RestfulObjects-Reason";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    
    private final Response response;
    private final HttpStatusCode httpStatusCode;
    private final Class<T> returnType;
    public static final String HTTP_CONTENT_TYPE_APPLICATION_JSON = "application/json";

    public static <T> JsonResponse<T> of(Response response, Class<T> returnType) {
        return new JsonResponse<T>(response, returnType);
    }

    public JsonResponse(Response response, Class<T> returnType) {
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


}

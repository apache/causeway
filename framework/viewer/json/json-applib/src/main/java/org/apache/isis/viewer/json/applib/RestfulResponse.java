package org.apache.isis.viewer.json.applib;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class RestfulResponse<T> {

    private static final SimpleDateFormat RFC1123_DATE_FORMAT = new SimpleDateFormat("EEE, dd MMM yyyyy HH:mm:ss z");
    
    public abstract static class Header<X> {
        public final static Header<String> WARNING = new Header<String>("Warning"){
            @Override
            public String parse(String value) {
                return value;
            }
        };
        public final static Header<Date> LAST_MODIFIED = new Header<Date>("Last-Modified"){
            @Override
            public Date parse(String value) {
                try {
                    return RFC1123_DATE_FORMAT.parse(value);
                } catch (ParseException e) {
                    return null;
                }
            }};
        public final static Header<CacheControl> CACHE_CONTROL = new Header<CacheControl>("Cache-Control"){
            @Override
            public CacheControl parse(String value) {
                return CacheControl.valueOf(value);
            }};
        public final static Header<MediaType> MEDIA_TYPE = new Header<MediaType>("Content-Type"){
            @Override
            public MediaType parse(String value) {
                return MediaType.valueOf(value);
            }};
        public final static Header<RepresentationType> X_REPRESENTATION_TYPE = new Header<RepresentationType>("X-Representation-Type"){
            @Override
            public RepresentationType parse(String value) {
                return RepresentationType.lookup(value);
            }};
        
        private final String name;
        
        public Header(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
        
        public abstract X parse(String value);
        
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

    public <V> V getHeader(Header<V> header) {
        MultivaluedMap<String, Object> metadata = response.getMetadata();
        // in spite of the always returns a String
        String value = (String) metadata.getFirst(header.getName());
        return header.parse(value);
    }

}

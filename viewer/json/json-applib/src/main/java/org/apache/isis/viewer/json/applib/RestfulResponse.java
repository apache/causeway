package org.apache.isis.viewer.json.applib;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.isis.viewer.json.applib.util.JsonMapper;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

public class RestfulResponse<T> {

    public static class Header<X> {
        
        public final static Header<String> WARNING = new Header<String>("Warning", Parser.forStrings());
        public final static Header<Date> LAST_MODIFIED = new Header<Date>("Last-Modified", Parser.forDates());
        public final static Header<CacheControl> CACHE_CONTROL = new Header<CacheControl>("Cache-Control", Parser.forCacheControl());
        public final static Header<MediaType> CONTENT_TYPE = new Header<MediaType>("Content-Type", Parser.forMediaType());
        public final static Header<RepresentationType> X_REPRESENTATION_TYPE = new Header<RepresentationType>("X-Representation-Type", Parser.forRepresentationType());
        
        private final String name;
        private final Parser<X> parser;
        
        private Header(String name, Parser<X> parser) {
            this.name = name;
            this.parser = parser;
        }

        public String getName() {
            return name;
        }
        
        public X parse(String value) {
            return parser.valueOf(value);
        }
        
    }

    private final Response response;
    private final HttpStatusCode httpStatusCode;
    private final Class<T> returnType;

    public static <T> RestfulResponse<T> of(Response response, Class<T> returnType) {
        return new RestfulResponse<T>(response, returnType);
    }

    private RestfulResponse(Response response, Class<T> returnType) {
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

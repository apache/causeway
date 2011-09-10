package org.apache.isis.viewer.json.applib;

import java.util.Date;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;

public final class RestfulRequest {

    public static class Header<X> {
        public static Header<Date> IF_UNMODIFIED_SINCE = new Header<Date>("If-Unmodified-Since", Parser.forDates());
        public static Header<Boolean> X_FOLLOW_LINKS = new Header<Boolean>("X-Follow-Links", Parser.forBoolean());
        public static Header<Boolean> X_VALIDATION_ONLY = new Header<Boolean>("X-Validation-Only", Parser.forBoolean());
            
        private final String name;
        private final Parser<X> parser;
        private Header(String name, Parser<X> parser) {
            this.name = name;
            this.parser = parser;
        }

        public String getName() {
            return name;
        }
        
        void setHeader(ClientRequest clientRequest, X t) {
            clientRequest.header(getName(), asString(t));
        }

        public String asString(X x) {
            return parser.asString(x);
        }
    }

    private final ClientRequest clientRequest;
    
    public RestfulRequest(ClientRequest clientRequest) {
        this.clientRequest = clientRequest;
    }

    public <T> void header(Header<T> header, T t) {
        header.setHeader(clientRequest, t);
    }

    /**
     * Exposed primarily for testing.
     */
    public ClientRequest getClientRequest() {
        return clientRequest;
    }

    public Response execute() throws Exception {
        return clientRequest.execute();
    }

    public <T> RestfulResponse<T> execute(Class<T> requiredType) {
        try {
            Response executeJaxrs = execute();
            return RestfulResponse.of(executeJaxrs, requiredType);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}

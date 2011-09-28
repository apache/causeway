package org.apache.isis.viewer.json.applib;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;

public final class RestfulRequest {

    public enum DomainModel {
        NONE,
        SIMPLE,
        FORMAL,
        SELECTABLE;
        
        public static Parser<DomainModel> parser() {
            return new Parser<RestfulRequest.DomainModel>() {
                
                @Override
                public DomainModel valueOf(String str) {
                    return DomainModel.valueOf(str.toUpperCase());
                }
                
                @Override
                public String asString(DomainModel t) {
                    return t.name().toLowerCase();
                }
            };
        }
    }
    
    public static class QueryParameter<Q> {

        public static QueryParameter<Iterable<String>> FOLLOW_LINKS = new QueryParameter<Iterable<String>>("x-ro-follow-links", Parser.forIterableOfStrings());
        public static QueryParameter<Integer> PAGE = new QueryParameter<Integer>("x-ro-page", Parser.forInteger());
        public static QueryParameter<Integer> PAGE_SIZE = new QueryParameter<Integer>("x-ro-page-size", Parser.forInteger());
        public static QueryParameter<Iterable<String>> SORT_BY = new QueryParameter<Iterable<String>>("x-ro-sort-by", Parser.forIterableOfStrings());
        public static QueryParameter<DomainModel> DOMAIN_MODEL = new QueryParameter<DomainModel>("x-ro-domain-model", DomainModel.parser());
        public static QueryParameter<Boolean> VALIDATE_ONLY = new QueryParameter<Boolean>("x-ro-validate-only", Parser.forBoolean());
        
        private final String name;
        private final Parser<Q> parser;
        
        private QueryParameter(String name, Parser<Q> parser) {
            this.name = name;
            this.parser = parser;
        }
        
        public String getName() {
            return name;
        }

        public Parser<Q> getParser() {
            return parser;
        }
    }

    public static class Header<X> {
        public static Header<String> IF_MATCH = new Header<String>("If-Match", Parser.forString());
        public static Header<Iterable<String>> ACCEPT = new Header<Iterable<String>>("If-Match", Parser.forIterableOfStrings());
            
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

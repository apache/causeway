package org.apache.isis.viewer.json.applib;

import static org.apache.isis.viewer.json.applib.util.UrlDecodeUtils.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;

import com.google.common.collect.Maps;

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
        
        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }
    
    public static class QueryParameter<Q> {

        public static QueryParameter<List<List<String>>> FOLLOW_LINKS = new QueryParameter<List<List<String>>>("x-ro-follow-links", Parser.forListOfListOfStrings(), Collections.<List<String>>emptyList());
        public static QueryParameter<Integer> PAGE = new QueryParameter<Integer>("x-ro-page", Parser.forInteger(), 1);
        public static QueryParameter<Integer> PAGE_SIZE = new QueryParameter<Integer>("x-ro-page-size", Parser.forInteger(), 25);
        public static QueryParameter<List<String>> SORT_BY = new QueryParameter<List<String>>("x-ro-sort-by", Parser.forListOfStrings(), Collections.<String>emptyList());
        public static QueryParameter<DomainModel> DOMAIN_MODEL = new QueryParameter<DomainModel>("x-ro-domain-model", DomainModel.parser(), DomainModel.SIMPLE);
        public static QueryParameter<Boolean> VALIDATE_ONLY = new QueryParameter<Boolean>("x-ro-validate-only", Parser.forBoolean(), false);
        
        private final String name;
        private final Parser<Q> parser;
        private final Q defaultValue;
        
        private QueryParameter(String name, Parser<Q> parser, Q defaultValue) {
            this.name = name;
            this.parser = parser;
            this.defaultValue = defaultValue;
        }
        
        public String getName() {
            return name;
        }

        public Parser<Q> getParser() {
            return parser;
        }

        public void setValue(ClientRequest clientRequest, Q value) {
            clientRequest.queryParameter(getName(), parser.asString(value));
        }
        
        public Q valueOf(Map<?, ?> parameterMap) {
            if(parameterMap == null) {
                return defaultValue;
            }
            @SuppressWarnings("unchecked")
            Map<String, String[]> parameters = (Map<String, String[]>) parameterMap; 
            final String[] values = parameters.get(getName());
            if(values == null) {
                return defaultValue;
            }
            // special case processing
            if(values.length == 1) {
                return getParser().valueOf(urlDecode(values[0]));
            }
            return getParser().valueOf(urlDecode(values));
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }

    public static class Header<X> {
        public static Header<String> IF_MATCH = new Header<String>("If-Match", Parser.forString());
        public static Header<List<MediaType>> ACCEPT = new Header<List<MediaType>>("Accept", Parser.forListOfMediaTypes());
            
        private final String name;
        private final Parser<X> parser;
        private Header(String name, Parser<X> parser) {
            this.name = name;
            this.parser = parser;
        }

        public String getName() {
            return name;
        }
        
        public Parser<X> getParser() {
            return parser;
        }
        
        void setHeader(ClientRequest clientRequest, X t) {
            clientRequest.header(getName(), parser.asString(t));
        }
        
        @Override
        public String toString() {
            return getName();
        }
    }

    private final ClientRequest clientRequest;
    private final HttpMethod httpMethod;
    private final Map<QueryParameter<?>, Object> queryArgs = Maps.newLinkedHashMap();
    
    public RestfulRequest(ClientRequest clientRequest, HttpMethod httpMethod) {
        this.clientRequest = clientRequest;
        this.httpMethod = httpMethod;
    }


    /**
     * Exposed primarily for testing.
     */
    public ClientRequest getClientRequest() {
        return clientRequest;
    }

    public <T> RestfulRequest withHeader(Header<T> header, T t) {
        header.setHeader(clientRequest, t);
        return this;
    }

    public <T> RestfulRequest withHeader(Header<List<T>> header, T... ts) {
        header.setHeader(clientRequest, Arrays.asList(ts));
        return this;
    }

    public <Q> RestfulRequest withArg(RestfulRequest.QueryParameter<Q> queryParam, String argStr) {
        final Q arg = queryParam.getParser().valueOf(argStr);
        return withArg(queryParam, arg);
    }

    public <Q> RestfulRequest withArg(RestfulRequest.QueryParameter<Q> queryParam, Q arg) {
        queryArgs.put(queryParam, arg);
        return this;
    }

    public RestfulResponse<JsonRepresentation> execute() {
        try {
            if(httpMethod == HttpMethod.GET) {
                setQueryArgs();
            }
            if(httpMethod == HttpMethod.POST) {
                throw new RuntimeException("not yet implemented");
            }

            Response executeJaxrs = clientRequest.execute();
            return RestfulResponse.ofT(executeJaxrs);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void setQueryArgs() {
        for (QueryParameter queryParam : queryArgs.keySet()) {
            queryParam.setValue(clientRequest, queryArgs.get(queryParam));
        }
    }


    @SuppressWarnings("unchecked")
    public <T extends JsonRepresentation> RestfulResponse<T> executeT() {
        final RestfulResponse<JsonRepresentation> restfulResponse = execute();
        return (RestfulResponse<T>) restfulResponse;
    }


}

package org.apache.isis.viewer.json.applib.blocks;

import java.util.Arrays;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.util.UrlEncodingUtils;
import org.jboss.resteasy.client.ClientRequest;

public enum Method {
    GET(ArgStrategy.QUERY_STRING),
    PUT(ArgStrategy.BODY),
    POST(ArgStrategy.BODY),
    DELETE(ArgStrategy.QUERY_STRING);

    private enum ArgStrategy {
        QUERY_STRING,
        BODY;
        void setUpArgs(ClientRequest restEasyRequest, JsonRepresentation requestArgs) {
            if(this == QUERY_STRING) {
                final MultivaluedMap<String, String> queryParameters = restEasyRequest.getQueryParameters();
                for(Map.Entry<String, JsonRepresentation> entry: requestArgs.mapIterable()) {
                    final String param = entry.getKey();
                    final JsonRepresentation argRepr = entry.getValue();
                    final String arg = UrlEncodingUtils.asUrlEncoded(argRepr.asArg());
                    queryParameters.add(param, arg);
                }
            } else {
                restEasyRequest.body(MediaType.APPLICATION_JSON_TYPE, requestArgs.toString());
            }
        }
    }
    
    private final ArgStrategy argStrategy;

    private Method(ArgStrategy argStrategy) {
        this.argStrategy = argStrategy;
    }

    public void setUp(ClientRequest restEasyRequest, JsonRepresentation requestArgs) {
        restEasyRequest.setHttpMethod(name());
        if(requestArgs == null) {
            return;
        }
        if(!requestArgs.isMap()) {
            throw new IllegalArgumentException("requestArgs must be a map; instead got: " + requestArgs);
        }
        argStrategy.setUpArgs(restEasyRequest, requestArgs);
    }

}

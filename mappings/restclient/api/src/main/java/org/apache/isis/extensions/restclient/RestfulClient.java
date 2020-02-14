/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.extensions.restclient;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.extensions.restclient.auth.BasicAuthFilter;
import org.apache.isis.extensions.restclient.auth.BasicAuthFilter.Credentials;
import org.apache.isis.extensions.restclient.log.RestfulLoggingFilter;

import static org.apache.isis.core.commons.internal.base._NullSafe.stream;

import lombok.extern.log4j.Log4j2;

/**
 * Setup the Restful Client with Basic-Auth:
 * <blockquote><pre>
RestfulClientConfig clientConfig = new RestfulClientConfig();
clientConfig.setRestfulBase("http://localhost:8080/helloworld/restful/");
// setup basic-auth
clientConfig.setUseBasicAuth(true); // default = false
clientConfig.setRestfulAuthUser("sven");
clientConfig.setRestfulAuthPassword("pass");
// setup request/response debug logging
clientConfig.setUseRequestDebugLogging(true); // default = false

RestfulClient client = RestfulClient.ofConfig(clientConfig);
 * </pre></blockquote>
 * 
 * Synchronous example:
 * <blockquote><pre>{@code

Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke", 
                SuppressionType.RO);

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Response response = request.post(args);

ResponseDigest<MyObject> digest = client.digest(response, MyObject.class);
}

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.getEntities().getSingletonOrFail().get$$instanceId());
} else {
    digest.getFailureCause().printStackTrace();
}
 * </pre></blockquote>
 * Asynchronous example:
 * <blockquote><pre>{@code 
Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke", 
                SuppressionType.RO);

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Future<Response> asyncResponse = request
        .async()
        .post(args);

CompletableFuture<ResponseDigest<MyObject>> digestFuture = 
                client.digest(asyncResponse, MyObject.class);

ResponseDigest<MyObject> digest = digestFuture.get(); // blocking
}

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.getEntities().getSingletonOrFail().get$$instanceId());
} else {
    digest.getFailureCause().printStackTrace();
}
 * </pre></blockquote>
 * 
 * Maven Setup:
 * <blockquote><pre>{@code
<dependency>
    <groupId>org.apache.isis.core</groupId>
    <artifactId>isis-core-applib</artifactId>
    <version>2.0.0-M2-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jersey.ext</groupId>
    <artifactId>jersey-spring5</artifactId>
    <version>2.29.1</version>
</dependency>
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>javax.json</artifactId>
    <version>1.1.4</version>
</dependency>
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.moxy</artifactId>
    <version>2.7.5</version>
</dependency>
 * }</pre></blockquote>
 * 
 * @since 2.0
 */
@Log4j2
public class RestfulClient {

    private static final String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json;profile=\"urn:org.apache.isis/v1\"";

    private RestfulClientConfig clientConfig;
    private Client client;

    public static RestfulClient ofConfig(RestfulClientConfig clientConfig) {
        RestfulClient restClient = new RestfulClient();
        restClient.init(clientConfig);
        return restClient;
    }

    public void init(RestfulClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        client = ClientBuilder.newClient();

        registerDefaultJsonProvider();
        registerBasicAuthFilter();
        registerRequestDebugLoggingFilter();
    }

    public RestfulClientConfig getConfig() {
        return clientConfig;
    }

    public Client getJaxRsClient() {
        return client;
    }

    // -- REQUEST BUILDER

    public Builder request(String path, SuppressionType ... suppressionTypes) {
        return request(path, SuppressionType.setOf(suppressionTypes));
    }

    public Builder request(String path, EnumSet<SuppressionType> suppressionTypes) {
        final String responseContentType = DEFAULT_RESPONSE_CONTENT_TYPE
                + toSuppressionLiteral(suppressionTypes);

        return client.target(relativePathToUri(path)).request(responseContentType);
    }

    // -- ARGUMENT BUILDER

    public ActionParameterListBuilder arguments() {
        return new ActionParameterListBuilder();
    }

    // -- RESPONSE PROCESSING (SYNC)

    public <T> ResponseDigest<T> digest(Response response, Class<T> entityType) {
        return ResponseDigest.of(response, entityType);
    }

    public <T> ResponseDigest<T> digestList(Response response, Class<T> entityType, GenericType<List<T>> genericType) {
        return ResponseDigest.ofList(response, entityType, genericType);
    }
    
    // -- RESPONSE PROCESSING (ASYNC)

    public <T> CompletableFuture<ResponseDigest<T>> digest(
            final Future<Response> asyncResponse, 
            final Class<T> entityType) {

        final CompletableFuture<ResponseDigest<T>> completableFuture = CompletableFuture.supplyAsync(()->{
            try {
                Response response = asyncResponse.get();
                ResponseDigest<T> digest = digest(response, entityType);

                return digest;

            } catch (Exception e) {
                return ResponseDigest.ofAsyncFailure(asyncResponse, entityType, e);
            }
        });

        return completableFuture;
    }
    
    public <T> CompletableFuture<ResponseDigest<T>> digestList(
            final Future<Response> asyncResponse, 
            final Class<T> entityType,
            GenericType<List<T>> genericType) {

        final CompletableFuture<ResponseDigest<T>> completableFuture = CompletableFuture.supplyAsync(()->{
            try {
                Response response = asyncResponse.get();
                ResponseDigest<T> digest = digestList(response, entityType, genericType);

                return digest;

            } catch (Exception e) {
                
                return ResponseDigest.ofAsyncFailure(asyncResponse, entityType, e);
                
            }
        });

        return completableFuture;
    }

    // -- FILTER

    private void registerDefaultJsonProvider() {
        try {
            Class<?> MOXyJsonProvider = _Context.loadClass("org.eclipse.persistence.jaxb.rs.MOXyJsonProvider");
            client.register(MOXyJsonProvider);
        } catch (Exception e) {
            log.warn("This implementation of RestfulClient does require the class 'MOXyJsonProvider'"
                    + " on the class-path."
                    + " Are you missing a maven dependency?");
        }
    }

    private void registerBasicAuthFilter() {
        if(clientConfig.isUseBasicAuth()){
            final Credentials credentials = Credentials.of(
                    clientConfig.getRestfulAuthUser(), 
                    clientConfig.getRestfulAuthPassword());
            client.register(BasicAuthFilter.of(credentials));
        }
    }

    private void registerRequestDebugLoggingFilter() {
        if(clientConfig.isUseRequestDebugLogging()){
            client.register(new RestfulLoggingFilter());
        }
    }

    // -- HELPER

    private String relativePathToUri(String path) {
        final String baseUri = _Strings.suffix(clientConfig.getRestfulBase(), "/");
        while(path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUri + path;
    }

    private String toSuppressionLiteral(EnumSet<SuppressionType> suppressionTypes) {
        final String suppressionSetLiteral = stream(suppressionTypes)
                .map(SuppressionType::name)
                .collect(Collectors.joining(","));

        if(_Strings.isNotEmpty(suppressionSetLiteral)) {
            return ";suppress=" + suppressionSetLiteral;
        }

        return "";
    }



}

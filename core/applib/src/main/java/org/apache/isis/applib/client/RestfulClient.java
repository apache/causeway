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
package org.apache.isis.applib.client;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.client.auth.BasicAuthFilter;
import org.apache.isis.applib.client.auth.BasicAuthFilter.Credentials;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;

/**
 * Setup the Restful Client with Basic-Auth:
 * <pre>
RestfulClientConfig clientConfig = new RestfulClientConfig();
clientConfig.setRestfulBase("http://localhost:8080/helloworld/restful/");
// setup basic-auth
clientConfig.setUseBasicAuth(true);
clientConfig.setRestfulAuthUser("sven");
clientConfig.setRestfulAuthPassword("pass");

RestfulClient client = RestfulClient.ofConfig(clientConfig);
 * </pre>
 * 
 * Synchronous example:
 * <pre>

Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke", 
                SuppressionType.setOf(SuppressionType.RO));

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Response response = request.post(args);

ResponseDigest<MyObject> digest = client.digest(response, MyObject.class);

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.get().get$$instanceId());
} else {
    digest.getFailureCause().printStackTrace();
}
 * </pre>
 * Asynchronous example:
 * <pre>
 
Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke", 
                SuppressionType.setOf(SuppressionType.RO));

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Future<Response> asyncResponse = request
        .async()
        .post(args);

CompletableFuture<ResponseDigest<MyObject>> digestFuture = 
                client.digest(asyncResponse, MyObject.class);
        
ResponseDigest<MyObject> digest = digestFuture.get(); // blocking

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.get().get$$instanceId());
} else {
    digest.getFailureCause().printStackTrace();
}
 * </pre>
 * 
 * Maven Setup:
 * <pre>{@code
<dependency>
    <groupId>org.apache.isis.core</groupId>
    <artifactId>isis-core-applib</artifactId>
    <version>2.0.0-M2-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>javax.ws.rs</groupId>
    <artifactId>javax.ws.rs-api</artifactId>
    <version>2.1.1</version>
</dependency>
<dependency>
    <groupId>org.glassfish.jersey.core</groupId>
    <artifactId>jersey-client</artifactId>
    <version>2.25.1</version>
</dependency>
<dependency>
    <groupId>org.eclipse.persistence</groupId>
    <artifactId>org.eclipse.persistence.moxy</artifactId>
    <version>2.6.0</version>
</dependency>} 
 * </pre>
 * 
 * @since 2.0.0-M2
 */
public class RestfulClient {
    
    public static String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json;profile=urn:org.apache.isis/v1";

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
        
        if(clientConfig.isUseBasicAuth()){
            final Credentials credentials = Credentials.of(
                    clientConfig.getRestfulAuthUser(), 
                    clientConfig.getRestfulAuthPassword());
            client.register(BasicAuthFilter.of(credentials));
        }
        
        try {
            Class<?> MOXyJsonProvider = _Context.loadClass("org.eclipse.persistence.jaxb.rs.MOXyJsonProvider");
            client.register(MOXyJsonProvider);
        } catch (Exception e) {
            // this is just provided for convenience
        }
        
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

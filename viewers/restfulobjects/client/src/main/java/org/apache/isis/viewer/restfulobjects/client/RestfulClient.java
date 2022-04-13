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
package org.apache.isis.viewer.restfulobjects.client;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.functional.Try;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.viewer.restfulobjects.client.auth.BasicAuthFilter;
import org.apache.isis.viewer.restfulobjects.client.auth.BasicAuthFilter.Credentials;
import org.apache.isis.viewer.restfulobjects.client.log.ClientConversationLogger;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Setup the Restful Client with Basic-Auth:
 *
 * <p>
 *     For example:
 * </p>
 *
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
 * Make a Request and then digest the Response:
 * <blockquote><pre>{@code

Builder request = client.request(
                "services/myService/actions/lookupMyObjectById/invoke",
                SuppressionType.RO);

Entity<String> args = client.arguments()
        .addActionParameter("id", "12345")
        .build();

Response response = request.post(args);

Try<MyObject> digest = client.digest(response, MyObject.class);
}

if(digest.isSuccess()) {
    System.out.println("result: "+ digest.getValue().orElseThrow().get$$instanceId());
} else {
    digest.getFailure().get().printStackTrace();
}
 * </pre></blockquote>
 *
 * Maven Setup:
 * <blockquote><pre>{@code
<dependency>
    <groupId>org.glassfish.jersey.ext</groupId>
    <artifactId>jersey-spring5</artifactId>
    <version>2.35</version>
</dependency>
 * }</pre></blockquote>
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class RestfulClient {

    private static final String DEFAULT_RESPONSE_CONTENT_TYPE = "application/json;profile=\"urn:org.apache.isis/v2\"";

    private RestfulClientConfig clientConfig;
    private Client client;

    public static RestfulClient ofConfig(final RestfulClientConfig clientConfig) {
        RestfulClient restClient = new RestfulClient();
        restClient.init(clientConfig);
        return restClient;
    }

    public void init(final RestfulClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        client = ClientBuilder.newClient();

        registerDefaultJsonProvider();
        registerBasicAuthFilter();
        registerConversationFilters();
    }

    public RestfulClientConfig getConfig() {
        return clientConfig;
    }

    public Client getJaxRsClient() {
        return client;
    }

    // -- REQUEST BUILDER

    public Builder request(final String path, final SuppressionType ... suppressionTypes) {
        return request(path, SuppressionType.setOf(suppressionTypes));
    }

    public Builder request(final String path, final EnumSet<SuppressionType> suppressionTypes) {
        final String responseContentType = DEFAULT_RESPONSE_CONTENT_TYPE
                + toSuppressionLiteral(suppressionTypes);

        return client.target(relativePathToUri(path)).request(responseContentType);
    }

    // -- ARGUMENT BUILDER

    public ActionParameterListBuilder arguments() {
        return new ActionParameterListBuilder();
    }

    // -- RESPONSE PROCESSING

    public <T> Try<T> digest(final Response response, final Class<T> entityType) {
        val digest = ResponseDigest.wrap(response, entityType);
        if(digest.isSuccess()) {
            return Try.success(digest.getEntity().orElse(null));
        }
        return Try.failure(digest.getFailureCause());
    }

    public <T> Try<Can<T>> digestList(final Response response, final Class<T> entityType, final GenericType<List<T>> genericType) {
        val listDigest = ResponseDigest.wrapList(response, entityType, genericType);
        if(listDigest.isSuccess()) {
            return Try.success(listDigest.getEntities());
        }
        return Try.failure(listDigest.getFailureCause());
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

    private void registerConversationFilters() {
        if(clientConfig.isUseRequestDebugLogging()){
            client.register(new ClientConversationLogger());
        }
        clientConfig.getClientConversationFilters().stream()
        .filter(Objects::nonNull)
        .forEach(client::register);
    }

    // -- HELPER

    private String relativePathToUri(String path) {
        final String baseUri = _Strings.suffix(clientConfig.getRestfulBase(), "/");
        while(path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUri + path;
    }

    private String toSuppressionLiteral(final EnumSet<SuppressionType> suppressionTypes) {
        final String suppressionSetLiteral = stream(suppressionTypes)
                .map(SuppressionType::name)
                .collect(Collectors.joining(","));

        if(_Strings.isNotEmpty(suppressionSetLiteral)) {
            return ";suppress=" + suppressionSetLiteral;
        }

        return "";
    }


}

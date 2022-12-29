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
package org.apache.causeway.viewer.restfulobjects.client;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.context._Context;
import org.apache.causeway.viewer.restfulobjects.client.auth.BasicAuthFilter;
import org.apache.causeway.viewer.restfulobjects.client.auth.BasicAuthFilter.Credentials;
import org.apache.causeway.viewer.restfulobjects.client.log.ClientConversationLogger;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Setup the Restful Client with Basic-Auth:
 *
 * <p>
 *     For example:
 * </p>
 *
 * <blockquote><pre>
RestfulClientConfig clientConfig = RestfulClientConfig.builder();
.restfulBase("http://localhost:8080/helloworld/restful/")
// setup basic-auth
.useBasicAuth(true) // default = false
.restfulAuthUser("sven")
.restfulAuthPassword("pass")
// setup request/response debug logging
.useRequestDebugLogging(true) // default = false
.build();

RestfulClient client = RestfulClient.ofConfig(clientConfig);
 * </pre></blockquote>
 *
 * Make a Request and then digest the Response:
 * <blockquote><pre>{@code

Builder request = client.request("services/myService/actions/lookupMyObjectById/invoke")
    .accept(RestfulClientMediaType.SIMPLE_JSON.mediaTypeFor(MyObject.class, EnumSet.of(SuppressionType.RO)));

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
 * @since 2.0 {@index}
 */
@Log4j2
public class RestfulClient implements AutoCloseable {

    private RestfulClientConfig clientConfig;
    private Client client;

    public static RestfulClient ofConfig(
            final RestfulClientConfig clientConfig) {
        return new RestfulClient(clientConfig, UnaryOperator.identity());
    }

    public static RestfulClient ofConfig(
            final RestfulClientConfig clientConfig,
            final UnaryOperator<ClientBuilder> configRefiner) {
        return new RestfulClient(clientConfig, configRefiner);
    }

    private RestfulClient(
            final @NonNull RestfulClientConfig clientConfig,
            final @NonNull UnaryOperator<ClientBuilder> configRefiner) {
        this.clientConfig = clientConfig;

        final ClientBuilder clientBuilder = configRefiner.apply(ClientBuilder.newBuilder());
        this.client = clientBuilder.build();

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

    @Override
    public void close() {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Throwable ex) {
            // just ignore
        }
    }

    // -- REQUEST BUILDER

    public Builder request(final String path) {
        return client
                .target(relativePathToUri(path))
                .request();
    }

    // -- ARGUMENT BUILDER

    public ActionParameterListBuilder arguments() {
        return new ActionParameterListBuilder();
    }

    // -- RESPONSE PROCESSING

    public <T> Try<T> digest(final Response response, final Class<T> entityType) {
        final var digest = ResponseDigest.wrap(response, entityType);
        if(digest.isSuccess()) {
            return Try.success(digest.getEntity().orElse(null));
        }
        return Try.failure(digest.getFailureCause());
    }

    public <T> Try<Can<T>> digestList(final Response response, final Class<T> entityType, final GenericType<List<T>> genericType) {
        final var listDigest = ResponseDigest.wrapList(response, entityType, genericType);
        if(listDigest.isSuccess()) {
            return Try.success(listDigest.getEntities());
        }
        return Try.failure(listDigest.getFailureCause());
    }

    // -- UTILITY

    /**
     * Returns an {@link URI} constructed from this client's base path plus given relative {@code path}.
     * @param path relative to this client's base
     */
    public URI uri(final String path) {
        return relativePathToUri(path).build();
    }

    // -- HELPER FILTER

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

    // -- HELPER OTHER

    private UriBuilder relativePathToUri(String path) {
        final String baseUri = _Strings.suffix(clientConfig.getRestfulBase(), "/");
        while(path.startsWith("/")) {
            path = path.substring(1);
        }
        return UriBuilder.fromUri(baseUri + path);
    }

}

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
package org.apache.isis.legacy.restclient;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.legacy.restclient.lib.ClientExecutor;
import org.apache.isis.legacy.restclient.lib.ClientRequestConfigurer;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.RestfulRequest;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageResource;
import org.apache.isis.viewer.restfulobjects.applib.user.UserResource;
import org.apache.isis.viewer.restfulobjects.applib.version.VersionResource;

import lombok.Getter;
import lombok.val;

public class RestfulClient {

    private final HomePageResource homePageResource;
    private final UserResource userResource;
    private final VersionResource versionResource;
    private final DomainObjectResource domainObjectResource;
    private final DomainServiceResource domainServiceResource;
    private final DomainTypeResource domainTypeResource;

    private final ClientExecutor executor;
    @Getter private final ClientRequestFactory clientRequestFactory; // exposed for testing purposes only


    /**
     * Using {@link javax.ws.rs.client.Client}.
     */
    public RestfulClient(final URI baseUri) {
        this(baseUri, ClientBuilder.newClient());
    }

    /**
     * Using {@link javax.ws.rs.client.Client}.
     */
    public RestfulClient(final URI baseUri, Client client) {
        this(baseUri, ClientExecutor.of(client));
    }

    /**
     * Using arbitrary {@link ClientExecutor} implementation.
     */
    public RestfulClient(final URI baseUri, final ClientExecutor clientExecutor) {
        this.executor = clientExecutor;
        this.clientRequestFactory = ClientRequestFactory.of(clientExecutor, baseUri);

        this.homePageResource = clientRequestFactory.createProxy(HomePageResource.class);
        this.userResource = clientRequestFactory.createProxy(UserResource.class);
        this.domainTypeResource = clientRequestFactory.createProxy(DomainTypeResource.class);
        this.domainServiceResource = clientRequestFactory.createProxy(DomainServiceResource.class);
        this.domainObjectResource = clientRequestFactory.createProxy(DomainObjectResource.class);
        this.versionResource = clientRequestFactory.createProxy(VersionResource.class);
    }

    // ///////////////////////////////////////////////////////////////
    // resources
    // ///////////////////////////////////////////////////////////////

    public HomePageResource getHomePageResource() {
        return homePageResource;
    }

    public UserResource getUserResource() {
        return userResource;
    }

    public VersionResource getVersionResource() {
        return versionResource;
    }

    public DomainTypeResource getDomainTypeResource() {
        return domainTypeResource;
    }

    public DomainObjectResource getDomainObjectResource() {
        return domainObjectResource;
    }

    public DomainServiceResource getDomainServiceResource() {
        return domainServiceResource;
    }

    // ///////////////////////////////////////////////////////////////
    // resource walking support
    // ///////////////////////////////////////////////////////////////

    public RepresentationWalker createWalker(final Response response) {
        return new RepresentationWalker(this, response);
    }

    public RestfulResponse<JsonRepresentation> follow(final LinkRepresentation link) throws Exception {
        return followT(link);
    }

    public <T extends JsonRepresentation> RestfulResponse<T> followT(final LinkRepresentation link) throws Exception {
        return followT(link, JsonRepresentation.newMap());
    }

    public RestfulResponse<JsonRepresentation> follow(final LinkRepresentation link, final JsonRepresentation requestArgs) throws Exception {
        return followT(link, requestArgs);
    }

    public <T extends JsonRepresentation> RestfulResponse<T> followT(
            final LinkRepresentation link, 
            final JsonRepresentation requestArgs) throws Exception {
        
        final ClientRequestConfigurer clientRequestConfigurer = 
                ClientRequestConfigurer.create(executor, link.getHref());

        clientRequestConfigurer.accept(MediaType.APPLICATION_JSON_TYPE);
        clientRequestConfigurer.setHttpMethod(link.getHttpMethod());

        clientRequestConfigurer.configureArgs(requestArgs);

        final RestfulRequest restfulRequest = new RestfulRequest();
        return execute(restfulRequest, clientRequestConfigurer);
    }

    public RestfulRequest createRequest(final RestfulHttpMethod httpMethod, final String uriTemplate) {

        final boolean includesScheme = uriTemplate.startsWith("http:") || uriTemplate.startsWith("https:");
        final String base = clientRequestFactory.getBase().toString();
        final String uri = (includesScheme ? "" : base) + uriTemplate;

        final ClientRequestConfigurer clientRequestConfigurer = ClientRequestConfigurer.create(executor, uri);

        clientRequestConfigurer.accept(MediaType.APPLICATION_JSON_TYPE);
        clientRequestConfigurer.setHttpMethod(httpMethod);

        return new RestfulRequest();
    }
    
    // -- HELPER
    
    private <T extends JsonRepresentation> RestfulResponse<T> execute(
            final RestfulRequest restfulRequest,
            final ClientRequestConfigurer clientRequestConfigurer) {
        
        val args = restfulRequest.getArgs();
        
        try {
            if (!args.isEmpty()) {
                clientRequestConfigurer.configureArgs(args);
            }
            val clientRequest = clientRequestConfigurer.getClientRequest();
            val response = clientRequest.execute();

            return _Casts.<RestfulResponse<T>>uncheckedCast(RestfulResponse.ofT(response));
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }



}

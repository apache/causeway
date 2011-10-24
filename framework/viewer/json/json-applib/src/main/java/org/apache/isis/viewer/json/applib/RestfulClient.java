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
package org.apache.isis.viewer.json.applib;

import java.net.URI;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.isis.viewer.json.applib.blocks.LinkRepresentation;
import org.apache.isis.viewer.json.applib.capabilities.CapabilitiesResource;
import org.apache.isis.viewer.json.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domainobjects.DomainServiceResource;
import org.apache.isis.viewer.json.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.user.UserResource;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

public class RestfulClient {

    private final HomePageResource homePageResource;
    private final UserResource userResource;
    private final CapabilitiesResource capabilitiesResource;
    private final DomainObjectResource domainObjectResource;
    private final DomainServiceResource domainServiceResource;
    private final DomainTypeResource domainTypeResource;

    private final ClientExecutor executor;
    private final ClientRequestFactory clientRequestFactory;
    private HttpMethod httpMethod;

    public RestfulClient(final URI baseUri) {
        this(baseUri, new ApacheHttpClientExecutor(new HttpClient()));
    }

    public RestfulClient(final URI baseUri, ClientExecutor clientExecutor) {
        this.executor = clientExecutor;
        this.clientRequestFactory = new ClientRequestFactory(clientExecutor, baseUri);
        
        this.homePageResource = clientRequestFactory.createProxy(HomePageResource.class);
        this.userResource = clientRequestFactory.createProxy(UserResource.class);
        this.domainTypeResource = clientRequestFactory.createProxy(DomainTypeResource.class);
        this.domainServiceResource = clientRequestFactory.createProxy(DomainServiceResource.class);
        this.domainObjectResource = clientRequestFactory.createProxy(DomainObjectResource.class);
        this.capabilitiesResource = clientRequestFactory.createProxy(CapabilitiesResource.class);
    }

    
    /////////////////////////////////////////////////////////////////
    // resources
    /////////////////////////////////////////////////////////////////

    public HomePageResource getHomePageResource() {
        return homePageResource;
    }

    public UserResource getUserResource() {
        return userResource;
    }

    public CapabilitiesResource getCapabilitiesResource() {
        return capabilitiesResource;
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

    
    /////////////////////////////////////////////////////////////////
    // resource walking support
    /////////////////////////////////////////////////////////////////

    public RepresentationWalker createWalker(Response response) {
        return new RepresentationWalker(this, response);
    }

    public Response follow(LinkRepresentation link) throws Exception {
        return follow(link, JsonRepresentation.newMap());
    }

    // TODO: generalize into request args
    public Response follow(LinkRepresentation link, JsonRepresentation requestArgs) throws Exception {
        Response response = link.follow(executor, requestArgs);
        // this is a bit hacky
        @SuppressWarnings("unchecked")
        BaseClientResponse<String> restEasy = (BaseClientResponse<String>)response;
        restEasy.setReturnType(String.class);
        return response;
    }

    public RestfulRequest createRequest(HttpMethod httpMethod, String uriTemplate) {
        
        ClientRequest clientRequest = createRequest(uriTemplate);
        clientRequest.accept(MediaType.APPLICATION_JSON_TYPE);
        clientRequest.setHttpMethod(httpMethod.getJavaxRsMethod());
        
        return new RestfulRequest(clientRequest, httpMethod);
    }

    private ClientRequest createRequest(String uriTemplate) {
        boolean includesScheme = uriTemplate.startsWith("http:") || uriTemplate.startsWith("https:");
        if(includesScheme) {
            return clientRequestFactory.createRequest(uriTemplate);
        } else {
            return clientRequestFactory.createRelativeRequest(uriTemplate);
        }
    }
    
    /**
     * exposed for testing purposes only.
     */
    public ClientRequestFactory getClientRequestFactory() {
        return clientRequestFactory;
    }


}

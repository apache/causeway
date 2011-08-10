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

import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.HttpClient;
import org.apache.isis.viewer.json.applib.blocks.Link;
import org.apache.isis.viewer.json.applib.domain.DomainObjectResource;
import org.apache.isis.viewer.json.applib.domain.ServicesResource;
import org.apache.isis.viewer.json.applib.homepage.HomePageResource;
import org.apache.isis.viewer.json.applib.types.SpecsResource;
import org.apache.isis.viewer.json.applib.user.UserResource;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClientExecutor;

public class RestfulClient {

    private final HomePageResource homePageResource;
    private final ServicesResource servicesResource;
    private final DomainObjectResource domainObjectResource;
    private final UserResource userResource;
    private final SpecsResource specsResource;

    private final ClientExecutor executor;
    private final ClientRequestFactory clientRequestFactory;


    public RestfulClient(final URI baseUri) {
        this(baseUri, new ApacheHttpClientExecutor(new HttpClient()));
    }

    public RestfulClient(final URI baseUri, ClientExecutor clientExecutor) {
        this.executor = clientExecutor;
        this.clientRequestFactory = new ClientRequestFactory(clientExecutor, baseUri);
        
        this.homePageResource = clientRequestFactory.createProxy(HomePageResource.class);
        this.userResource = clientRequestFactory.createProxy(UserResource.class);
        this.specsResource = clientRequestFactory.createProxy(SpecsResource.class);
        this.servicesResource = clientRequestFactory.createProxy(ServicesResource.class);
        this.domainObjectResource = clientRequestFactory.createProxy(DomainObjectResource.class);
    }


    public HomePageResource getHomePageResource() {
        return homePageResource;
    }

    public ServicesResource getServicesResource() {
        return servicesResource;
    }

    public UserResource getUserResource() {
        return userResource;
    }

    public SpecsResource getSpecsResource() {
        return specsResource;
    }

    public DomainObjectResource getDomainObjectResource() {
        return domainObjectResource;
    }
    
    public ClientExecutor getExecutor() {
        return executor;
    }

    public RepresentationWalker createWalker(Response response) {
        return new RepresentationWalker(this, response);
    }

    public Response follow(Link link) throws Exception {
        return follow(link, null);
    }

    public Response follow(Link link, JsonRepresentation requestBody) throws Exception {
        Response response = link.follow(executor, requestBody);
        // this is a bit hacky
        @SuppressWarnings("unchecked")
        BaseClientResponse<String> restEasy = (BaseClientResponse<String>)response;
        restEasy.setReturnType(String.class);
        return response;
    }



}

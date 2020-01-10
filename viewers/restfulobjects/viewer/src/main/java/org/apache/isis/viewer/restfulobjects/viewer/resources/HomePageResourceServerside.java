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
package org.apache.isis.viewer.restfulobjects.viewer.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.homepage.HomePageResource;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

@Component
public class HomePageResourceServerside extends ResourceAbstract implements HomePageResource {

    @Inject
    public HomePageResourceServerside(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration) {
        super(metaModelContext, isisConfiguration);
    }

    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_HOME_PAGE })
    public Response homePage() {
        init(RepresentationType.HOME_PAGE, Where.NOWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    public Response deleteHomePageNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting the home page resource is not allowed.");
    }

    @Override
    public Response putHomePageNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to the home page resource is not allowed.");
    }

    @Override
    public Response postHomePageNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to the home page resource is not allowed.");
    }


    @Override
    @GET
    @Path("/notAuthenticated")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response notAuthenticated() {

        throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
    }

}
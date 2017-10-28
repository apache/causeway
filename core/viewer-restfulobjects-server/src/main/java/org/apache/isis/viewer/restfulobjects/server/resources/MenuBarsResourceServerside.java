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
package org.apache.isis.viewer.restfulobjects.server.resources;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.menus.MenuBars;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.menubars.MenuBarsResource;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.server.resources.serialization.SerializationStrategy;

public class MenuBarsResourceServerside extends ResourceAbstract implements MenuBarsResource {

    @Override
    @Produces({ MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS })
    public Response menuBars() {
        return doMenuBars(SerializationStrategy.XML);
    }

    @Override
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS })
    public Response menuBarsJson() {
        return doMenuBars(SerializationStrategy.JSON);
    }

    private Response doMenuBars(final SerializationStrategy serializationStrategy) {
        init(RepresentationType.MENUBARS, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final Response.ResponseBuilder builder;

        final MenuBarsService menuBarsService =
                getResourceContext().getServicesInjector().lookupService(MenuBarsService.class);

        final MenuBars menuBars = menuBarsService.menuBars();

        builder = Response.status(Response.Status.OK)
                .entity(serializationStrategy.entity(menuBars))
                .type(serializationStrategy.type(RepresentationType.MENUBARS));

        return builder.build();
    }

    @Override
    public Response deleteMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting the menuBars resource is not allowed.");

    }

    @Override
    public Response putMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to the menuBars resource is not allowed.");

    }

    @Override
    public Response postMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to the menuBars resource is not allowed.");
    }

}


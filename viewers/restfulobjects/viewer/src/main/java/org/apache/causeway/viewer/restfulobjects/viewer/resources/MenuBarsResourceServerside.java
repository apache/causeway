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
package org.apache.causeway.viewer.restfulobjects.viewer.resources;

import java.util.function.Consumer;

import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.Produces;
import org.springframework.http.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.links.Link;
import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.menubars.MenuBarsResource;
import org.apache.causeway.viewer.restfulobjects.applib.util.MediaTypes;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;

@Component
public class MenuBarsResourceServerside extends ResourceAbstract implements MenuBarsResource {

    public static final String SERVICE_IDENTIFIER = "1";

    public MenuBarsResourceServerside() {
        super();
    }

    @Override
    @Produces({
        MediaType.APPLICATION_JSON_VALUE, RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS,
        MediaType.APPLICATION_XML_VALUE, RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS
    })
    public Response menuBars() {

        var resourceContext = createResourceContext(
                RepresentationType.MENUBARS, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var serializationStrategy = resourceContext.getSerializationStrategy();
        var menuBarsService = metaModelContext.getServiceRegistry().lookupServiceElseFail(MenuBarsService.class);

        final Response.ResponseBuilder builder;
        final MenuBars menuBars = menuBarsService.menuBars();
        addLinksForServiceActions(resourceContext, menuBars);

        builder = Response.status(Response.Status.OK)
                .entity(serializationStrategy.entity(menuBars))
                .type(MediaTypes.toJakarta(serializationStrategy.type(RepresentationType.MENUBARS)));

        return builder.build();
    }

    void addLinksForServiceActions(final ResourceContext resourceContext, final MenuBars menuBars) {
        menuBars.visit(linksForServiceActionsAddingVisitor(resourceContext));
    }

    // public ... for testing
    public static Consumer<ServiceActionLayoutData> linksForServiceActionsAddingVisitor(
            final ResourceContext resourceContext) {
        return (final ServiceActionLayoutData actionLayoutData) -> {
                final String logicalTypeName = actionLayoutData.getLogicalTypeName();
                final String relativeUrl = String.format(
                        "objects/%s/%s/actions/%s",
                        logicalTypeName, SERVICE_IDENTIFIER, actionLayoutData.getId());
                Link link = new Link(
                        Rel.ACTION.getName(),
                        HttpMethod.GET,
                        resourceContext.restfulUrlFor(relativeUrl),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            };
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


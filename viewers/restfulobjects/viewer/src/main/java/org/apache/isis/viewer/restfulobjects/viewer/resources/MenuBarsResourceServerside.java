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
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.ServiceActionLayoutData;
import org.apache.isis.applib.layout.links.Link;
import org.apache.isis.applib.layout.menubars.MenuBars;
import org.apache.isis.applib.services.menu.MenuBarsService;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.menubars.MenuBarsResource;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.val;

@Component
public class MenuBarsResourceServerside extends ResourceAbstract implements MenuBarsResource {

    public static final String SERVICE_IDENTIFIER = "1";

    @Inject
    public MenuBarsResourceServerside(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration,
            final IsisInteractionTracker isisInteractionTracker) {
        super(metaModelContext, isisConfiguration, isisInteractionTracker);
    }

    @Override
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LAYOUT_MENUBARS,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_LAYOUT_MENUBARS
    })
    public Response menuBars() {
        
        val resourceContext = createResourceContext(
                RepresentationType.MENUBARS, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val serializationStrategy = resourceContext.getSerializationStrategy();
        val menuBarsService = metaModelContext.getServiceRegistry().lookupServiceElseFail(MenuBarsService.class);
        
        final Response.ResponseBuilder builder;
        final MenuBars menuBars = menuBarsService.menuBars();
        addLinksForServiceActions(resourceContext, menuBars);

        builder = Response.status(Response.Status.OK)
                .entity(serializationStrategy.entity(menuBars))
                .type(serializationStrategy.type(RepresentationType.MENUBARS));

        return builder.build();
    }

    void addLinksForServiceActions(final ResourceContext resourceContext, final MenuBars menuBars) {
        menuBars.visit(linksForServiceActionsAddingVisitor(resourceContext));
    }
    
    // public ... for testing
    public static MenuBars.Visitor linksForServiceActionsAddingVisitor(final ResourceContext resourceContext) {
        return new MenuBars.Visitor() {
            @Override
            public void visit(final ServiceActionLayoutData actionLayoutData) {
                final String objectType = actionLayoutData.getObjectType();
                final String relativeUrl = String.format(
                        "objects/%s/%s/actions/%s",
                        objectType, SERVICE_IDENTIFIER, actionLayoutData.getId());
                Link link = new Link(
                        Rel.ACTION.getName(),
                        HttpMethod.GET,
                        resourceContext.urlFor(relativeUrl),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            }
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


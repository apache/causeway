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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ServiceActionLayoutData;
import org.apache.causeway.applib.layout.links.Link;
import org.apache.causeway.applib.layout.menubars.MenuBars;
import org.apache.causeway.applib.services.menu.MenuBarsService;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.menubars.MenuBarsResource;
import org.apache.causeway.viewer.restfulobjects.applib.util.Links;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class MenuBarsResourceServerside extends ResourceAbstract
implements MenuBarsResource {

    public static final String SERVICE_IDENTIFIER = "1";

    public MenuBarsResourceServerside() {
        super();
    }

    @Override
    public ResponseEntity<Object> menuBars() {

        var resourceContext = createResourceContext(
                RepresentationType.MENUBARS, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var serializationStrategy = resourceContext.getSerializationStrategy();
        var menuBarsService = metaModelContext.getServiceRegistry().lookupServiceElseFail(MenuBarsService.class);

        final MenuBars menuBars = menuBarsService.menuBars();
        addLinksForServiceActions(resourceContext, menuBars);

        var response = responseFactory.ok(
                serializationStrategy.entity(menuBars),
                serializationStrategy.type(RepresentationType.MENUBARS));

        return _EndpointLogging.response(log, "GET /menuBars", response);
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
                Link link = Links.get(
                        Rel.ACTION,
                        resourceContext.restfulUrlFor(relativeUrl),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            };
    }

    @Override
    public ResponseEntity<Object> deleteMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Deleting the menuBars resource is not allowed.");
    }

    @Override
    public ResponseEntity<Object> putMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Putting to the menuBars resource is not allowed.");
    }

    @Override
    public ResponseEntity<Object> postMenuBarsNotAllowed() {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatus.METHOD_NOT_ALLOWED, "Posting to the menuBars resource is not allowed.");
    }

}


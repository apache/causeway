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

import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ListReprRenderer;

public class DomainServicesListReprRenderer extends ListReprRenderer {

    public DomainServicesListReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, representation);
    }


    @Override
    public JsonRepresentation render() {
        super.render();
        if (includesSelf) {
            addLinkToSelf();
            addLinkToUp();
        }
        getExtensions();
        return representation;
    }


    private void addLinkToSelf() {
        final JsonRepresentation link = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.LIST, "services").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final DomainServicesListReprRenderer renderer = new DomainServicesListReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(streamServiceAdapters());
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.UP.getName(), RepresentationType.HOME_PAGE, "").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPut("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

}
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

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;

public class UserReprRenderer
extends ReprRendererAbstract<InteractionContext> {

    UserReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.USER, representation);
    }

    @Override
    public UserReprRenderer with(final InteractionContext authentication) {
        representation.mapPutString("userName", authentication.getUser().name());
        final JsonRepresentation roles = JsonRepresentation.newArray();

        authentication.getUser().streamRoleNames()
        .forEach(roles::arrayAdd);

        representation.mapPutJsonRepresentation("roles", roles);
        return this;
    }

    @Override
    public JsonRepresentation render() {
        if (includesSelf) {
            addLinkToSelf();
            addLinkToUp();
            addLinkToLogout();
        }
        getExtensions();
        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.USER, "user").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final UserReprRenderer renderer = new UserReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(getResourceContext().getInteractionService().currentInteractionContextElseFail());
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUp() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.UP.getName(), RepresentationType.HOME_PAGE, "").build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final HomePageReprRenderer renderer = new HomePageReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPutJsonRepresentation("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private void addLinkToLogout() {
        final JsonRepresentation link = LinkBuilder.newBuilder(resourceContext, Rel.LOGOUT.getName(), RepresentationType.HOME_PAGE, "user/logout").build();

        getLinks().arrayAdd(link);
    }

}

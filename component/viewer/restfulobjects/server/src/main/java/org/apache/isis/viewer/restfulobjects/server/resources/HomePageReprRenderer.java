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

import java.util.Collection;
import java.util.List;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollower;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ListReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.TypeListReprRenderer;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;

public class HomePageReprRenderer extends ReprRendererAbstract<HomePageReprRenderer, Void> {

    HomePageReprRenderer(final RendererContext resourceContext, final LinkFollower linkFollower, final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.HOME_PAGE, representation);
    }

    @Override
    public HomePageReprRenderer with(final Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {

        // self
        if (includesSelf) {
            addLinkToSelf(representation);
        }

        addLinkToUser(getRendererContext().getAuthenticationSession());
        addLinkToServices(((ResourceContext)getRendererContext()).getPersistenceSession().getServices());
        addLinkToVersion();
        addLinkToDomainTypes(((ResourceContext)getRendererContext()).getSpecificationLookup().allSpecifications());

        // inks and extensions
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }

    private void addLinkToSelf(final JsonRepresentation representation) {
        final JsonRepresentation link = LinkBuilder.newBuilder(rendererContext, Rel.SELF.getName(), getRepresentationType(), "").build();

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=" + Rel.SELF.getName() + "]");
        if (linkFollower.matches(link)) {

            final HomePageReprRenderer renderer = new HomePageReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());

            link.mapPut("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private void addLinkToVersion() {
        final JsonRepresentation link = LinkBuilder.newBuilder(getRendererContext(), Rel.VERSION.getName(), RepresentationType.VERSION, "version").build();

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=" + Rel.VERSION.getName() + "]");
        if (linkFollower.matches(link)) {

            final VersionReprRenderer renderer = new VersionReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());

            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToServices(List<ObjectAdapter> serviceAdapters) {

        final JsonRepresentation link = LinkBuilder.newBuilder(getRendererContext(), Rel.SERVICES.getName(), RepresentationType.LIST, "services").build();

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=" + Rel.SERVICES.getName() + "]");
        if (linkFollower.matches(link)) {

            final ListReprRenderer renderer = new ListReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());
            renderer.usingLinkToBuilder(new DomainServiceLinkTo()).withSelf("services").with(serviceAdapters);

            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUser(AuthenticationSession authenticationSession) {
        final JsonRepresentation link = LinkBuilder.newBuilder(getRendererContext(), Rel.USER.getName(), RepresentationType.USER, "user").build();

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=" + Rel.USER.getName() + "]");
        if (linkFollower.matches(link)) {
            final UserReprRenderer renderer = new UserReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(authenticationSession);

            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToDomainTypes(final Collection<ObjectSpecification> specifications) {

        final JsonRepresentation link = LinkBuilder.newBuilder(getRendererContext(), Rel.DOMAIN_TYPES.getName(), RepresentationType.TYPE_LIST, "domainTypes").build();

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=" + Rel.DOMAIN_TYPES.getName() + "]");
        if (linkFollower.matches(link)) {

            final TypeListReprRenderer renderer = new TypeListReprRenderer(getRendererContext(), linkFollower, JsonRepresentation.newMap());

            renderer.withSelf("domainTypes").with(specifications);

            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

}
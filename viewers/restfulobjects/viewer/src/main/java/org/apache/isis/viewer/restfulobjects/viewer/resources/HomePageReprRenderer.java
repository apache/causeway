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

import java.util.Collection;
import java.util.stream.Stream;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.isis.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ListReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.TypeListReprRenderer;

import lombok.val;

public class HomePageReprRenderer extends ReprRendererAbstract<HomePageReprRenderer, Void> {

    HomePageReprRenderer(final IResourceContext resourceContext, final LinkFollowSpecs linkFollower, final JsonRepresentation representation) {
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
            addLinkToSelf();
        }

        val metaModelContext = super.getResourceContext().getMetaModelContext();

        addLinkToUser(getResourceContext().getAuthenticationSession());
        addLinkToMenuBars();
        addLinkToServices(metaModelContext.streamServiceAdapters());
        addLinkToVersion();
        addLinkToDomainTypes(getResourceContext().getSpecificationLoader().snapshotSpecifications());

        // inks and extensions
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }

    private void addLinkToSelf() {
        final JsonRepresentation link = LinkBuilder.newBuilder(
                resourceContext, 
                Rel.SELF.getName(), 
                RepresentationType.HOME_PAGE, 
                "")
                .build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final HomePageReprRenderer renderer = new HomePageReprRenderer(
                    getResourceContext(), 
                    linkFollower, 
                    JsonRepresentation.newMap());
            
            link.mapPut("value", renderer.render());
        }
        getLinks().arrayAdd(link);
    }

    private void addLinkToVersion() {
        final JsonRepresentation link = LinkBuilder.newBuilder(
                getResourceContext(), 
                Rel.VERSION.getName(),
                RepresentationType.VERSION, 
                "version")
                .build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final VersionReprRenderer renderer = new VersionReprRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToServices(Stream<ManagedObject> serviceAdapters) {

        final JsonRepresentation link = LinkBuilder.newBuilder(
                getResourceContext(), 
                Rel.SERVICES.getName(), 
                RepresentationType.LIST, 
                "services")
                .build();

        final LinkFollowSpecs linkFollowSpecs = getLinkFollowSpecs().follow("links");
        if (linkFollowSpecs.matches(link)) {

            final ListReprRenderer renderer = 
                    new ListReprRenderer(
                            getResourceContext(), 
                            linkFollowSpecs, 
                            JsonRepresentation.newMap());
            
            renderer.usingLinkToBuilder(new DomainServiceLinkTo())
            .withLink(Rel.SELF, "services")
            .with(serviceAdapters);

            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUser(AuthenticationSession authenticationSession) {
        final JsonRepresentation link = LinkBuilder.newBuilder(
                getResourceContext(), 
                Rel.USER.getName(), 
                RepresentationType.USER, 
                "user")
                .build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final UserReprRenderer renderer = new UserReprRenderer(
                    getResourceContext(), 
                    linkFollower, 
                    JsonRepresentation.newMap());
            
            renderer.with(authenticationSession);
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToMenuBars() {
        final JsonRepresentation link = LinkBuilder.newBuilder(
                        getResourceContext(), 
                        Rel.MENUBARS.getName(), 
                        RepresentationType.MENUBARS, 
                        "menuBars")
                .build();
        
        getLinks().arrayAdd(link);
    }

    private void addLinkToDomainTypes(final Collection<ObjectSpecification> specifications) {

        final JsonRepresentation link = 
                LinkBuilder.newBuilder(
                        getResourceContext(), 
                        Rel.DOMAIN_TYPES.getName(), 
                        RepresentationType.TYPE_LIST, 
                        "domain-types")
                .build();

        final LinkFollowSpecs linkFollower = getLinkFollowSpecs().follow("links");
        if (linkFollower.matches(link)) {
            final TypeListReprRenderer renderer = new TypeListReprRenderer(
                            getResourceContext(), 
                            linkFollower, 
                            JsonRepresentation.newMap());
            
            renderer.withLink(Rel.SELF, "domain-types").with(specifications);
            link.mapPut("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

}
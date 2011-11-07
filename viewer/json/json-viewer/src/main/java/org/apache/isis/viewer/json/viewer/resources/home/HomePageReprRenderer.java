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
package org.apache.isis.viewer.json.viewer.resources.home;

import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.viewer.ResourceContext;
import org.apache.isis.viewer.json.viewer.representations.LinkBuilder;
import org.apache.isis.viewer.json.viewer.representations.LinkFollower;
import org.apache.isis.viewer.json.viewer.representations.Rel;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.representations.RendererFactoryRegistry;
import org.apache.isis.viewer.json.viewer.representations.ReprRenderer;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererAbstract;
import org.apache.isis.viewer.json.viewer.representations.ReprRendererFactoryAbstract;
import org.apache.isis.viewer.json.viewer.resources.capabilities.CapabilitiesReprRenderer;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.DomainServiceLinkTo;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.ListReprRenderer;
import org.apache.isis.viewer.json.viewer.resources.user.UserReprRenderer;

public class HomePageReprRenderer extends ReprRendererAbstract<HomePageReprRenderer, Void> {

    public static class Factory extends ReprRendererFactoryAbstract {
        public Factory() {
            super(RepresentationType.HOME_PAGE);
        }

        @Override
        public ReprRenderer<?, ?> newRenderer(ResourceContext resourceContext, LinkFollower linkFollower, JsonRepresentation representation) {
            return new HomePageReprRenderer(resourceContext, linkFollower, getRepresentationType(), representation);
        }
    }

    private HomePageReprRenderer(ResourceContext resourceContext, LinkFollower linkFollower, RepresentationType representationType, JsonRepresentation representation) {
        super(resourceContext, linkFollower, representationType, representation);
    }

    @Override
    public HomePageReprRenderer with(Void t) {
        return this;
    }

    @Override
    public JsonRepresentation render() {
        
        // self
        if(includesSelf) {
            addLinkToSelf(representation);
        }

        addLinkToUser();
        addLinkToServices();
        addLinkToCapabilities();

        // inks and extensions
        representation.mapPut("extensions", JsonRepresentation.newMap());

        return representation;
    }

    private void addLinkToSelf(JsonRepresentation representation) {
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, Rel.SELF, getRepresentationType(), "");

        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=self]");
        if(linkFollower.isFollowing()) {

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.HOME_PAGE);
            final HomePageReprRenderer renderer = 
                    (HomePageReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            
            linkBuilder.withValue(renderer.render());
        }
        getLinks().arrayAdd(linkBuilder.build());
    }

    private void addLinkToCapabilities() {
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.CAPABILITIES, RepresentationType.CAPABILITIES, "capabilities");
        
        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=capabilities]");
        if(linkFollower.isFollowing()) {

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.CAPABILITIES);
            final CapabilitiesReprRenderer renderer = (CapabilitiesReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            
            linkBuilder.withValue(renderer.render());
        }
        
        getLinks().arrayAdd(linkBuilder.build());
    }

    private void addLinkToServices() {

        final LinkBuilder linkBuilder = 
                LinkBuilder.newBuilder(getResourceContext(), Rel.SERVICES, RepresentationType.LIST, "services");
        
        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=services]");
        if(linkFollower.isFollowing()) {
            
            final List<ObjectAdapter> serviceAdapters = getResourceContext().getPersistenceSession().getServices();

            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.LIST);
            
            final ListReprRenderer renderer = (ListReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.usingLinkToBuilder(new DomainServiceLinkTo())
                    .withSelf("services")
                    .with(serviceAdapters);
            
            linkBuilder.withValue(renderer.render());
        }
        
        getLinks().arrayAdd(linkBuilder.build());
    }

    private void addLinkToUser() {
        final LinkBuilder userLinkBuilder = 
                LinkBuilder.newBuilder(getResourceContext(), Rel.USER, RepresentationType.USER, "user");
        
        final LinkFollower linkFollower = getLinkFollower().follow("links[rel=user]");
        if(linkFollower.isFollowing()) {
            final RendererFactory factory = RendererFactoryRegistry.instance.find(RepresentationType.USER);
            final UserReprRenderer renderer = 
                    (UserReprRenderer) factory.newRenderer(getResourceContext(), linkFollower, JsonRepresentation.newMap());
            renderer.with(getResourceContext().getAuthenticationSession());
            
            userLinkBuilder.withValue(renderer.render());
        }
        
        getLinks().arrayAdd(userLinkBuilder.build());
    }

}
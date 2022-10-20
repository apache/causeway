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

import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.viewer.commons.applib.services.branding.BrandingUiService;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkFollowSpecs;
import org.apache.causeway.viewer.restfulobjects.rendering.ReprRendererAbstract;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ListReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.TypeListReprRenderer;

import lombok.val;

public class HomePageReprRenderer
extends ReprRendererAbstract<Void> {

    // self managed injections via constructor
    @Inject BrandingUiService brandingUiService;

    HomePageReprRenderer(
            final IResourceContext resourceContext,
            final LinkFollowSpecs linkFollower,
            final JsonRepresentation representation) {
        super(resourceContext, linkFollower, RepresentationType.HOME_PAGE, representation);
        resourceContext.getMetaModelContext().getServiceInjector().injectServicesInto(this);
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

        addLinkToUser(getResourceContext().getInteractionService().currentInteractionContextElseFail());
        addLinksToApplicationLogos();
        addLinkToMenuBars();
        addLinkToServices(metaModelContext.streamServiceAdapters());
        addLinkToVersion();
        addLinkToDomainTypes(getResourceContext().getMetaModelContext().getSpecificationLoader().snapshotSpecifications());

        // inks and extensions
        representation.mapPutJsonRepresentation("extensions", JsonRepresentation.newMap());

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

            link.mapPutJsonRepresentation("value", renderer.render());
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
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToServices(final Stream<ManagedObject> serviceAdapters) {

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

            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinkToUser(final InteractionContext authentication) {
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

            renderer.with(authentication);
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

    private void addLinksToApplicationLogos() {

        brandingUiService
        .getSignInBranding()
        .getLogoHref()
        .ifPresent(href->
            getLinks()
                .arrayAdd(LinkBuilder.newBuilder(
                        getResourceContext(),
                        Rel.BRAND_LOGO_SIGNIN.getName(),
                        RepresentationType.IMAGE,
                        href)
                .buildAsApplicationResource()));

        brandingUiService
        .getHeaderBranding()
        .getLogoHref()
        .ifPresent(href->
            getLinks()
                .arrayAdd(LinkBuilder.newBuilder(
                        getResourceContext(),
                        Rel.BRAND_LOGO_HEADER.getName(),
                        RepresentationType.IMAGE,
                        href)
                .buildAsApplicationResource()));
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

    private void addLinkToDomainTypes(final Can<ObjectSpecification> specifications) {

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
            link.mapPutJsonRepresentation("value", renderer.render());
        }

        getLinks().arrayAdd(link);
    }

}

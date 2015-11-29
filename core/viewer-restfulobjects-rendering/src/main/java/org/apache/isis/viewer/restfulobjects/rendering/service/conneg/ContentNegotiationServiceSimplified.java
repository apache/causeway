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
package org.apache.isis.viewer.restfulobjects.rendering.service.conneg;

import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.DomainServiceLayout;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectCollectionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

@DomainService(
        nature = NatureOfService.DOMAIN
)
@DomainServiceLayout(
        menuOrder = "1500"
)
public class ContentNegotiationServiceSimplified extends ContentNegotiationServiceAbstract {

    private static final String ACCEPT_OBJECT = "urn:org.apache.isis:repr-types/object/v1";

    private ContentNegotiationServiceForRestfulObjectsV1_0 restfulObjectsV1_0 = new ContentNegotiationServiceForRestfulObjectsV1_0();

    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAdapter objectAdapter) {

        final List<MediaType> acceptableMediaTypes = renderContext2.getAcceptableMediaTypes();

        boolean canAccept = canAccept(acceptableMediaTypes, ACCEPT_OBJECT);
        if(!canAccept) {
            return null;
        }

        final InteractionInitiatedBy interactionInitiatedBy =
                determineInteractionInitiatedByFrom(renderContext2);
        final Where where = renderContext2.getWhere();

        final JsonRepresentation rootRepresentation = JsonRepresentation.newMap();

        List<OneToOneAssociation> properties = objectAdapter.getSpecification().getProperties(Contributed.INCLUDED);
        for (final OneToOneAssociation property : properties) {

            final Consent visibility = property.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                continue;
            }

            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            final ObjectPropertyReprRenderer renderer =
                    new ObjectPropertyReprRenderer(renderContext2, null, property.getId(), propertyRepresentation);
            renderer.with(new ObjectAndProperty(objectAdapter, property));

            final JsonRepresentation propertyValueRepresentation = renderer.render();
            rootRepresentation.mapPut(property.getId(), propertyValueRepresentation.getRepresentation("value"));
        }

        List<OneToManyAssociation> collections = objectAdapter.getSpecification().getCollections(Contributed.INCLUDED);
        for (final OneToManyAssociation collection : collections) {

            final Consent visibility = collection.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                continue;
            }

            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            final ObjectCollectionReprRenderer renderer =
                    new ObjectCollectionReprRenderer(renderContext2, null, collection.getId(), propertyRepresentation);
            renderer.with(new ObjectAndCollection(objectAdapter, collection));

            final JsonRepresentation collectionValueRepresentation = renderer.render();
            rootRepresentation.mapPut(collection.getId(), collectionValueRepresentation);
        }


        final JsonRepresentation $$roRepresentation = JsonRepresentation.newMap();
        rootRepresentation.mapPut("$$ro", $$roRepresentation);

        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        renderContext2, objectAdapter, $$roRepresentation, rootRepresentation);

        return responseBuilder(responseBuilder);
    }

    private static InteractionInitiatedBy determineInteractionInitiatedByFrom(
            final RendererContext rendererContext) {
        if (rendererContext instanceof RepresentationService.Context4) {
            return ((RepresentationService.Context4) rendererContext).getInteractionInitiatedBy();
        } else {
            // fallback
            return InteractionInitiatedBy.USER;
        }
    }

    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndProperty objectAndProperty)  {
        return null;
    }

    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndCollection objectAndCollection) {
        return null;
    }

    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndAction objectAndAction)  {
        return null;
    }

    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 renderContext2,
            final ObjectAndActionInvocation objectAndActionInvocation) {
        return null;
    }

    /**
     * For easy subclassing to further customize, eg additional headers
     */
    protected Response.ResponseBuilder responseBuilder(final Response.ResponseBuilder responseBuilder) {
        return responseBuilder;
    }

    @Inject
    protected DomainObjectContainer container;
}

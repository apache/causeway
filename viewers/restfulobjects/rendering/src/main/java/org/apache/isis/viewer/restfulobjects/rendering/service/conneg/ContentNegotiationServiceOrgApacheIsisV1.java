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

import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.client.SuppressionType;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

import lombok.val;

@Service
@Named("isisRoRendering.ContentNegotiationServiceOrgApacheIsisV1")
@Order(OrderPrecedence.MIDPOINT - 200)
@Qualifier("OrgApacheIsisV1")
public class ContentNegotiationServiceOrgApacheIsisV1 extends ContentNegotiationServiceAbstract {

    /**
     * Unlike RO v1.0, use a single content-type of <code>application/json;profile="urn:org.apache.isis/v1"</code>.
     *
     * <p>
     * The response content types ({@link #CONTENT_TYPE_OAI_V1_OBJECT}, {@link #CONTENT_TYPE_OAI_V1_OBJECT_COLLECTION},
     * {@link #CONTENT_TYPE_OAI_V1_LIST}) append the 'repr-type' parameter.
     * </p>
     */
    public static final String ACCEPT_PROFILE = "urn:org.apache.isis/v1";

    /**
     * The media type (as a string) used as the content-Type header when a domain object is rendered.
     *
     * @see #ACCEPT_PROFILE for discussion.
     */
    public static final String CONTENT_TYPE_OAI_V1_OBJECT = "application/json;"
            + "profile=\"" + ACCEPT_PROFILE + "\""
            + ";repr-type=\"object\""
            ;
    /**
     * The media type (as a string) used as the content-Type header when a parented collection is rendered.
     *
     * @see #ACCEPT_PROFILE for discussion.
     */
    public static final String CONTENT_TYPE_OAI_V1_OBJECT_COLLECTION = "application/json;"
            + "profile=\"" + ACCEPT_PROFILE + "\""
            + ";repr-type=\"object-collection\""
            ;
    /**
     * The media type (as a string) used as the content-Type header when a standalone collection is rendered.
     *
     * @see #ACCEPT_PROFILE for discussion.
     */
    public static final String CONTENT_TYPE_OAI_V1_LIST = "application/json;"
            + "profile=\"" + ACCEPT_PROFILE + "\""
            + ";repr-type=\"list\""
            ;

    private ContentNegotiationServiceForRestfulObjectsV1_0 restfulObjectsV1_0 = new ContentNegotiationServiceForRestfulObjectsV1_0();

    /**
     * Domain object is returned as a map with the RO 1.0 representation as a special '$$ro' property
     * within that map.
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        boolean canAccept = canAccept(resourceContext);
        if(!canAccept) {
            return null;
        }

        final EnumSet<SuppressionType> suppression = suppress(resourceContext);
        final boolean suppressRO = suppression.contains(SuppressionType.RO);

        final JsonRepresentation rootRepresentation = JsonRepresentation.newMap();

        appendObjectTo(resourceContext, objectAdapter, rootRepresentation, suppression);

        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            $$roRepresentation = JsonRepresentation.newMap();
            rootRepresentation.mapPut("$$ro", $$roRepresentation);
        } else {
            $$roRepresentation = null;
        }
        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        resourceContext, objectAdapter, $$roRepresentation, rootRepresentation);

        responseBuilder.type(CONTENT_TYPE_OAI_V1_OBJECT);

        return responseBuilder(responseBuilder);
    }

    /**
     * Individual property of an object is not supported.
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndProperty objectAndProperty)  {

        return null;
    }

    /**
     * Individual (parented) collection of an object is returned as a list with the RO representation
     * as an object in the list with a single property named '$$ro'
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndCollection objectAndCollection) {

        if(!canAccept(resourceContext)) {
            return null;
        }
        final EnumSet<SuppressionType> suppression = suppress(resourceContext);
        final boolean suppressRO = suppression.contains(SuppressionType.RO);

        final JsonRepresentation rootRepresentation = JsonRepresentation.newArray();

        ManagedObject objectAdapter = objectAndCollection.getObjectAdapter();
        OneToManyAssociation collection = objectAndCollection.getMember();

        appendCollectionTo(resourceContext, objectAdapter, collection, rootRepresentation, suppression);

        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            // $$ro representation will be an object in the list with a single property named "$$ro"
            final JsonRepresentation $$roContainerRepresentation = JsonRepresentation.newMap();
            rootRepresentation.arrayAdd($$roContainerRepresentation);

            $$roRepresentation = JsonRepresentation.newMap();
            $$roContainerRepresentation.mapPut("$$ro", $$roRepresentation);
        } else {
            $$roRepresentation = null;
        }

        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        resourceContext, objectAndCollection, $$roRepresentation, rootRepresentation);

        responseBuilder.type(CONTENT_TYPE_OAI_V1_OBJECT_COLLECTION);

        return responseBuilder(responseBuilder);
    }

    /**
     * Action prompt is not supported.
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndAction objectAndAction)  {
        return null;
    }

    /**
     * Action invocation is supported provided it returns a single domain object or a list of domain objects
     * (ie invocations returning void or scalar value are not supported).
     *
     * Action invocations returning a domain object will be rendered as a map with the RO v1.0 representation as a
     * '$$ro' property within (same as {@link #buildResponse(RepresentationService.Context2, ObjectAdapter)}), while
     * action invocations returning a list will be rendered as a list with the RO v1.0 representation as a map object
     * with a single '$$ro' property (similar to {@link #buildResponse(RepresentationService.Context2, ObjectAndCollection)})
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        if(!canAccept(resourceContext)) {
            return null;
        }
        final EnumSet<SuppressionType> suppression = suppress(resourceContext);
        final boolean suppressRO = suppression.contains(SuppressionType.RO);

        JsonRepresentation rootRepresentation = null;
        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            $$roRepresentation = JsonRepresentation.newMap();
        } else {
            $$roRepresentation = null;
        }

        final ManagedObject returnedAdapter = objectAndActionInvocation.getReturnedAdapter();
        //final ObjectSpecification returnType = objectAndActionInvocation.getAction().getReturnType();

        if (returnedAdapter == null) {
            return null;
        }

        final ActionResultRepresentation.ResultType resultType = objectAndActionInvocation.determineResultType();
        switch (resultType) {
        case DOMAIN_OBJECT:

            rootRepresentation = JsonRepresentation.newMap();
            appendObjectTo(resourceContext, returnedAdapter, rootRepresentation, suppression);

            break;

        case LIST:

            rootRepresentation = JsonRepresentation.newArray();

            //final CollectionFacet collectionFacet = returnType.getFacet(CollectionFacet.class);

            final Stream<ManagedObject> collectionAdapters = 
                    CollectionFacet.Utils.streamAdapters(returnedAdapter);

            appendStreamTo(resourceContext, collectionAdapters, rootRepresentation, suppression);

            // $$ro representation will be an object in the list with a single property named "$$ro"
            if(!suppressRO) {
                JsonRepresentation $$roContainerRepresentation = JsonRepresentation.newMap();
                rootRepresentation.arrayAdd($$roContainerRepresentation);
                $$roContainerRepresentation.mapPut("$$ro", $$roRepresentation);
            }

            break;

        case SCALAR_VALUE:
        case VOID:

            // not supported
            return null;
        }

        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        resourceContext, objectAndActionInvocation, $$roRepresentation, rootRepresentation);

        // set appropriate Content-Type
        responseBuilder.type(
                resultType == ActionResultRepresentation.ResultType.DOMAIN_OBJECT
                ? CONTENT_TYPE_OAI_V1_OBJECT
                        : CONTENT_TYPE_OAI_V1_LIST
                );

        return responseBuilder(responseBuilder);
    }

    /**
     * For easy subclassing to further customize, eg additional headers
     */
    protected Response.ResponseBuilder responseBuilder(final Response.ResponseBuilder responseBuilder) {
        return responseBuilder;
    }

    boolean canAccept(final IResourceContext resourceContext) {
        final List<MediaType> acceptableMediaTypes = resourceContext.getAcceptableMediaTypes();
        return mediaTypeParameterMatches(acceptableMediaTypes, "profile", ACCEPT_PROFILE);
    }

    protected EnumSet<SuppressionType> suppress(
            final IResourceContext resourceContext) {
        final List<MediaType> acceptableMediaTypes = resourceContext.getAcceptableMediaTypes();
        return SuppressionType.ParseUtil.parse(mediaTypeParameterList(acceptableMediaTypes, "suppress"));
    }

    private void appendObjectTo(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final JsonRepresentation rootRepresentation,
            final EnumSet<SuppressionType> suppression) {

        appendPropertiesTo(resourceContext, objectAdapter, rootRepresentation, suppression);

        final Where where = resourceContext.getWhere();
        final Stream<OneToManyAssociation> collections = objectAdapter.getSpecification()
                .streamCollections(Contributed.INCLUDED);

        collections.forEach(collection->{
            final JsonRepresentation collectionRepresentation = JsonRepresentation.newArray();

            rootRepresentation.mapPut(collection.getId(), collectionRepresentation);

            final InteractionInitiatedBy interactionInitiatedBy = determineInteractionInitiatedByFrom(resourceContext);
            final Consent visibility = collection.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                return;
            }

            appendCollectionTo(resourceContext, objectAdapter, collection, collectionRepresentation, suppression);
        });

    }

    private void appendPropertiesTo(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final JsonRepresentation rootRepresentation,
            final EnumSet<SuppressionType> suppression) {
        
        final InteractionInitiatedBy interactionInitiatedBy = determineInteractionInitiatedByFrom(resourceContext);
        final Where where = resourceContext.getWhere();
        final Stream<OneToOneAssociation> properties = objectAdapter.getSpecification()
                .streamProperties(Contributed.INCLUDED);

        properties.forEach(property->{
            final Consent visibility = property.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                return;
            }

            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            final ObjectPropertyReprRenderer renderer =
                    new ObjectPropertyReprRenderer(resourceContext, null, property.getId(), propertyRepresentation)
                    .asStandalone();
            renderer.with(new ObjectAndProperty(objectAdapter, property));

            final JsonRepresentation propertyValueRepresentation = renderer.render();

            if(!suppression.contains(SuppressionType.HREF)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                rootRepresentation.mapPut("$$href", upHref);
            }
            if(!suppression.contains(SuppressionType.TITLE)) {
                final String upTitle = propertyValueRepresentation.getString("links[rel=up].title");
                rootRepresentation.mapPut("$$title", upTitle);
            }
            if(!suppression.contains(SuppressionType.DOMAIN_TYPE)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                final String[] parts = upHref.split("[/]");
                if(parts.length > 2) {
                    final String upObjectType = parts[parts.length - 2];
                    rootRepresentation.mapPut("$$domainType", upObjectType);
                }
            }
            if(!suppression.contains(SuppressionType.ID)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                final String[] parts = upHref.split("[/]");
                if(parts.length > 1) {
                    final String upInstanceId = parts[parts.length-1];
                    rootRepresentation.mapPut("$$instanceId", upInstanceId);
                }
            }

            final JsonRepresentation value = propertyValueRepresentation.getRepresentation("value");
            rootRepresentation.mapPut(property.getId(), value);
        });

    }

    private void appendCollectionTo(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final OneToManyAssociation collection,
            final JsonRepresentation representation, 
            final EnumSet<SuppressionType> suppression) {

        val interactionInitiatedBy = determineInteractionInitiatedByFrom(resourceContext);
        val valueAdapter = collection.get(objectAdapter, interactionInitiatedBy);
        if (valueAdapter == null) {
            return;
        }

        final Stream<ManagedObject> adapters = CollectionFacet.Utils.streamAdapters(valueAdapter);
        appendStreamTo(resourceContext, adapters, representation, suppression);
    }

    private void appendStreamTo(
            final IResourceContext resourceContext,
            final Stream<ManagedObject> adapters,
            final JsonRepresentation collectionRepresentation, 
            final EnumSet<SuppressionType> suppression) {

        adapters.forEach(elementAdapter->{
            JsonRepresentation elementRepresentation = JsonRepresentation.newMap();
            appendPropertiesTo(resourceContext, elementAdapter, elementRepresentation, suppression);

            collectionRepresentation.arrayAdd(elementRepresentation);
        });
    }

    private static InteractionInitiatedBy determineInteractionInitiatedByFrom(
            final IResourceContext resourceContext) {
        return resourceContext.getInteractionInitiatedBy();
    }

}

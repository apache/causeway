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

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + (Integer.MAX_VALUE - 10)
        )
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
     * Whether to suppress the RO v1.0 '$$ro' node; suppress='false' or suppress='true'
     */
    public static final String ACCEPT_RO = "suppress";

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
            final RepresentationService.Context2 rendererContext,
            final ObjectAdapter objectAdapter) {

        boolean canAccept = canAccept(rendererContext);
        if(!canAccept) {
            return null;
        }
        boolean suppressRO = suppress(rendererContext);

        final JsonRepresentation rootRepresentation = JsonRepresentation.newMap();

        appendObjectTo(rendererContext, objectAdapter, rootRepresentation);

        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            $$roRepresentation = JsonRepresentation.newMap();
            rootRepresentation.mapPut("$$ro", $$roRepresentation);
        } else {
            $$roRepresentation = null;
        }
        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        rendererContext, objectAdapter, $$roRepresentation, rootRepresentation);

        responseBuilder.type(CONTENT_TYPE_OAI_V1_OBJECT);

        return responseBuilder(responseBuilder);
    }

    /**
     * Individual property of an object is not supported.
     */
    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 rendererContext,
            final ObjectAndProperty objectAndProperty)  {

        return null;
    }

    /**
     * Individual (parented) collection of an object is returned as a list with the RO representation
     * as an object in the list with a single property named '$$ro'
     */
    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 rendererContext,
            final ObjectAndCollection objectAndCollection) {

        if(!canAccept(rendererContext)) {
            return null;
        }
        boolean suppressRO = suppress(rendererContext);

        final JsonRepresentation rootRepresentation = JsonRepresentation.newArray();

        ObjectAdapter objectAdapter = objectAndCollection.getObjectAdapter();
        OneToManyAssociation collection = objectAndCollection.getMember();

        appendCollectionTo(rendererContext, objectAdapter, collection, rootRepresentation);

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
                        rendererContext, objectAndCollection, $$roRepresentation, rootRepresentation);

        responseBuilder.type(CONTENT_TYPE_OAI_V1_OBJECT_COLLECTION);

        return responseBuilder(responseBuilder);
    }

    /**
     * Action prompt is not supported.
     */
    @Override
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 rendererContext,
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
    @Programmatic
    public Response.ResponseBuilder buildResponse(
            final RepresentationService.Context2 rendererContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        if(!canAccept(rendererContext)) {
            return null;
        }
        boolean suppressRO = suppress(rendererContext);

        JsonRepresentation rootRepresentation = null;
        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            $$roRepresentation = JsonRepresentation.newMap();
        } else {
            $$roRepresentation = null;
        }

        final ObjectAdapter returnedAdapter = objectAndActionInvocation.getReturnedAdapter();
        final ObjectSpecification returnType = objectAndActionInvocation.getAction().getReturnType();

        if (returnedAdapter == null) {
            return null;
        }

        final ActionResultRepresentation.ResultType resultType = objectAndActionInvocation.determineResultType();
        switch (resultType) {
        case DOMAIN_OBJECT:

            rootRepresentation = JsonRepresentation.newMap();
            appendObjectTo(rendererContext, returnedAdapter, rootRepresentation);

            break;

        case LIST:

            rootRepresentation = JsonRepresentation.newArray();

            final CollectionFacet collectionFacet = returnType.getFacet(CollectionFacet.class);
            final Collection<ObjectAdapter> collectionAdapters = collectionFacet.collection(returnedAdapter);
            appendIterableTo(rendererContext, collectionAdapters, rootRepresentation);

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
                        rendererContext, objectAndActionInvocation, $$roRepresentation, rootRepresentation);

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

    boolean canAccept(final RepresentationService.Context2 rendererContext) {
        final List<MediaType> acceptableMediaTypes = rendererContext.getAcceptableMediaTypes();
        return mediaTypeParameterMatches(acceptableMediaTypes, "profile", ACCEPT_PROFILE);
    }

    protected boolean suppress(
            final RepresentationService.Context2 rendererContext) {
        final List<MediaType> acceptableMediaTypes = rendererContext.getAcceptableMediaTypes();
        return mediaTypeParameterMatches(acceptableMediaTypes, "suppress", "true");
    }

    private void appendObjectTo(
            final RepresentationService.Context2 rendererContext,
            final ObjectAdapter objectAdapter,
            final JsonRepresentation rootRepresentation) {

        appendPropertiesTo(rendererContext, objectAdapter, rootRepresentation);

        final Where where = rendererContext.getWhere();
        final Stream<OneToManyAssociation> collections = objectAdapter.getSpecification()
                .streamCollections(Contributed.INCLUDED);
        
        collections.forEach(collection->{
            final JsonRepresentation collectionRepresentation = JsonRepresentation.newArray();

            rootRepresentation.mapPut(collection.getId(), collectionRepresentation);

            final InteractionInitiatedBy interactionInitiatedBy = determineInteractionInitiatedByFrom(rendererContext);
            final Consent visibility = collection.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                return;
            }

            appendCollectionTo(rendererContext, objectAdapter, collection, collectionRepresentation);
        });
        
    }

    private void appendPropertiesTo(
            final RepresentationService.Context2 rendererContext,
            final ObjectAdapter objectAdapter,
            final JsonRepresentation rootRepresentation) {
        final InteractionInitiatedBy interactionInitiatedBy = determineInteractionInitiatedByFrom(rendererContext);
        final Where where = rendererContext.getWhere();
        final Stream<OneToOneAssociation> properties = objectAdapter.getSpecification()
                .streamProperties(Contributed.INCLUDED);
        
        properties.forEach(property->{
            final Consent visibility = property.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                return;
            }

            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            final ObjectPropertyReprRenderer renderer =
                    new ObjectPropertyReprRenderer(rendererContext, null, property.getId(), propertyRepresentation)
                    .asStandalone();
            renderer.with(new ObjectAndProperty(objectAdapter, property));

            final JsonRepresentation propertyValueRepresentation = renderer.render();

            final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
            rootRepresentation.mapPut("$$href", upHref);
            final String upTitle = propertyValueRepresentation.getString("links[rel=up].title");
            rootRepresentation.mapPut("$$title", upTitle);
            final String upInstanceId = upHref.substring(upHref.lastIndexOf("/")+1);
            rootRepresentation.mapPut("$$instanceId", upInstanceId);

            final JsonRepresentation value = propertyValueRepresentation.getRepresentation("value");
            rootRepresentation.mapPut(property.getId(), value);
        });

    }

    private void appendCollectionTo(
            final RepresentationService.Context2 rendererContext,
            final ObjectAdapter objectAdapter,
            final OneToManyAssociation collection,
            final JsonRepresentation representation) {

        final InteractionInitiatedBy interactionInitiatedBy = determineInteractionInitiatedByFrom(rendererContext);
        final ObjectAdapter valueAdapter = collection.get(objectAdapter, interactionInitiatedBy);
        if (valueAdapter == null) {
            return;
        }

        final CollectionFacet facet = CollectionFacet.Utils.getCollectionFacetFromSpec(valueAdapter);
        final Iterable<ObjectAdapter> iterable = facet.iterable(valueAdapter);
        appendIterableTo(rendererContext, iterable, representation);
    }

    private void appendIterableTo(
            final RepresentationService.Context2 rendererContext,
            final Iterable<ObjectAdapter> iterable,
            final JsonRepresentation collectionRepresentation) {
        for (final ObjectAdapter elementAdapter : iterable) {

            JsonRepresentation elementRepresentation = JsonRepresentation.newMap();
            appendPropertiesTo(rendererContext, elementAdapter, elementRepresentation);

            collectionRepresentation.arrayAdd(elementRepresentation);
        }
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

}

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
package org.apache.causeway.viewer.restfulobjects.rendering.service.conneg;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import jakarta.inject.Named;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.fasterxml.jackson.databind.node.POJONode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.client.RepresentationTypeSimplifiedV2;
import org.apache.causeway.applib.client.SuppressionType;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.ActionResultRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.dtos.ScalarValueDtoV2;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectPropertyReprRenderer;

import lombok.RequiredArgsConstructor;

/**
 * @since 1.x {@index}
 */
@Service
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".ContentNegotiationServiceOrgApacheCausewayV2")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT - 200)
@Qualifier("OrgApacheCausewayV2")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ContentNegotiationServiceOrgApacheCausewayV2
extends ContentNegotiationServiceAbstract {

    /**
     * Unlike RO v1.0, use a single content-type of <code>application/json;profile="urn:org.apache.causeway/v2"</code>.
     * <p>
     * The response content types {@link RepresentationTypeSimplifiedV2} append the 'repr-type' parameter.
     */
    public static final String ACCEPT_PROFILE = "urn:org.apache.causeway/v2";

    private final ContentNegotiationServiceForRestfulObjectsV1_0 restfulObjectsV1_0;

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
            rootRepresentation.mapPutJsonRepresentation("$$ro", $$roRepresentation);
        } else {
            $$roRepresentation = null;
        }
        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        resourceContext, objectAdapter, $$roRepresentation, rootRepresentation);

        responseBuilder.type(
                RepresentationTypeSimplifiedV2.OBJECT.getContentTypeHeaderValue(ACCEPT_PROFILE));

        return responseBuilder(responseBuilder);
    }

    /**
     * Individual property of an object is not supported.
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty)  {

        return null;
    }

    /**
     * Individual (parented) collection of an object is returned as a list with the RO representation
     * as an object in the list with a single property named '$$ro'
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedCollection managedCollection) {

        if(!canAccept(resourceContext)) {
            return null;
        }
        final EnumSet<SuppressionType> suppression = suppress(resourceContext);
        final boolean suppressRO = suppression.contains(SuppressionType.RO);

        final JsonRepresentation rootRepresentation = JsonRepresentation.newArray();

        appendCollectionTo(resourceContext, managedCollection, rootRepresentation, suppression);

        final JsonRepresentation $$roRepresentation;
        if(!suppressRO) {
            // $$ro representation will be an object in the list with a single property named "$$ro"
            final JsonRepresentation $$roContainerRepresentation = JsonRepresentation.newMap();
            rootRepresentation.arrayAdd($$roContainerRepresentation);

            $$roRepresentation = JsonRepresentation.newMap();
            $$roContainerRepresentation.mapPutJsonRepresentation("$$ro", $$roRepresentation);
        } else {
            $$roRepresentation = null;
        }

        final Response.ResponseBuilder responseBuilder =
                restfulObjectsV1_0.buildResponseTo(
                        resourceContext, managedCollection, $$roRepresentation, rootRepresentation);

        responseBuilder.type(
                RepresentationTypeSimplifiedV2.OBJECT_COLLECTION.getContentTypeHeaderValue(ACCEPT_PROFILE));

        return responseBuilder(responseBuilder);
    }

    /**
     * Action prompt is not supported.
     */
    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction)  {
        return null;
    }

    /**
     * Action invocation is supported provided it returns a single domain object or a list of domain objects
     * (ie invocations returning void or scalar value are not supported).
     *
     * Action invocations returning a domain object will be rendered as a map with the RO v1.0 representation as a
     * '$$ro' property within (same as {@link #buildResponse(IResourceContext, ManagedObject)}), while
     * action invocations returning a list will be rendered as a list with the RO v1.0 representation as a map object
     * with a single '$$ro' property (similar to {@link #buildResponse(IResourceContext, ManagedCollection)})
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

        final JsonRepresentation rootRepresentation;
        final JsonRepresentation $$roRepresentation;
        if(suppressRO) {
            $$roRepresentation = null;
        } else {
            $$roRepresentation = JsonRepresentation.newMap();
        }

        final ManagedObject returnedAdapter = objectAndActionInvocation.getReturnedAdapter();

        final ActionResultRepresentation.ResultType resultType = objectAndActionInvocation.determineResultType();
        final RepresentationTypeSimplifiedV2 headerContentType;

        switch (resultType) {
        case DOMAIN_OBJECT:

            if(ManagedObjects.isNullOrUnspecifiedOrEmpty(returnedAdapter)) {
                // 404 not found
                return Responses.ofNotFound();

            } else {
                rootRepresentation = JsonRepresentation.newMap();
                appendObjectTo(resourceContext, returnedAdapter, rootRepresentation, suppression);
            }

            headerContentType = RepresentationTypeSimplifiedV2.OBJECT;

            break;

        case LIST:

            if(!objectAndActionInvocation.hasElements()) {
                // 404 not found
                return Responses.ofNotFound();

            }

            rootRepresentation = JsonRepresentation.newArray();

            objectAndActionInvocation.streamElementAdapters()
            .forEach(elementAdapter->
                appendElementTo(resourceContext, elementAdapter, rootRepresentation, suppression));

            // $$ro representation will be an object in the list with a single property named "$$ro"
            if(!suppressRO) {
                JsonRepresentation $$roContainerRepresentation = JsonRepresentation.newMap();
                rootRepresentation.arrayAdd($$roContainerRepresentation);
                $$roContainerRepresentation.mapPutJsonRepresentation("$$ro", $$roRepresentation);
            }

            headerContentType = RepresentationTypeSimplifiedV2.LIST;

            break;

        case SCALAR_VALUES:

            if(!objectAndActionInvocation.hasElements()) {
                // 404 not found
                return Responses.ofNotFound();

            }

            rootRepresentation = JsonRepresentation.newArray();

            objectAndActionInvocation.streamElementAdapters()
            .map(elementAdapter->{
                var dto = dtoForValue(returnedAdapter)
                        .orElseGet(()->elementAdapter.objSpec().getCorrespondingClass());
                return dto;
            })
            .forEach(rootRepresentation::arrayAdd);

            headerContentType = RepresentationTypeSimplifiedV2.VALUES;

            break;

        case SCALAR_VALUE:
            var dto = dtoForValue(returnedAdapter).orElse(null);
            if(dto==null) {
                // 404 not found
                return Responses.ofNotFound();
            }

            var jsonNode = new POJONode(dto);
            rootRepresentation = new JsonRepresentation(jsonNode);
            headerContentType = RepresentationTypeSimplifiedV2.VALUE;

            break;

        case VOID:
            // represented as empty array
            rootRepresentation = JsonRepresentation.newArray();
            headerContentType = RepresentationTypeSimplifiedV2.VOID;
            break;
        default:
            throw _Exceptions.unmatchedCase(resultType);
        }

        var responseBuilder = restfulObjectsV1_0
                .buildResponseTo(resourceContext, objectAndActionInvocation, $$roRepresentation, rootRepresentation)
                .type(headerContentType.getContentTypeHeaderValue(ACCEPT_PROFILE));  // set appropriate Content-Type

        return responseBuilder(responseBuilder);
    }

    private Optional<Object> dtoForValue(final @Nullable ManagedObject valueObject) {
        if(ManagedObjects.isNullOrUnspecifiedOrEmpty(valueObject)
                || !valueObject.objSpec().isValue()) {
            return Optional.empty();
        }
        var valSpec = valueObject.objSpec();
        var dto = valSpec.isCompositeValue()
                ? ScalarValueDtoV2.forValue(valueObject.getPojo(),
                        //XXX honor value semantics context?
                        _Casts.uncheckedCast(valSpec.valueFacetElseFail().selectDefaultSemantics().orElseThrow()))
                : ScalarValueDtoV2.forValue(valueObject.getPojo());
        return Optional.of(dto);
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
            final ManagedObject owner,
            final JsonRepresentation rootRepresentation,
            final EnumSet<SuppressionType> suppression) {

        appendPropertiesTo(resourceContext, owner, rootRepresentation, suppression);

        var where = resourceContext.getWhere();

        owner.objSpec()
        .streamCollections(MixedIn.INCLUDED)
        .forEach(collection->{

            var collectionRepresentation = JsonRepresentation.newArray();
            rootRepresentation.mapPutJsonRepresentation(collection.getId(), collectionRepresentation);

            var interactionInitiatedBy = resourceContext.getInteractionInitiatedBy();
            var visibilityConsent = collection.isVisible(owner, interactionInitiatedBy, where);
            if (!visibilityConsent.isAllowed()) {
                return;
            }

            var managedCollection = ManagedCollection.of(owner, collection, where);

            appendCollectionTo(resourceContext, managedCollection, collectionRepresentation, suppression);
        });

    }

    private void appendPropertiesTo(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter,
            final JsonRepresentation rootRepresentation,
            final EnumSet<SuppressionType> suppression) {

        var interactionInitiatedBy = resourceContext.getInteractionInitiatedBy();
        var where = resourceContext.getWhere();
        final Stream<OneToOneAssociation> properties = objectAdapter.objSpec()
                .streamProperties(MixedIn.INCLUDED);

        properties.forEach(property->{
            final Consent visibility = property.isVisible(objectAdapter, interactionInitiatedBy, where);
            if (!visibility.isAllowed()) {
                return;
            }

            final JsonRepresentation propertyRepresentation = JsonRepresentation.newMap();
            var renderer =
                    new ObjectPropertyReprRenderer(resourceContext, null, property.getId(), propertyRepresentation)
                    .asStandalone()
                    .with(ManagedProperty.of(objectAdapter, property, where));

            final JsonRepresentation propertyValueRepresentation = renderer.render();

            if(!suppression.contains(SuppressionType.HREF)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                rootRepresentation.mapPutString("$$href", upHref);
            }
            if(!suppression.contains(SuppressionType.TITLE)) {
                final String upTitle = propertyValueRepresentation.getString("links[rel=up].title");
                rootRepresentation.mapPutString("$$title", upTitle);
            }
            if(!suppression.contains(SuppressionType.DOMAIN_TYPE)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                final String[] parts = upHref.split("[/]");
                if(parts.length > 2) {
                    final String upObjectType = parts[parts.length - 2];
                    rootRepresentation.mapPutString("$$domainType", upObjectType);
                }
            }
            if(!suppression.contains(SuppressionType.ID)) {
                final String upHref = propertyValueRepresentation.getString("links[rel=up].href");
                final String[] parts = upHref.split("[/]");
                if(parts.length > 1) {
                    final String upInstanceId = parts[parts.length-1];
                    rootRepresentation.mapPutString("$$instanceId", upInstanceId);
                }
            }

            final JsonRepresentation value = propertyValueRepresentation.getRepresentation("value");
            rootRepresentation.mapPutJsonRepresentation(property.getId(), value);
        });

    }

    private void appendCollectionTo(
            final IResourceContext resourceContext,
            final ManagedCollection managedCollection,
            final JsonRepresentation representation,
            final EnumSet<SuppressionType> suppression) {

        managedCollection.streamElements(resourceContext.getInteractionInitiatedBy())
        .forEach(element->
            appendElementTo(resourceContext, element, representation, suppression));
    }

    private void appendElementTo(
            final IResourceContext resourceContext,
            final ManagedObject elementAdapter,
            final JsonRepresentation collectionRepresentation,
            final EnumSet<SuppressionType> suppression) {

        var elementRepresentation = JsonRepresentation.newMap();
        appendPropertiesTo(resourceContext, elementAdapter, elementRepresentation, suppression);
        collectionRepresentation.arrayAdd(elementRepresentation);
    }

}

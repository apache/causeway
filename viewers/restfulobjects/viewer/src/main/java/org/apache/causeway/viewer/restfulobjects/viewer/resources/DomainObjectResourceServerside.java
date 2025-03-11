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

import java.io.InputStream;
import java.util.Optional;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.layout.component.ActionLayoutData;
import org.apache.causeway.applib.layout.component.CollectionLayoutData;
import org.apache.causeway.applib.layout.component.DomainObjectLayoutData;
import org.apache.causeway.applib.layout.component.PropertyLayoutData;
import org.apache.causeway.applib.layout.grid.Grid;
import org.apache.causeway.applib.layout.links.Link;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.core.metamodel.consent.Consent;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIcon;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;
import org.apache.causeway.viewer.restfulobjects.viewer.context.ResourceContext;

import org.jspecify.annotations.NonNull;

import lombok.extern.log4j.Log4j2;

@Component
@Path("/objects")
@Log4j2
public class DomainObjectResourceServerside
extends ResourceAbstract
implements DomainObjectResource {

    public DomainObjectResourceServerside() {
        super();
        log.debug("<init>");
    }

    // //////////////////////////////////////////////////////////
    // persist
    // //////////////////////////////////////////////////////////

    @Override
    @POST
    @Path("/{domainType}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response persist(
            @PathParam("domainType") final String domainType,
            final InputStream object) {

        var resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.JUST_CREATED);

        final JsonRepresentation objectRepr = RequestParams.ofRequestBody(object).asMap();
        if (!objectRepr.isMap()) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", objectRepr));
        }

        var domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType)
                .orElse(null);

        if (domainTypeSpec == null) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no class with domainType Id of '%s')", domainType));
        }

        final ManagedObject adapter = domainTypeSpec.createObject();

        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, adapter);

        final JsonRepresentation membersMap = objectRepr.getMap("members");
        if (membersMap == null) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not find members map; got %s", objectRepr));
        }

        if (!updateHelper.copyOverProperties(membersMap, ObjectAdapterUpdateHelper.Intent.PERSISTING_NEW)) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, "Illegal property value"));
        }

        final Consent validity = adapter.objSpec().isValid(adapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, validity.getReasonAsString().orElse(null)));
        }

        MmEntityUtils.persistInCurrentTransaction(adapter);

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, adapter);

        return _EndpointLogging.response(log, "POST /objects/{}", domainType,
                domainResourceHelper.objectRepresentation());
    }

    // //////////////////////////////////////////////////////////
    // domain object
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response object(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId) {

        var resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}", domainType, instanceId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}", domainType, instanceId,
                domainResourceHelper.objectRepresentation());
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response object(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            final InputStream object) {

        var resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        final JsonRepresentation argRepr = RequestParams.ofRequestBody(object).asMap();
        if (!argRepr.isMap()) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithMessage(
                            HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", argRepr));
        }

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId, roEx));
        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, objectAdapter);

        if (!updateHelper.copyOverProperties(argRepr, ObjectAdapterUpdateHelper.Intent.UPDATE_EXISTING)) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithBody(
                            HttpStatusCode.BAD_REQUEST, argRepr, "Illegal property value"));
        }

        final Consent validity = objectAdapter.objSpec().isValid(objectAdapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithBody(
                            HttpStatusCode.BAD_REQUEST, argRepr, validity.getReasonAsString().orElse(null)));
        }

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "PUT /objects/{}/{}", domainType, instanceId,
                domainResourceHelper.objectRepresentation());
    }

    @DELETE
    @Path("/{domainType}/{instanceId}")
    @Override
    public Response deleteMethodNotSupported(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId) {
        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}", domainType, instanceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting objects is not supported."));
    }

    @POST
    @Path("/{domainType}/{instanceId}")
    @Override
    public Response postMethodNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId) {
        throw _EndpointLogging.error(log, "POST /objects/{}/{}", domainType, instanceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to object resource is not allowed."));
    }

    // //////////////////////////////////////////////////////////
    // domain object layout
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/object-icon")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        "image/png",
        "image/gif",
        "image/jpeg",
        "image/jpg",
        "image/svg+xml"
    })
    public Response image(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId) {

//        createResourceContext(
//                RepresentationType.OBJECT_ICON, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/object-icon", domainType, instanceId, roEx));
        var objectIcon = objectAdapter.getIcon();

        return _EndpointLogging.response(log, "GET /objects/{}/{}/object-icon", domainType, instanceId,
                Response
                .ok(
                        objectIcon.asBytes(),
                        objectIcon.getMimeType().getBaseType())
                .build());
    }

    public Response.ResponseBuilder objectIconResponse(
            final @NonNull ObjectIcon objectIcon) {
        return Response.ok();
    }

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/object-layout")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_LAYOUT_BS,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_LAYOUT_BS
    })
    public Response layout(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var serializationStrategy = resourceContext.getSerializationStrategy();

        return _EndpointLogging.response(log, "GET({}) /objects/{}/{}/object-layout", serializationStrategy.name(), domainType, instanceId,
                layoutAsGrid(domainType, instanceId)
                .map(grid->{

                    addLinks(resourceContext, domainType, instanceId, grid);

                    return Response.status(Response.Status.OK)
                            .entity(serializationStrategy.entity(grid))
                            .type(serializationStrategy.type(RepresentationType.OBJECT_LAYOUT));
                })
                .orElseGet(Responses::ofNotFound)
                .build());
    }

    private Optional<Grid> layoutAsGrid(
            final String domainType,
            final String instanceId) {

        return getSpecificationLoader().specForLogicalTypeName(domainType)
            .flatMap(spec->Facets.bootstrapGrid(
                    spec,
                    getObjectAdapterElseThrowNotFound(domainType, instanceId,
                            roEx->_EndpointLogging
                                .error(log, "GET /objects/{}/{}/object-layout", domainType, instanceId, roEx))));
    }

    // public ... for testing
    public static void addLinks(
            final ResourceContext resourceContext,
            final String domainType,
            final String instanceId,
            final Grid grid) {

        grid.visit(new Grid.VisitorAdapter() {
            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                Link link = new Link(
                        Rel.ELEMENT.getName(),
                        HttpMethod.GET,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId
                                ),
                        RepresentationType.DOMAIN_OBJECT.getJsonMediaType().toString());
                domainObjectLayoutData.setLink(link);
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                Link link = new Link(
                        Rel.ACTION.getName(),
                        HttpMethod.GET,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/actions/" + actionLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                Link link = new Link(
                        Rel.PROPERTY.getName(),
                        HttpMethod.GET,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/properties/" + propertyLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_PROPERTY.getJsonMediaType().toString());
                propertyLayoutData.setLink(link);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                Link link = new Link(
                        Rel.COLLECTION.getName(),
                        HttpMethod.GET,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/collections/" + collectionLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_COLLECTION.getJsonMediaType().toString());
                collectionLayoutData.setLink(link);
            }
        });
    }

    // //////////////////////////////////////////////////////////
    // domain object property
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response propertyDetails(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("propertyId") final String propertyId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_PROPERTY, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        return _EndpointLogging.response(log, "GET /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                _DomainResourceHelper
                .ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.READ));
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response modifyProperty(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("propertyId") final String propertyId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        PropertyInteraction.start(objectAdapter, propertyId, resourceContext.getWhere())
        .checkVisibility()
        .checkUsability(AccessIntent.MUTATE)
        .modifyProperty(property->{
            var proposedNewValue = new JsonParserHelper(resourceContext, property.getElementType())
                    .parseAsMapWithSingleValue(RequestParams.ofRequestBody(body));

            return proposedNewValue;
        })
        .validateElseThrow(veto->
            _EndpointLogging.error(log, "PUT /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, InteractionFailureHandler.onFailure(veto)));

        return _EndpointLogging.response(log, "PUT /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                _DomainResourceHelper
                .ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.WRITE));
    }

    @Override
    @DELETE
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response clearProperty(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("propertyId") final String propertyId) {

        var resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "DELETE /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        PropertyInteraction.start(objectAdapter, propertyId, resourceContext.getWhere())
        .checkVisibility()
        .checkUsability(AccessIntent.MUTATE)
        .modifyProperty(property->null)
        .validateElseThrow(veto->
            _EndpointLogging.error(log, "DELETE /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, InteractionFailureHandler.onFailure(veto)));

        return _EndpointLogging.response(log, "DELETE /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                _DomainResourceHelper
                .ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.WRITE));
    }

    @POST
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Override
    public Response postPropertyNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("propertyId") final String propertyId) {

        throw _EndpointLogging.error(log, "POST /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to a property resource is not allowed."));
    }

    // //////////////////////////////////////////////////////////
    // domain object collection
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response accessCollection(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("collectionId") final String collectionId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_COLLECTION, Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/collections/{}", domainType, instanceId, collectionId, roEx));

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/collections/{}", domainType, instanceId, collectionId,
                domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.READ));
    }

    //XXX[CAUSEWAY-3084] - removal of (direct) collection modification - business logic should handle that via actions instead
//    @Override
//    @PUT
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response addToSet(
//            @PathParam("domainType") final String domainType,
//            @PathParam("instanceId") final String instanceId,
//            @PathParam("collectionId") final String collectionId,
//            final InputStream body) {
//
//        throw _EndpointLogging.error(log, "POST /objects/{}/{}/collections/{}", domainType, instanceId, collectionId,
//                RestfulObjectsApplicationException
//                .createWithMessage(
//                        HttpStatusCode.METHOD_NOT_ALLOWED,
//                        "The framework no longer supports mutable collections."));
//    }
//
//    @Override
//    @POST
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response addToList(
//            @PathParam("domainType") final String domainType,
//            @PathParam("instanceId") final String instanceId,
//            @PathParam("collectionId") final String collectionId,
//            final InputStream body) {
//
//        throw _EndpointLogging.error(log, "POST /objects/{}/{}/collections/{}", domainType, instanceId, collectionId,
//                RestfulObjectsApplicationException
//                .createWithMessage(
//                        HttpStatusCode.METHOD_NOT_ALLOWED,
//                        "The framework no longer supports mutable collections."));
//    }
//
//    @Override
//    @DELETE
//    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
//    @Consumes({ MediaType.WILDCARD })
//    @Produces({
//        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
//        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
//    })
//    public Response removeFromCollection(
//            @PathParam("domainType") final String domainType,
//            @PathParam("instanceId") final String instanceId,
//            @PathParam("collectionId") final String collectionId) {
//
//        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}/collections/{}", domainType, instanceId, collectionId,
//                RestfulObjectsApplicationException
//                .createWithMessage(
//                        HttpStatusCode.METHOD_NOT_ALLOWED,
//                        "The framework no longer supports mutable collections."));
//    }

    // //////////////////////////////////////////////////////////
    // domain object action
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_ACTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response actionPrompt(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/actions/{}", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                domainResourceHelper.actionPrompt(actionId));
    }

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response deleteActionPromptNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId) {

        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting action prompt resource is not allowed."));
    }

    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response postActionPromptNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId) {

        throw _EndpointLogging.error(log, "POST /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Posting to an action prompt resource is not allowed."));
    }

    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response putActionPromptNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId) {

        throw _EndpointLogging.error(log, "PUT /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Putting to an action prompt resource is not allowed."));
    }

    // //////////////////////////////////////////////////////////
    // domain object action invoke
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionQueryOnly(
            final @PathParam("domainType") String domainType,
            final @PathParam("instanceId") String instanceId,
            final @PathParam("actionId") String actionId,
            final @QueryParam("x-causeway-querystring") String xCausewayUrlEncodedQueryString) {

        final String urlUnencodedQueryString = UrlUtils.urlDecodeUtf8(
                xCausewayUrlEncodedQueryString != null
                    ? xCausewayUrlEncodedQueryString
                    : httpServletRequest.getQueryString());
        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                RequestParams.ofQueryString(urlUnencodedQueryString));

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeActionQueryOnly(actionId, arguments));
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeActionIdempotent(
            final @PathParam("domainType") String domainType,
            final @PathParam("instanceId") String instanceId,
            final @PathParam("actionId") String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "PUT /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeActionIdempotent(actionId, arguments));
    }

    @Override
    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response invokeAction(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "POST /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "POST /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeAction(actionId, arguments));
    }

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Override
    public Response deleteInvokeActionNotAllowed(
            @PathParam("domainType") final String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId) {

        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                        RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED,
                        "Deleting an action invocation resource is not allowed."));
    }

}

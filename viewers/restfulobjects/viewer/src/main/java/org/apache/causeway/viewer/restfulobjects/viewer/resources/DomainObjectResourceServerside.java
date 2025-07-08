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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
import org.apache.causeway.core.metamodel.interactions.managed.ManagedMember;
import org.apache.causeway.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.causeway.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.MmEntityUtils;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.causeway.viewer.restfulobjects.rendering.ResponseFactory;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor.ResourceLink;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DomainObjectResourceServerside
extends ResourceAbstract
implements DomainObjectResource {

    public DomainObjectResourceServerside() {
        super();
        log.debug("<init>");
    }

    // PERSIST

    @Override
    public ResponseEntity<Object> persist(
            final String domainType,
            final InputStream object) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS,
                RepresentationService.Intent.JUST_CREATED, ResourceLink.OBJECT));

        final JsonRepresentation objectRepr = RequestParams.ofRequestBody(object).asMap();
        if (!objectRepr.isMap()) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatus.BAD_REQUEST, "Body is not a map; got %s".formatted(objectRepr)));
        }

        var domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType)
                .orElse(null);

        if (domainTypeSpec == null) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatus.BAD_REQUEST,
                        "Could not determine type of domain object to persist (no class with domainType Id of '%s')".formatted(domainType)));
        }

        final ManagedObject adapter = domainTypeSpec.createObject();

        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, adapter);

        final JsonRepresentation membersMap = objectRepr.getMap("members");
        if (membersMap == null) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithMessage(HttpStatus.BAD_REQUEST, "Could not find members map; got %s".formatted(objectRepr)));
        }

        if (!updateHelper.copyOverProperties(membersMap, ObjectAdapterUpdateHelper.Intent.PERSISTING_NEW)) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithBody(HttpStatus.BAD_REQUEST, objectRepr, "Illegal property value"));
        }

        final Consent validity = adapter.objSpec().isValid(adapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw _EndpointLogging.error(log, "POST /objects/{}", domainType,
                    RestfulObjectsApplicationException
                    .createWithBody(HttpStatus.BAD_REQUEST, objectRepr, validity.getReasonAsString().orElse(null)));
        }

        MmEntityUtils.persistInCurrentTransaction(adapter);

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, adapter);

        return _EndpointLogging.response(log, "POST /objects/{}", domainType,
                domainResourceHelper.objectRepresentation());
    }

    // DOMAIN OBJECT

    @Override
    public ResponseEntity<Object> object(
            final String domainType,
            final String instanceId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS,
                RepresentationService.Intent.ALREADY_PERSISTENT, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}", domainType, instanceId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}", domainType, instanceId,
                domainResourceHelper.objectRepresentation());
    }

    @Override
    public ResponseEntity<Object> object(
            final String domainType,
            final String instanceId,
            final InputStream object) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS,
                RepresentationService.Intent.ALREADY_PERSISTENT, ResourceLink.OBJECT));

        final JsonRepresentation argRepr = RequestParams.ofRequestBody(object).asMap();
        if (!argRepr.isMap()) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithMessage(
                        HttpStatus.BAD_REQUEST, "Body is not a map; got %s".formatted(argRepr)));
        }

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId, roEx));
        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, objectAdapter);

        if (!updateHelper.copyOverProperties(argRepr, ObjectAdapterUpdateHelper.Intent.UPDATE_EXISTING)) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithBody(
                        HttpStatus.BAD_REQUEST, argRepr, "Illegal property value"));
        }

        final Consent validity = objectAdapter.objSpec().isValid(objectAdapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw _EndpointLogging.error(log, "PUT /objects/{}/{}", domainType, instanceId,
                    RestfulObjectsApplicationException
                    .createWithBody(
                        HttpStatus.BAD_REQUEST, argRepr, validity.getReasonAsString().orElse(null)));
        }

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "PUT /objects/{}/{}", domainType, instanceId,
                domainResourceHelper.objectRepresentation());
    }

    @Override
    public ResponseEntity<Object> deleteMethodNotSupported(
            final String domainType,
            final String instanceId) {
        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}", domainType, instanceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED, "Deleting objects is not supported."));
    }

    @Override
    public ResponseEntity<Object> postMethodNotAllowed(
            final String domainType,
            final String instanceId) {
        throw _EndpointLogging.error(log, "POST /objects/{}/{}", domainType, instanceId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED, "Posting to object resource is not allowed."));
    }

    // DOMAIN OBJECT LAYOUT

    @Override
    public ResponseEntity<Object> image(
            final String domainType,
            final String instanceId) {

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/object-icon", domainType, instanceId, roEx));
        var objectIcon = objectAdapter.getIcon();

        return _EndpointLogging.response(log, "GET /objects/{}/{}/object-icon", domainType, instanceId,
            responseFactory.ok(
                        objectIcon.asBytes(),
                        MediaType.parseMediaType(objectIcon.getMimeType().getBaseType())));
    }

    @Override
    public ResponseEntity<Object> layout(
            final String domainType,
            final String instanceId) {

        var resourceContext = createResourceContext(
                RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var serializationStrategy = resourceContext.getSerializationStrategy();

        return _EndpointLogging.response(log, "GET({}) /objects/{}/{}/object-layout", serializationStrategy.name(), domainType, instanceId,
                layoutAsGrid(domainType, instanceId)
                .map(grid->{

                    addLinks(resourceContext, domainType, instanceId, grid);

                    return responseFactory.ok(
                            serializationStrategy.entity(grid),
                            serializationStrategy.type(RepresentationType.OBJECT_LAYOUT));
                })
                .orElseGet(ResponseFactory::notFound));
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
                Link link = newLink(
                        Rel.ELEMENT,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId
                                ),
                        RepresentationType.DOMAIN_OBJECT.getJsonMediaType().toString());
                domainObjectLayoutData.setLink(link);
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                Link link = newLink(
                        Rel.ACTION,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/actions/" + actionLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                Link link = newLink(
                        Rel.PROPERTY,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/properties/" + propertyLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_PROPERTY.getJsonMediaType().toString());
                propertyLayoutData.setLink(link);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                Link link = newLink(
                        Rel.COLLECTION,
                        resourceContext.restfulUrlFor(
                                "objects/" + domainType + "/" + instanceId + "/collections/" + collectionLayoutData.getId()
                                ),
                        RepresentationType.OBJECT_COLLECTION.getJsonMediaType().toString());
                collectionLayoutData.setLink(link);
            }
        });
    }

    // DOMAIN OBJECT PROPERTY

    @Override
    public ResponseEntity<Object> propertyDetails(
            final String domainType,
            final String instanceId,
            final String propertyId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.OBJECT_PROPERTY, Where.OBJECT_FORMS,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        return _EndpointLogging.response(log, "GET /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                _DomainResourceHelper
                .ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.READ));
    }

    @Override
    public ResponseEntity<Object> modifyProperty(
            final String domainType,
            final String instanceId,
            final String propertyId,
            final InputStream body) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.GENERIC, Where.OBJECT_FORMS,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        PropertyInteraction
            .start(objectAdapter, propertyId, resourceContext.where())
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
    public ResponseEntity<Object> clearProperty(
            final String domainType,
            final String instanceId,
            final String propertyId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.GENERIC, Where.OBJECT_FORMS,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "DELETE /objects/{}/{}/properties/{}", domainType, instanceId, propertyId, roEx));

        PropertyInteraction.start(objectAdapter, propertyId, resourceContext.where())
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

    @Override
    public ResponseEntity<Object> postPropertyNotAllowed(
            final String domainType,
            final String instanceId,
            final String propertyId) {

        throw _EndpointLogging.error(log, "POST /objects/{}/{}/properties/{}", domainType, instanceId, propertyId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to a property resource is not allowed."));
    }

    // DOMAIN OBJECT COLLECTION

    @Override
    public ResponseEntity<Object> accessCollection(
            final String domainType,
            final String instanceId,
            final String collectionId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.OBJECT_COLLECTION, Where.PARENTED_TABLES,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/collections/{}", domainType, instanceId, collectionId, roEx));

        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/collections/{}", domainType, instanceId, collectionId,
                domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.READ));
    }

    // DOMAIN OBJECT ACTION

    @Override
    public ResponseEntity<Object> actionPrompt(
            final String domainType,
            final String instanceId,
            final String actionId) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT));

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/actions/{}", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                domainResourceHelper.actionPrompt(actionId));
    }

    @Override
    public ResponseEntity<Object> deleteActionPromptNotAllowed(
            final String domainType,
            final String instanceId,
            final String actionId) {

        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting action prompt resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> postActionPromptNotAllowed(
            final String domainType,
            final String instanceId,
            final String actionId) {

        throw _EndpointLogging.error(log, "POST /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Posting to an action prompt resource is not allowed."));
    }

    @Override
    public ResponseEntity<Object> putActionPromptNotAllowed(
            final String domainType,
            final String instanceId,
            final String actionId) {

        throw _EndpointLogging.error(log, "PUT /objects/{}/{}/actions/{}", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Putting to an action prompt resource is not allowed."));
    }

    // DOMAIN OBJECT ACTION INVOKE

    @Override
    public ResponseEntity<Object> invokeActionQueryOnly(
            final String domainType,
            final String instanceId,
            final String actionId,
            final String xCausewayUrlEncodedQueryString) {

        final String urlUnencodedQueryString = UrlUtils.urlDecodeUtf8(
                xCausewayUrlEncodedQueryString != null
                    ? xCausewayUrlEncodedQueryString
                    : httpServletRequest.getQueryString());
        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT),
                RequestParams.ofQueryString(urlUnencodedQueryString));

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "GET /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "GET /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeActionQueryOnly(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> invokeActionIdempotent(
            final String domainType,
            final String instanceId,
            final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT),
                body);

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "PUT /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "PUT /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeActionIdempotent(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> invokeAction(
            final String domainType,
            final String instanceId,
            final String actionId,
            final InputStream body) {

        var resourceContext = createResourceContext(
                new ResourceDescriptor(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES,
                    RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.OBJECT),
                body);

        final JsonRepresentation arguments = resourceContext.queryStringAsJsonRepr();

        var objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId,
                roEx->_EndpointLogging.error(log, "POST /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId, roEx));
        var domainResourceHelper = _DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return _EndpointLogging.response(log, "POST /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                domainResourceHelper.invokeAction(actionId, arguments));
    }

    @Override
    public ResponseEntity<Object> deleteInvokeActionNotAllowed(
            final String domainType,
            final String instanceId,
            final String actionId) {

        throw _EndpointLogging.error(log, "DELETE /objects/{}/{}/actions/{}/invoke", domainType, instanceId, actionId,
                RestfulObjectsApplicationException
                .createWithMessage(
                    HttpStatus.METHOD_NOT_ALLOWED,
                        "Deleting an action invocation resource is not allowed."));
    }

}

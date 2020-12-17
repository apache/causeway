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

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.links.Link;
import org.apache.isis.commons.internal.base._Bytes;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.interactions.managed.ManagedMember;
import org.apache.isis.core.metamodel.interactions.managed.MemberInteraction.AccessIntent;
import org.apache.isis.core.metamodel.interactions.managed.PropertyInteraction;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.iactn.InteractionTracker;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Component
@Path("/objects") @Log4j2
public class DomainObjectResourceServerside extends ResourceAbstract implements DomainObjectResource {

    @Inject
    public DomainObjectResourceServerside(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration,
            final InteractionTracker isisInteractionTracker) {
        super(metaModelContext, isisConfiguration, isisInteractionTracker);
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
    public Response persist(@PathParam("domainType") String domainType, final InputStream object) {

        val resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.JUST_CREATED);

        final String objectStr = Util.asStringUtf8(object);
        final JsonRepresentation objectRepr = Util.readAsMap(objectStr);
        if (!objectRepr.isMap()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", objectRepr);
        }

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().lookupBySpecIdElseLoad(ObjectSpecId.of(domainType));
        if (domainTypeSpec == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no class with domainType Id of '%s')", domainType);
        }

        final ManagedObject adapter = domainTypeSpec.createObject(); 

        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, adapter);

        final JsonRepresentation membersMap = objectRepr.getMap("members");
        if (membersMap == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not find members map; got %s", objectRepr);
        }

        if (!updateHelper.copyOverProperties(membersMap, ObjectAdapterUpdateHelper.Intent.PERSISTING_NEW)) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, "Illegal property value");
        }

        final Consent validity = adapter.getSpecification().isValid(adapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, validity.getReason());
        }

        EntityUtil.persistInCurrentTransaction(adapter);
        
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, adapter);

        return domainResourceHelper.objectRepresentation();
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
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.objectRepresentation();
    }


    @Override
    @PUT
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, final InputStream object) {

        val resourceContext = createResourceContext(
                RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        final String objectStr = Util.asStringUtf8(object);
        final JsonRepresentation argRepr = Util.readAsMap(objectStr);
        if (!argRepr.isMap()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", argRepr);
        }

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(resourceContext, objectAdapter);

        if (!updateHelper.copyOverProperties(argRepr, ObjectAdapterUpdateHelper.Intent.UPDATE_EXISTING)) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, argRepr, "Illegal property value");
        }

        final Consent validity = objectAdapter.getSpecification().isValid(objectAdapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, argRepr, validity.getReason());
        }

        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);
        
        return domainResourceHelper.objectRepresentation();
    }

    @DELETE
    @Path("/{domainType}/{instanceId}")
    @Override
    public Response deleteMethodNotSupported(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting objects is not supported.");
    }

    @POST
    @Path("/{domainType}/{instanceId}")
    @Override
    public Response postMethodNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to object resource is not allowed.");
    }


    // //////////////////////////////////////////////////////////
    // domain object layout
    // //////////////////////////////////////////////////////////

    static class IconKey {
        private final Class<?> domainClass;
        private final String modifier;

        IconKey(final Class<?> domainClass, final String modifier) {
            this.domainClass = domainClass;
            this.modifier = modifier;
        }

        String getImageName() {
            final StringBuilder buf = new StringBuilder(domainClass.getSimpleName());
            if(!_Strings.isNullOrEmpty(modifier)) {
                buf.append("-").append(modifier);
            }
            buf.append(".png");
            return buf.toString();
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final IconKey iconKey = (IconKey) o;

            if (domainClass != null ? !domainClass.equals(iconKey.domainClass) : iconKey.domainClass != null) {
                return false;
            }
            return modifier != null ? modifier.equals(iconKey.modifier) : iconKey.modifier == null;
        }

        @Override
        public int hashCode() {
            int result = domainClass != null ? domainClass.hashCode() : 0;
            result = 31 * result + (modifier != null ? modifier.hashCode() : 0);
            return result;
        }

        byte[] toBytes() {
            String imageName =  getImageName();

            try {

                final InputStream resource = _Resources.load(domainClass, imageName);
                return _Bytes.of(resource);

            } catch (IOException e) {
                return null;
            }

        }
    }

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/image")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        "image/png"
    })
    public Response image(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId) {

        createResourceContext(
                RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ManagedObject objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final ObjectSpecification objectSpec = objectAdapter.getSpecification();
        final String iconName = objectSpec.getIconName(objectAdapter);
        final Class<?> correspondingClass = objectSpec.getCorrespondingClass();
        final IconKey iconKey = new IconKey(correspondingClass, iconName);
        final byte[] bytes = iconKey.toBytes();
        return bytes != null
                ? Response.ok(bytes).build()
                        : Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    @GET
    @Path("/{domainType}/{instanceId}/object-layout")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_LAYOUT_BS3,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_LAYOUT_BS3
    })
    public Response layout(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId) {

        val resourceContext = createResourceContext(
                RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val serializationStrategy = resourceContext.getSerializationStrategy();

        return layoutAsGrid(domainType, instanceId)
            .map(grid->{

                addLinks(resourceContext, domainType, instanceId, grid);
                
                return Response.status(Response.Status.OK)
                        .entity(serializationStrategy.entity(grid))
                        .type(serializationStrategy.type(RepresentationType.OBJECT_LAYOUT));
            })
            .orElseGet(Responses::ofNotFound)
            .build();
        
    }

    private Optional<Grid> layoutAsGrid(
            final String domainType,
            final String instanceId) {

        val objectSpec = getSpecificationLoader().lookupBySpecIdElseLoad(ObjectSpecId.of(domainType));
        val gridFacet = objectSpec.getFacet(GridFacet.class);
        
        if(gridFacet == null) {
            return Optional.empty();
        } 
        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val grid = gridFacet.getGrid(objectAdapter);
        return Optional.of(grid);
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
                        resourceContext.urlFor(
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
                        resourceContext.urlFor(
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
                        resourceContext.urlFor(
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
                        resourceContext.urlFor(
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
    public Response propertyDetails(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.OBJECT_PROPERTY, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.propertyDetails(
                propertyId,
                ManagedMember.RepresentationMode.READ
                );
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response modifyProperty(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId, final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE));

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        
        PropertyInteraction.start(objectAdapter, propertyId, resourceContext.getWhere())
        .checkVisibility()
        .checkUsability(AccessIntent.MUTATE)
        .modifyProperty(property->{
            val proposedNewValue = new JsonParserHelper(resourceContext, property.getSpecification())
                    .parseAsMapWithSingleValue(Util.asStringUtf8(body));
            
            return proposedNewValue;
        })
        .validateElseThrow(InteractionFailureHandler::onFailure);

        return DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.WRITE);
    }

    @Override
    @DELETE
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response clearProperty(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE));

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        
        PropertyInteraction.start(objectAdapter, propertyId, resourceContext.getWhere())
        .checkVisibility()
        .checkUsability(AccessIntent.MUTATE)
        .modifyProperty(property->null)
        .validateElseThrow(InteractionFailureHandler::onFailure);

        return DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter)
                .propertyDetails(propertyId, ManagedMember.RepresentationMode.WRITE);
    }

    @POST
    @Path("/{domainType}/{instanceId}/properties/{propertyId}")
    @Override
    public Response postPropertyNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("propertyId") String propertyId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to a property resource is not allowed.");
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
    public Response accessCollection(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.OBJECT_COLLECTION, Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        
        return domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.READ);
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response addToSet(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId, final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE));

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter);

        val collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, AccessIntent.MUTATE)
                .getCollection();

        if (!collection.getCollectionSemantics().isAnySet()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Collection '%s' does not have set semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = Util.asStringUtf8(body);
        final ManagedObject argAdapter = new JsonParserHelper(resourceContext, collectionSpec)
                .parseAsMapWithSingleValue(bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.WRITE);
    }

    @Override
    @POST
    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response addToList(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId, final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE));

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter);

        val collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, AccessIntent.MUTATE)
                .getCollection();

        if (!collection.getCollectionSemantics().isListOrArray()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Collection '%s' does not have list or array semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = Util.asStringUtf8(body);
        final ManagedObject argAdapter = new JsonParserHelper(resourceContext, collectionSpec).parseAsMapWithSingleValue(
                bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.WRITE);
    }

    @Override
    @DELETE
    @Path("/{domainType}/{instanceId}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_XML_ERROR
    })
    public Response removeFromCollection(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE));

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = ObjectAdapterAccessHelper.of(resourceContext, objectAdapter);

        val collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, AccessIntent.MUTATE)
                .getCollection();

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final ManagedObject argAdapter = new JsonParserHelper(resourceContext, collectionSpec)
                .parseAsMapWithSingleValue(resourceContext.getUrlUnencodedQueryString());

        final Consent consent = collection.isValidToRemove(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.removeElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return domainResourceHelper.collectionDetails(collectionId, ManagedMember.RepresentationMode.WRITE);
    }

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
    public Response actionPrompt(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId) {
        
        val resourceContext = createResourceContext(
                RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.actionPrompt(actionId);
    }

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response deleteActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting action prompt resource is not allowed.");
    }

    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response postActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to an action prompt resource is not allowed.");
    }

    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}")
    @Override
    public Response putActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Putting to an action prompt resource is not allowed.");
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
            final @QueryParam("x-isis-querystring") String xIsisUrlEncodedQueryString) {

        final String urlUnencodedQueryString = _UrlDecoderUtil
                .urlDecodeNullSafe(xIsisUrlEncodedQueryString != null? xIsisUrlEncodedQueryString: httpServletRequest.getQueryString());
        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                urlUnencodedQueryString);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.invokeActionQueryOnly(actionId, arguments);
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

        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.invokeActionIdempotent(actionId, arguments);
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
            @PathParam("domainType") String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId,
            final InputStream body) {
        
        val resourceContext = createResourceContext(
                ResourceDescriptor.of(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE),
                body);

        final JsonRepresentation arguments = resourceContext.getQueryStringAsJsonRepr();

        val objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        val domainResourceHelper = DomainResourceHelper.ofObjectResource(resourceContext, objectAdapter);

        return domainResourceHelper.invokeAction(actionId, arguments);
    }

    @DELETE
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Override
    public Response deleteInvokeActionNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting an action invocation resource is not allowed.");
    }

}

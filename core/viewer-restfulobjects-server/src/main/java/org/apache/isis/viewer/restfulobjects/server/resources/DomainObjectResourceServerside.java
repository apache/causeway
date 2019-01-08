/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.common.io.Resources;

import org.apache.log4j.Logger;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.component.ActionLayoutData;
import org.apache.isis.applib.layout.component.CollectionLayoutData;
import org.apache.isis.applib.layout.component.DomainObjectLayoutData;
import org.apache.isis.applib.layout.component.PropertyLayoutData;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.applib.layout.links.Link;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulHttpMethod;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberReprMode;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.PrettyPrinting;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.server.resources.serialization.SerializationStrategy;

@Path("/objects")
public class DomainObjectResourceServerside extends ResourceAbstract implements DomainObjectResource {

    private final static Logger LOG = Logger.getLogger(DomainObjectResourceServerside.class);

    public DomainObjectResourceServerside() {
        LOG.debug("<init>");
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

        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.JUST_CREATED);

        final String objectStr = Util.asStringUtf8(object);
        final JsonRepresentation objectRepr = Util.readAsMap(objectStr);
        if (!objectRepr.isMap()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", objectRepr);
        }

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        if (domainTypeSpec == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no class with domainType Id of '%s')", domainType);
        }

        final ObjectAdapter objectAdapter = getResourceContext().getPersistenceSession().createTransientInstance(domainTypeSpec);

        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(getResourceContext(), objectAdapter);

        final JsonRepresentation membersMap = objectRepr.getMap("members");
        if (membersMap == null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Could not find members map; got %s", objectRepr);
        }

        if (!updateHelper.copyOverProperties(membersMap, ObjectAdapterUpdateHelper.Intent.PERSISTING_NEW)) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, "Illegal property value");
        }

        final Consent validity = objectAdapter.getSpecification().isValid(objectAdapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, objectRepr, validity.getReason());
        }
        getResourceContext().getPersistenceSession().makePersistentInTransaction(objectAdapter);

        return newDomainResourceHelper(objectAdapter).objectRepresentation(RepresentationService.Intent.JUST_CREATED);
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
    @PrettyPrinting
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId) {
        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);

        return newDomainResourceHelper(objectAdapter).objectRepresentation();
    }


    @Override
    @PUT
    @Path("/{domainType}/{instanceId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
            MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_OBJECT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    @PrettyPrinting
    public Response object(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, final InputStream object) {

        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS, RepresentationService.Intent.ALREADY_PERSISTENT);

        final String objectStr = Util.asStringUtf8(object);
        final JsonRepresentation argRepr = Util.readAsMap(objectStr);
        if (!argRepr.isMap()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", argRepr);
        }

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final ObjectAdapterUpdateHelper updateHelper = new ObjectAdapterUpdateHelper(getResourceContext(), objectAdapter);

        if (!updateHelper.copyOverProperties(argRepr, ObjectAdapterUpdateHelper.Intent.UPDATE_EXISTING)) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, argRepr, "Illegal property value");
        }

        final Consent validity = objectAdapter.getSpecification().isValid(objectAdapter, InteractionInitiatedBy.USER);
        if (validity.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithBody(HttpStatusCode.BAD_REQUEST, argRepr, validity.getReason());
        }

        return newDomainResourceHelper(objectAdapter).objectRepresentation();
    }

    @Override
    public Response deleteMethodNotSupported(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting objects is not supported.");
    }

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
            if(!Strings.isNullOrEmpty(modifier)) {
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
            URL resource = Resources.getResource(domainClass, imageName);
            try {
                return Resources.toByteArray(resource);
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
    @PrettyPrinting
    public Response image(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId) {

        init(RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
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
    @PrettyPrinting
    public Response layout(
            @PathParam("domainType")
            final String domainType,
            @PathParam("instanceId")
            final String instanceId) {

        init(RepresentationType.OBJECT_LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final List<MediaType> acceptableMediaTypes = getResourceContext().getAcceptableMediaTypes();
        final SerializationStrategy serializationStrategy =
                acceptableMediaTypes.contains(MediaType.APPLICATION_XML_TYPE) ||
                acceptableMediaTypes.contains(RepresentationType.OBJECT_LAYOUT.getXmlMediaType())
                    ? SerializationStrategy.XML
                    : SerializationStrategy.JSON;

        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        final GridFacet gridFacet = objectSpec.getFacet(GridFacet.class);
        final Response.ResponseBuilder builder;
        if(gridFacet == null) {
            builder = Responses.ofNotFound();
        } else {
            final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
            Grid grid = gridFacet.getGrid(objectAdapter);
            addLinks(domainType, instanceId, grid);
            builder = Response.status(Response.Status.OK)
                    .entity(serializationStrategy.entity(grid))
                    .type(serializationStrategy.type(RepresentationType.OBJECT_LAYOUT));
        }

        return builder.build();
    }

    private void addLinks(
            final String domainType,
            final String instanceId,
            final Grid grid) {
        grid.visit(new Grid.VisitorAdapter() {
            @Override
            public void visit(final DomainObjectLayoutData domainObjectLayoutData) {
                Link link = new Link(
                        Rel.ELEMENT.getName(),
                        RestfulHttpMethod.GET.getJavaxRsMethod(),
                        getResourceContext().urlFor(
                            "objects/" + domainType + "/" + instanceId
                        ),
                        RepresentationType.DOMAIN_OBJECT.getJsonMediaType().toString());
                domainObjectLayoutData.setLink(link);
            }

            @Override
            public void visit(final ActionLayoutData actionLayoutData) {
                Link link = new Link(
                        Rel.ACTION.getName(),
                        RestfulHttpMethod.GET.getJavaxRsMethod(),
                        getResourceContext().urlFor(
                            "objects/" + domainType + "/" + instanceId + "/actions/" + actionLayoutData.getId()
                        ),
                        RepresentationType.OBJECT_ACTION.getJsonMediaType().toString());
                actionLayoutData.setLink(link);
            }

            @Override
            public void visit(final PropertyLayoutData propertyLayoutData) {
                Link link = new Link(
                        Rel.PROPERTY.getName(),
                        RestfulHttpMethod.GET.getJavaxRsMethod(),
                        getResourceContext().urlFor(
                            "objects/" + domainType + "/" + instanceId + "/properties/" + propertyLayoutData.getId()
                        ),
                        RepresentationType.OBJECT_PROPERTY.getJsonMediaType().toString());
                propertyLayoutData.setLink(link);
            }

            @Override
            public void visit(final CollectionLayoutData collectionLayoutData) {
                Link link = new Link(
                        Rel.COLLECTION.getName(),
                        RestfulHttpMethod.GET.getJavaxRsMethod(),
                        getResourceContext().urlFor(
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
    @PrettyPrinting
    public Response propertyDetails(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("propertyId") final String propertyId) {
        init(RepresentationType.OBJECT_PROPERTY, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);

        return helper.propertyDetails(
                propertyId,
                MemberReprMode.READ
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
        init(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        setCommandExecutor(Command.Executor.USER);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(getResourceContext(), objectAdapter);

        final OneToOneAssociation property = accessHelper.getPropertyThatIsVisibleForIntent(propertyId,
                ObjectAdapterAccessHelper.Intent.MUTATE);

        final ObjectSpecification propertySpec = property.getSpecification();
        final String bodyAsString = Util.asStringUtf8(body);

        final ObjectAdapter argAdapter = new JsonParserHelper(getResourceContext(), propertySpec).parseAsMapWithSingleValue(
                bodyAsString);

        final Consent consent = property.isAssociationValid(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        property.set(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return helper.propertyDetails(
                propertyId,
                MemberReprMode.WRITE
        );
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
        init(Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        setCommandExecutor(Command.Executor.USER);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(getResourceContext(), objectAdapter);

        final OneToOneAssociation property = accessHelper.getPropertyThatIsVisibleForIntent(
                propertyId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final Consent consent = property.isAssociationValid(objectAdapter, null, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        property.set(objectAdapter, null, InteractionInitiatedBy.USER);

        return helper.propertyDetails(
                propertyId,
                MemberReprMode.WRITE
        );
    }

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
    @PrettyPrinting
    public Response accessCollection(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("collectionId") final String collectionId) {
        init(RepresentationType.OBJECT_COLLECTION, Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);

        return newDomainResourceHelper(objectAdapter).collectionDetails(collectionId, MemberReprMode.READ);
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
        init(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        if (!collection.getCollectionSemantics().isSet()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Collection '%s' does not have set semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = Util.asStringUtf8(body);
        final ObjectAdapter argAdapter = new JsonParserHelper(getResourceContext(), collectionSpec).parseAsMapWithSingleValue(
                bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return helper.collectionDetails(collectionId, MemberReprMode.WRITE);
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
        init(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        if (!collection.getCollectionSemantics().isListOrArray()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Collection '%s' does not have list or array semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = Util.asStringUtf8(body);
        final ObjectAdapter argAdapter = new JsonParserHelper(getResourceContext(), collectionSpec).parseAsMapWithSingleValue(
                bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return helper.collectionDetails(collectionId, MemberReprMode.WRITE);
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
        init(Where.PARENTED_TABLES, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);
        final ObjectAdapterAccessHelper accessHelper = new ObjectAdapterAccessHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = accessHelper.getCollectionThatIsVisibleForIntent(
                collectionId, ObjectAdapterAccessHelper.Intent.MUTATE);

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final ObjectAdapter argAdapter = new JsonParserHelper(getResourceContext(), collectionSpec).parseAsMapWithSingleValue(
                getResourceContext().getUrlUnencodedQueryString());

        final Consent consent = collection.isValidToRemove(objectAdapter, argAdapter, InteractionInitiatedBy.USER);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.removeElement(objectAdapter, argAdapter, InteractionInitiatedBy.USER);

        return helper.collectionDetails(collectionId, MemberReprMode.WRITE);
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
    @PrettyPrinting
    public Response actionPrompt(@PathParam("domainType") String domainType, @PathParam("instanceId") final String instanceId, @PathParam("actionId") final String actionId) {
        init(RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);

        return helper.actionPrompt(actionId);
    }

    @Override
    public Response deleteActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting action prompt resource is not allowed.");
    }

    @Override
    public Response postActionPromptNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.METHOD_NOT_ALLOWED, "Posting to an action prompt resource is not allowed.");
    }

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
    @PrettyPrinting
    public Response invokeActionQueryOnly(
            final @PathParam("domainType") String domainType,
            final @PathParam("instanceId") String instanceId,
            final @PathParam("actionId") String actionId,
            final @QueryParam("x-isis-querystring") String xIsisUrlEncodedQueryString) {

        final String urlUnencodedQueryString = UrlEncodingUtils.urlDecodeNullSafe(xIsisUrlEncodedQueryString != null? xIsisUrlEncodedQueryString: httpServletRequest.getQueryString());
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE, urlUnencodedQueryString);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();

        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);

        return helper.invokeActionQueryOnly(actionId, arguments);
    }

    @Override
    @PUT
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
            MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    @PrettyPrinting
    public Response invokeActionIdempotent(
            final @PathParam("domainType") String domainType,
            final @PathParam("instanceId") String instanceId,
            final @PathParam("actionId") String actionId,
            final InputStream body) {

        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE, body);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();
        
        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);

        return helper.invokeActionIdempotent(actionId, arguments);
    }

    @Override
    @POST
    @Path("/{domainType}/{instanceId}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({
            MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR,
            MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_ACTION_RESULT, RestfulMediaType.APPLICATION_XML_ERROR
    })
    @PrettyPrinting
    public Response invokeAction(
            @PathParam("domainType") String domainType,
            @PathParam("instanceId") final String instanceId,
            @PathParam("actionId") final String actionId,
            final InputStream body) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES, RepresentationService.Intent.NOT_APPLICABLE,
                body);

        setCommandExecutor(Command.Executor.USER);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();
        
        final ObjectAdapter objectAdapter = getObjectAdapterElseThrowNotFound(domainType, instanceId);
        final DomainResourceHelper helper = newDomainResourceHelper(objectAdapter);

        return helper.invokeAction(actionId, arguments);
    }

    @Override
    public Response deleteInvokeActionNotAllowed(@PathParam("domainType") String domainType, @PathParam("instanceId") String instanceId, @PathParam("actionId") String actionId) {
        throw RestfulObjectsApplicationException.createWithMessage(RestfulResponse.HttpStatusCode.METHOD_NOT_ALLOWED, "Deleting an action invocation resource is not allowed.");
    }


    private DomainResourceHelper newDomainResourceHelper(final ObjectAdapter objectAdapter) {
        return new DomainResourceHelper(getResourceContext(), objectAdapter);
    }


}

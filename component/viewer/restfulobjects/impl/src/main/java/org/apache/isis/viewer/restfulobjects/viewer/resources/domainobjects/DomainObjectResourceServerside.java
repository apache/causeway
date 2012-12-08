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
package org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.ClientResponseType;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.links.LinkRepresentation;
import org.apache.isis.viewer.restfulobjects.domainobjects.DomainObjectResource;
import org.apache.isis.viewer.restfulobjects.viewer.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.DomainResourceHelper.Intent;
import org.apache.isis.viewer.restfulobjects.viewer.resources.domainobjects.DomainResourceHelper.MemberMode;
import org.apache.isis.viewer.restfulobjects.viewer.util.UrlParserUtils;

@Path("/objects")
public class DomainObjectResourceServerside extends ResourceAbstract implements DomainObjectResource {

    // //////////////////////////////////////////////////////////
    // persist
    // //////////////////////////////////////////////////////////

    @Override
    @POST
    @Path("/")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    @ClientResponseType(entityType = String.class)
    public Response persist(final InputStream object) {

        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS);

        final String objectStr = DomainResourceHelper.asStringUtf8(object);
        final JsonRepresentation objectRepr = DomainResourceHelper.readAsMap(objectStr);
        if (!objectRepr.isMap()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", objectRepr);
        }

        final LinkRepresentation describedByLink = objectRepr.getLink("links[rel=describedby]");
        if (!describedByLink.isLink()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no links[rel=describedby] link); got %s", objectRepr);
        }

        final String domainTypeStr = UrlParserUtils.domainTypeFrom(describedByLink);
        if (domainTypeStr == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no href in links[rel=describedby]); got %s", describedByLink);
        }
        final ObjectSpecification domainTypeSpec = getSpecificationLoader().loadSpecification(domainTypeStr);
        if (domainTypeSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Could not determine type of domain object to persist (no such class '%s')", domainTypeStr);
        }

        final ObjectAdapter objectAdapter = getResourceContext().getPersistenceSession().createTransientInstance(domainTypeSpec);

        final JsonRepresentation propertiesList = objectRepr.getArrayEnsured("members[memberType=property]");
        if (propertiesList == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Could not find properties list (no members[memberType=property]); got %s", objectRepr);
        }
        if (!DomainResourceHelper.copyOverProperties(getResourceContext(), objectAdapter, propertiesList)) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, objectRepr, "Illegal property value");
        }

        final Consent validity = objectAdapter.getSpecification().isValid(objectAdapter);
        if (validity.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, objectRepr, validity.getReason());
        }
        getResourceContext().getPersistenceSession().makePersistent(objectAdapter);

        return new DomainResourceHelper(getResourceContext(), objectAdapter).objectRepresentation();
    }

    // //////////////////////////////////////////////////////////
    // domain object
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response object(@PathParam("oid") final String oidStr) {
        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);

        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);
        return helper.objectRepresentation();
    }

    @Override
    @PUT
    @Path("/{oid}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_OBJECT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response object(@PathParam("oid") final String oidStr, final InputStream object) {

        init(RepresentationType.DOMAIN_OBJECT, Where.OBJECT_FORMS);

        final String objectStr = DomainResourceHelper.asStringUtf8(object);
        final JsonRepresentation objectRepr = DomainResourceHelper.readAsMap(objectStr);
        if (!objectRepr.isMap()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Body is not a map; got %s", objectRepr);
        }

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);

        final JsonRepresentation propertiesList = objectRepr.getArrayEnsured("members[memberType=property]");
        if (propertiesList == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Could not find properties list (no members[memberType=property]); got %s", objectRepr);
        }

        final IsisTransactionManager transactionManager = getResourceContext().getPersistenceSession().getTransactionManager();
        transactionManager.startTransaction();
        try {
            if (!DomainResourceHelper.copyOverProperties(getResourceContext(), objectAdapter, propertiesList)) {
                transactionManager.abortTransaction();
                throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, objectRepr, "Illegal property value");
            }

            final Consent validity = objectAdapter.getSpecification().isValid(objectAdapter);
            if (validity.isVetoed()) {
                transactionManager.abortTransaction();
                throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, objectRepr, validity.getReason());
            }

            transactionManager.endTransaction();
        } finally {
            // in case an exception got thrown somewhere...
            if (!transactionManager.getTransaction().getState().isComplete()) {
                transactionManager.abortTransaction();
            }
        }

        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);
        return helper.objectRepresentation();
    }

    // //////////////////////////////////////////////////////////
    // domain object property
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{oid}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_PROPERTY, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response propertyDetails(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId) {
        init(RepresentationType.OBJECT_PROPERTY, Where.OBJECT_FORMS);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.propertyDetails(objectAdapter, propertyId, MemberMode.NOT_MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    @Override
    @PUT
    @Path("/{oid}/properties/{propertyId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response modifyProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId, final InputStream body) {
        init(Where.OBJECT_FORMS);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        final OneToOneAssociation property = helper.getPropertyThatIsVisibleAndUsable(propertyId, Intent.MUTATE, getResourceContext().getWhere());

        final ObjectSpecification propertySpec = property.getSpecification();
        final String bodyAsString = DomainResourceHelper.asStringUtf8(body);

        final ObjectAdapter argAdapter = helper.parseAsMapWithSingleValue(propertySpec, bodyAsString);

        final Consent consent = property.isAssociationValid(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        property.set(objectAdapter, argAdapter);

        return helper.propertyDetails(objectAdapter, propertyId, MemberMode.MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    @Override
    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response clearProperty(@PathParam("oid") final String oidStr, @PathParam("propertyId") final String propertyId) {
        init(Where.OBJECT_FORMS);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        final OneToOneAssociation property = helper.getPropertyThatIsVisibleAndUsable(propertyId, Intent.MUTATE, getResourceContext().getWhere());

        final Consent consent = property.isAssociationValid(objectAdapter, null);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        property.set(objectAdapter, null);

        return helper.propertyDetails(objectAdapter, propertyId, MemberMode.MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    // //////////////////////////////////////////////////////////
    // domain object collection
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_COLLECTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response accessCollection(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId) {
        init(RepresentationType.OBJECT_COLLECTION, Where.PARENTED_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.collectionDetails(objectAdapter, collectionId, MemberMode.NOT_MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    @Override
    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response addToSet(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId, final InputStream body) {
        init(Where.PARENTED_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = helper.getCollectionThatIsVisibleAndUsable(collectionId, Intent.MUTATE, getResourceContext().getWhere());

        if (!collection.getCollectionSemantics().isSet()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.BAD_REQUEST, "Collection '%s' does not have set semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = DomainResourceHelper.asStringUtf8(body);
        final ObjectAdapter argAdapter = helper.parseAsMapWithSingleValue(collectionSpec, bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter);

        return helper.collectionDetails(objectAdapter, collectionId, MemberMode.MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    @Override
    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response addToList(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId, final InputStream body) {
        init(Where.PARENTED_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = helper.getCollectionThatIsVisibleAndUsable(collectionId, Intent.MUTATE, getResourceContext().getWhere());

        if (!collection.getCollectionSemantics().isListOrArray()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.METHOD_NOT_ALLOWED, "Collection '%s' does not have list or array semantics", collectionId);
        }

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final String bodyAsString = DomainResourceHelper.asStringUtf8(body);
        final ObjectAdapter argAdapter = helper.parseAsMapWithSingleValue(collectionSpec, bodyAsString);

        final Consent consent = collection.isValidToAdd(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.addElement(objectAdapter, argAdapter);

        return helper.collectionDetails(objectAdapter, collectionId, MemberMode.MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    @Override
    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response removeFromCollection(@PathParam("oid") final String oidStr, @PathParam("collectionId") final String collectionId) {
        init(Where.PARENTED_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        final OneToManyAssociation collection = helper.getCollectionThatIsVisibleAndUsable(collectionId, Intent.MUTATE, getResourceContext().getWhere());

        final ObjectSpecification collectionSpec = collection.getSpecification();
        final ObjectAdapter argAdapter = helper.parseAsMapWithSingleValue(collectionSpec, getResourceContext().getQueryString());

        final Consent consent = collection.isValidToRemove(objectAdapter, argAdapter);
        if (consent.isVetoed()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED, consent.getReason());
        }

        collection.removeElement(objectAdapter, argAdapter);

        return helper.collectionDetails(objectAdapter, collectionId, MemberMode.MUTATING, Caching.NONE, getResourceContext().getWhere());
    }

    // //////////////////////////////////////////////////////////
    // domain object action
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_OBJECT_ACTION, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response actionPrompt(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId) {
        init(RepresentationType.OBJECT_ACTION, Where.OBJECT_FORMS);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.actionPrompt(actionId, getResourceContext().getWhere());
    }

    // //////////////////////////////////////////////////////////
    // domain object action invoke
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeActionQueryOnly(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES);

        final JsonRepresentation arguments = getResourceContext().getQueryStringAsJsonRepr();

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.invokeActionQueryOnly(actionId, arguments, getResourceContext().getWhere());
    }

    @Override
    @PUT
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeActionIdempotent(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId, final InputStream arguments) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.invokeActionIdempotent(actionId, arguments, getResourceContext().getWhere());
    }

    @Override
    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Consumes({ MediaType.WILDCARD })
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response invokeAction(@PathParam("oid") final String oidStr, @PathParam("actionId") final String actionId, final InputStream body) {
        init(RepresentationType.ACTION_RESULT, Where.STANDALONE_TABLES);

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final DomainResourceHelper helper = new DomainResourceHelper(getResourceContext(), objectAdapter);

        return helper.invokeAction(actionId, body, getResourceContext().getWhere());
    }

}

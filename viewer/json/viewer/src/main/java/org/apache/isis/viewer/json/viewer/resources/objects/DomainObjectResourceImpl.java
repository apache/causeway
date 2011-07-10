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
package org.apache.isis.viewer.json.viewer.resources.objects;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.resources.DomainObjectResource;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;

@Path("/objects")
public class DomainObjectResourceImpl extends ResourceAbstract implements DomainObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String object(
    		@PathParam("oid") final String oidStr) {

    	final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);

    	final DomainObjectRepBuilder builder = DomainObjectRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter);
        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String propertyDetails(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId) {
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getProperty(objectAdapter, propertyId, Intent.ACCESS);
        
        final PropertyRepBuilder builder = PropertyRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, property);

        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String accessCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId){
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollection(objectAdapter, collectionId, Intent.ACCESS);
        
        final CollectionRepBuilder builder = CollectionRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, collection);

        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String actionPrompt(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId) {

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectAction(objectAdapter, actionId, Intent.ACCESS);
        
        ActionRepBuilder builder = ActionRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, action);

        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object invokeActionIdempotent(
        @PathParam("oid") String oidStr, 
        @PathParam("actionId") String actionId, 
        @QueryParam("argument") List<String> arguments) {

    	final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
    	final ObjectAction action = getObjectAction(objectAdapter, actionId, Intent.ACCESS);
    	
    	if(action.isContributed()) {
    		throw new NotYetImplementedException();
    	}
    	if(action.getParameterCount() > 0) {
    		throw new NotYetImplementedException();
    	}
    	// TODO: check action is idempotent, else throw exception
    	
    	ObjectAdapter[] parameters = new ObjectAdapter[0];
		ObjectAdapter returnedAdapter = action.execute(objectAdapter, parameters);
		if(returnedAdapter == null) {
	        return responseOfOk();
		}
		CollectionFacet facet = returnedAdapter.getSpecification().getFacet(CollectionFacet.class);
		if(facet != null) {
			Collection<ObjectAdapter> collectionAdapters = facet.collection(returnedAdapter);
			return jsonRepresentationOf(collectionAdapters);
		} else {
			return jsonRepresentationOf(returnedAdapter);
		}
    }


    @PUT
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyProperty(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, 
        @FormParam("proposedValue") final String proposedValue) {
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getProperty(objectAdapter, propertyId, Intent.MUTATE);

        return null;
    }

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToSet(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr){
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollection(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
    }

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String clearProperty(
        @PathParam("oid") final String oidStr, 
        @PathParam("propertyId") final String propertyId){
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getProperty(objectAdapter, propertyId, Intent.MUTATE);
        
        return null;
    }

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr){

    	final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
    	final OneToManyAssociation collection = getCollection(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
    }


    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToList(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr){

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollection(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
	}


    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream body){

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectAction(objectAdapter, actionId, Intent.MUTATE);
        
        return null;
    }

    
    /////////////////////////////////////////////////////////////////////

	private enum Intent {
		ACCESS,
		MUTATE;

		public boolean isMutate() {
			return this == MUTATE;
		}
	}

	private OneToOneAssociation getProperty(
			final ObjectAdapter objectAdapter, final String propertyId, final Intent intent) {
		ObjectAssociation association = objectAdapter.getSpecification().getAssociation(propertyId);
        if(association == null || !association.isOneToOneAssociation()) { 
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, property, MemberType.PROPERTY, intent);
	}

	private OneToManyAssociation getCollection(
			final ObjectAdapter objectAdapter, final String collectionId, final Intent intent) {
		ObjectAssociation association = objectAdapter.getSpecification().getAssociation(collectionId);
        if(association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, collection, MemberType.COLLECTION, intent);
	}

	private ObjectAction getObjectAction(final ObjectAdapter objectAdapter,
			final String actionId, Intent intent) {
		ObjectAction action = objectAdapter.getSpecification().getObjectAction(actionId);
		return ensureVisibleAndUsableForIntent(objectAdapter, action, MemberType.ACTION, intent);
	}

	public <T extends ObjectMember> T ensureVisibleAndUsableForIntent(
			final ObjectAdapter objectAdapter, T objectMember, MemberType memberType, Intent intent) {
		String memberId = objectMember.getId();
        if(objectMember.isVisible(getSession(), objectAdapter).isVetoed()) {
            throwNotFoundException(memberId, memberType);
        }
        if(intent.isMutate() && objectMember.isUsable(getSession(), objectAdapter).isVetoed()) {
        	throwPreconditionFailedException(memberId, memberType);
        }
        return objectMember;
	}


    private static void throwNotFoundException(final String memberId, MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw new WebApplicationException(responseOfNotFound(
                memberTypeStr + " '" + memberId + "' either does not exist or is not visible"));
    }

    private static void throwPreconditionFailedException(final String memberId, final MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw new WebApplicationException(responseOfPreconditionFailed(
                memberTypeStr + " is not usable: '" + memberId + "'"));
    }
}

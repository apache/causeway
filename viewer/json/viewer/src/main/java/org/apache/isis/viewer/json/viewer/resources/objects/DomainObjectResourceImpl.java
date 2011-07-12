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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.consent.InteractionInvocationMethod;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.encodeable.EncodableFacet;
import org.apache.isis.core.metamodel.facets.object.value.ValueFacet;
import org.apache.isis.core.metamodel.facets.properties.modify.PropertySetterFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.progmodel.facets.properties.modify.PropertyModifyFacetFactory;
import org.apache.isis.viewer.json.applib.resources.DomainObjectResource;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

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
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(objectAdapter, propertyId, Intent.ACCESS);
        
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
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(objectAdapter, collectionId, Intent.ACCESS);
        
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
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(objectAdapter, actionId, Intent.ACCESS);
        
        ActionRepBuilder builder = ActionRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, action);
        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object invokeActionIdempotent(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId, 
        @QueryParam("arg") final List<String> arguments) {

    	final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
    	final ObjectAction action = getObjectActionThatIsVisibleAndUsable(objectAdapter, actionId, Intent.ACCESS);
    	
    	if(!isIdempotent(action)) {
            throw new WebApplicationException(responseOfMethodNotAllowed(
                     "Method not allowed; action '" + action.getId() + "' is not idempotent"));
    	}
    	int numParameters = action.getParameterCount();
		int numArguments = arguments.size();
		if(numArguments != numParameters) {
            throw new WebApplicationException(responseOfBadRequest(
                     "Action '" + action.getId() + "' has " + numParameters + " parameters but received " + numArguments + " arguments"));
    	}
    	
    	final List<ObjectAdapter> parameters = argumentAdaptersFor(action, arguments);
		return invokeActionUsingAdapters(action, objectAdapter, parameters);
    }

    private boolean isIdempotent(final ObjectAction action) {
    	// TODO: determine whether action is idempotent
		return true;
	}

	private List<ObjectAdapter> argumentAdaptersFor(ObjectAction action, List<String> arguments) {
		List<ObjectActionParameter> parameters = action.getParameters();
    	List<ObjectAdapter> argumentAdapters = Lists.newArrayList(); 
    	for(int i=0; i<parameters.size(); i++) {
    		ObjectActionParameter parameter = parameters.get(i);
    		ObjectSpecification paramSpc = parameter.getSpecification();
    		String argument = arguments.get(i);
			argumentAdapters.add(objectAdapterFor(paramSpc, argument));
    	}
    	
		return argumentAdapters;
	}

	/**
	 * Similar to {@link #objectAdapterFor(ObjectSpecification, Object)}, however the object
	 * being interpreted is a String holding URL encoded JSON (rather than having already been
	 * parsed into a List/Map representation).
	 */
	private ObjectAdapter objectAdapterFor(ObjectSpecification spec, String urlEncodedJson) {
        final String json = UrlDecoderUtils.urlDecode(urlEncodedJson);
        if(spec.containsFacet(EncodableFacet.class)) {
        	EncodableFacet encodableFacet = spec.getFacet(EncodableFacet.class);
        	return encodableFacet.fromEncodedString(json);
        } else {
			@SuppressWarnings("unchecked")
			Map<String,Object> representation = objectMapper.convertValue(json, LinkedHashMap.class);
            return objectAdapterFor(spec, representation);
        }
	}

	
	///////////////////////////////////////////////////////////////////
	// put
	///////////////////////////////////////////////////////////////////
	
	@PUT
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object modifyProperty(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId, 
        @FormParam("arg") final String proposedValue) {

		// TODO: replace @FormParam with body inputstream
		
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(objectAdapter, propertyId, Intent.MUTATE);
        
        ObjectSpecification objectSpec = property.getSpecification();
        
		ObjectAdapter proposedValueAdapter = objectAdapterFor(objectSpec, proposedValue);
		
		Consent consent = property.isAssociationValid(objectAdapter, proposedValueAdapter);
		if(consent.isVetoed()) {
			throw new WebApplicationException(responseOfPreconditionFailed(consent.getReason()));
		}

		PropertySetterFacet setterFacet = property.getFacet(PropertySetterFacet.class);
		setterFacet.setProperty(objectAdapter, proposedValueAdapter);

        return responseOfOk();
    }

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToSet(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("arg") final String proposedValueOidStr){
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
    }

	///////////////////////////////////////////////////////////////////
	// delete
	///////////////////////////////////////////////////////////////////

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String clearProperty(
        @PathParam("oid") final String oidStr, 
        @PathParam("propertyId") final String propertyId){
    	
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToOneAssociation property = getPropertyThatIsVisibleAndUsable(objectAdapter, propertyId, Intent.MUTATE);
        
        return null;
    }

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("arg") final String proposedValueOidStr){

    	final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
    	final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
    }

    
	///////////////////////////////////////////////////////////////////
	// post
	///////////////////////////////////////////////////////////////////

    @POST
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToList(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("arg") final String proposedValueOidStr){

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final OneToManyAssociation collection = getCollectionThatIsVisibleAndUsable(objectAdapter, collectionId, Intent.MUTATE);
        
        return null;
	}


    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public Object invokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream body){

        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        final ObjectAction action = getObjectActionThatIsVisibleAndUsable(objectAdapter, actionId, Intent.MUTATE);

		List<ObjectAdapter> argumentAdapters = parseBody(action, body);
		return invokeActionUsingAdapters(action, objectAdapter, argumentAdapters);
    }

	private List<ObjectAdapter> parseBody(final ObjectAction action, final InputStream body) {
		List<ObjectAdapter> argAdapters = Lists.newArrayList();
		try {
			byte[] byteArray = ByteStreams.toByteArray(body);
			String bodyAsString = new String(byteArray, Charsets.UTF_8);
			
			List<?> arguments = objectMapper.readValue(bodyAsString, ArrayList.class);
			
	    	int numParameters = action.getParameterCount();
			int numArguments = arguments.size();
			if(numArguments != numParameters) {
	            throw new WebApplicationException(responseOfBadRequest(
	                     "Action '" + action.getId() + "' has " + numParameters + " parameters but received " + numArguments + " arguments"));
	    	}

			for(int i=0; i<numParameters; i++) {
				ObjectAdapter argAdapter = toObjectAdapter(action, arguments, i);
				argAdapters.add(argAdapter);
			}
	        return argAdapters;

		} catch (JsonParseException e) {
			throw new WebApplicationException(e, responseOfBadRequest("could not parse body"));
		} catch (JsonMappingException e) {
			throw new WebApplicationException(e, responseOfBadRequest("could not map body to a Map structure"));
		} catch (IOException e) {
			throw new WebApplicationException(e, responseOfBadRequest("could not read body"));
		}
	}

	private ObjectAdapter toObjectAdapter(final ObjectAction action, List<?> arguments, int i) {
		List<ObjectActionParameter> parameters = action.getParameters();

		ObjectSpecification paramSpec = parameters.get(i).getSpecification();
		Object arg = arguments.get(i);
		
		ObjectAdapter objectAdapter = toObjectAdapter(action, i, paramSpec, arg);
		return objectAdapter;
	}

	private ObjectAdapter toObjectAdapter(final ObjectAction action, int i, ObjectSpecification paramSpec, Object arg) {
		try {
			return objectAdapterFor(paramSpec, arg);
		} catch (ExpectedStringRepresentingValueException e) {
			throw new WebApplicationException(responseOfBadRequest("Action '" + action.getId() + "', argument " + i + " should be a URL encoded string representing a value of type " + resourceFor(paramSpec)));
		} catch (ExpectedMapRepresentingReferenceException e) {
			throw new WebApplicationException(responseOfBadRequest("Action '" + action.getId() + "', argument " + i + " should be a map representing a link to reference of type " + resourceFor(paramSpec)));
		}
	}



	/////////////////////////////////////////////////////////////////////
	// helpers
	/////////////////////////////////////////////////////////////////////

	private Object invokeActionUsingAdapters(final ObjectAction action,
			final ObjectAdapter objectAdapter,
			final List<ObjectAdapter> argAdapters) {
		
		List<ObjectActionParameter> parameters = action.getParameters();
		for(int i=0; i<parameters.size(); i++) {
			ObjectActionParameter parameter = parameters.get(i);
			ObjectAdapter paramAdapter = argAdapters.get(i);
			if(paramAdapter.getSpecification().containsFacet(ValueFacet.class)) {
				Object arg = paramAdapter.getObject();
				String reasonNotValid = parameter.isValid(objectAdapter, arg);
				if(reasonNotValid != null) {
					throw new WebApplicationException(responseOfPreconditionFailed(reasonNotValid));
				}
			}
		}
		ObjectAdapter[] argArray = argAdapters.toArray(new ObjectAdapter[0]);
		Consent consent = action.isProposedArgumentSetValid(objectAdapter, argArray);
		if(consent.isVetoed()) {
			throw new WebApplicationException(responseOfPreconditionFailed(consent.getReason()));
		}
		
		final ObjectAdapter returnedAdapter = action.execute(objectAdapter, argArray);
		if(returnedAdapter == null) {
	        return responseOfOk();
		}
		final CollectionFacet facet = returnedAdapter.getSpecification().getFacet(CollectionFacet.class);
		if(facet != null) {
			final Collection<ObjectAdapter> collectionAdapters = facet.collection(returnedAdapter);
			return jsonRepresentationOf(collectionAdapters);
		} else {
			return jsonRepresentationOf(returnedAdapter);
		}
	}


	private static String resourceFor(ObjectSpecification paramSpec) {
		// TODO: should return a string in the form http://localhost:8080/types/xxx
		return paramSpec.getFullIdentifier();
	}

	private enum Intent {
		ACCESS,
		MUTATE;

		public boolean isMutate() {
			return this == MUTATE;
		}
	}

	private OneToOneAssociation getPropertyThatIsVisibleAndUsable(
			final ObjectAdapter objectAdapter, final String propertyId, final Intent intent) {
		ObjectAssociation association = objectAdapter.getSpecification().getAssociation(propertyId);
        if(association == null || !association.isOneToOneAssociation()) { 
            throwNotFoundException(propertyId, MemberType.PROPERTY);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, property, MemberType.PROPERTY, intent);
	}

	private OneToManyAssociation getCollectionThatIsVisibleAndUsable(
			final ObjectAdapter objectAdapter, final String collectionId, final Intent intent) {
		ObjectAssociation association = objectAdapter.getSpecification().getAssociation(collectionId);
        if(association == null || !association.isOneToManyAssociation()) {
            throwNotFoundException(collectionId, MemberType.COLLECTION);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        return ensureVisibleAndUsableForIntent(objectAdapter, collection, MemberType.COLLECTION, intent);
	}

	private ObjectAction getObjectActionThatIsVisibleAndUsable(final ObjectAdapter objectAdapter,
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
		if(intent.isMutate()) {
	        Consent usable = objectMember.isUsable(getSession(), objectAdapter);
			if(usable.isVetoed()) {
				String memberTypeStr = memberType.name().toLowerCase();
				throw new WebApplicationException(responseOfPreconditionFailed(
				        memberTypeStr + " is not usable: '" + memberId + "' (" + usable.getReason() + ")"));
			}
        }
        return objectMember;
	}

    private static void throwNotFoundException(final String memberId, MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw new WebApplicationException(responseOfNotFound(
                memberTypeStr + " '" + memberId + "' either does not exist or is not visible"));
    }

}

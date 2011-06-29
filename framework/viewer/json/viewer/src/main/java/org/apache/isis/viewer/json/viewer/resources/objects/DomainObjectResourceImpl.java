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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.resources.DomainObjectResource;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.util.UrlDecoderUtils;

@Path("/objects")
public class DomainObjectResourceImpl extends ResourceAbstract implements DomainObjectResource {

    @GET
    @Path("/{oid}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String object(@PathParam("oid") final String oidStr) {
        init();
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);

        DomainObjectRepBuilder builder = DomainObjectRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter);
        return jsonRepresentionFrom(builder);
    }

    @GET
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String propertyDetails(
        @PathParam("oid") final String oidStr,
        @PathParam("propertyId") final String propertyId) {

        init();
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        ObjectAssociation association = objectAdapter.getSpecification().getAssociation(propertyId);
        if(association == null || !association.isOneToOneAssociation()) { 
            throwPropertyNotFoundException(propertyId);
        }
        OneToOneAssociation property = (OneToOneAssociation) association;
        PropertyRepBuilder builder = PropertyRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, property);
        if(!builder.isMemberVisible()) {
            throwPropertyNotFoundException(propertyId);
        }

        return jsonRepresentionFrom(builder);
    }

    @PUT
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String modifyProperty(
        @PathParam("oid") final String oidEncodedStr,
        @PathParam("propertyId") final String propertyId, 
        @FormParam("proposedValue") final String proposedValue) {

        init();
        final String oidStr = UrlDecoderUtils.urlDecode(oidEncodedStr);

        return null;
    }

    @DELETE
    @Path("/{oid}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String clearProperty(
        @PathParam("oid") final String oidStr, 
        @PathParam("propertyId") final String propertyId){
        
        return null;
    }

    @GET
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String accessCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId){
        
        init();
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        ObjectAssociation association = objectAdapter.getSpecification().getAssociation(collectionId);
        if(association == null || !association.isOneToManyAssociation()) {
            throwCollectionNotFoundException(collectionId);
        }
        OneToManyAssociation collection = (OneToManyAssociation) association;
        CollectionRepBuilder builder = CollectionRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, collection);
        if(!builder.isMemberVisible()) {
            throwCollectionNotFoundException(collectionId);
        }

        return jsonRepresentionFrom(builder);
    }

    @PUT
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String addToCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr){
        
        return null;
    }

    @DELETE
    @Path("/{oid}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String removeFromCollection(
        @PathParam("oid") final String oidStr,
        @PathParam("collectionId") final String collectionId,
        @FormParam("proposedValue") final String proposedValueOidStr){
        
        return null;
    }

    @GET
    @Path("/{oid}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON })
    public String actionPrompt(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId) {

        init();
        final ObjectAdapter objectAdapter = getObjectAdapter(oidStr);
        ObjectAction action = objectAdapter.getSpecification().getObjectAction(actionId);
        if(action == null) {
            throwActionNotFoundException(actionId);
        }
        ActionRepBuilder builder = ActionRepBuilder.newBuilder(getResourceContext().repContext(), objectAdapter, action);
        if(!builder.isMemberVisible()) {
            throwActionNotFoundException(actionId);
        }

        return jsonRepresentionFrom(builder);
    }

    @POST
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeAction(
        @PathParam("oid") final String oidStr, 
        @PathParam("actionId") final String actionId,
        final InputStream body){

        return null;
    }


    @GET
    @Path("/{oid}/actions/{actionId}/invoke")
    @Produces({ MediaType.APPLICATION_JSON })
    public String invokeActionIdempotent(
        @PathParam("oid") String oidStr, 
        @PathParam("actionId") String actionId, 
        @QueryParam("argument") List<String> arguments) {

        return null;
    }


    
    /////////////////////////////////////////////////////////////////////
    
    private void throwPropertyNotFoundException(final String propertyId) {
        throwNotFoundException(propertyId, MemberType.PROPERTY);
    }

    private void throwCollectionNotFoundException(final String collectionId) {
        throwNotFoundException(collectionId, MemberType.COLLECTION);
    }

    private void throwActionNotFoundException(final String actionId) {
        throwNotFoundException(actionId, MemberType.ACTION);
    }


    private void throwNotFoundException(final String propertyId, MemberType memberType) {
        String memberTypeStr = memberType.name().toLowerCase();
        throw new WebApplicationException(responseOfNotFound("No such " +
                memberTypeStr +
                "/" +
                memberTypeStr +
                " not visible: '" + propertyId + "'"));
    }


}

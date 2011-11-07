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
package org.apache.isis.viewer.json.viewer.resources.domaintypes;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.json.applib.JsonRepresentation;
import org.apache.isis.viewer.json.applib.RepresentationType;
import org.apache.isis.viewer.json.applib.RestfulMediaType;
import org.apache.isis.viewer.json.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.json.applib.blocks.LinkRepresentation;
import org.apache.isis.viewer.json.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.json.viewer.JsonApplicationException;
import org.apache.isis.viewer.json.viewer.representations.RendererFactory;
import org.apache.isis.viewer.json.viewer.resources.ResourceAbstract;
import org.apache.isis.viewer.json.viewer.resources.domainobjects.QueryStringUtil;
import org.apache.isis.viewer.json.viewer.util.UrlParserUtils;

/**
 * Implementation note: it seems to be necessary to annotate the implementation with {@link Path} rather than the
 * interface (at least under RestEasy 1.0.2 and 1.1-RC2).
 */
@Path("/domainTypes")
public class DomainTypeResourceServerside extends ResourceAbstract implements DomainTypeResource {

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPES })
    public Response domainTypes() {
        RepresentationType representationType = RepresentationType.DOMAIN_TYPES;
        init(representationType);

        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);
        
        final DomainTypesReprRenderer renderer = 
                (DomainTypesReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(allSpecifications).includesSelf();
        
        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    
    @GET
    @Path("/{domainType}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public Response domainType(@PathParam("domainType") final String domainType){

        RepresentationType representationType = RepresentationType.DOMAIN_TYPE;
        init(representationType);

        final ObjectSpecification objectSpec = getSpecificationLoader().loadSpecification(domainType);

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);

        final DomainTypeReprRenderer renderer = 
                (DomainTypeReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(objectSpec).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @GET
    @Path("/{domainType}/typeactions/isSubtypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public Response domainTypeIsSubtypeOf(
        @PathParam("domainType") String domainType, 
        @QueryParam("args") final String argumentsQueryString) {
        
        RepresentationType representationType = RepresentationType.DOMAIN_TYPE_IS_SUBTYPE_OF;
        init();

        JsonRepresentation arguments = QueryStringUtil.parseQueryString(argumentsQueryString, "Type action", "isSubtypeOf");

        if(!arguments.isLink("supertype")) {
            throw JsonApplicationException.create(HttpStatusCode.BAD_REQUEST, "Args should contain a link '%s'", "supertype");
        }
        final LinkRepresentation supertypeLink = arguments.getLink("supertype");
        final String supertypeFullName = UrlParserUtils.domainTypeFromLink(supertypeLink);
        
        final ObjectSpecification domainTypeSpec = getSpecificationLoader().loadSpecification(domainType);
        final ObjectSpecification supertypeSpec = getSpecificationLoader().loadSpecification(supertypeFullName);

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);

        final DomainTypeIsSubtypeOfReprRenderer renderer = 
                (DomainTypeIsSubtypeOfReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        final ObjectSpecAndSuperSpec objectSpecAndSuperSpec = new ObjectSpecAndSuperSpec(domainTypeSpec, supertypeSpec);
        renderer.with(objectSpecAndSuperSpec).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_PROPERTY })
    public Response typeProperty(
            @PathParam("domainType") final String domainType,
            @PathParam("propertyId") final String propertyId){
        RepresentationType representationType = RepresentationType.TYPE_PROPERTY;
        init(representationType);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if(parentSpec == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        
        final ObjectMember objectMember = parentSpec.getAssociation(propertyId);
        if(objectMember == null || objectMember.isOneToManyAssociation()) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        OneToOneAssociation property = (OneToOneAssociation) objectMember;

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);
        
        final TypePropertyReprRenderer renderer = 
                (TypePropertyReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndProperty(parentSpec, property)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_COLLECTION })
    public Response typeCollection(
            @PathParam("domainType") final String domainType,
            @PathParam("collectionId") final String collectionId){
        RepresentationType representationType = RepresentationType.TYPE_COLLECTION;
        init(representationType);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if(parentSpec == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        
        final ObjectMember objectMember = parentSpec.getAssociation(collectionId);
        if(objectMember == null || objectMember.isOneToOneAssociation()) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        OneToManyAssociation collection = (OneToManyAssociation) objectMember;

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);
        
        final TypeCollectionReprRenderer renderer = 
                (TypeCollectionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndCollection(parentSpec, collection)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION })
    public Response typeAction(
            @PathParam("domainType") final String domainType,
            @PathParam("actionId") final String actionId){
        RepresentationType representationType = RepresentationType.TYPE_ACTION;
        init(representationType);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if(parentSpec == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        
        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if(objectMember == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        ObjectAction action = (ObjectAction) objectMember;

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);
        
        final TypeActionReprRenderer renderer = 
                (TypeActionReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndAction(parentSpec, action)).includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }

    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramName}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_PARAMETER })
    public Response typeActionParam(
            @PathParam("domainType") final String domainType,
            @PathParam("actionId") final String actionId,
            @PathParam("paramName") final String paramName){
        RepresentationType representationType = RepresentationType.TYPE_ACTION_PARAMETER;
        init(representationType);

        final ObjectSpecification parentSpec = getSpecificationLoader().loadSpecification(domainType);
        if(parentSpec == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        
        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if(objectMember == null) {
            throw JsonApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        ObjectAction parentAction = (ObjectAction) objectMember;
        
        ObjectActionParameter actionParam = parentAction.getParameter(paramName);

        final RendererFactory rendererFactory = 
                rendererFactoryRegistry.find(representationType);
        
        final TypeActionParamReprRenderer renderer = 
                (TypeActionParamReprRenderer) rendererFactory.newRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndActionParam(parentSpec, actionParam))
                .includesSelf();

        return responseOfOk(renderer, Caching.ONE_DAY).build();
    }



}
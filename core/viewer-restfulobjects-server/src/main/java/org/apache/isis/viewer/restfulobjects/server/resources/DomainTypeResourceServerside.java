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
package org.apache.isis.viewer.restfulobjects.server.resources;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.layout.grid.Grid;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.applib.domaintypes.DomainTypeResource;
import org.apache.isis.viewer.restfulobjects.applib.util.UrlEncodingUtils;
import org.apache.isis.viewer.restfulobjects.rendering.Caching;
import org.apache.isis.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ActionDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ActionParameterDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.CollectionDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndActionParam;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.PropertyDescriptionReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.TypeActionResultReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.domaintypes.TypeListReprRenderer;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.server.resources.serialization.SerializationStrategy;
import org.apache.isis.viewer.restfulobjects.server.util.UrlParserUtils;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
@Path("/domain-types")
public class DomainTypeResourceServerside extends ResourceAbstract implements DomainTypeResource {

    @Override
    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_LIST })
    public Response domainTypes() {
        final RepresentationType representationType = RepresentationType.TYPE_LIST;
        init(representationType, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final Collection<ObjectSpecification> allSpecifications = getSpecificationLoader().allSpecifications();

        final TypeListReprRenderer renderer = new TypeListReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(allSpecifications).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public Response domainType(@PathParam("domainType") final String domainType) {

        init(RepresentationType.DOMAIN_TYPE, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));

        final DomainTypeReprRenderer renderer = new DomainTypeReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(objectSpec).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/layout")
    @Produces({
            MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LAYOUT_BS3,
            MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_LAYOUT_BS3
    })
    public Response layout(@PathParam("domainType") final String domainType) {

        init(RepresentationType.LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);
        final SerializationStrategy serializationStrategy = SerializationStrategy.determineFrom(getResourceContext().getAcceptableMediaTypes());

        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        final GridFacet gridFacet = objectSpec.getFacet(GridFacet.class);
        final Response.ResponseBuilder builder;
        if(gridFacet == null) {
            builder = Responses.ofNotFound();
            return builder.build();
        } else {
            Grid grid = gridFacet.getGrid(null);
            builder = Response.status(Response.Status.OK)
                    .entity(serializationStrategy.entity(grid))
                    .type(serializationStrategy.type(RepresentationType.LAYOUT));
        }

        return builder.build();
    }

    @Override
    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION })
    public Response typeProperty(@PathParam("domainType") final String domainType, @PathParam("propertyId") final String propertyId) {
        final RepresentationType representationType = RepresentationType.PROPERTY_DESCRIPTION;
        init(representationType, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectSpecification parentSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getAssociation(propertyId);
        if (objectMember == null || objectMember.isOneToManyAssociation()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final OneToOneAssociation property = (OneToOneAssociation) objectMember;

        final PropertyDescriptionReprRenderer renderer = new PropertyDescriptionReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndProperty(parentSpec, property)).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION })
    public Response typeCollection(@PathParam("domainType") final String domainType, @PathParam("collectionId") final String collectionId) {
        final RepresentationType representationType = RepresentationType.COLLECTION_DESCRIPTION;
        init(representationType, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectSpecification parentSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getAssociation(collectionId);
        if (objectMember == null || objectMember.isOneToOneAssociation()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final OneToManyAssociation collection = (OneToManyAssociation) objectMember;

        final CollectionDescriptionReprRenderer renderer = new CollectionDescriptionReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndCollection(parentSpec, collection)).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION })
    public Response typeAction(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId) {
        final RepresentationType representationType = RepresentationType.ACTION_DESCRIPTION;
        init(representationType, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectSpecification parentSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if (objectMember == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final ObjectAction action = (ObjectAction) objectMember;

        final ActionDescriptionReprRenderer renderer = new ActionDescriptionReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndAction(parentSpec, action)).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramName}")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION })
    public Response typeActionParam(@PathParam("domainType") final String domainType, @PathParam("actionId") final String actionId, @PathParam("paramName") final String paramName) {
        final RepresentationType representationType = RepresentationType.ACTION_PARAMETER_DESCRIPTION;
        init(representationType, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final ObjectSpecification parentSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        if (parentSpec == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }

        final ObjectMember objectMember = parentSpec.getObjectAction(actionId);
        if (objectMember == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND);
        }
        final ObjectAction parentAction = (ObjectAction) objectMember;

        final ObjectActionParameter actionParam = parentAction.getParameterByName(paramName);

        final ActionParameterDescriptionReprRenderer renderer = new ActionParameterDescriptionReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndActionParam(parentSpec, actionParam)).includesSelf();

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    // //////////////////////////////////////////////////////////
    // domain type actions
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/type-actions/isSubtypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSubtypeOf(
            @PathParam("domainType") final String domainType, 
            @QueryParam("supertype") final String superTypeStr, // simple style
            @QueryParam("args") final String argsUrlEncoded // formal style
            ) {
        init(Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final String supertype = domainTypeFor(superTypeStr, argsUrlEncoded, "supertype");

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        final ObjectSpecification supertypeSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(supertype));

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSubtypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(getResourceContext(), "supertype", supertypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = domainTypeSpec.isOfType(supertypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }


    @Override
    @GET
    @Path("/{domainType}/type-actions/isSupertypeOf/invoke")
    @Produces({ MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT, RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSupertypeOf(
            @PathParam("domainType") final String domainType, 
            @QueryParam("subtype") final String subTypeStr, // simple style
            @QueryParam("args") final String argsUrlEncoded // formal style
            ) {

        init(Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        final String subtype = domainTypeFor(subTypeStr, argsUrlEncoded, "subtype");

        final ObjectSpecification domainTypeSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(domainType));
        final ObjectSpecification subtypeSpec = getSpecificationLoader().lookupBySpecId(ObjectSpecId.of(subtype));

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(getResourceContext(), null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSupertypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(getResourceContext(), Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(getResourceContext(), "subtype", subtypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = subtypeSpec.isOfType(domainTypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return Responses.ofOk(renderer, Caching.ONE_DAY).build();
    }

    private static String domainTypeFor(
            final String domainTypeStr,
            final String argsAsUrlEncodedQueryString,
            final String argsParamName) {

        // simple style; simple return
        if (!Strings.isNullOrEmpty(domainTypeStr)) {
            return domainTypeStr;
        }


        // formal style; must parse from args that has a link with an href to the domain type
        final String argsAsQueryString = UrlEncodingUtils.urlDecode(argsAsUrlEncodedQueryString);
        final String href = linkFromFormalArgs(argsAsQueryString, argsParamName);
        return UrlParserUtils.domainTypeFrom(href);
    }

    private static String linkFromFormalArgs(final String argumentsAsQueryString, final String paramName) {
        final JsonRepresentation arguments = Util.readQueryStringAsMap(argumentsAsQueryString);
        if (!arguments.isLink(paramName)) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST, "Args should contain a link '%s'", paramName);
        }

        return arguments.getLink(paramName).getHref();
    }

}
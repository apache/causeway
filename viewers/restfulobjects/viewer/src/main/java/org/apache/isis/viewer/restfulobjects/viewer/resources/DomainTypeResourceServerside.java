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

import java.util.function.UnaryOperator;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.iactnlayer.InteractionLayerTracker;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.facets.object.grid.GridFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.Rel;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulMediaType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
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
import org.apache.isis.viewer.restfulobjects.viewer.util.UrlParserUtils;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Implementation note: it seems to be necessary to annotate the implementation
 * with {@link Path} rather than the interface (at least under RestEasy 1.0.2
 * and 1.1-RC2).
 */
@Component
@Path("/domain-types")
@Log4j2
public class DomainTypeResourceServerside
extends ResourceAbstract
implements DomainTypeResource {

    @Inject
    public DomainTypeResourceServerside(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration,
            final InteractionLayerTracker iInteractionLayerTracker) {
        super(metaModelContext, isisConfiguration, iInteractionLayerTracker);
        log.debug("<init>");
    }

    @Override
    @GET
    @Path("/")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_TYPE_LIST })
    public Response domainTypes() {

        val resourceContext = createResourceContext(
                RepresentationType.TYPE_LIST, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val domainTypeSpecifications = getSpecificationLoader().snapshotSpecifications()
                .filter(spec->spec.isEntityOrViewModel()); // concrete types only, no abstract types

        final TypeListReprRenderer renderer =
                new TypeListReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(domainTypeSpecifications).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/",
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    @GET
    @Path("/{domainType}")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_DOMAIN_TYPE })
    public Response domainType(
            @PathParam("domainType") final String domainType) {

        val resourceContext = createResourceContext(
                RepresentationType.DOMAIN_TYPE, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val objectSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);

        val renderer = new DomainTypeReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(objectSpec).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}", domainType,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    @GET
    @Path("/{domainType}/layout")
    @Produces({
        MediaType.APPLICATION_JSON, RestfulMediaType.APPLICATION_JSON_LAYOUT_BS3,
        MediaType.APPLICATION_XML, RestfulMediaType.APPLICATION_XML_LAYOUT_BS3
    })
    public Response layout(
            @PathParam("domainType") final String domainType) {

        val resourceContext = createResourceContext(
                RepresentationType.LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val serializationStrategy = resourceContext.getSerializationStrategy();

        val responseBuilder = getSpecificationLoader().specForLogicalTypeName(domainType)
                .map(spec -> spec.getFacet(GridFacet.class))
                .map(gridFacet -> gridFacet.getGrid(null))
                .map(grid ->
                        Response.status(Response.Status.OK)
                                .entity(serializationStrategy.entity(grid))
                                .type(serializationStrategy.type(RepresentationType.LAYOUT)))
                .orElse(Responses.ofNotFound());

        return _EndpointLogging.response(log, "GET({}) /domain-types/{}/layout", serializationStrategy.name(), domainType,
                responseBuilder.build());
    }

    @Override
    @GET
    @Path("/{domainType}/properties/{propertyId}")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_PROPERTY_DESCRIPTION })
    public Response typeProperty(
            @PathParam("domainType") final String domainType,
            @PathParam("propertyId") final String propertyId) {

        val resourceContext = createResourceContext(
                RepresentationType.PROPERTY_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        val objectMember = parentSpec.getAssociation(propertyId)
                .orElseThrow(()->
                    _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                        RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND)));

        if (objectMember.isOneToManyAssociation()) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }
        final OneToOneAssociation property = (OneToOneAssociation) objectMember;

        final PropertyDescriptionReprRenderer renderer = new PropertyDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndProperty(parentSpec, property)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    @GET
    @Path("/{domainType}/collections/{collectionId}")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_COLLECTION_DESCRIPTION })
    public Response typeCollection(
            @PathParam("domainType") final String domainType,
            @PathParam("collectionId") final String collectionId) {

        val resourceContext = createResourceContext(
                RepresentationType.COLLECTION_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        val objectMember = parentSpec.getAssociation(collectionId)
                .orElseThrow(()->
                    _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                            RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND)));

        if (objectMember.isOneToOneAssociation()) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }
        final OneToManyAssociation collection = (OneToManyAssociation) objectMember;

        final CollectionDescriptionReprRenderer renderer = new CollectionDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndCollection(parentSpec, collection)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_ACTION_DESCRIPTION })
    public Response typeAction(
            @PathParam("domainType") final String domainType,
            @PathParam("actionId") final String actionId) {

        val resourceContext = createResourceContext(
                RepresentationType.ACTION_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        val action = parentSpec.getAction(actionId)
                .orElseThrow(()->_EndpointLogging.error(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
                        RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND)));

        final ActionDescriptionReprRenderer renderer = new ActionDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndAction(parentSpec, action)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    @Override
    @GET
    @Path("/{domainType}/actions/{actionId}/params/{paramName}")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_ACTION_PARAMETER_DESCRIPTION })
    public Response typeActionParam(
            @PathParam("domainType") final String domainType,
            @PathParam("actionId") final String actionId,
            @PathParam("paramName") final String paramName) {

        val resourceContext = createResourceContext(
                RepresentationType.ACTION_PARAMETER_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        val parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramName,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        val parentAction = parentSpec.getAction(actionId)
                .orElseThrow(()->_EndpointLogging.error(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramName,
                        RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND)));

        final ObjectActionParameter actionParam = parentAction.getParameterByName(paramName);

        final ActionParameterDescriptionReprRenderer renderer = new ActionParameterDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndActionParam(parentSpec, actionParam)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramName,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    // //////////////////////////////////////////////////////////
    // domain type actions
    // //////////////////////////////////////////////////////////

    @Override
    @GET
    @Path("/{domainType}/type-actions/isSubtypeOf/invoke")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT,
        RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSubtypeOf(
            @PathParam("domainType") final String domainType,
            @QueryParam("supertype") final String superTypeStr, // simple style
            @QueryParam("args") final String argsUrlEncoded // formal style
            ) {

        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE));

        final String supertype = domainTypeFor(superTypeStr, argsUrlEncoded, "supertype",
                roEx->_EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType, roEx));

        val domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        val supertypeSpec = getSpecificationLoader().specForLogicalTypeName(supertype).orElse(null);
        if (domainTypeSpec == null
                || supertypeSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(resourceContext, null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSubtypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(resourceContext, "supertype", supertypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = domainTypeSpec.isOfType(supertypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return _EndpointLogging.response(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }


    @Override
    @GET
    @Path("/{domainType}/type-actions/isSupertypeOf/invoke")
    @Produces({
        MediaType.APPLICATION_JSON,
        RestfulMediaType.APPLICATION_JSON_TYPE_ACTION_RESULT,
        RestfulMediaType.APPLICATION_JSON_ERROR })
    public Response domainTypeIsSupertypeOf(
            @PathParam("domainType") final String domainType,
            @QueryParam("subtype") final String subTypeStr, // simple style
            @QueryParam("args") final String argsUrlEncoded // formal style
            ) {

        val resourceContext = createResourceContext(
                ResourceDescriptor.generic(Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE));

        final String subtype = domainTypeFor(subTypeStr, argsUrlEncoded, "subtype",
                roEx->_EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType, roEx));

        val domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        val subtypeSpec = getSpecificationLoader().specForLogicalTypeName(subtype).orElse(null);
        if (domainTypeSpec == null
                || subtypeSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType,
                    RestfulObjectsApplicationException.create(HttpStatusCode.NOT_FOUND));
        }

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(resourceContext, null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSupertypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(resourceContext, "subtype", subtypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = subtypeSpec.isOfType(domainTypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return _EndpointLogging.response(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType,
                Responses.ofOk(renderer, Caching.ONE_DAY).build());
    }

    private static String domainTypeFor(
            final String domainTypeStr,
            final String argsAsUrlEncodedQueryString,
            final String argsParamName,
            final @NonNull UnaryOperator<RestfulObjectsApplicationException> onRoException) {

        // simple style; simple return
        if (!_Strings.isNullOrEmpty(domainTypeStr)) {
            return domainTypeStr;
        }

        // formal style; must parse from args that has a link with an href to the domain type
        final String argsAsQueryString = UrlEncodingUtils.urlDecode(argsAsUrlEncodedQueryString);
        final String href = linkFromFormalArgs(argsAsQueryString, argsParamName, onRoException);
        return UrlParserUtils.domainTypeFrom(href);
    }

    private static String linkFromFormalArgs(
            final String argumentsAsQueryString,
            final String paramName,
            final @NonNull UnaryOperator<RestfulObjectsApplicationException> onRoException) {
        final JsonRepresentation arguments = Util.readQueryStringAsMap(argumentsAsQueryString);
        if (!arguments.isLink(paramName)) {
            throw onRoException.apply(RestfulObjectsApplicationException
                    .createWithMessage(HttpStatusCode.BAD_REQUEST, "Args should contain a link '%s'", paramName));
        }

        return arguments.getLink(paramName).getHref();
    }

}

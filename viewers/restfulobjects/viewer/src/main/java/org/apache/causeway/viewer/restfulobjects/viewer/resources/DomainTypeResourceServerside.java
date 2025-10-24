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

import java.util.function.UnaryOperator;

import org.jspecify.annotations.NonNull;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.core.metamodel.facets.object.grid.GridFacet.GridVariant;
import org.apache.causeway.core.metamodel.spec.feature.ObjectActionParameter;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.Rel;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.domaintypes.DomainTypeResource;
import org.apache.causeway.viewer.restfulobjects.rendering.Caching;
import org.apache.causeway.viewer.restfulobjects.rendering.LinkBuilder;
import org.apache.causeway.viewer.restfulobjects.rendering.ResponseFactory;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.rendering.context.ResourceDescriptor.ResourceLink;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ActionDescriptionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ActionParameterDescriptionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.CollectionDescriptionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.DomainTypeReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndAction;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndActionParam;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndCollection;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.ParentSpecAndProperty;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.PropertyDescriptionReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.TypeActionResultReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.domaintypes.TypeListReprRenderer;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;
import org.apache.causeway.viewer.restfulobjects.viewer.util.UrlParserUtils;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class DomainTypeResourceServerside
extends ResourceAbstract
implements DomainTypeResource {

    public DomainTypeResourceServerside() {
        super();
        log.debug("<init>");
    }

    @Override
    public ResponseEntity<Object> domainTypes() {

        var resourceContext = createResourceContext(
                RepresentationType.TYPE_LIST, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var domainTypeSpecifications = getSpecificationLoader().snapshotSpecifications()
                .filter(spec->spec.isEntityOrViewModel()); // concrete types only, no abstract types

        final TypeListReprRenderer renderer = new TypeListReprRenderer(resourceContext, null, JsonRepresentation.newMap())
            .with(domainTypeSpecifications).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/",
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> domainType(final String domainType) {

        var resourceContext = createResourceContext(
                RepresentationType.DOMAIN_TYPE, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var objectSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if(objectSpec==null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}", domainType,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        var renderer = new DomainTypeReprRenderer(resourceContext, null, JsonRepresentation.newMap())
            .with(objectSpec).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}", domainType,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> layout(final String domainType) {

        var resourceContext = createResourceContext(
                RepresentationType.LAYOUT, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var serializationStrategy = resourceContext.getSerializationStrategy();

        var response = getSpecificationLoader().specForLogicalTypeName(domainType)
                .map(mo->Facets.bootstrapGrid(GridVariant.NORMALIZED, mo))
                .map(grid ->
                    responseFactory.ok(serializationStrategy.entity(grid),
                                serializationStrategy.type(RepresentationType.LAYOUT)))
                .orElseGet(ResponseFactory::notFound);

        return _EndpointLogging.response(log, "GET({}) /domain-types/{}/layout", serializationStrategy.name(), domainType,
                response);
    }

    @Override
    public ResponseEntity<Object> typeProperty(final String domainType, final String propertyId) {

        var resourceContext = createResourceContext(
                RepresentationType.PROPERTY_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        var objectMember = parentSpec.getAssociation(propertyId)
                .orElseThrow(()->
                    _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                        RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND)));

        if (objectMember.isOneToManyAssociation()) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }
        final OneToOneAssociation property = (OneToOneAssociation) objectMember;

        final PropertyDescriptionReprRenderer renderer = new PropertyDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndProperty(parentSpec, property)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/properties/{}", domainType, propertyId,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> typeCollection(final String domainType, final String collectionId) {

        var resourceContext = createResourceContext(
                RepresentationType.COLLECTION_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        var objectMember = parentSpec.getAssociation(collectionId)
                .orElseThrow(()->
                    _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                            RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND)));

        if (objectMember.isOneToOneAssociation()) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }
        final OneToManyAssociation collection = (OneToManyAssociation) objectMember;

        final CollectionDescriptionReprRenderer renderer = new CollectionDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndCollection(parentSpec, collection)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/collections/{}", domainType, collectionId,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> typeAction(final String domainType, final String actionId) {

        var resourceContext = createResourceContext(
                RepresentationType.ACTION_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        var action = parentSpec.getAction(actionId)
                .orElseThrow(()->_EndpointLogging.error(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
                        RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND)));

        final ActionDescriptionReprRenderer renderer = new ActionDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndAction(parentSpec, action)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/actions/{}", domainType, actionId,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> typeActionParam(final String domainType, final String actionId, final String paramId) {

        var resourceContext = createResourceContext(
                RepresentationType.ACTION_PARAMETER_DESCRIPTION, Where.ANYWHERE, RepresentationService.Intent.NOT_APPLICABLE);

        var parentSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        if (parentSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramId,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        var parentAction = parentSpec.getAction(actionId)
                .orElseThrow(()->_EndpointLogging.error(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramId,
                        RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND)));

        final ObjectActionParameter actionParam = parentAction.getParameterById(paramId);

        final ActionParameterDescriptionReprRenderer renderer = new ActionParameterDescriptionReprRenderer(resourceContext, null, JsonRepresentation.newMap());
        renderer.with(new ParentSpecAndActionParam(parentSpec, actionParam)).includesSelf();

        return _EndpointLogging.response(log, "GET /domain-types/{}/actions/{}/params/{}", domainType, actionId, paramId,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    // -- DOMAIN TYPE ACTIONS

    @Override
    public ResponseEntity<Object> domainTypeIsSubtypeOf(
            final String domainType,
            final String superTypeStr, // simple style
            final String argsUrlEncoded // formal style
            ) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
                RepresentationType.GENERIC, Where.ANYWHERE,
                RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.NONE));

        final String supertype = domainTypeFor(superTypeStr, argsUrlEncoded, "supertype",
                roEx->_EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType, roEx));

        var domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        var supertypeSpec = getSpecificationLoader().specForLogicalTypeName(supertype).orElse(null);
        if (domainTypeSpec == null
                || supertypeSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(resourceContext, null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSubtypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(resourceContext, "supertype", supertypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = domainTypeSpec.isOfType(supertypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return _EndpointLogging.response(log, "GET /domain-types/{}/type-actions/isSubtypeOf/invoke", domainType,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    @Override
    public ResponseEntity<Object> domainTypeIsSupertypeOf(
            final String domainType,
            final String subTypeStr, // simple style
            final String argsUrlEncoded // formal style
            ) {

        var resourceContext = createResourceContext(new ResourceDescriptor(
            RepresentationType.GENERIC, Where.ANYWHERE,
            RepresentationService.Intent.NOT_APPLICABLE, ResourceLink.NONE));

        final String subtype = domainTypeFor(subTypeStr, argsUrlEncoded, "subtype",
                roEx->_EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType, roEx));

        var domainTypeSpec = getSpecificationLoader().specForLogicalTypeName(domainType).orElse(null);
        var subtypeSpec = getSpecificationLoader().specForLogicalTypeName(subtype).orElse(null);
        if (domainTypeSpec == null
                || subtypeSpec == null) {
            throw _EndpointLogging.error(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType,
                    RestfulObjectsApplicationException.create(HttpStatus.NOT_FOUND));
        }

        final TypeActionResultReprRenderer renderer = new TypeActionResultReprRenderer(resourceContext, null, JsonRepresentation.newMap());

        final String url = "domain-types/" + domainType + "/type-actions/isSupertypeOf/invoke";
        final LinkBuilder linkBuilder = LinkBuilder.newBuilder(resourceContext, Rel.SELF.getName(), RepresentationType.TYPE_ACTION_RESULT, url);
        final JsonRepresentation arguments = DomainTypeReprRenderer.argumentsTo(resourceContext, "subtype", subtypeSpec);
        final JsonRepresentation selfLink = linkBuilder.withArguments(arguments).build();

        final boolean value = subtypeSpec.isOfType(domainTypeSpec);
        renderer.with(domainTypeSpec).withSelf(selfLink).withValue(value);

        return _EndpointLogging.response(log, "GET /domain-types/{}/type-actions/isSupertypeOf/invoke", domainType,
            responseFactory.ok(renderer, Caching.ONE_DAY));
    }

    private static String domainTypeFor(
            final String domainTypeStr,
            final String argsAsUrlEncodedQueryString,
            final String argsParamId,
            final @NonNull UnaryOperator<RestfulObjectsApplicationException> onRoException) {

        // simple style; simple return
        if (!_Strings.isNullOrEmpty(domainTypeStr)) {
            return domainTypeStr;
        }

        // formal style; must parse from args that has a link with an href to the domain type
        var requestParams = RequestParams.ofQueryString(UrlUtils.urlDecodeUtf8(argsAsUrlEncodedQueryString));
        final String href = linkFromFormalArgs(requestParams, argsParamId, onRoException);
        return UrlParserUtils.domainTypeFrom(href);
    }

    private static String linkFromFormalArgs(
            final RequestParams requestParams,
            final String paramId,
            final @NonNull UnaryOperator<RestfulObjectsApplicationException> onRoException) {
        final JsonRepresentation arguments = requestParams.asMap();
        if (!arguments.isLink(paramId)) {
            throw onRoException.apply(RestfulObjectsApplicationException
                    .createWithMessage(HttpStatus.BAD_REQUEST, "Args should contain a link '%s'".formatted(paramId)));
        }

        return arguments.getLink(paramId).getHref();
    }

}

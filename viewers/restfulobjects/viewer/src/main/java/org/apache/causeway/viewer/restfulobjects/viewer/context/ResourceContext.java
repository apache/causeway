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
package org.apache.causeway.viewer.restfulobjects.viewer.context;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.id.LogicalType;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.commons.io.UrlUtils;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulRequest.DomainModel;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulRequest.RequestParameter;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.DomainServiceLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService.Intent;
import org.apache.causeway.viewer.restfulobjects.rendering.util.RequestParams;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

public record ResourceContext(
    MetaModelContext metaModelContext,
    ResourceDescriptor resourceDescriptor,
    HttpHeaders httpHeaders,
    HttpServletRequest httpServletRequest,
    HttpServletResponse httpServletResponse,
    String applicationAbsoluteBase,
    String restfulAbsoluteBase,

    List<List<String>> followLinks,
    boolean isValidateOnly,

    InteractionInitiatedBy interactionInitiatedBy,

    JsonRepresentation queryStringAsJsonRepr,
    ObjectAdapterLinkTo objectAdapterLinkTo,
    Set<Bookmark> rendered
)
implements IResourceContext {

    // -- NON CANONICAL CONSTRUCTORS

    public ResourceContext(
            final MetaModelContext metaModelContext,
            final ResourceDescriptor resourceDescriptor,
            final String applicationAbsoluteBase,
            final String restfulAbsoluteBase,
            final RequestParams urlUnencodedQueryString,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Map<String, String[]> requestParams) {

        this(
            metaModelContext,
            resourceDescriptor,
            applicationAbsoluteBase, restfulAbsoluteBase,
            httpServletRequest, httpServletResponse, interactionInitiatedBy,
            requestArgsAsMap(requestParams, urlUnencodedQueryString));
    }

    private ResourceContext(
            final MetaModelContext metaModelContext,
            final ResourceDescriptor resourceDescriptor,
            final String applicationAbsoluteBase,
            final String restfulAbsoluteBase,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final InteractionInitiatedBy interactionInitiatedBy,
            final JsonRepresentation requestArgsAsMap) {

        this(metaModelContext, resourceDescriptor,
            httpHeadersFromServletRequest(httpServletRequest),
            httpServletRequest, httpServletResponse,
            _Strings.suffix(applicationAbsoluteBase, "/"),
            _Strings.suffix(restfulAbsoluteBase, "/"),
            Collections.unmodifiableList(arg(requestArgsAsMap, RequestParameter.FOLLOW_LINKS)),
            arg(requestArgsAsMap, RequestParameter.VALIDATE_ONLY),
            interactionInitiatedBy,
            requestArgsAsMap,
            switch(resourceDescriptor.resourceLink()) {
                case NONE -> null;
                case OBJECT -> new DomainObjectLinkTo();
                case SERVICE -> new DomainServiceLinkTo();
            },
            new HashSet<>());

        ensureDomainModelQueryParamSupported();
    }

    @Override public Where where() {
        return resourceDescriptor().where();
    }

    /**
     * Only applies to rendering of objects
     */
    @Override public Intent intent() {
        return resourceDescriptor().intent();
    }

    private void ensureDomainModelQueryParamSupported() {
        final DomainModel domainModel = arg(queryStringAsJsonRepr(), RequestParameter.DOMAIN_MODEL);
        if(domainModel != DomainModel.FORMAL) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST,
                    "x-ro-domain-model of '%s' is not supported", domainModel);
        }
    }

    private static JsonRepresentation requestArgsAsMap(final Map<String, String[]> params, RequestParams urlUnencodedQueryString) {
        if(simpleQueryArgs(params)) {
            // try to process regular params and build up JSON repr
            final JsonRepresentation map = JsonRepresentation.newMap();
            for(String paramId: params.keySet()) {
                String paramValue = params.get(paramId)[0];
                // this is rather hacky :-(
                final String key = paramId.startsWith("x-ro") ? paramId : paramId + ".value";

                // test whether we can parse as an int
                var parseResult = _Ints.parseInt(paramValue, 10);
                if(parseResult.isPresent()) {
                    map.mapPutInt(key, parseResult.getAsInt());
                } else {
                    map.mapPutString(key, stripQuotes(paramValue));
                }
            }
            return map;
        } else {
            return Optional.ofNullable(urlUnencodedQueryString)
                .orElseGet(RequestParams::ofEmptyQueryString)
                .asMap();
        }
    }

    static String stripQuotes(final String str) {
        if(_Strings.isNullOrEmpty(str)) {
            return str;
        }
        if(str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.lastIndexOf("\""));
        }
        return str;
    }

    private static boolean simpleQueryArgs(final Map<String, String[]> params) {
        if(params==null || params.isEmpty()) {
            return false;
        }
        for(String paramId: params.keySet()) {
            if("x-causeway-querystring".equals(paramId) || paramId.startsWith("{")) {
                return false;
            }
        }
        return true;
    }

    static <Q> Q arg(final JsonRepresentation queryStringAsJsonRepr, final RequestParameter<Q> requestParameter) {
        return requestParameter.valueOf(queryStringAsJsonRepr);
    }

    public SerializationStrategy getSerializationStrategy() {
        return SerializationStrategy.determineFrom(acceptableMediaTypes());
    }

    @Override
    public boolean canEagerlyRender(final ManagedObject objectAdapter) {
        return ManagedObjects.bookmark(objectAdapter)
        .map(rendered::add)
        .orElse(true);
    }

    @Override
    public String restfulUrlFor(final @NonNull String url) {
        return restfulAbsoluteBase + url;
    }

    @Override
    public String applicationUrlFor(final @NonNull String url) {
        return applicationAbsoluteBase + (
                url.startsWith("/")
                ? url.substring(1)
                : url);
    }

    @Override
    public List<MediaType> acceptableMediaTypes() {
        return httpHeaders.getAccept();
    }

    // -- UTIL

    public ManagedObject lookupServiceAdapterElseFail(
            final @Nullable String serviceIdOrAlias) {

        final ManagedObject serviceAdapter = getSpecificationLoader()
                .lookupLogicalType(serviceIdOrAlias)
                .map(LogicalType::logicalName)
                .map(this::lookupServiceAdapterById)
                .orElse(null);

        if(serviceAdapter==null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND,
                    "Could not locate service '%s'", serviceIdOrAlias);
        }
        return serviceAdapter;
    }

    // -- JUNIT

    public static ResourceContext forTesting(String queryString, HttpServletRequest servletRequest) {
        return new ResourceContext(MetaModelContext.instanceNullable(),
            ResourceDescriptor.empty(), null, null,
            RequestParams.ofQueryString(UrlUtils.urlDecodeUtf8(queryString)),
            servletRequest, null,
            null, (Map<String, String[]>)null);
    }

    public static ResourceContext forTesting(
            ResourceDescriptor resourceDescriptor,
            HttpServletRequest httpServletRequest,
            HttpHeaders httpHeaders) {
        throw _Exceptions.notImplemented();
    }

    // -- HELPER

    static HttpHeaders httpHeadersFromServletRequest(HttpServletRequest request) {
        var headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }
        return headers;
    }



}

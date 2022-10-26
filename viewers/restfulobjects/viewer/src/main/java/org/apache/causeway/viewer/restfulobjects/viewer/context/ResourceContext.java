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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Providers;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.collections._Sets;
import org.apache.causeway.commons.internal.primitives._Ints;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.causeway.viewer.restfulobjects.applib.RepresentationType;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulRequest.DomainModel;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulRequest.RequestParameter;
import org.apache.causeway.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.causeway.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.causeway.viewer.restfulobjects.rendering.util.Util;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.ResourceDescriptor;
import org.apache.causeway.viewer.restfulobjects.viewer.resources.serialization.SerializationStrategy;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;

public class ResourceContext
implements IResourceContext {

    @Getter(onMethod_={@Override})
    private MetaModelContext metaModelContext;

    @Getter private final HttpHeaders httpHeaders;
    @Getter private final Request request;
    @Getter private final HttpServletRequest httpServletRequest;
    @Getter private final HttpServletResponse httpServletResponse;
    @Getter private final SecurityContext securityContext;
    private final String applicationAbsoluteBase;
    private final String restfulAbsoluteBase;

    @Getter private List<List<String>> followLinks;
    @Getter private boolean validateOnly;

    private final Where where;
    private final RepresentationService.Intent intent;
    @Getter private final InteractionInitiatedBy interactionInitiatedBy;
    private final String urlUnencodedQueryString;
    private final JsonRepresentation readQueryStringAsMap;

    // -- constructor and init

    public ResourceContext(
            final ResourceDescriptor resourceDescriptor,
            final HttpHeaders httpHeaders,
            final Providers providers,
            final Request request,
            final String applicationAbsoluteBase,
            final String restfulAbsoluteBase,
            final String urlUnencodedQueryStringIfAny,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final SecurityContext securityContext,
            final MetaModelContext metaModelContext,
            final InteractionInitiatedBy interactionInitiatedBy,
            final Map<String, String[]> requestParams) {

        this.metaModelContext = metaModelContext;

        this.httpHeaders = httpHeaders;
        //not used ... this.providers = providers;
        this.request = request;
        this.where = resourceDescriptor.getWhere();
        this.intent = resourceDescriptor.getIntent();
        this.urlUnencodedQueryString = urlUnencodedQueryStringIfAny;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.securityContext = securityContext;
        this.interactionInitiatedBy = interactionInitiatedBy;

        this.applicationAbsoluteBase = _Strings.suffix(applicationAbsoluteBase, "/");
        this.restfulAbsoluteBase = _Strings.suffix(restfulAbsoluteBase, "/");

        this.readQueryStringAsMap = requestArgsAsMap(requestParams);

        init(resourceDescriptor.getRepresentationType());
    }


    void init(final RepresentationType representationType) {

        // previously we checked for compatible accept headers here.
        // now, though, this is a responsibility of the various ContentNegotiationService implementations
        ensureDomainModelQueryParamSupported();

        this.followLinks = Collections.unmodifiableList(getArg(RequestParameter.FOLLOW_LINKS));
        this.validateOnly = getArg(RequestParameter.VALIDATE_ONLY);
    }

    private void ensureDomainModelQueryParamSupported() {
        final DomainModel domainModel = getArg(RequestParameter.DOMAIN_MODEL);
        if(domainModel != DomainModel.FORMAL) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.BAD_REQUEST,
                    "x-ro-domain-model of '%s' is not supported", domainModel);
        }
    }

    /**
     * Note that this can return non-null for all HTTP methods; will be either the
     * query string (GET, DELETE) or read out of the input stream (PUT, POST).
     */
    public String getUrlUnencodedQueryString() {
        return urlUnencodedQueryString;
    }

    public JsonRepresentation getQueryStringAsJsonRepr() {
        return readQueryStringAsMap;
    }

    protected JsonRepresentation requestArgsAsMap(final Map<String, String[]> params) {

        if(simpleQueryArgs(params)) {
            // try to process regular params and build up JSON repr
            final JsonRepresentation map = JsonRepresentation.newMap();
            for(String paramName: params.keySet()) {
                String paramValue = params.get(paramName)[0];
                // this is rather hacky :-(
                final String key = paramName.startsWith("x-ro") ? paramName : paramName + ".value";

                // test whether we can parse as an int
                val parseResult = _Ints.parseInt(paramValue, 10);
                if(parseResult.isPresent()) {
                    map.mapPutInt(key, parseResult.getAsInt());
                } else {
                    map.mapPutString(key, stripQuotes(paramValue));
                }

            }
            return map;
        } else {
            final String queryString = getUrlUnencodedQueryString();
            return Util.readQueryStringAsMap(queryString);
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
        for(String paramName: params.keySet()) {
            if("x-causeway-querystring".equals(paramName) || paramName.startsWith("{")) {
                return false;
            }
        }
        return true;
    }


    public <Q> Q getArg(final RequestParameter<Q> requestParameter) {
        final JsonRepresentation queryStringJsonRepr = getQueryStringAsJsonRepr();
        return requestParameter.valueOf(queryStringJsonRepr);
    }

    @Override
    public Where getWhere() {
        return where;
    }

    /**
     * Only applies to rendering of objects
     */
    @Override
    public RepresentationService.Intent getIntent() {
        return intent;
    }

    public SerializationStrategy getSerializationStrategy() {
        return SerializationStrategy.determineFrom(getAcceptableMediaTypes());
    }

    // -- canEagerlyRender
    private Set<Bookmark> rendered = _Sets.newHashSet();

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
    public List<MediaType> getAcceptableMediaTypes() {
        return httpHeaders.getAcceptableMediaTypes();
    }


    @Getter(onMethod = @__(@Override))
    @Setter //(onMethod = @__(@Override))
    private ObjectAdapterLinkTo objectAdapterLinkTo;

}

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
package org.apache.isis.viewer.restfulobjects.server;

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
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.runtime.system.context.session.RuntimeContextBase;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.DomainModel;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulRequest.RequestParameter;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;

import lombok.Getter;

public class ResourceContext extends RuntimeContextBase implements RendererContext {

    @Getter private final HttpHeaders httpHeaders;
    @Getter private final UriInfo uriInfo;
    @Getter private final Request request;
    //not used ... private final Providers providers;
    @Getter private final HttpServletRequest httpServletRequest;
    @Getter private final HttpServletResponse httpServletResponse;
    @Getter private final SecurityContext securityContext;

    @Getter private List<List<String>> followLinks;
    @Getter private boolean validateOnly;

    private final Where where;
    private final RepresentationService.Intent intent;
    @Getter private final InteractionInitiatedBy interactionInitiatedBy;
    private final String urlUnencodedQueryString;

    private JsonRepresentation readQueryStringAsMap;

    // -- constructor and init

    public ResourceContext(
            final RepresentationType representationType,
            final HttpHeaders httpHeaders,
            final Providers providers,
            final UriInfo uriInfo,
            final Request request,
            final Where where,
            final RepresentationService.Intent intent,
            final String urlUnencodedQueryStringIfAny,
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final SecurityContext securityContext,
            final InteractionInitiatedBy interactionInitiatedBy) {

        super();

        this.httpHeaders = httpHeaders;
        //not used ... this.providers = providers;
        this.uriInfo = uriInfo;
        this.request = request;
        this.where = where;
        this.intent = intent;
        this.urlUnencodedQueryString = urlUnencodedQueryStringIfAny;
        this.httpServletRequest = httpServletRequest;
        this.httpServletResponse = httpServletResponse;
        this.securityContext = securityContext;
        this.interactionInitiatedBy = interactionInitiatedBy;

        init(representationType);
    }


    void init(final RepresentationType representationType) {
        getQueryStringAsJsonRepr(); // force it to be cached

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

        if (readQueryStringAsMap == null) {
            readQueryStringAsMap = requestArgsAsMap();
        }
        return readQueryStringAsMap;
    }

    protected JsonRepresentation requestArgsAsMap() {
        final Map<String,String[]> params = httpServletRequest.getParameterMap();

        if(simpleQueryArgs(params)) {
            // try to process regular params and build up JSON repr
            final JsonRepresentation map = JsonRepresentation.newMap();
            for(String paramName: params.keySet()) {
                String paramValue = params.get(paramName)[0];
                // this is rather hacky :-(
                final String key = paramName.startsWith("x-ro") ? paramName : paramName + ".value";
                try {
                    // and this is even more hacky :-(
                    int paramValueAsInt = Integer.parseInt(paramValue);
                    map.mapPut(key, paramValueAsInt);
                } catch(Exception ex) {
                    map.mapPut(key, stripQuotes(paramValue));
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

    private static boolean simpleQueryArgs(Map<String, String[]> params) {
        if(params.isEmpty()) {
            return false;
        }
        for(String paramName: params.keySet()) {
            if("x-isis-querystring".equals(paramName) || paramName.startsWith("{")) {
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
     * @return
     */
    @Override
    public RepresentationService.Intent getIntent() {
        return intent;
    }

    // -- canEagerlyRender
    private Set<Oid> rendered = _Sets.newHashSet();
    @Override
    public boolean canEagerlyRender(ObjectAdapter objectAdapter) {
        final Oid oid = objectAdapter.getOid();
        return rendered.add(oid);
    }


    // -- configuration settings

    private static final boolean HONOR_UI_HINTS_DEFAULT = false;

    private static final boolean OBJECT_PROPERTY_VALUES_ONLY_DEFAULT = false;

    private static final boolean SUPPRESS_DESCRIBED_BY_LINKS_DEFAULT = false;
    private static final boolean SUPPRESS_UPDATE_LINK_DEFAULT = false;
    private static final boolean SUPPRESS_MEMBER_ID_DEFAULT = false;
    private static final boolean SUPPRESS_MEMBER_LINKS_DEFAULT = false;
    private static final boolean SUPPRESS_MEMBER_EXTENSIONS_DEFAULT = false;
    private static final boolean SUPPRESS_MEMBER_DISABLED_REASON_DEFAULT = false;

    @Override
    public boolean honorUiHints() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.honorUiHints", HONOR_UI_HINTS_DEFAULT);
    }

    @Override
    public boolean objectPropertyValuesOnly() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.objectPropertyValuesOnly", OBJECT_PROPERTY_VALUES_ONLY_DEFAULT);
    }

    @Override
    public boolean suppressDescribedByLinks() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressDescribedByLinks", SUPPRESS_DESCRIBED_BY_LINKS_DEFAULT);
    }

    @Override
    public boolean suppressUpdateLink() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressUpdateLink", SUPPRESS_UPDATE_LINK_DEFAULT);
    }

    @Override
    public boolean suppressMemberId() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressMemberId", SUPPRESS_MEMBER_ID_DEFAULT);
    }

    @Override
    public boolean suppressMemberLinks() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressMemberLinks", SUPPRESS_MEMBER_LINKS_DEFAULT);
    }

    @Override
    public boolean suppressMemberExtensions() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressMemberExtensions", SUPPRESS_MEMBER_EXTENSIONS_DEFAULT);
    }

    @Override
    public boolean suppressMemberDisabledReason() {
        return getConfiguration().getBoolean("isis.viewer.restfulobjects.suppressMemberDisabledReason", SUPPRESS_MEMBER_DISABLED_REASON_DEFAULT);
    }

    @Override
    public String urlFor(final String url) {
        return getUriInfo().getBaseUri().toString() + url;
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return httpHeaders.getAcceptableMediaTypes();
    }

}

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

import java.io.InputStream;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.*;
import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.profiles.Localization;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.url.UrlEncodingUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.util.OidUtils;
import org.apache.isis.viewer.restfulobjects.server.util.UrlDecoderUtils;

public abstract class ResourceAbstract {

    @Context
    HttpHeaders httpHeaders;

    @Context
    UriInfo uriInfo;

    @Context
    Request request;

    @Context
    HttpServletRequest httpServletRequest;

    @Context
    HttpServletResponse httpServletResponse;

    @Context
    SecurityContext securityContext;

    private ResourceContext resourceContext;

    protected void init(final Where where) {
        init(RepresentationType.GENERIC, where);
    }

    protected void init(final RepresentationType representationType, Where where) {
        String queryStringIfAny = getUrlDecodedQueryStringIfAny();
        init(representationType, where, queryStringIfAny);
    }

    private String getUrlDecodedQueryStringIfAny() {
        final String queryStringIfAny = httpServletRequest.getQueryString();
        return UrlEncodingUtils.urlDecodeNullSafe(queryStringIfAny);
    }

    protected void init(
            final RepresentationType representationType,
            final Where where,
            final InputStream arguments) {
        final String urlDecodedQueryString = Util.asStringUtf8(arguments);
        init(representationType, where, urlDecodedQueryString);
    }

    protected void init(
            final RepresentationType representationType,
            final Where where,
            final String urlUnencodedQueryString) {
        if (!IsisContext.inSession()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }
        if (getAuthenticationSession() == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }

        this.resourceContext = new ResourceContext(
                representationType, httpHeaders, uriInfo, request, where, urlUnencodedQueryString, httpServletRequest, httpServletResponse,
                securityContext, getLocalization(), getAuthenticationSession(), getPersistenceSession(), getAdapterManager(), getSpecificationLoader(), getConfiguration());
    }

    protected ResourceContext getResourceContext() {
        return resourceContext;
    }

    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ObjectAdapter getObjectAdapterElseThrowNotFound(String domainType, final String instanceId) {
        ObjectAdapter objectAdapter = getObjectAdapterElseNull(domainType, instanceId);

        if (objectAdapter == null) {
            final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceId);
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, "could not determine adapter for OID: '%s:%s'", domainType, instanceIdUnencoded);
        }
        return objectAdapter;
    }

    protected ObjectAdapter getObjectAdapterElseNull(String domainType, final String instanceId) {
        return OidUtils.getObjectAdapterElseNull(resourceContext, domainType, instanceId);
    }

    protected ObjectAdapter getServiceAdapter(final String serviceId) {
        final List<ObjectAdapter> serviceAdapters = getPersistenceSession().getServices();
        for (final ObjectAdapter serviceAdapter : serviceAdapters) {
            final Object servicePojo = serviceAdapter.getObject();
            final String id = ServiceUtil.id(servicePojo);
            if (serviceId.equals(id)) {
                return serviceAdapter;
            }
        }
        throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, "Could not locate service '%s'", serviceId);
    }


    // //////////////////////////////////////////////////////////////
    // Dependencies (from singletons)
    // //////////////////////////////////////////////////////////////

    protected IsisConfiguration getConfiguration () {
        return IsisContext.getConfiguration();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected Localization getLocalization() {
        return IsisContext.getLocalization();
    }

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (injected via @Context)
    // //////////////////////////////////////////////////////////////

    protected HttpServletRequest getServletRequest() {
        return getResourceContext().getHttpServletRequest();
    }

}

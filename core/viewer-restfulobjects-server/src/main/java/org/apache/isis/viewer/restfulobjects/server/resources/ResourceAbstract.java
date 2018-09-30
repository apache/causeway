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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.url.UrlDecoderUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.deployment.DeploymentCategory;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.authentication.AuthenticationManager;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.util.OidUtils;

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

    @Context
    Providers providers;

    private ResourceContext resourceContext;

    protected void init(
            final Where where,
            final RepresentationService.Intent intent) {
        init(RepresentationType.GENERIC, where, intent);
    }

    protected void init(
            final RepresentationType representationType,
            Where where,
            final RepresentationService.Intent intent) {
        String queryStringIfAny = getUrlDecodedQueryStringIfAny();
        init(representationType, where, intent, queryStringIfAny);
    }

    private String getUrlDecodedQueryStringIfAny() {
        final String queryStringIfAny = httpServletRequest.getQueryString();
        return UrlDecoderUtil.urlDecodeNullSafe(queryStringIfAny);
    }

    protected void init(
            final RepresentationType representationType,
            final Where where,
            final RepresentationService.Intent intent,
            final InputStream arguments) {
        final String urlDecodedQueryString = Util.asStringUtf8(arguments);
        init(representationType, where, intent, urlDecodedQueryString);
    }

    protected void init(
            final RepresentationType representationType,
            final Where where,
            final RepresentationService.Intent intent,
            final String urlUnencodedQueryString) {
        if (!getIsisSessionFactory().inSession()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }
        if (getAuthenticationSession() == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }

        this.resourceContext = new ResourceContext(
                representationType, httpHeaders, providers, uriInfo, request, where, intent, urlUnencodedQueryString, httpServletRequest, httpServletResponse,
                securityContext,
                InteractionInitiatedBy.USER);
    }

    protected ResourceContext getResourceContext() {
        return resourceContext;
    }


    protected void setCommandExecutor(Command.Executor executor) {
        getServicesInjector().lookupServiceElseFail(CommandContext.class).getCommand().internal().setExecutor(executor);
    }

    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ObjectAdapter getObjectAdapterElseThrowNotFound(String domainType, final String instanceId) {
        ObjectAdapter objectAdapter = getObjectAdapterElseNull(domainType, instanceId);

        if (objectAdapter == null) {
            final String instanceIdUnencoded = org.apache.isis.viewer.restfulobjects.server.util.UrlDecoderUtils.urlDecode(instanceId);
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
            final Object servicePojo = serviceAdapter.getPojo();
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

    protected DeploymentCategory getDeploymentCategory() {
        return getIsisSessionFactory().getDeploymentCategory();
    }

    protected IsisConfiguration getConfiguration () {
        return getIsisSessionFactory().getConfiguration();
    }

    protected ServicesInjector getServicesInjector () {
        return getIsisSessionFactory().getServicesInjector();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    protected AuthenticationManager getAuthenticationManager() {
        return getIsisSessionFactory().getAuthenticationManager();
    }

    protected SpecificationLoader getSpecificationLoader() {
        return getIsisSessionFactory().getSpecificationLoader();
    }

    protected PersistenceSession getPersistenceSession() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    protected IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }

    // //////////////////////////////////////////////////////////////
    // Dependencies (injected via @Context)
    // //////////////////////////////////////////////////////////////

    protected HttpServletRequest getServletRequest() {
        return getResourceContext().getHttpServletRequest();
    }

}

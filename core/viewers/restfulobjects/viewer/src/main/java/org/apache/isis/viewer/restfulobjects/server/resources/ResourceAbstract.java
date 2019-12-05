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
import org.apache.isis.commons.internal.url.UrlDecoderUtil;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.specloader.SpecificationLoader;
import org.apache.isis.runtime.system.context.IsisContext;
import org.apache.isis.runtime.system.session.IsisSession;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.client.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.server.context.ResourceContext;

import lombok.val;

public abstract class ResourceAbstract {

    @Context HttpHeaders httpHeaders;
    @Context UriInfo uriInfo;
    @Context Request request;
    @Context HttpServletRequest httpServletRequest;
    @Context HttpServletResponse httpServletResponse;
    @Context SecurityContext securityContext;
    @Context Providers providers;

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
        if (!IsisSession.isInSession()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }
        if (IsisContext.getCurrentAuthenticationSession() == null) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }

        this.resourceContext = new ResourceContext(
                representationType, 
                httpHeaders, providers, uriInfo, request, 
                where, intent, urlUnencodedQueryString, 
                httpServletRequest, httpServletResponse,
                securityContext,
                InteractionInitiatedBy.USER);
    }

    protected ResourceContext getResourceContext() {
        return resourceContext;
    }


    protected void setCommandExecutor(Command.Executor executor) {
        resourceContext.getServiceRegistry()
        .lookupServiceElseFail(CommandContext.class).getCommand().internal().setExecutor(executor);
    }

    // //////////////////////////////////////////////////////////////
    // Isis integration
    // //////////////////////////////////////////////////////////////

    protected ManagedObject getObjectAdapterElseThrowNotFound(String domainType, final String instanceId) {
        ManagedObject objectAdapter = getObjectAdapterElseNull(domainType, instanceId);

        if (objectAdapter == null) {
            final String instanceIdUnencoded = org.apache.isis.viewer.restfulobjects.rendering.UrlDecoderUtils.urlDecode(instanceId);
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, 
                    "Could not determine adapter for OID: '%s:%s'", domainType, instanceIdUnencoded);
        }
        return objectAdapter;
    }

    protected ManagedObject getObjectAdapterElseNull(String domainType, final String instanceId) {
        return resourceContext.getObjectAdapterElseNull(domainType, instanceId);
    }

    protected ManagedObject getServiceAdapter(final String serviceId) {

        val metaModelContext = resourceContext.getServiceRegistry()
                .lookupServiceElseFail(MetaModelContext.class);

        final ManagedObject serviceAdapter = metaModelContext.lookupServiceAdapterById(serviceId);
        if(serviceAdapter==null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, 
                    "Could not locate service '%s'", serviceId);    
        }
        return serviceAdapter;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return resourceContext.getSpecificationLoader();
    }

}

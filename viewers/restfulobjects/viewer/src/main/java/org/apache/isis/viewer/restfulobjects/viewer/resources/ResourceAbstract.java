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

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import org.apache.isis.applib.annotation.Where;
import org.apache.isis.commons.internal.codec._UrlDecoderUtil;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.iactn.IsisInteractionTracker;
import org.apache.isis.viewer.restfulobjects.applib.RepresentationType;
import org.apache.isis.viewer.restfulobjects.applib.RestfulResponse.HttpStatusCode;
import org.apache.isis.viewer.restfulobjects.rendering.RestfulObjectsApplicationException;
import org.apache.isis.viewer.restfulobjects.rendering.UrlDecoderUtils;
import org.apache.isis.viewer.restfulobjects.rendering.service.RepresentationService;
import org.apache.isis.viewer.restfulobjects.rendering.util.Util;
import org.apache.isis.viewer.restfulobjects.viewer.context.ResourceContext;

import lombok.val;

public abstract class ResourceAbstract {

    protected final MetaModelContext metaModelContext;
    protected final IsisConfiguration isisConfiguration;
    protected final IsisInteractionTracker isisInteractionTracker;

    @Context HttpHeaders httpHeaders;
    @Context UriInfo uriInfo;
    @Context Request request;
    @Context HttpServletRequest httpServletRequest;
    @Context HttpServletResponse httpServletResponse;
    @Context SecurityContext securityContext;
    @Context Providers providers;

    @Inject
    protected ResourceAbstract(
            final MetaModelContext metaModelContext,
            final IsisConfiguration isisConfiguration,
            final IsisInteractionTracker isisInteractionTracker) {
        
        this.metaModelContext = metaModelContext;
        this.isisConfiguration = isisConfiguration;
        this.isisInteractionTracker = isisInteractionTracker;
    }
    
    // -- FACTORIES

    protected ResourceContext createResourceContext(
            final RepresentationType representationType,
            final Where where,
            final RepresentationService.Intent intent) {
        
        return createResourceContext(ResourceDescriptor.of(representationType, where, intent));
    }
    
    protected ResourceContext createResourceContext(final ResourceDescriptor resourceDescriptor) {
        String queryStringIfAny = getUrlDecodedQueryStringIfAny();
        return createResourceContext(resourceDescriptor, queryStringIfAny);
    }

    protected ResourceContext createResourceContext(
            final ResourceDescriptor resourceDescriptor,
            final InputStream arguments) {
        
        final String urlDecodedQueryString = Util.asStringUtf8(arguments);
        return createResourceContext(resourceDescriptor, urlDecodedQueryString);
    }

    protected ResourceContext createResourceContext(
            final ResourceDescriptor resourceDescriptor,
            final String urlUnencodedQueryString) {
        
        if (!isisInteractionTracker.isInInteractionSession()) {
            throw RestfulObjectsApplicationException.create(HttpStatusCode.UNAUTHORIZED);
        }

        final String baseUri = isisConfiguration.getViewer().getRestfulobjects().getBaseUri()
                                    .orElse(uriInfo.getBaseUri().toString());

        return resourceContext(
                resourceDescriptor, baseUri, urlUnencodedQueryString, httpServletRequest.getParameterMap());
    }

    public ResourceContext resourceContextForTesting(
            final ResourceDescriptor resourceDescriptor, 
            final Map<String, String[]> requestParams) {
        
        return resourceContext(
                resourceDescriptor, "/restful", /*urlUnencodedQueryString*/ null, requestParams);
    }

    // -- ISIS INTEGRATION

    protected ManagedObject getObjectAdapterElseThrowNotFound(String domainType, final String instanceIdEncoded) {
        final String instanceIdUnencoded = UrlDecoderUtils.urlDecode(instanceIdEncoded);
        final String oidStrUnencoded = Oid.marshaller().joinAsOid(domainType, instanceIdUnencoded);
        val rootOid = RootOid.deString(oidStrUnencoded);
        
        return Optional.ofNullable(rootOid.loadObject(getSpecificationLoader()))
                .orElseThrow(()->RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, 
                        "Could not determine adapter for OID: '%s:%s'", domainType, instanceIdUnencoded));
    }

    protected ManagedObject getServiceAdapter(final String serviceId) {
        final ManagedObject serviceAdapter = metaModelContext.lookupServiceAdapterById(serviceId);
        if(serviceAdapter==null) {
            throw RestfulObjectsApplicationException.createWithMessage(HttpStatusCode.NOT_FOUND, 
                    "Could not locate service '%s'", serviceId);    
        }
        return serviceAdapter;
    }

    protected SpecificationLoader getSpecificationLoader() {
        return metaModelContext.getSpecificationLoader();
    }
    
    // -- HELPER 
    
    private String getUrlDecodedQueryStringIfAny() {
        final String queryStringIfAny = httpServletRequest.getQueryString();
        return _UrlDecoderUtil.urlDecodeNullSafe(queryStringIfAny);
    }
    
    private ResourceContext resourceContext(
            final ResourceDescriptor resourceDescriptor,
            final String baseUri,
            final String urlUnencodedQueryString, 
            final Map<String, String[]> requestParams) {
        
        return new ResourceContext(
                resourceDescriptor, 
                httpHeaders, providers, baseUri, request,
                urlUnencodedQueryString, 
                httpServletRequest, httpServletResponse,
                securityContext,
                metaModelContext, 
                InteractionInitiatedBy.USER,
                requestParams);
    }

}

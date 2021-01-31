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
package org.apache.isis.viewer.restfulobjects.rendering.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Service
@Named("isis.viewer.ro.RepresentationServiceContentNegotiator")
@Order(OrderPrecedence.EARLY)
@Primary
@Qualifier("ContentNegotiator")
@Log4j2
public class RepresentationServiceContentNegotiator implements RepresentationService {

    @Inject private List<ContentNegotiationService> contentNegotiationServices;
    
    @Override
    public Response objectRepresentation(
            final IResourceContext renderContext,
            final ManagedObject objectAdapter) {

        final ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(renderContext, objectAdapter));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    @Override
    public Response propertyDetails(
            final IResourceContext renderContext,
            final ManagedProperty objectAndProperty) {

        final ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(renderContext, objectAndProperty));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }


    @Override
    public Response collectionDetails(
            final IResourceContext renderContext,
            final ManagedCollection objectAndCollection) {

        final ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(renderContext, objectAndCollection));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }




    @Override
    public Response actionPrompt(
            final IResourceContext renderContext,
            final ManagedAction objectAndAction) {

        final ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(renderContext, objectAndAction));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }


    @Override
    public Response actionResult(
            final IResourceContext renderContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(renderContext, objectAndActionInvocation));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    void assertContentNegotiationServiceHandled(final ResponseBuilder responseBuilder) {
        if (responseBuilder == null) {
            throw new IllegalStateException("Could not locate " + ContentNegotiationService.class.getSimpleName() + " to handle request");
        }
    }

    /**
     * Iterates over all {@link #contentNegotiationServices injected} {@link ContentNegotiationService}s to find one
     * that returns a {@link ResponseBuilder} using the provided function.
     *
     * <p>
     *     There will always be at least one such service, namely the
     *     {@link ContentNegotiationServiceForRestfulObjectsV1_0}.
     * </p>
     *
     * @param connegServiceBuildResponse - the function to ask of the {@link ContentNegotiationService}.
     */
    ResponseBuilder buildResponse(
            Function<ContentNegotiationService, ResponseBuilder> connegServiceBuildResponse) {
        
        log.debug("ContentNegotiationServices:\n{}", ()->contentNegotiationServices.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(s->" - "+s)
                .collect(Collectors.joining("\n")));
        
        for (val contentNegotiationService : contentNegotiationServices) {
            val responseBuilder = connegServiceBuildResponse.apply(contentNegotiationService);
            if(responseBuilder != null) {
                
                log.debug("--> winner: {}", ()->contentNegotiationService.getClass().getSimpleName());
                return responseBuilder;
            }
        }
        return null;
    }

    /**
     * Override to allow further customization.
     */
    protected Response buildResponse(final ResponseBuilder responseBuilder) {
        return responseBuilder.build();
    }

    
}

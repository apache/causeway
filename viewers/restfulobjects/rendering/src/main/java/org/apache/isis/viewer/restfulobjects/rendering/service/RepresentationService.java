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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
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

/**
 * Configures the Restful Objects viewer to emit custom representations (rather than the
 * standard representations defined in the RO spec).
 *
 * <p>
 *     The default implementations ultimately generate representations according
 *     to the <a href="http://restfulobjects.org">Restful Objects spec</a> v1.0.
 *     It does this through a level of abstraction by delegating to
 *     implementations of the
 *     {@link org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService}
 *     SPI.  This provides a mechanism for altering representations according
 *     to the HTTP `Accept` header.
 * </p>
 *
 * <p>
 * This interface is EXPERIMENTAL and may change in the future.
 * </p>
 *
 * @since 1.x {@index}
 */
@Service
@Named("isis.viewer.ro.RepresentationService")
@Priority(PriorityPrecedence.EARLY)
@Log4j2
public class RepresentationService {

    private final List<ContentNegotiationService> contentNegotiationServices;

    @Inject
    public RepresentationService(List<ContentNegotiationService> contentNegotiationServices) {
        this.contentNegotiationServices = contentNegotiationServices;
    }

    /**
     * As returned by {@link IResourceContext#getIntent()}, applies only to the representation of
     * domain objects.
     */
    public enum Intent {
        /**
         * object just created, ie return a 201
         */
        JUST_CREATED,
        /**
         * object already persistent, ie return a 200
         */
        ALREADY_PERSISTENT,
        /**
         * representation is not of a domain object, so does not apply.
         */
        NOT_APPLICABLE
    }


    /**
     * Returns a representation of a single object.
     *
     * <p>
     *     By default this representation is as per section 14.4 of the
     *     RO spec, v1.0.
     * </p>
     */
    public Response objectRepresentation(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        final Response.ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAdapter));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    /**
     * Returns a representation of a single property of an object.
     *
     * <p>
     *     By default this representation is as per section 16.4 of the
     *     RO spec, v1.0.
     * </p>
     */
    public Response propertyDetails(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty) {

        final Response.ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndProperty));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    /**
     * Returns a representation of a single collection of an object.
     *
     * <p>
     *     By default this representation is as per section 17.5 of the
     *     RO spec, v1.0.
     * </p>
     */
    public Response collectionDetails(
            final IResourceContext resourceContext,
            final ManagedCollection objectAndCollection) {

        final Response.ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndCollection));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    /**
     * Returns a representation of a single action (prompt) of an object.
     *
     * <p>
     *     By default this representation is as per section 18.2 of the
     *     RO spec, v1.0.
     * </p>
     */
    public Response actionPrompt(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction) {

        final Response.ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndAction));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    /**
     * Returns a representation of a single action invocation of an object.
     *
     * <p>
     *     By default this representation is as per section 19.5 of the
     *     RO spec, v1.0.
     * </p>
     */
    public Response actionResult(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final Response.ResponseBuilder responseBuilder = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndActionInvocation));

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    void assertContentNegotiationServiceHandled(final Response.ResponseBuilder responseBuilder) {
        if (responseBuilder == null) {
            throw new IllegalStateException("Could not locate " + ContentNegotiationService.class.getSimpleName() + " to handle request");
        }
    }

    /**
     * Iterates over all {@link #contentNegotiationServices injected} {@link ContentNegotiationService}s to find one
     * that returns a {@link Response.ResponseBuilder} using the provided function.
     *
     * <p>
     *     There will always be at least one such service, namely the
     *     {@link ContentNegotiationServiceForRestfulObjectsV1_0}.
     * </p>
     *
     * @param connegServiceBuildResponse - the function to ask of the {@link ContentNegotiationService}.
     */
    Response.ResponseBuilder buildResponse(
            Function<ContentNegotiationService, Response.ResponseBuilder> connegServiceBuildResponse) {

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
    protected Response buildResponse(final Response.ResponseBuilder responseBuilder) {
        return responseBuilder.build();
    }

}

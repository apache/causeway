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
package org.apache.causeway.viewer.restfulobjects.rendering.service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.causeway.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService;
import org.apache.causeway.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;

import lombok.extern.slf4j.Slf4j;

/**
 * Configures the Restful Objects viewer to emit custom representations (rather than the
 * standard representations defined in the RO spec).
 *
 * <p>The default implementations ultimately generate representations according
 * to the <a href="http://restfulobjects.org">Restful Objects spec</a> v1.0.
 * It does this through a level of abstraction by delegating to
 * implementations of the
 * {@link org.apache.causeway.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService}
 * SPI. This provides a mechanism for altering representations according
 * to the HTTP `Accept` header.
 *
 * <p>This interface is EXPERIMENTAL and may change in the future.
 *
 * @since 1.x revised for 4.0 {@index}
 */
@Service
@Named(CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".RepresentationService")
@Priority(PriorityPrecedence.EARLY)
@Slf4j
public class RepresentationService {

    private final List<ContentNegotiationService> contentNegotiationServices;

    @Inject
    public RepresentationService(final List<ContentNegotiationService> contentNegotiationServices) {
        this.contentNegotiationServices = contentNegotiationServices;
    }

    /**
     * As returned by {@link IResourceContext#intent()}, applies only to the representation of
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
     * @apiNote By default this representation is as per section 14.4 of the RO spec, v1.0.
     */
    public ResponseEntity<Object> objectRepresentation(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {

        final var response = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAdapter));

        return assertContentNegotiationServiceHandled(response);
    }

    /**
     * Returns a representation of a single property of an object.
     *
     * @apiNote By default this representation is as per section 16.4 of theRO spec, v1.0.
     */
    public ResponseEntity<Object> propertyDetails(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty) {

        final var response = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndProperty));

        return assertContentNegotiationServiceHandled(response);
    }

    /**
     * Returns a representation of a single collection of an object.
     *
     * @apiNote By default this representation is as per section 17.5 of the RO spec, v1.0.
     */
    public ResponseEntity<Object> collectionDetails(
            final IResourceContext resourceContext,
            final ManagedCollection objectAndCollection) {

        final var response = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndCollection));

        return assertContentNegotiationServiceHandled(response);
    }

    /**
     * Returns a representation of a single action (prompt) of an object.
     *
     * @apiNote By default this representation is as per section 18.2 of the RO spec, v1.0.
     */
    public ResponseEntity<Object> actionPrompt(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction) {

        final var response = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndAction));

        return assertContentNegotiationServiceHandled(response);
    }

    /**
     * Returns a representation of a single action invocation of an object.
     *
     * @apiNote By default this representation is as per section 19.5 of the RO spec, v1.0.
     */
    public ResponseEntity<Object> actionResult(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {

        final var response = buildResponse(
                connegService -> connegService.buildResponse(resourceContext, objectAndActionInvocation));

        return assertContentNegotiationServiceHandled(response);
    }

    <T> ResponseEntity<T> assertContentNegotiationServiceHandled(final ResponseEntity<T> responseBuilder) {
        if (responseBuilder == null) {
            throw _Exceptions.illegalState("Could not locate %s to handle request",
                ContentNegotiationService.class.getSimpleName());
        }
        return responseBuilder;
    }

    /**
     * Iterates over all {@link #contentNegotiationServices injected} {@link ContentNegotiationService}s to find one
     * that returns a {@link Response.ResponseBuilder} using the provided function.
     *
     * <p>There will always be at least one such service, namely the
     * {@link ContentNegotiationServiceForRestfulObjectsV1_0}.
     *
     * @param connegServiceBuildResponse - the function to ask of the {@link ContentNegotiationService}.
     */
    ResponseEntity<Object> buildResponse(
            final Function<ContentNegotiationService,  ResponseEntity<Object>> connegServiceBuildResponse) {

        if(log.isDebugEnabled()) {
            log.debug("ContentNegotiationServices:\n{}", contentNegotiationServices.stream()
                .map(Object::getClass)
                .map(Class::getSimpleName)
                .map(s->" - "+s)
                .collect(Collectors.joining("\n")));
        }

        for (var contentNegotiationService : contentNegotiationServices) {
            var responseBuilder = connegServiceBuildResponse.apply(contentNegotiationService);
            if(responseBuilder != null) {
                if(log.isDebugEnabled()) {
                    log.debug("--> winner: {}", contentNegotiationService.getClass().getSimpleName());
                }
                return responseBuilder;
            }
        }
        return null;
    }

}

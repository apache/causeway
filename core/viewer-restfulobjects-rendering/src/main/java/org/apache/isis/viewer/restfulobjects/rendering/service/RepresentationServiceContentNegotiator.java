/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.restfulobjects.rendering.service;

import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ActionResultReprRenderer.SelfLink;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.MemberReprMode;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndAction;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndCollection;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndProperty;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationService;
import org.apache.isis.viewer.restfulobjects.rendering.service.conneg.ContentNegotiationServiceForRestfulObjectsV1_0;

@Singleton
public class RepresentationServiceContentNegotiator implements RepresentationService {


    @PostConstruct
    public void init() {
    }


    @Override
    @Programmatic
    public Response objectRepresentation(
            final Context renderContext,
            final ObjectAdapter objectAdapter) {

        final ResponseBuilder responseBuilder = buildResponse(new Function<ContentNegotiationService, ResponseBuilder>() {
            @Override
            public ResponseBuilder apply(final ContentNegotiationService connegService) {
                return connegService.buildResponse(renderContext, objectAdapter);
            }
        });

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }

    @Override
    @Programmatic
    public Response propertyDetails(
            final Context renderContext,
            final ObjectAndProperty objectAndProperty,
            final MemberReprMode memberReprMode) {

        final ResponseBuilder responseBuilder = buildResponse(new Function<ContentNegotiationService, ResponseBuilder>() {
            @Override
            public ResponseBuilder apply(final ContentNegotiationService connegService) {
                return connegService.buildResponse(renderContext, objectAndProperty);
            }
        });

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }


    @Override
    @Programmatic
    public Response collectionDetails(
            final Context renderContext,
            final ObjectAndCollection objectAndCollection,
            final MemberReprMode memberReprMode) {

        final ResponseBuilder responseBuilder = buildResponse(new Function<ContentNegotiationService, ResponseBuilder>() {
            @Override
            public ResponseBuilder apply(final ContentNegotiationService connegService) {
                return connegService.buildResponse(renderContext, objectAndCollection);
            }
        });

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }




    @Override
    @Programmatic
    public Response actionPrompt(
            final Context renderContext,
            final ObjectAndAction objectAndAction) {

        final ResponseBuilder responseBuilder = buildResponse(new Function<ContentNegotiationService, ResponseBuilder>() {
            @Override
            public ResponseBuilder apply(final ContentNegotiationService connegService) {
                return connegService.buildResponse(renderContext, objectAndAction);
            }
        });

        assertContentNegotiationServiceHandled(responseBuilder);
        return buildResponse(responseBuilder);
    }


    @Override
    @Programmatic
    public Response actionResult(
            final Context renderContext,
            final ObjectAndActionInvocation objectAndActionInvocation,
            final SelfLink selfLink) {

        final ResponseBuilder responseBuilder = buildResponse(new Function<ContentNegotiationService, ResponseBuilder>() {
            @Override
            public ResponseBuilder apply(final ContentNegotiationService connegService) {
                return connegService.buildResponse(renderContext, objectAndActionInvocation);
            }
        });

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
            final Function<ContentNegotiationService, ResponseBuilder> connegServiceBuildResponse) {
        for (final ContentNegotiationService contentNegotiationService : contentNegotiationServices) {
            final ResponseBuilder responseBuilder = connegServiceBuildResponse.apply(contentNegotiationService);
            if(responseBuilder != null) {
                return responseBuilder;
            }
        }
        return null;
    }

    /**
     * Overriddable to allow further customization.
     */
    protected Response buildResponse(final ResponseBuilder responseBuilder) {
        return responseBuilder.build();
    }

    @Inject @Any Instance<ContentNegotiationService> contentNegotiationServices;
}

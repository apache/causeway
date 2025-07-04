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
package org.apache.causeway.viewer.restfulobjects.rendering.service.acceptheader;

import java.io.IOException;
import java.util.List;
import jakarta.inject.Named;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import org.springframework.http.MediaType;
import jakarta.ws.rs.ext.Provider;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.InteractionScope;
import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.acceptheader.AcceptHeaderService;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.applib.util.MediaTypes;

import static org.apache.causeway.commons.internal.base._NullSafe.stream;

/**
 * @since 1.x {@index}
 */
@Service
@Named(AcceptHeaderServiceForRest.LOGICAL_TYPE_NAME)
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("ForRest")
@InteractionScope
public class AcceptHeaderServiceForRest implements AcceptHeaderService {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".AcceptHeaderServiceForRest";

    private static ThreadLocal<List<MediaType>> mediaTypesByThread = new ThreadLocal<>();

    /**
     * Not API - called by RO viewer filter.
     */
    private static void setMediaTypes(List<MediaType> mediaTypes) {
        mediaTypesByThread.set(mediaTypes);
    }
    /**
     * Not API - called by RO viewer filter.
     */
    private static void removeMediaTypes() {
        mediaTypesByThread.remove();
    }

    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return mediaTypesByThread.get();
    }

    @Component
    @Provider
    public static class RequestFilter implements ContainerRequestFilter  {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            var acceptableMediaTypes = requestContext.getAcceptableMediaTypes();

            final List<MediaType> mediaTypes = stream(acceptableMediaTypes)
                    .filter(_NullSafe::isPresent)
                    .map(MediaTypes::fromJakarta)
                    .toList();

            setMediaTypes(mediaTypes);
        }
    }

    @Component
    @Provider
    public static class ResponseFilter implements ContainerResponseFilter {
        @Override
        public void filter(
                final ContainerRequestContext requestContext,
                final ContainerResponseContext responseContext) throws IOException {

            removeMediaTypes();
        }
    }

}

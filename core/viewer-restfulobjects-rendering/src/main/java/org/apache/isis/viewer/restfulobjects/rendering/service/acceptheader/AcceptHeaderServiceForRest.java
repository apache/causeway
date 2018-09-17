/**
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
package org.apache.isis.viewer.restfulobjects.rendering.service.acceptheader;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import com.google.common.net.MediaType;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.acceptheader.AcceptHeaderService;
import org.apache.isis.commons.internal.collections._Lists;

@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
@RequestScoped
public class AcceptHeaderServiceForRest implements AcceptHeaderService {

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


    @Programmatic
    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return mediaTypesByThread.get();
    }


    @Provider
    public static class RequestFilter implements javax.ws.rs.container.ContainerRequestFilter
    {
        @Override
        public void filter(final ContainerRequestContext requestContext) throws IOException {
            List<javax.ws.rs.core.MediaType> acceptableMediaTypes = requestContext.getAcceptableMediaTypes();

            final List<MediaType> mediaTypes =
                    _Lists.newArrayList(
                            _Lists.map(acceptableMediaTypes, (
                                        @Nullable
                                        final javax.ws.rs.core.MediaType input) -> {
                                    if (input == null) {
                                        return null;
                                    }
                                    final MediaType mediaType = MediaType.create(input.getType(), input.getSubtype());
                                    final SetMultimap<String, String> parameters = Multimaps.forMap(input.getParameters());
                                    return mediaType.withParameters(parameters);
                            })
                            );
            setMediaTypes(mediaTypes);
        }
    }

    @Provider
    public static class ResponseFilter implements javax.ws.rs.container.ContainerResponseFilter
    {
        @Override
        public void filter(
                final ContainerRequestContext requestContext, final ContainerResponseContext responseContext)
                        throws IOException {
            removeMediaTypes();
        }
    }

}

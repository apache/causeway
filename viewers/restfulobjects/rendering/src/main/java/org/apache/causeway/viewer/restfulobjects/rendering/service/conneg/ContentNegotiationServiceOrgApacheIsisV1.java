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
package org.apache.causeway.viewer.restfulobjects.rendering.service.conneg;

import java.util.List;

import jakarta.inject.Named;
import org.springframework.http.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.applib.CausewayModuleViewerRestfulObjectsApplib;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.Responses;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

import lombok.extern.slf4j.Slf4j;

/**
 * @since 1.x {@index}
 */
@Service
@Named(ContentNegotiationServiceOrgApacheIsisV1.LOGICAL_TYPE_NAME)
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT - 200)
@Qualifier("OrgApacheIsisV1")
@Slf4j
public class ContentNegotiationServiceOrgApacheIsisV1 extends ContentNegotiationServiceAbstract {

    static final String LOGICAL_TYPE_NAME = CausewayModuleViewerRestfulObjectsApplib.NAMESPACE + ".ContentNegotiationServiceOrgApacheIsisV1";
    public static final String ACCEPT_PROFILE = "urn:org.apache.isis/v1";

    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedObject objectAdapter) {
        return whenV1ThenNotImplemented(resourceContext);
    }

    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedProperty objectAndProperty)  {
        return whenV1ThenNotImplemented(resourceContext);
    }

    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedCollection managedCollection) {
        return whenV1ThenNotImplemented(resourceContext);
    }

    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ManagedAction objectAndAction)  {
        return whenV1ThenNotImplemented(resourceContext);
    }

    @Override
    public Response.ResponseBuilder buildResponse(
            final IResourceContext resourceContext,
            final ObjectAndActionInvocation objectAndActionInvocation) {
        return whenV1ThenNotImplemented(resourceContext);
    }

    // -- HELPER

    private boolean canAccept(final IResourceContext resourceContext) {
        final List<MediaType> acceptableMediaTypes = resourceContext.acceptableMediaTypes();
        return mediaTypeParameterMatches(acceptableMediaTypes, "profile", ACCEPT_PROFILE);
    }

    private ResponseBuilder whenV1ThenNotImplemented(final IResourceContext resourceContext) {
        if(!canAccept(resourceContext)) {
            return null;
        }
        log.warn("profile '{}' is no longer supported use '{}' instead",
                ACCEPT_PROFILE,
                ContentNegotiationServiceOrgApacheCausewayV2.ACCEPT_PROFILE);
        return Responses.ofNotImplemented();
    }

}

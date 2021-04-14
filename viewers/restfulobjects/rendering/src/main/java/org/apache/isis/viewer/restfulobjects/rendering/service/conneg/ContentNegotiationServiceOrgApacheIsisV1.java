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
package org.apache.isis.viewer.restfulobjects.rendering.service.conneg;

import java.util.List;

import javax.inject.Named;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.Responses;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

import lombok.extern.log4j.Log4j2;

/**
 * @since 1.x {@index}
 */
@Service
@Named("isis.viewer.ro.ContentNegotiationServiceOrgApacheIsisV1")
@Order(OrderPrecedence.MIDPOINT - 200)
@Qualifier("OrgApacheIsisV1")
@Log4j2
public class ContentNegotiationServiceOrgApacheIsisV1 extends ContentNegotiationServiceAbstract {

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
        final List<MediaType> acceptableMediaTypes = resourceContext.getAcceptableMediaTypes();
        return mediaTypeParameterMatches(acceptableMediaTypes, "profile", ACCEPT_PROFILE);
    }

    private ResponseBuilder whenV1ThenNotImplemented(final IResourceContext resourceContext) {
        if(!canAccept(resourceContext)) {
            return null;
        }
        log.warn("profile '{}' is no longer supported use '{}' instead",
                ACCEPT_PROFILE,
                ContentNegotiationServiceOrgApacheIsisV2.ACCEPT_PROFILE);
        return Responses.ofNotImplemented();
    }

}

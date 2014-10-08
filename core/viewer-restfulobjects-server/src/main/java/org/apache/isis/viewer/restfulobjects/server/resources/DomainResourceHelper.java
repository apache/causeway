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
package org.apache.isis.viewer.restfulobjects.server.resources;

import javax.ws.rs.core.Response;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.viewer.restfulobjects.applib.JsonRepresentation;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.DomainObjectLinkTo;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAdapterLinkTo;
import org.apache.isis.viewer.restfulobjects.server.ResourceContext;
import org.apache.isis.viewer.restfulobjects.server.resources.ResourceAbstract.Caching;

public class DomainResourceHelper implements ResponseGeneratorService.ResponseContext {

    private final ResponseGeneratorService generatorService;

    public DomainResourceHelper(final ResourceContext resourceContext, final ObjectAdapter objectAdapter) {
        this.resourceContext = resourceContext;
        this.objectAdapter = objectAdapter;

        using(new DomainObjectLinkTo());

        generatorService = lookupService(ResponseGeneratorService.class);
    }

    public DomainResourceHelper using(final ObjectAdapterLinkTo linkTo) {
        adapterLinkTo = linkTo;
        adapterLinkTo.usingUrlBase(resourceContext).with(objectAdapter);
        return this;
    }

    //region > ResponseContext impl

    private final ResourceContext resourceContext;
    private final ObjectAdapter objectAdapter;
    private ObjectAdapterLinkTo adapterLinkTo;

    @Override
    public ResourceContext getResourceContext() {
        return resourceContext;
    }

    @Override
    public ObjectAdapter getObjectAdapter() {
        return objectAdapter;
    }

    @Override
    public ObjectAdapterLinkTo getAdapterLinkTo() {
        return adapterLinkTo;
    }
    //endregion


    public Response objectRepresentation() {
        return generatorService.objectRepresentation(this);
    }

    public Response propertyDetails(
            final String propertyId,
            final ResponseGeneratorService.MemberMode memberMode,
            final Caching caching) {
        return generatorService.propertyDetails(this, propertyId, memberMode, caching);
    }

    public Response collectionDetails(
            final String collectionId,
            final ResponseGeneratorService.MemberMode memberMode,
            final Caching caching) {

        return generatorService.collectionDetails(this, collectionId, memberMode, caching);
    }

    public Response actionPrompt(final String actionId) {

        return generatorService.actionPrompt(this, actionId);
    }

    public Response invokeActionQueryOnly(final String actionId, final JsonRepresentation arguments) {

        return generatorService.invokeActionQueryOnly(this, actionId, arguments);
    }

    public Response invokeActionIdempotent(final String actionId, final JsonRepresentation arguments) {

        return generatorService.invokeActionIdempotent(this, actionId, arguments);
    }

    public Response invokeAction(final String actionId, final JsonRepresentation arguments) {

        return generatorService.invokeAction(this, actionId, arguments);
    }


    // //////////////////////////////////////
    // dependencies (from context)
    // //////////////////////////////////////

    private PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    /**
     * Service locator
     */
    private <T> T lookupService(Class<T> serviceType) {
        return getPersistenceSession().getServiceOrNull(serviceType);
    }

}


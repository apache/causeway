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

import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

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
public interface RepresentationService {

    /**
     * As returned by {@link IResourceContext#getIntent()}, applies only to the representation of
     * domain objects.
     */
    enum Intent {
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

    Response objectRepresentation(
            IResourceContext resourceContext,
            ManagedObject objectAdapter);

    Response propertyDetails(
            IResourceContext resourceContext,
            ManagedProperty objectAndProperty);

    Response collectionDetails(
            IResourceContext resourceContext,
            ManagedCollection objectAndCollection);

    Response actionPrompt(
            IResourceContext resourceContext,
            ManagedAction objectAndAction);

    Response actionResult(
            IResourceContext resourceContext,
            ObjectAndActionInvocation objectAndActionInvocation);

}

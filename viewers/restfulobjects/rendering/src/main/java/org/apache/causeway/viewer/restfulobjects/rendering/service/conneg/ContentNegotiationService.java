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

import org.springframework.http.ResponseEntity;

import org.apache.causeway.core.metamodel.interactions.managed.ManagedAction;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.causeway.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.causeway.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

/**
 * Generates a representation according to HTTP `Accept` header of the request.
 *
 * <p> This idea is discussed in section 34.1 of the
 * <a href="http://restfulobjects.org">Restful Objects spec</a> v1.0.
 *
 * <p> The principal motivation is to allow more flexible representations to be
 * generated for REST clients that (perhaps through their use of a certain
 * JavaScript library, say) expect, or at least works best with, a certain
 * style of representation.
 *
 * @apiNote This domain service uses internal framework classes and so does not constitute a formal API/SPI for the framework
 *
 * @since 1.x revised for 4.0 {@index}
 */
public interface ContentNegotiationService {

    /**
     * Returns a representation of a single object.
     *
     * @apiNote By default this representation is as per section 14.4 of the RO spec, v1.0.
     */
    ResponseEntity<Object> buildResponse(
            IResourceContext resourceContext,
            ManagedObject objectAdapter);

    /**
     * Returns a representation of a single property of an object.
     *
     * @apiNote By default this representation is as per section 16.4 of the RO spec, v1.0.
     */
    ResponseEntity<Object> buildResponse(
            IResourceContext resourceContext,
            ManagedProperty objectAndProperty);

    /**
     * Returns a representation of a single collection of an object.
     *
     * @apiNote By default this representation is as per section 17.5 of the RO spec, v1.0.
     */
    ResponseEntity<Object> buildResponse(
            IResourceContext resourceContext,
            ManagedCollection objectAndCollection);

    /**
     * Returns a representation of a single action (prompt) of an object.
     *
     * @apiNote By default this representation is as per section 18.2 of the RO spec, v1.0.
     */
    ResponseEntity<Object> buildResponse(
            IResourceContext resourceContext,
            ManagedAction objectAndAction);

    /**
     * Returns a representation of a single action invocation of an object.
     *
     * @apiNote By default this representation is as per section 19.5 of the RO spec, v1.0.
     */
    ResponseEntity<Object> buildResponse(
            IResourceContext resourceContext,
            ObjectAndActionInvocation objectAndActionInvocation);
}

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

import javax.ws.rs.core.Response;

import org.apache.isis.core.metamodel.interactions.managed.ManagedAction;
import org.apache.isis.core.metamodel.interactions.managed.ManagedCollection;
import org.apache.isis.core.metamodel.interactions.managed.ManagedProperty;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.restfulobjects.rendering.IResourceContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.ObjectAndActionInvocation;

public interface ContentNegotiationService {

    public Response.ResponseBuilder buildResponse(
            IResourceContext renderContext,
            ManagedObject objectAdapter);

    public Response.ResponseBuilder buildResponse(
            IResourceContext renderContext,
            ManagedProperty objectAndProperty);

    public Response.ResponseBuilder buildResponse(
            IResourceContext renderContext,
            ManagedCollection objectAndCollection);

    public Response.ResponseBuilder buildResponse(
            IResourceContext renderContext,
            ManagedAction objectAndAction);

    public Response.ResponseBuilder buildResponse(
            IResourceContext renderContext,
            ObjectAndActionInvocation objectAndActionInvocation);
}

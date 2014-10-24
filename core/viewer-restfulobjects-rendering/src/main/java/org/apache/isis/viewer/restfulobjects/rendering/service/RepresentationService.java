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
package org.apache.isis.viewer.restfulobjects.rendering.service;

import javax.ws.rs.core.Response;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.restfulobjects.rendering.RendererContext;
import org.apache.isis.viewer.restfulobjects.rendering.domainobjects.*;

/**
 * Configure the Restful Objects viewer to emit custom representations (rather than the
 * standard representations defined in the RO spec).
 *
 * <p>
 * This interface is EXPERIMENTAL and may change in the future.
 * </p>
 */
public interface RepresentationService {

    @Programmatic
    Response objectRepresentation(
            Context resourceContext,
            ObjectAdapter objectAdapter);

    @Programmatic
    Response propertyDetails(
            Context rendererContext,
            ObjectAndProperty objectAndProperty,
            MemberReprMode memberReprMode);

    @Programmatic
    Response collectionDetails(
            Context rendererContext,
            ObjectAndCollection objectAndCollection,
            MemberReprMode memberReprMode);

    @Programmatic
    Response actionPrompt(
            Context rendererContext,
            ObjectAndAction objectAndAction);

    @Programmatic
    Response actionResult(
            Context rendererContext,
            ObjectAndActionInvocation objectAndActionInvocation,
            ActionResultReprRenderer.SelfLink selfLink);

    public static interface Context extends RendererContext {
        ObjectAdapterLinkTo getAdapterLinkTo();
    }
}

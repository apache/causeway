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

package org.apache.isis.viewer.restfulobjects.viewer.webmodule.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.applib.services.iactnlayer.InteractionContext;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;

/**
 * Decouples the <code>IsisRestfulObjectsInteractionFilter</code> from the mechanism of obtaining the
 * {@link InteractionContext}.
 *
 * @since 2.0 {@index}
 */
public interface AuthenticationStrategy {

    /**
     * Returns a still-valid {@link InteractionContext} or {@code null}
     * @see AuthenticationManager#isSessionValid(InteractionContext)
     */
    InteractionContext lookupValid(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse);

    /**
     * Binds the request to a still-valid {@link InteractionContext} if applicable
     * @param httpServletRequest
     * @param httpServletResponse
     * @param auth
     */
    void bind(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            InteractionContext auth);

    void invalidate(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse);
}

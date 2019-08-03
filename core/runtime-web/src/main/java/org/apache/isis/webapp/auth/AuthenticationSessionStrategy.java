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

package org.apache.isis.webapp.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.security.authentication.AuthenticationSession;
import org.apache.isis.security.authentication.manager.AuthenticationManager;

/**
 * Decouples the {@link IsisSessionFilter} from the mechanism of obtaining the
 * {@link AuthenticationSession}.
 */
public interface AuthenticationSessionStrategy {

    /**
     * Returns a still-valid {@link AuthenticationSession} or {@code null}
     * @see {@link AuthenticationManager#isSessionValid(AuthenticationSession)
     */
    AuthenticationSession lookupValid(
            HttpServletRequest httpServletRequest, 
            HttpServletResponse httpServletResponse);

    /**
     * Binds the request to a still-valid {@link AuthenticationSession} if applicable
     * @param httpServletRequest
     * @param httpServletResponse
     * @param authSession
     */
    void bind(
            HttpServletRequest httpServletRequest, 
            HttpServletResponse httpServletResponse, 
            AuthenticationSession authSession);

    void invalidate(
            HttpServletRequest httpServletRequest, 
            HttpServletResponse httpServletResponse);
}

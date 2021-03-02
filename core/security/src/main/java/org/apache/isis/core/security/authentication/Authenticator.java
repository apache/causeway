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

package org.apache.isis.core.security.authentication;

/**
 * Represents an authentication mechanism capable of authenticating certain
 * types of {@link AuthenticationRequest} and returning an {@link Authentication}
 * if the credentials are valid.
 *
 * <p>
 *     There can be multiple {@link Authenticator}s registered.  If so, all
 *     Authenticators that can authenticate any given {@link AuthenticationRequest}
 *     must
 * </p>
 *
 * @apiNote This is a framework internal class and so does not constitute a formal API.
 *
 * @since 1.x but refactored in v2 {@index}
 */
public interface Authenticator {

    /**
     * Whether the provided {@link AuthenticationRequest} is recognized by this
     * {@link Authenticator}.
     */
    boolean canAuthenticate(Class<? extends AuthenticationRequest> authenticationRequestClass);

    /**
     * Authenticates the provided {@link AuthenticationRequest request},
     * returning a non-null {@link Authentication} if valid.
     *
     * @param code
     *            - a hint; is guaranteed by the framework to be unique, but the authenticator decides whether to use it or not.
     */
    Authentication authenticate(AuthenticationRequest request, String code);

    /**
     * Invalidates this {@link Authentication}, meaning that the user will need
     * to log in again to use the application.
     */
    void logout(Authentication authentication);
}

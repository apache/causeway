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
package org.apache.causeway.extensions.spring.security.oauth2.restful;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.core.security.authentication.AuthenticationRequestPassword;
import org.apache.causeway.viewer.restfulobjects.viewer.webmodule.auth.AuthenticationStrategyAbstract;

/**
 * This class enables support for JWT for Restful clients.
 *
 * <p>
 *     To use, it requires that the restfulobjects viewer is on the classpath:
 *
 * <pre>
 * &lt;dependency&gt;
 *   &lt;groupId&gt;org.apache.causeway.viewer&lt;/groupId&gt;
 *   &lt;artifactId&gt;causeway-viewer-restfulobjects-viewer&lt;/artifactId&gt;
 * &lt;/dependency&gt;
 * </pre>
 *
 * <p>
 *     It should then additionally be specified as the authentication strategy:
 *
 * <pre>
 * isis.viewer.restfulobjects.authentication.strategy-class-name=\
 *      org.apache.causeway.extensions.spring.security.oauth2.restful.AuthenticationStrategyJwt
 * </pre>
 * </p>
 *
 *
 */
public class    AuthenticationStrategyJwt extends AuthenticationStrategyAbstract {

    @Override
    public InteractionContext lookupValid(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse) {

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Jwt) {
                Jwt jwt = (Jwt) principal;
                var authenticationRequestPwd = new AuthenticationRequestPassword(getName(jwt), null);
                var authenticationManager = super.getAuthenticationManager(httpServletRequest);
                return authenticationManager.authenticate(authenticationRequestPwd);
            }
        }

        return null;
    }

    protected String getName(final Jwt jwt) {
        return Optional.ofNullable((String)jwt.getClaim("appid"))
                       .orElse(jwt.getSubject());
    }

    /**
     * This implementation is stateless and so does not support binding the {@link InteractionContext} (aka
     * authentication) into a store (eg a session); instead each request is authenticated afresh.
     */
    @Override
    public void bind(
            final HttpServletRequest httpServletRequest,
            final HttpServletResponse httpServletResponse,
            final InteractionContext authentication) {

    }

}

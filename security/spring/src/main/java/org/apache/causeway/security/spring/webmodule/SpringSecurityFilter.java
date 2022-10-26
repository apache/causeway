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
package org.apache.causeway.security.spring.webmodule;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.apache.causeway.applib.services.iactnlayer.InteractionContext;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserCurrentSessionTimeZoneHolder;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.applib.services.user.UserMemento.AuthenticationSource;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverter;

import lombok.NonNull;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
//@Log4j2
public class SpringSecurityFilter implements Filter {

    @Autowired private InteractionService interactionService;
    @Inject List<AuthenticationConverter> converters;
    @Inject private UserCurrentSessionTimeZoneHolder userCurrentSessionTimeZoneHolder;

    @Override
    public void doFilter(
            final ServletRequest servletRequest,
            final ServletResponse servletResponse,
            final FilterChain filterChain) throws IOException, ServletException {

        val userMemento = springAuthentication()
                .flatMap(this::userMementoFromSpringAuthentication)
                .orElse(null);

        if (userMemento == null) {
            setUnauthorized(servletResponse);
            return; // either not authenticated or unknown principal type (not handled)
        }

        val interactionContext = InteractionContext.ofUserWithSystemDefaults(userMemento)
                .withTimeZoneIfAny(userCurrentSessionTimeZoneHolder.getUserTimeZone());

        val result = interactionService.runAndCatch(
                interactionContext,
                ()->filterChain.doFilter(servletRequest, servletResponse));

        result.ifFailure(failure->{
            failure.printStackTrace(); // debug
            setUnauthorized(servletResponse);
        });

        // re-throw
        result.ifFailureFail();
    }

    // -- HELPER

    private void setUnauthorized(final ServletResponse servletResponse){
        ((HttpServletResponse) servletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Optionally Spring's {@link Authentication}, based on presence
     * (no matter whether actually authenticated).
     */
    private Optional<Authentication> springAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Optionally an authorized {@link UserMemento} based on presence of an actually
     * authenticated Spring {@link Authentication}.
     */
    private Optional<UserMemento> userMementoFromSpringAuthentication(
            final @NonNull Authentication springAuthentication) {

        // make sure session is actually authenticated
        if(!springAuthentication.isAuthenticated()) {
            return Optional.empty();
        }

        for (final AuthenticationConverter converter : converters) {
            try {
                val userMemento = converter.convert(springAuthentication);
                if(userMemento != null) {
                    return Optional.of(
                            // adds generic authorized user role to indicate 'authorized'
                            // (as required by Wicket viewer)
                            userMemento
                                .withRoleAdded(UserMemento.AUTHORIZED_USER_ROLE)
                                .withAuthenticationSource(AuthenticationSource.EXTERNAL));
                }
            } catch(final Throwable ignored) {
            }
        }
        return Optional.empty();
    }

}

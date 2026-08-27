/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.htmx.security.secman;

import org.springframework.security.core.GrantedAuthority;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverter;

import org.jspecify.annotations.NonNull;

@jakarta.annotation.Priority(PriorityPrecedence.EARLY)
public final class AuthenticationConverterOfSecmanUserDetails
extends AuthenticationConverter.Abstract<SecmanUserDetails> {

    public AuthenticationConverterOfSecmanUserDetails() {
        super(SecmanUserDetails.class);
    }

    @Override
    protected UserMemento convertPrincipal(final @NonNull SecmanUserDetails principal) {
        var userMemento = UserMemento.ofNameAndRoleNames(
                principal.getUsername(),
                principal.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList());
        if (principal.atPath() != null) {
            userMemento = userMemento.withMultiTenancyToken(principal.atPath());
        }
        if (principal.languageLocale() != null) {
            userMemento = userMemento.withLanguageLocale(principal.languageLocale());
        }
        if (principal.numberFormatLocale() != null) {
            userMemento = userMemento.withNumberFormatLocale(principal.numberFormatLocale());
        }
        if (principal.timeFormatLocale() != null) {
            userMemento = userMemento.withTimeFormatLocale(principal.timeFormatLocale());
        }
        return userMemento;
    }
}

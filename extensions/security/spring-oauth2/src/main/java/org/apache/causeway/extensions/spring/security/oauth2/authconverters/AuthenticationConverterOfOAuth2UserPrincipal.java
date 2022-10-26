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
package org.apache.causeway.extensions.spring.security.oauth2.authconverters;

import java.net.MalformedURLException;
import java.net.URL;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.security.spring.authconverters.AuthenticationConverter;

import lombok.NonNull;
import lombok.val;

/**
 * Applies if {@link Authentication} holds a principal of type {@link OAuth2User}.
 */
@Component
@javax.annotation.Priority(PriorityPrecedence.LATE - 150)
public class AuthenticationConverterOfOAuth2UserPrincipal
extends AuthenticationConverter.Abstract<OAuth2User> {

    public AuthenticationConverterOfOAuth2UserPrincipal() {
        super(OAuth2User.class);
    }

    @Override
    protected UserMemento convertPrincipal(final @NonNull OAuth2User oAuth2User) {
        return UserMemento.ofNameAndRoleNames(usernameFrom(oAuth2User))
                .withAvatarUrl(avatarUrlFrom(oAuth2User))
                .withRealName(realNameFrom(oAuth2User));
    }

    // -- HOOKS FOR CUSTOMIZATION

    protected String usernameFrom(final OAuth2User oAuth2User) {
        val loginAttr = oAuth2User.getAttributes().get("login");
        return loginAttr instanceof CharSequence
                ? ((CharSequence) loginAttr).toString()
                : oAuth2User.getName();
    }

    protected URL avatarUrlFrom(final OAuth2User oAuth2User) {
        final Object avatarUrlObj = oAuth2User.getAttributes().get("avatar_url");
        if(avatarUrlObj instanceof String) {
            try {
                return new URL((String)avatarUrlObj);
            } catch (MalformedURLException e) {
                return null;
            }
        }
        return null;
    }

    protected String realNameFrom(final OAuth2User oAuth2User) {
        final Object nameAttr = oAuth2User.getAttributes().get("name");
        if(nameAttr instanceof String) {
            return (String)nameAttr;
        }
        return null;
    }

}

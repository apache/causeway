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
package org.apache.causeway.viewer.webcomponents.security.secman;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import org.jspecify.annotations.Nullable;

final class SecmanUserDetails implements UserDetails, CredentialsContainer, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String username;
    private final boolean accountNonLocked;
    private final List<GrantedAuthority> authorities;
    private final String atPath;
    private final Locale languageLocale;
    private final Locale numberFormatLocale;
    private final Locale timeFormatLocale;
    private String encryptedPassword;

    SecmanUserDetails(
            final String username,
            final String encryptedPassword,
            final boolean accountNonLocked,
            final Collection<String> roleNames,
            final @Nullable String atPath,
            final @Nullable Locale languageLocale,
            final @Nullable Locale numberFormatLocale,
            final @Nullable Locale timeFormatLocale) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.accountNonLocked = accountNonLocked;
        this.authorities = roleNames.stream()
                .sorted()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
        this.atPath = atPath;
        this.languageLocale = languageLocale;
        this.numberFormatLocale = numberFormatLocale;
        this.timeFormatLocale = timeFormatLocale;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return encryptedPassword;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountNonLocked;
    }

    @Override
    public void eraseCredentials() {
        encryptedPassword = null;
    }

    @Nullable String atPath() {
        return atPath;
    }

    @Nullable Locale languageLocale() {
        return languageLocale;
    }

    @Nullable Locale numberFormatLocale() {
        return numberFormatLocale;
    }

    @Nullable Locale timeFormatLocale() {
        return timeFormatLocale;
    }
}

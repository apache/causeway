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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.apache.causeway.applib.services.iactn.InteractionService;
import org.apache.causeway.extensions.secman.applib.role.dom.ApplicationRole;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUser;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserRepository;
import org.apache.causeway.extensions.secman.applib.user.dom.ApplicationUserStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecmanAuthenticationBridgeTest {

    private final ApplicationUserRepository repository = Mockito.mock(ApplicationUserRepository.class);
    private final InteractionService interactionService = Mockito.mock(InteractionService.class);
    private SecmanUserDetailsService service;

    @BeforeEach
    void setUp() throws Exception {
        when(interactionService.callAnonymous(any())).thenAnswer(invocation ->
                ((Callable<?>) invocation.getArgument(0)).call());
        service = new SecmanUserDetailsService(repository, interactionService);
    }

    @Test
    void lookupCarriesCredentialsStatusRolesTenancyAndLocalesInsideAnonymousInteraction() {
        final var user = user("sven", "{bcrypt}encoded", ApplicationUserStatus.UNLOCKED);
        when(repository.findByUsername("sven")).thenReturn(Optional.of(user));

        final var details = (SecmanUserDetails) service.loadUserByUsername("sven");

        assertThat(details.getUsername()).isEqualTo("sven");
        assertThat(details.getPassword()).isEqualTo("{bcrypt}encoded");
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.getAuthorities()).extracting("authority").containsExactly("petclinic-user");
        assertThat(details.atPath()).isEqualTo("/clinic/eu");
        assertThat(details.languageLocale()).isEqualTo(Locale.FRENCH);
        assertThat(details.numberFormatLocale()).isEqualTo(Locale.GERMANY);
        assertThat(details.timeFormatLocale()).isEqualTo(Locale.UK);
        verify(interactionService).callAnonymous(any());
    }

    @Test
    void lockedUserCarriesDisabledAccountStatus() {
        final var user = user("locked", "{bcrypt}encoded", ApplicationUserStatus.LOCKED);
        when(repository.findByUsername("locked")).thenReturn(Optional.of(user));

        final var details = (SecmanUserDetails) service.loadUserByUsername("locked");

        assertThat(details.isAccountNonLocked()).isFalse();
        assertThat(details.isEnabled()).isFalse();
    }

    @Test
    void absentAndPasswordlessUsersFailGenerically() {
        when(repository.findByUsername("absent")).thenReturn(Optional.empty());
        final var passwordless = user("passwordless", null, ApplicationUserStatus.UNLOCKED);
        when(repository.findByUsername("passwordless")).thenReturn(Optional.of(passwordless));

        assertThatThrownBy(() -> service.loadUserByUsername("absent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username or password");
        assertThatThrownBy(() -> service.loadUserByUsername("passwordless"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void internalLookupFailureFailsClosed() {
        when(repository.findByUsername("broken")).thenThrow(new IllegalStateException("database detail"));

        assertThatThrownBy(() -> service.loadUserByUsername("broken"))
                .isInstanceOf(InternalAuthenticationServiceException.class)
                .hasMessage("Local authentication is temporarily unavailable");
        verify(interactionService).callAnonymous(any());
    }

    @Test
    void providerValidatesPasswordRejectsWrongPasswordAndErasesCredentials() {
        final var encoder = new BCryptPasswordEncoder();
        final var user = user("sven", encoder.encode("correct horse"), ApplicationUserStatus.UNLOCKED);
        when(repository.findByUsername("sven")).thenReturn(Optional.of(user));
        final var provider = new DaoAuthenticationProvider(service);
        provider.setPasswordEncoder(encoder);

        final var authenticationManager = new ProviderManager(provider);
        final var authenticated = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sven", "correct horse"));
        assertThat(authenticated.isAuthenticated()).isTrue();
        assertThat(((SecmanUserDetails) authenticated.getPrincipal()).getPassword()).isNull();

        assertThatThrownBy(() -> authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("sven", "anything else")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void erasedPrincipalCanBeSerializedWithoutCredentials() throws Exception {
        final var details = new SecmanUserDetails(
                "sven",
                "encoded",
                true,
                Set.of("petclinic-user"),
                "/clinic/eu",
                Locale.FRENCH,
                Locale.GERMANY,
                Locale.UK);
        details.eraseCredentials();

        final var bytes = new ByteArrayOutputStream();
        try (var output = new ObjectOutputStream(bytes)) {
            output.writeObject(details);
        }
        final SecmanUserDetails restored;
        try (var input = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            restored = (SecmanUserDetails) input.readObject();
        }

        assertThat(restored.getUsername()).isEqualTo("sven");
        assertThat(restored.getPassword()).isNull();
        assertThat(restored.getAuthorities()).extracting("authority").containsExactly("petclinic-user");
        assertThat(restored.atPath()).isEqualTo("/clinic/eu");
    }

    @Test
    void converterRefinesWithoutASecondRepositoryLookup() {
        final var details = new SecmanUserDetails(
                "sven",
                "encoded",
                true,
                Set.of("petclinic-user", "petclinic-admin"),
                "/clinic/eu",
                Locale.FRENCH,
                Locale.GERMANY,
                Locale.UK);
        final var authentication = UsernamePasswordAuthenticationToken.authenticated(
                details, null, details.getAuthorities());

        final var memento = new AuthenticationConverterOfSecmanUserDetails().convert(authentication);

        assertThat(memento.name()).isEqualTo("sven");
        assertThat(memento.roles().stream().map(role -> role.name()).toList())
                .containsExactlyInAnyOrder("petclinic-user", "petclinic-admin");
        assertThat(memento.multiTenancyToken()).isEqualTo("/clinic/eu");
        assertThat(memento.languageLocale()).isEqualTo(Locale.FRENCH);
        assertThat(memento.numberFormatLocale()).isEqualTo(Locale.GERMANY);
        assertThat(memento.timeFormatLocale()).isEqualTo(Locale.UK);
        Mockito.verifyNoInteractions(repository);
    }

    private static ApplicationUser user(
            final String username,
            final String encryptedPassword,
            final ApplicationUserStatus status) {
        final var role = Mockito.mock(ApplicationRole.class);
        when(role.getName()).thenReturn("petclinic-user");
        final var user = Mockito.mock(ApplicationUser.class);
        when(user.getUsername()).thenReturn(username);
        when(user.getEncryptedPassword()).thenReturn(encryptedPassword);
        when(user.getStatus()).thenReturn(status);
        when(user.getRoles()).thenReturn(Set.of(role));
        when(user.getAtPath()).thenReturn("/clinic/eu");
        when(user.getLanguage()).thenReturn(Locale.FRENCH);
        when(user.getNumberFormat()).thenReturn(Locale.GERMANY);
        when(user.getTimeFormat()).thenReturn(Locale.UK);
        return user;
    }
}

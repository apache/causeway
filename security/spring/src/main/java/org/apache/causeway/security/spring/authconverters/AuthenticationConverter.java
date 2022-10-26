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
package org.apache.causeway.security.spring.authconverters;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.commons.internal.base._Casts;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Defines an SPI to attempt to convert a Spring {@link Authentication} into
 * an Apache Causeway {@link UserMemento}.
 *
 * <p>
 *     The {@link Authentication} will have already been verified as having been
 *     {@link Authentication#isAuthenticated() authenticated}.
 * </p>
 *
 * <p>
 *     Implementations should be defined as Spring {@link Component}s
 *     and added to the {@link Configuration application context}
 *     either by being {@link Import imported} explicitly
 *     or  implicitly through {@link ComponentScan}.
 * </p>
 *
 * <p>
 *     All known converters are checked one by one, but checking stops once one
 *     converter has successively converted the {@link Authentication} into a
 *     {@link UserMemento} (in other words, chain-of-responsibility pattern).
 *     Use the {@link javax.annotation.Priority} annotation to influence the order
 *     in which converter implementations are checked.
 * </p>
 *
 * @since 2.0 {@index}
 */
public interface AuthenticationConverter {

    /**
     * Attempt to convert a Spring {@link Authentication} (which will have been
     * {@link Authentication#isAuthenticated() authenticated}) into a
     * {@link UserMemento}.
     *
     * <p>
     *     There are many different implementations of {@link Authentication},
     *     so the implementation should be targeted at a specific
     *     implementation.
     * </p>
     *
     * <p>
     *     The framework provides some default implementations for the most
     *     common use cases.
     * </p>
     *
     * @param authentication to attempt to convert
     * @return non-null if could be converted
     */
    @Nullable
    UserMemento convert(@NonNull Authentication authentication);

    // -- BASE CLASS FOR CONVENIENCE

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    abstract class Abstract<T> implements AuthenticationConverter {

        private final @NonNull Class<T> principalClass;

        @Override
        public final UserMemento convert(final @NonNull Authentication authentication) {
            val principal = authentication.getPrincipal();
            return _Casts.castTo(principalClass, principal)
                    .map(this::convertPrincipal)
                    .orElse(null);
        }

        protected abstract UserMemento convertPrincipal(@NonNull T principal);

    }


}

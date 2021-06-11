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
package org.apache.isis.applib.services.iactnlayer;

import java.io.Serializable;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.With;

/**
 * Provides the user and scenario specific environment for an {@link Interaction}.
 *
 * @since 2.0 {@index}
 */
@lombok.Value @Builder
@RequiredArgsConstructor
public class InteractionContext implements Serializable {

    private static final long serialVersionUID = -220896735209733865L;

    // -- FACTORIES

    /**
     * Creates a new {@link InteractionContext} with the specified user and
     * system defaults for clock, locale and time-zone.
     */
    public static InteractionContext ofUserWithSystemDefaults(
            final @NonNull UserMemento user) {
        return InteractionContext.builder()
                .user(user)
                .clock(VirtualClock.system())
                .locale(Locale.getDefault())
                .timeZone(TimeZone.getDefault())
                .build();
    }

    // -- IMMUTABLE FIELDS

    /**
     * The (programmatically) simulated (or actual) user.
     *
     * @apiNote immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) user
     */
    @With @Getter @Builder.Default
    @NonNull UserMemento user = UserMemento.system();

    /**
     * The (programmatically) simulated (or actual) clock.
     *
     * @apiNote immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) clock
     */
    @With @Getter @Builder.Default
    @NonNull VirtualClock clock = VirtualClock.system();

    @With @Getter @Builder.Default
    @NonNull Locale locale = Locale.getDefault();

    @With @Getter @Builder.Default
    @NonNull TimeZone timeZone = TimeZone.getDefault();


    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link UserMemento}.
     */
    public static UnaryOperator<InteractionContext> switchUser(@NonNull final UserMemento userMemento) {
        return interactionContext -> interactionContext.withUser(userMemento);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link VirtualClock}.
     */
    public static UnaryOperator<InteractionContext> switchClock(@NonNull final VirtualClock clock) {
        return interactionContext -> interactionContext.withClock(clock);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link Locale}.
     */
    public static UnaryOperator<InteractionContext> switchLocale(@NonNull final Locale locale) {
        return interactionContext -> interactionContext.withLocale(locale);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link TimeZone}.
     */
    public static UnaryOperator<InteractionContext> switchTimeZone(@NonNull final TimeZone timeZone) {
        return interactionContext -> interactionContext.withTimeZone(timeZone);
    }

    /**
     * Convenience method to combine {@link UnaryOperator}s, for example as per {@link #switchUser(UserMemento)} and {@link #switchTimeZone(TimeZone)}.
     *
     * <p>
     * NOTE: this implementation can result in heap pollution; better to use the {@link #combine(Stream) overload}.
     * </p>
     *
     * @see #combine(Stream)
     */
    public static <T> UnaryOperator<T> combine(UnaryOperator<T>... mappers) {
        return combine(Stream.of(mappers));
    }

    /**
     * Convenience method to combine {@link UnaryOperator}s, for example as per {@link #switchUser(UserMemento)} and {@link #switchTimeZone(TimeZone)}.
     *
     * credit: https://stackoverflow.com/a/51065029/56880
     */
    public static <T> UnaryOperator<T> combine(Stream<UnaryOperator<T>> mappers) {
        return mappers.reduce(t -> t, (a,b) -> a.andThen(b)::apply);
    }


}

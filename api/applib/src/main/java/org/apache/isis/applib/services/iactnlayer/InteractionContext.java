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
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.locale.UserLocale;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.With;

/**
 * Provides the user and scenario specific environment for an {@link Interaction}.
 *
 * @since 2.0 {@index}
 */
@Getter
@lombok.experimental.FieldDefaults(makeFinal=false, level= AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
@Builder
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
                .timeZone(ZoneId.systemDefault())
                .build();
    }

    // -- IMMUTABLE FIELDS

    /**
     * The (programmatically) simulated (or actual) user.
     *
     * @apiNote practically immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) user
     *
     */
    @With @Getter @Builder.Default
    final @NonNull UserMemento user = UserMemento.system();

    /**
     * The (programmatically) simulated (or actual) clock.
     *
     * @apiNote immutable, allows an {@link Interaction} to (logically) run with its
     * own simulated (or actual) clock
     */
    @With @Getter @Builder.Default
    final @NonNull VirtualClock clock = VirtualClock.system();

    @With UserLocale locale;
    public UserLocale getLocale(){
        if(locale!=null) {
            return locale; // if set, overrides any user preferences
        }
        return Optional.ofNullable(getUser())
                .map(UserMemento::asUserLocale)
                .orElseGet(UserLocale::getDefault);
    }

    @With @Getter @Builder.Default
    final @NonNull ZoneId timeZone = ZoneId.systemDefault();


    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link UserMemento}.
     */
    public static UnaryOperator<InteractionContext> switchUser(final @NonNull UserMemento userMemento) {
        return interactionContext -> interactionContext.withUser(userMemento);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link VirtualClock}.
     */
    public static UnaryOperator<InteractionContext> switchClock(final @NonNull VirtualClock clock) {
        return interactionContext -> interactionContext.withClock(clock);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link Locale}.
     */
    public static UnaryOperator<InteractionContext> switchLocale(final @NonNull UserLocale locale) {
        return interactionContext -> interactionContext.withLocale(locale);
    }

    /**
     * Convenience method for use with {@link org.apache.isis.applib.services.sudo.SudoService}, returning a
     * {@link UnaryOperator} that will act upon the provided {@link InteractionContext} to return the same but with
     * the specified {@link ZoneId}.
     */
    public static UnaryOperator<InteractionContext> switchTimeZone(final @NonNull ZoneId timeZone) {
        return interactionContext -> interactionContext.withTimeZone(timeZone);
    }

    /**
     * Convenience method to combine {@link UnaryOperator}s, for example as per {@link #switchUser(UserMemento)} and {@link #switchTimeZone(ZoneId)}.
     *
     * <p>
     * NOTE: this implementation can result in heap pollution; better to use the {@link #combine(Stream) overload}.
     * </p>
     *
     * @see #combine(Stream)
     */
    @SafeVarargs
    public static <T> UnaryOperator<T> combine(final UnaryOperator<T>... mappers) {
        return combine(Stream.of(mappers));
    }

    /**
     * Convenience method to combine {@link UnaryOperator}s, for example as per {@link #switchUser(UserMemento)} and {@link #switchTimeZone(ZoneId)}.
     *
     * credit: https://stackoverflow.com/a/51065029/56880
     */
    public static <T> UnaryOperator<T> combine(final Stream<UnaryOperator<T>> mappers) {
        return mappers.reduce(t -> t, (a,b) -> a.andThen(b)::apply);
    }

}

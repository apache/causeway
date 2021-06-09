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

import org.apache.isis.applib.clock.VirtualClock;
import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.user.UserMemento;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.With;

/**
 * Provides the user and scenario specific environment for an {@link Interaction}.
 *
 * @since 2.0 {@index}
 */
@Value @Builder
@RequiredArgsConstructor
public class ExecutionContext implements Serializable {

    private static final long serialVersionUID = -220896735209733865L;

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

    // -- FACTORIES

    /**
     * Creates a new {@link ExecutionContext} with the specified user and
     * system defaults for clock, locale and time-zone.
     */
    public static ExecutionContext ofUserWithSystemDefaults(
            final @NonNull UserMemento user) {
        return ExecutionContext.builder()
                .user(user)
                .clock(VirtualClock.system())
                .locale(Locale.getDefault())
                .timeZone(TimeZone.getDefault())
                .build();
    }

}

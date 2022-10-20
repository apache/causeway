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
package org.apache.causeway.applib.services.keyvaluestore;

import java.io.Serializable;
import java.util.Optional;

import org.springframework.lang.Nullable;

import lombok.NonNull;

/**
 * Defines a mechanism for viewers to store arbitrary key value pairs
 * on a per-session basis. That is usually a {@link javax.servlet.http.HttpSession}.
 * <p>
 * This store <i>is</i> used by the Wicket viewer. For example, the viewer
 * remembers which time-zone the user has logged in. Or when impersonating.
 *
 * @since 2.0 {@index}
 */
public interface KeyValueSessionStore {

    /**
     * Whether a session is available, for storing/retrieving key/value pairs.
     */
    boolean isSessionAvailable();

    /**
     * Puts given value onto the session store, overriding any existing value.
     * If value is null, removes the entry from the store.
     * <p>
     * In case there is no session for storing available, acts as a no-op.
     * @param key - unique key (required)
     * @param value - serializable value (optional)
     */
    void put(@NonNull String key, @Nullable Serializable value);

    /**
     * Optionally returns the value that is stored under given key,
     * based on whether a corresponding entry exists.
     * <p>
     * In case there is no session for storing available, will return {@link Optional#empty()}.
     */
    <T extends Serializable>
    Optional<T> lookupAs(@NonNull String key, @NonNull Class<T> requiredType);

    /**
     * Removes the entry from the store.
     * <p>
     * In case there is no session for storing available, acts as a no-op.
     */
    void clear(final @NonNull String key);

    // -- SHORTCUTS

    default Optional<String> lookupAsString(final @NonNull String key) {
        return lookupAs(key, String.class);
    }

    default boolean getAsBoolean(final @NonNull String key) {
        return lookupAs(key, Boolean.class).map(Boolean::booleanValue).orElse(false);
    }

}

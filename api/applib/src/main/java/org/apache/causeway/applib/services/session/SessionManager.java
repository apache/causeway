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
package org.apache.causeway.applib.services.session;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.iactn.InteractionContext;
import org.apache.causeway.applib.services.user.UserMemento;

/**
 * Defines a mechanism for viewers to store arbitrary key value pairs
 * on a per-session basis. That is usually a {@link jakarta.servlet.http.HttpSession}.
 *
 * @since 4.0 {@index}
 */
public interface SessionManager {

	public record SessionKey<T extends Serializable>(
			String id,
			Class<T> type) implements Serializable {
	}

	public static SessionKey<ZoneId> USER_TIMEZONE_ID = new SessionKey<>("causeway.user.zoneId", ZoneId.class);
	public static SessionKey<InteractionContext> USER_AUTHENTICATION = new SessionKey<>("causeway.user.auth", InteractionContext.class);
	public static SessionKey<UserMemento> USER_IMPERSONATION = new SessionKey<>("causeway.user.sudo", UserMemento.class);

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

    default <T extends Serializable>
    void put(@NonNull final SessionKey<T> key, @Nullable final T value) {
    	put(key.id(), value);
    }

    /**
     * Optionally returns the value that is stored under given key,
     * based on whether a corresponding entry exists.
     * <p>
     * In case there is no session for storing available, will return {@link Optional#empty()}.
     */
    <T extends Serializable>
    Optional<T> lookupAs(@NonNull String key, @NonNull Class<T> requiredType);

    default <T extends Serializable>
    Optional<T> lookup(@NonNull final SessionKey<T> key) {
    	return lookupAs(key.id(), key.type());
    }

    /**
     * Removes the entry from the store.
     * <p>
     * In case there is no session for storing available, acts as a no-op.
     */
    void remove(final @NonNull String key);

    default <T extends Serializable>
    void remove(@NonNull final SessionKey<T> key) {
    	remove(key.id());
    }

    // -- SHORTCUTS

    default Optional<String> lookupAsString(final @NonNull String key) {
        return lookupAs(key, String.class);
    }

    default boolean getAsBoolean(final @NonNull String key) {
        return lookupAs(key, Boolean.class).map(Boolean::booleanValue).orElse(false);
    }

    // -- NOOP

    public static SessionManager NOOP = new SessionManager() {
    	@Override public boolean isSessionAvailable() { return false; }
    	@Override public void put(@NonNull final String key, @Nullable final Serializable value) { }
    	@Override public <T extends Serializable> Optional<T> lookupAs(@NonNull final String key, @NonNull final Class<T> requiredType) {
    		return Optional.empty();
    	}
    	@Override public void remove(@NonNull final String key) { }
    };

    /**
     * For testing.
     */
    public record InMemory(Map<String, Serializable> map) implements SessionManager {
    	public InMemory {
    		map = map!=null
				? map
				: new HashMap<>();
    	}
    	@Override public boolean isSessionAvailable() { return true; }
    	@Override public void put(@NonNull final String key, @Nullable final Serializable value) {
    		map.put(key, value);
    	}
    	@Override public <T extends Serializable> Optional<T> lookupAs(@NonNull final String key, @NonNull final Class<T> requiredType) {
    		return Optional.ofNullable(map.get(key))
    				.map(requiredType::cast);
    	}
    	@Override public void remove(@NonNull final String key) {
    		map.remove(key);
    	}
    }

}

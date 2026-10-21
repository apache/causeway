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
package org.apache.causeway.applib.services.user;

import java.time.ZoneId;
import java.util.Optional;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.keyvaluestore.KeyValueSessionStore;

/**
 * Stores the user's current {@link ZoneId} with session scope.
 * <p>
 * eg. on application login
 *
 * @since 2.0 {@index}
 */
public record UserCurrentSessionTimeZoneHandler(
	    KeyValueSessionStore keyValueSessionStore) {

	private static final String SESSION_KEY_ZONE_ID =
			UserCurrentSessionTimeZoneHandler.class.getName() + "#zoneId";

	public UserCurrentSessionTimeZoneHandler {
		keyValueSessionStore = keyValueSessionStore!=null
				? keyValueSessionStore
				: KeyValueSessionStore.NOOP;
	}

	public UserCurrentSessionTimeZoneHandler(final @Nullable Optional<KeyValueSessionStore> keyValueSessionStoreOpt) {
		this(keyValueSessionStoreOpt!=null ? keyValueSessionStoreOpt.orElse(null) : null);
	}

	/**
	 * Sets the user's current {@link ZoneId}
	 * within the context of the current session.
	 */
    public void setUserTimeZone(final @NonNull ZoneId zoneId) {
        keyValueSessionStore.put(SESSION_KEY_ZONE_ID, zoneId);
    }

    /**
     * Optionally returns the user's current {@link ZoneId},
     * based on whether it was set before,
     * within the context of the current session.
     *
     * @apiNote not meant to fallback to system defaults,
     * instead return {@link Optional#empty()},
     * if there is no specific time-zone information available
     */
    public Optional<ZoneId> getUserTimeZone() {
        return keyValueSessionStore.lookupAs(SESSION_KEY_ZONE_ID, ZoneId.class);
    }

    /**
     * Clears the user's current {@link ZoneId}
     * within the context of the current session.
     */
    public void clearUserTimeZone() {
        keyValueSessionStore.clear(SESSION_KEY_ZONE_ID);
    }

}
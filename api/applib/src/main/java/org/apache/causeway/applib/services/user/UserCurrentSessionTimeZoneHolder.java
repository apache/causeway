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

import lombok.NonNull;

/**
 * Stores the user's current {@link ZoneId} with session scope.
 * <p>
 * eg. on application login
 *
 * @since 2.0 {@index}
 */
public interface UserCurrentSessionTimeZoneHolder {

    /**
     * Sets the user's current {@link ZoneId}
     * within the context of the current session.
     */
    void setUserTimeZone(@NonNull ZoneId zoneId);

    /**
     * Optionally returns the user's current {@link ZoneId},
     * based on whether it was set before,
     * within the context of the current session.
     *
     * @apiNote not meant to fallback to system defaults,
     * instead return {@link Optional#empty()},
     * if there is no specific time-zone information available
     */
    Optional<ZoneId> getUserTimeZone();

    /**
     * Clears the user's current {@link ZoneId}
     * within the context of the current session.
     */
    void clearUserTimeZone();

}
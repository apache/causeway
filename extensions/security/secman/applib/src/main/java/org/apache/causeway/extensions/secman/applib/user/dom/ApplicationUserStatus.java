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
package org.apache.causeway.extensions.secman.applib.user.dom;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Strings;

/**
 * Whether the user's account is locked or unlocked.
 *
 * @since 2.0 {@index}
 */
public enum ApplicationUserStatus {
    UNLOCKED,
    LOCKED;

    public static ApplicationUserStatus parse(final Boolean unlocked) {
        return unlocked != null && unlocked ? UNLOCKED : LOCKED;
    }

    @Override
    public String toString() {
        return _Strings.capitalize(name());
    }

    public static boolean isUnlocked(final @Nullable ApplicationUserStatus status) {
        return status == UNLOCKED;
    }

    public static boolean isLockedOrUnspecified(final @Nullable ApplicationUserStatus status) {
        return !isUnlocked(status);
    }

    /** Whether can transition to state LOCKED. That is, YES if not already at that state. */
    public static boolean canLock(final @Nullable ApplicationUserStatus status) {
        return status != ApplicationUserStatus.LOCKED;
    }

    /** Whether can transition to state UNLOCKED. That is, YES if not already at that state. */
    public static boolean canUnlock(final @Nullable ApplicationUserStatus status) {
        return status != ApplicationUserStatus.UNLOCKED;
    }

}

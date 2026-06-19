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
package org.apache.causeway.extensions.commandlog.applib.dom;

import lombok.RequiredArgsConstructor;

import org.springframework.lang.Nullable;

/**
 * Introduced in support of the Command Replay Feature.
 *
 * @since 2.x {@index}
 */
@RequiredArgsConstructor
public enum ReplayState {
    /**
     * Default state used when a command is executed.  In the context of regression testing, this can be thought of
     * as a recorded command on the current version of the application.
     */
    UNDEFINED("Recorded OK"),
    /**
     * When replaying (on either reference/current version of the application or on the candidate version),
     * indicates that the command has been imported but not yet been replayed.
     */
    PENDING("Pending"),
    /**
     * When replaying (on either reference/current version of the application or on the candidate version),
     * indicates that the command has been imported and replayed successfully.
     */
    OK("Replayed OK"),
    /**
     * When replaying (on either reference/current version of the application or on the candidate version),
     * indicates that the command has been imported but the attempt to replay it failed.
     */
    FAILED("Replay FAILED"),
    /**
     * Allows a command (either recorded or replayed) to be excluded from the current set.  This can be used to exclude
     * a command from being exported for replay, or to exclude a command imported from being replayed.
     */
    EXCLUDED("Excluded"),
    ;

    private final String title;

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isExportable() {
        return this == ReplayState.UNDEFINED;
    }

    public boolean isPendingOrFailed() {
        return this == ReplayState.PENDING
                || this == ReplayState.FAILED;
    }

    public boolean isReplayOrRetryEnabled() {
        return this == ReplayState.PENDING
                || this == ReplayState.OK
                || this == ReplayState.FAILED;
    }

    // -- NULL SAFE

    public static boolean isPendingOrFailed(final @Nullable ReplayState replayState) {
        return replayState != null && replayState.isPendingOrFailed();
    }

    public static boolean isReplayOrRetryEnabled(final @Nullable ReplayState replayState) {
        return replayState != null && replayState.isReplayOrRetryEnabled();
    }

    public static boolean isOkOrExcluded(ReplayState replayState) {
        return replayState == ReplayState.OK || replayState == ReplayState.EXCLUDED;
    }


}


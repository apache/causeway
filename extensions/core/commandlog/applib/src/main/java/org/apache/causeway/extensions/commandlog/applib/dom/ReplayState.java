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

import org.springframework.lang.Nullable;

/**
 * Introduced in support of the Command Replay Feature.
 *
 * @since 2.x {@index}
 */
public enum ReplayState {
    /**
     * As used on primary system, indicating an initial state.
     */
    UNDEFINED,
    /**
     * Marks a {@link CommandLogEntry} as exported, such that consecutive export actions will skip those.
     */
    EXPORTED,
    /**
     * For use on secondary system, indicates that the command has not yet been replayed.
     */
    PENDING,
    /**
     * For use on secondary system, indicates that the command has been replayed ok
     */
    OK,
    /**
     * For use on secondary system, indicates that the command has been replayed but encountered an error
     */
    FAILED,
    /**
     * For use on secondary system, indicates that the command should not be replayed.
     */
    EXCLUDED;

    public boolean isExported() { return this == EXPORTED; }
    public boolean isFailed() { return this == FAILED; }

    public boolean canExport() {
        return this == ReplayState.UNDEFINED;
    }

    public boolean canReplayOrRetryOrMarkForExclusion() {
        return this == ReplayState.PENDING
                || this == ReplayState.FAILED;
    }

    // -- NULL SAFE

    public static boolean canExport(final @Nullable ReplayState replayState) {
        return replayState!=null
            ? replayState.canExport()
            : true;
    }

    public static boolean canReplayOrRetryOrMarkForExclusion(final @Nullable ReplayState replayState) {
        return replayState!=null
            ? replayState.canReplayOrRetryOrMarkForExclusion()
            : false;
    }

    public static boolean isExported(final ReplayState replayState) {
        return replayState!=null
                ? replayState.isExported()
                : false;
    }

}


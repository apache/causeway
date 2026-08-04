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
package org.apache.causeway.extensions.commandlog.applib.subscriber;

import org.junit.jupiter.api.Test;

import org.apache.causeway.applib.services.command.PauseCommandLoggingEvent;
import org.apache.causeway.applib.services.command.ResumeCommandLoggingEvent;

import static org.assertj.core.api.Assertions.assertThat;

class CommandLogPauseStateTest {

    private final CommandLogPauseState pauseState = new CommandLogPauseState();
    private final CommandLogPauseStateListener listener = new CommandLogPauseStateListener(pauseState);

    @Test
    void nestedPausesRequireMatchingResumes() {
        listener.onPauseCommandLoggingEvent(new PauseCommandLoggingEvent(this));
        listener.onPauseCommandLoggingEvent(new PauseCommandLoggingEvent(this));

        listener.onResumeCommandLoggingEvent(new ResumeCommandLoggingEvent(this));
        assertThat(pauseState.isPaused()).isTrue();

        listener.onResumeCommandLoggingEvent(new ResumeCommandLoggingEvent(this));
        assertThat(pauseState.isPaused()).isFalse();
    }

    @Test
    void unmatchedResumeDoesNotMakeStatePausedOrNegative() {
        listener.onResumeCommandLoggingEvent(new ResumeCommandLoggingEvent(this));
        listener.onResumeCommandLoggingEvent(new ResumeCommandLoggingEvent(this));

        assertThat(pauseState.isPaused()).isFalse();

        listener.onPauseCommandLoggingEvent(new PauseCommandLoggingEvent(this));
        listener.onResumeCommandLoggingEvent(new ResumeCommandLoggingEvent(this));
        assertThat(pauseState.isPaused()).isFalse();
    }
}

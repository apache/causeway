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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.PauseCommandLoggingEvent;
import org.apache.causeway.applib.services.command.ResumeCommandLoggingEvent;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;

import lombok.RequiredArgsConstructor;

/**
 * Maintains command-log pause state in response to application events.
 */
@Service
@Named(CommandLogPauseStateListener.LOGICAL_TYPE_NAME)
@Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class CommandLogPauseStateListener {

    static final String LOGICAL_TYPE_NAME = CausewayModuleExtCommandLogApplib.NAMESPACE + ".CommandLogPauseStateListener";

    private final CommandLogPauseState commandLogPauseState;

    @EventListener(PauseCommandLoggingEvent.class)
    public void onPauseCommandLoggingEvent(final PauseCommandLoggingEvent event) {
        commandLogPauseState.pause();
    }

    @EventListener(ResumeCommandLoggingEvent.class)
    public void onResumeCommandLoggingEvent(final ResumeCommandLoggingEvent event) {
        commandLogPauseState.resume();
    }

}

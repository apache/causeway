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
package org.apache.causeway.extensions.commandlog.applib.dom.replay;

import java.util.Optional;

import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.experimental.UtilityClass;

@UtilityClass
class ReplayPendingBackgroundCommands {

    static final String WAIT_MESSAGE = "Please wait until pending background commands have executed and committed before continuing replay.";

    String disableReason(final ReplayContext replayContext) {
        return hasPendingBackgroundCommands(replayContext) ? WAIT_MESSAGE : null;
    }

    boolean hasPendingBackgroundCommands(final ReplayContext replayContext) {
        return Optional.ofNullable(replayContext)
                .map(ReplayContext::commandLogEntryRepository)
                .map(CommandLogEntryRepository::findBackgroundAndNotYetStarted)
                .map(pendingBackgroundCommands -> !pendingBackgroundCommands.isEmpty())
                .orElse(false);
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.causeway.extensions.sessionlog.applib.spiimpl;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import org.apache.causeway.applib.events.metamodel.MetamodelListener;
import org.apache.causeway.applib.services.clock.ClockService;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntry;
import org.apache.causeway.extensions.sessionlog.applib.dom.SessionLogEntryRepository;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Initializes the <i>Session Log</i> extension when the application is first bootstrapped.
 *
 * <p>
 *     In particular, will automatically logout all sessions from a previous run of the application, if configured to
 *     do so.
 * </p>
 *
 * @since 2.0 {@index}
 */
@Component
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class SessionLogServiceInitializer implements MetamodelListener {

    final SessionLogEntryRepository<? extends SessionLogEntry> sessionLogEntryRepository;
    final InteractionService interactionService;
    final ClockService clockService;
    final CausewayConfiguration causewayConfiguration;

    @Override
    public void onMetamodelLoaded() {
        if (!causewayConfiguration.getExtensions().getSessionLog().isAutoLogoutOnRestart()) {
            return;
        }

        interactionService.runAnonymous(() -> {
            val timestamp = clockService.getClock().nowAsJavaSqlTimestamp();
            sessionLogEntryRepository.logoutAllSessions(timestamp);
        });
    }

}

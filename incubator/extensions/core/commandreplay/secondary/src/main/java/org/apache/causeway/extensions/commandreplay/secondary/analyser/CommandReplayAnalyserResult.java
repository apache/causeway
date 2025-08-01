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
package org.apache.causeway.extensions.commandreplay.secondary.analyser;

import java.util.Objects;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.causeway.applib.util.schema.CommandDtoUtils;
import org.apache.causeway.core.config.CausewayConfiguration;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandreplay.secondary.CausewayModuleExtCommandReplaySecondary;
import org.apache.causeway.schema.common.v2.InteractionType;

import lombok.RequiredArgsConstructor;

/**
 * @since 2.0 {@index}
 */
@Service
@Named(CausewayModuleExtCommandReplaySecondary.NAMESPACE + ".CommandReplayAnalyserResult")
@jakarta.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandReplayAnalyserResult implements CommandReplayAnalyser {

    private final CausewayConfiguration causewayConfiguration;
    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = causewayConfiguration.extensions().commandReplay().analyser().exception().enabled();
    }

    @Override
    public String analyzeReplay(final CommandLogEntry commandLogEntry) {
        if(!enabled) {
            return null;
        }

        var dto = commandLogEntry.getCommandDto();
        if(dto.getMember().getInteractionType() == InteractionType.PROPERTY_EDIT) {
            return null;
        }

        // see if the outcome was the same...
        // ... either the same result when replayed
        var primaryResultStr = CommandDtoUtils.getUserData(dto, UserDataKeys.RESULT);

        var secondaryResult = commandLogEntry.getResult();
        var secondaryResultStr =
                secondaryResult != null ? secondaryResult.toString() : null;
        return Objects.equals(primaryResultStr, secondaryResultStr)
                ? null
                : String.format(
                        "Results differ.  Primary was '%s', secondary is '%s'",
                        primaryResultStr, secondaryResultStr);
    }

}

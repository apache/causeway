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
package org.apache.isis.extensions.commandreplay.secondary.analyser;

import java.util.Objects;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotations.PriorityPrecedence;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.commandlog.model.command.CommandModel;
import org.apache.isis.schema.common.v2.InteractionType;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * @since 2.0 {@index}
 */
@Service
@Named("isis.ext.commandReplaySecondary.CommandReplayAnalyserResult")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandReplayAnalyserResult implements CommandReplayAnalyser {

    private final IsisConfiguration isisConfiguration;
    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = isisConfiguration.getExtensions().getCommandReplay().getAnalyser().getException().isEnabled();
    }

    @Override
    public String analyzeReplay(final CommandModel commandModel) {
        if(!enabled) {
            return null;
        }

        val dto = commandModel.getCommandDto();
        if(dto.getMember().getInteractionType() == InteractionType.PROPERTY_EDIT) {
            return null;
        }

        // see if the outcome was the same...
        // ... either the same result when replayed
        val primaryResultStr = CommandDtoUtils.getUserData(dto, UserDataKeys.RESULT);

        val secondaryResult = commandModel.getResult();
        val secondaryResultStr =
                secondaryResult != null ? secondaryResult.toString() : null;
        return Objects.equals(primaryResultStr, secondaryResultStr)
                ? null
                : String.format(
                        "Results differ.  Primary was '%s', secondary is '%s'",
                        primaryResultStr, secondaryResultStr);
    }

}

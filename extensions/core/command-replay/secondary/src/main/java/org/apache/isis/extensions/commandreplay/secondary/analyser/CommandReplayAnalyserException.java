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

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.commanddto.conmap.UserDataKeys;
import org.apache.isis.applib.util.schema.CommandDtoUtils;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.extensions.commandlog.impl.jdo.CommandJdo;
import org.apache.isis.schema.common.v2.InteractionType;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isisExtensionsCommandReplaySecondary.CommandReplayAnalyserException")
@Order(OrderPrecedence.MIDPOINT)
@RequiredArgsConstructor
public class CommandReplayAnalyserException implements CommandReplayAnalyser {

    private final IsisConfiguration isisConfiguration;
    private boolean enabled;

    @PostConstruct
    public void init() {
        enabled = isisConfiguration.getExtensions().getCommandReplay().getAnalyser().getResult().isEnabled();
    }

    @Override
    public String analyzeReplay(final CommandJdo commandJdo) {
        if(!enabled) {
            return null;
        }

        val dto = commandJdo.getCommandDto();
        if(dto.getMember().getInteractionType() == InteractionType.PROPERTY_EDIT) {
            return null;
        }

        val primaryException = CommandDtoUtils.getUserData(dto, UserDataKeys.EXCEPTION);
        if (_Strings.isNullOrEmpty(primaryException)) {
            return null;
        }

        val replayedException = commandJdo.getException();

        val primaryExceptionTrimmed = trimmed(primaryException);
        val replayedExceptionTrimmed = trimmed(replayedException);
        return Objects.equals(primaryExceptionTrimmed, replayedExceptionTrimmed)
                ? null
                : String.format("Exceptions differ.  On primary system was '%s'", primaryException);
    }

    private String trimmed(final String str) {
        return withoutWhitespace(initialPartOfStackTrace(str));
    }

    // we only look at beginning of the stack trace because the latter part will differ when replayed
    private String initialPartOfStackTrace(final String str) {
        final int toInspectOfStackTrace = 500;
        return str.length() > toInspectOfStackTrace ? str.substring(0, toInspectOfStackTrace) : str;
    }

    private String withoutWhitespace(final String s) {
        return s.replaceAll("\\s", "");
    }

}

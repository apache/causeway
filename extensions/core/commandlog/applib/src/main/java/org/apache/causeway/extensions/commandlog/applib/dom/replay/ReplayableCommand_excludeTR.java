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

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.extensions.commandlog.applib.dom.ReplayState;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_excludeTR.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        hidden = Where.OBJECT_FORMS,
        sequence = "2.2",
        associateWith = "replayState",
        describedAs = "Marks Command to be EXCLUDED from replay."
)
@RequiredArgsConstructor
public class ReplayableCommand_excludeTR {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_excludeTR> {
    }

    private final ReplayableCommand replayableCommand;

    /**
     * @param replayState = unused, but required as a workaround for table row collections which will otherwise NPE.
     * @return
     */
    @MemberSupport
    public ReplayableCommand act(ReplayState replayState) {
        return replayableCommand.exclude();
    }

    public ReplayState default0Act() {
        return ReplayState.EXCLUDED;
    };
    public String disable0Act() {
        return "Exclude the command";
    };

    @MemberSupport
    private boolean hideAct() {
        return replayableCommand.getReplayState() == ReplayState.EXCLUDED;
    }
}

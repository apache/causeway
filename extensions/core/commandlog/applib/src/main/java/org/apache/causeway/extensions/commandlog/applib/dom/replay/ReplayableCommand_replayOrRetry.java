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

import lombok.RequiredArgsConstructor;

import org.apache.causeway.applib.annotation.*;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.NON_IDEMPOTENT,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_replayOrRetry.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        sequence = "0.1",
        cssClassFa = "solid circle-play",
        cssClass = "btn-primary"
        //hidden = Where.NOWHERE // show in tables //TODO NPE bug
)
@RequiredArgsConstructor
public class ReplayableCommand_replayOrRetry {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_replayOrRetry> {
    }

    private final ReplayableCommand replayableCommand;


    @MemberSupport
    public ReplayableCommand act() {
        replayableCommand.tryReplayOrRetry();
        return replayableCommand;
    }

    @MemberSupport
    public String disableAct() {
        return replayableCommand.disableReplayOrRetry();
    }
}

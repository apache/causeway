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

import jakarta.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.ParameterLayout;
import org.apache.causeway.applib.annotation.Publishing;
import org.apache.causeway.applib.annotation.RestrictTo;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.value.Clob;

import lombok.RequiredArgsConstructor;

@Action(
        restrictTo = RestrictTo.PROTOTYPING,
        semantics = SemanticsOf.SAFE,
        commandPublishing = Publishing.DISABLED,
        domainEvent = ReplayableCommand_exportTR.DomainEvent.class,
        executionPublishing = Publishing.DISABLED
)
@ActionLayout(
        named = "Export",
        describedAs = "Exports this command as result-bearing YAML",
        hidden = Where.OBJECT_FORMS // table row action.
)
@RequiredArgsConstructor
public class ReplayableCommand_exportTR {

    public static class DomainEvent extends ReplayableCommand.ActionDomainEvent<ReplayableCommand_exportTR> {
    }

    private final ReplayableCommand replayableCommand;

    @MemberSupport
    public Clob act(
            @ParameterLayout(describedAs = "File name prefix for the exported YAML")
            final String filenamePrefix,
            @ParameterLayout(describedAs = "Replace recorded bookmark identities with replay mappings")
            final boolean remapResults) {
        return mixin().act(filenamePrefix, remapResults);
    }

    @MemberSupport
    public String disableAct() {
        return mixin().disableAct();
    }

    @MemberSupport public String defaultFilenamePrefix() {
        return mixin().defaultFilenamePrefix();
    }

    @MemberSupport public boolean defaultRemapResults() {
        return mixin().defaultRemapResults();
    }

    private ReplayableCommand_export mixin() {
        return factoryService.mixin(ReplayableCommand_export.class, replayableCommand);
    }

    @Inject private FactoryService factoryService;

}

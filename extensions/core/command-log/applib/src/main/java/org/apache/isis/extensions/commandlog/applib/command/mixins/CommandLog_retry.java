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
package org.apache.isis.extensions.commandlog.applib.command.mixins;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.applib.annotation.Publishing;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.command.CommandExecutorService;
import org.apache.isis.applib.services.iactnlayer.InteractionService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ReplayState;

import lombok.RequiredArgsConstructor;

@Action(
    semantics = SemanticsOf.NON_IDEMPOTENT_ARE_YOU_SURE,
    domainEvent = CommandLog_retry.ActionDomainEvent.class,
    commandPublishing = Publishing.DISABLED
)
@ActionLayout(associateWith = "executeIn", sequence = "1")
@RequiredArgsConstructor
public class CommandLog_retry {

    private final CommandLog commandLog;

    public static class ActionDomainEvent
        extends IsisModuleExtCommandLogApplib.ActionDomainEvent<CommandLog_retry> { }

    @MemberSupport
    public CommandLog act() {

        commandLog.setReplayState(ReplayState.PENDING);
        commandLog.setResult(null);
        commandLog.setException((Exception)null);
        commandLog.setStartedAt(null);
        commandLog.setCompletedAt(null);

        return commandLog;
    }

    @Inject InteractionService interactionService;
    @Inject TransactionService transactionService;
    @Inject CommandExecutorService commandExecutorService;
    @Inject RepositoryService repositoryService;
    @Inject BookmarkService bookmarkService;
    @Inject MetaModelService metaModelService;


}

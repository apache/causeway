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
package org.apache.causeway.extensions.commandlog.applib.dom.mixins;

import javax.inject.Inject;

import org.apache.causeway.applib.annotation.Action;
import org.apache.causeway.applib.annotation.ActionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.applib.annotation.SemanticsOf;
import org.apache.causeway.applib.services.bookmark.BookmarkService;
import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Contributes an action to open the domain object that was the result of invoking the command (represented in
 * persistent form by the {@link CommandLogEntry}).
 *
 * @since 2.x {@index}
 */
@Action(
    semantics = SemanticsOf.SAFE,
    domainEvent = CommandLogEntry_openResultObject.ActionDomainEvent.class
)
@ActionLayout(named = "Open", associateWith = "result", sequence="1")
@RequiredArgsConstructor
public class CommandLogEntry_openResultObject {

    public static class ActionDomainEvent
            extends CausewayModuleExtCommandLogApplib.ActionDomainEvent<CommandLogEntry_openResultObject> { }

    private final CommandLogEntry commandLogEntry;

    @MemberSupport public Object act() {
        val targetBookmark = bookmarkService.lookup(commandLogEntry.getResult()).orElse(null);
        if(targetBookmark == null) {
            messageService.warnUser("Object not found - has it since been deleted?");
            return null;
        }
        return targetBookmark;
    }
    @MemberSupport public boolean hideAct() {
        return commandLogEntry.getResult() == null;
    }

    @Inject BookmarkService bookmarkService;
    @Inject MessageService messageService;


}

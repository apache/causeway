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
package org.apache.isis.extensions.commandlog.impl.jdo;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.ActionLayout;
import org.apache.isis.applib.annotation.SemanticsOf;
import org.apache.isis.applib.services.bookmark.BookmarkService;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;

import lombok.val;

@Action(
    semantics = SemanticsOf.SAFE,
    domainEvent = CommandJdo_openResultObject.ActionDomainEvent.class,
    associateWith = "result"
)
@ActionLayout(named = "Open", sequence="1")
public class CommandJdo_openResultObject {

    public static class ActionDomainEvent
            extends IsisModuleExtCommandLogImpl.ActionDomainEvent<CommandJdo_openResultObject> { }

    private final CommandJdo commandJdo;
    public CommandJdo_openResultObject(CommandJdo commandJdo) {
        this.commandJdo = commandJdo;
    }

    public Object act() {
        val targetBookmark = bookmarkService.lookup(commandJdo.getResult());
        if(targetBookmark == null) {
            messageService.warnUser("Object not found - has it since been deleted?");
            return null;
        }
        return targetBookmark;
    }
    public boolean hideAct() {
        return commandJdo.getResult() == null;
    }

    @Inject BookmarkService bookmarkService;
    @Inject MessageService messageService;


}

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

import java.util.Collections;
import java.util.List;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;

/**
 * Contributes a <code>siblingCommands</code> collection to the {@link CommandLogEntry}), in other words those
 * commands to be run in the background that have the same originating (parent) command.
 *
 * @since 2.x {@index}
 */
@Collection(
    domainEvent = CommandLogEntry_siblingCommands.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    sequence = "100.110"
)
@RequiredArgsConstructor
public class CommandLogEntry_siblingCommands {

    public static class CollectionDomainEvent
            extends CausewayModuleExtCommandLogApplib.CollectionDomainEvent<CommandLogEntry_siblingCommands, CommandLogEntry> { }

    private final CommandLogEntry commandLogEntry;

    @MemberSupport public List<? extends CommandLogEntry> coll() {
        final CommandLogEntry parentJdo = commandLogEntry.getParent();
        if(parentJdo == null) {
            return Collections.emptyList();
        }
        final List<? extends CommandLogEntry> siblingCommands = commandLogEntryRepository.findByParent(parentJdo);
        siblingCommands.remove(commandLogEntry);
        return siblingCommands;
    }


    @javax.inject.Inject
    private CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;

}

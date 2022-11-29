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

import java.util.List;

import org.apache.causeway.applib.annotation.Collection;
import org.apache.causeway.applib.annotation.CollectionLayout;
import org.apache.causeway.applib.annotation.MemberSupport;
import org.apache.causeway.extensions.commandlog.applib.CausewayModuleExtCommandLogApplib;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntry;
import org.apache.causeway.extensions.commandlog.applib.dom.CommandLogEntryRepository;

import lombok.RequiredArgsConstructor;


/**
 * Contributes the <code>childCommands</code> collection to {@link CommandLogEntry} entity.
 *
 * @since 2.x {@index}
 */
@Collection(
    domainEvent = CommandLogEntry_childCommands.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    sequence = "100.100"
)
@RequiredArgsConstructor
public class CommandLogEntry_childCommands {

    public static class CollectionDomainEvent
            extends CausewayModuleExtCommandLogApplib.CollectionDomainEvent<CommandLogEntry_childCommands, CommandLogEntry> { }

    private final CommandLogEntry commandLogEntry;

    @MemberSupport public List<? extends CommandLogEntry> coll() {
        return commandLogEntryRepository.findByParent(commandLogEntry);
    }

    @javax.inject.Inject
    private CommandLogEntryRepository<? extends CommandLogEntry> commandLogEntryRepository;

}

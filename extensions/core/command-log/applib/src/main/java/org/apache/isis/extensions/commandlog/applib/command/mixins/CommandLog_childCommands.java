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

import java.util.List;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberSupport;
import org.apache.isis.extensions.commandlog.applib.IsisModuleExtCommandLogApplib;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLog;
import org.apache.isis.extensions.commandlog.applib.command.CommandLog;
import org.apache.isis.extensions.commandlog.applib.command.ICommandLogRepository;

import lombok.RequiredArgsConstructor;


@Collection(
    domainEvent = CommandLog_childCommands.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table",
    sequence = "100.100"
)
@RequiredArgsConstructor
public class CommandLog_childCommands {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogApplib.CollectionDomainEvent<CommandLog_childCommands, ICommandLog> { }

    private final CommandLog commandLog;

    @MemberSupport
    public List<CommandLog> coll() {
        return commandLogRepository.findByParent(commandLog);
    }

    @javax.inject.Inject
    private ICommandLogRepository<CommandLog> commandLogRepository;

}

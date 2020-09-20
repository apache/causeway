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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Collection;
import org.apache.isis.applib.annotation.CollectionLayout;
import org.apache.isis.applib.annotation.MemberOrder;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.extensions.commandlog.impl.IsisModuleExtCommandLogImpl;

@Collection(
    domainEvent = CommandJdo_siblingCommands.CollectionDomainEvent.class
)
@CollectionLayout(
    defaultView = "table"
)
public class CommandJdo_siblingCommands {

    public static class CollectionDomainEvent
            extends IsisModuleExtCommandLogImpl.CollectionDomainEvent<CommandJdo_siblingCommands, CommandJdo> { }

    private final CommandJdo commandJdo;
    public CommandJdo_siblingCommands(final CommandJdo commandJdo) {
        this.commandJdo = commandJdo;
    }

    @MemberOrder(sequence = "100.110")
    public List<CommandJdo> coll() {
        final CommandJdo parentJdo = commandJdo.getParent();
        if(parentJdo == null) {
            return Collections.emptyList();
        }
        final List<CommandJdo> siblingCommands = commandJdoRepository.findByParent(parentJdo);
        siblingCommands.remove(commandJdo);
        return siblingCommands;
    }


    @Inject CommandJdoRepository commandJdoRepository;
    
}

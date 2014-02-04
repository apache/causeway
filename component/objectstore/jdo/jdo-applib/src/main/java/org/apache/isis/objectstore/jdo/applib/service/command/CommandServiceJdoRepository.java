/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.objectstore.jdo.applib.service.command;

import java.util.List;
import java.util.UUID;

import org.apache.isis.applib.AbstractFactoryAndRepository;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.query.QueryDefault;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;


public class CommandServiceJdoRepository extends AbstractFactoryAndRepository {

    @Programmatic
    public CommandJdo findByTransactionId(final UUID transactionId) {
        persistCurrentCommandIfRequired();
        return firstMatch(
                new QueryDefault<CommandJdo>(CommandJdo.class, 
                        "findByTransactionId", 
                        "transactionId", transactionId.toString()));
    }

    @Programmatic
    public List<CommandJdo> findCurrent() {
        persistCurrentCommandIfRequired();
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, "findCurrent"));
    }
    
    @Programmatic
    public List<CommandJdo> findCompleted() {
        persistCurrentCommandIfRequired();
        return allMatches(
                new QueryDefault<CommandJdo>(CommandJdo.class, "findCompleted"));
    }

    private void persistCurrentCommandIfRequired() {
        if(commandContext == null || commandService == null) {
            return;
        } 
        final Command command = commandContext.getCommand();
        final CommandJdo commandJdo = commandService.asUserInitiatedCommandJdo(command);
        if(commandJdo == null) {
            return;
        } 
        persistIfNotAlready(commandJdo);
    }

    // //////////////////////////////////////

    
    @javax.inject.Inject
    private CommandServiceJdo commandService;
    
    @javax.inject.Inject
    private CommandContext commandContext;

}

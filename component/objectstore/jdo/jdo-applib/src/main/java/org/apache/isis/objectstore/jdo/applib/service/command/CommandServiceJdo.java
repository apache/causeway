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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AbstractService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.spi.CommandService;

public class CommandServiceJdo extends AbstractService implements CommandService {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(CommandServiceJdo.class);

    /**
     * Creates an {@link CommandJdo}, initializing its 
     * {@link Command#setNature(Command.Nature) nature} to be
     * {@link Command.Nature#OTHER rendering}.
     */
    @Programmatic
    @Override
    public Command create() {
        CommandJdo command = newTransientInstance(CommandJdo.class);
        command.setNature(Command.Nature.OTHER);
        return command;
    }

    @Programmatic
    @Override
    public void startTransaction(final Command command, final UUID transactionId) {
        if(command instanceof CommandJdo) {
            // should be the case, since this service created the object in the #create() method
            final CommandJdo commandJdo = (CommandJdo) command;
            final UUID currentTransactionId = commandJdo.getTransactionId();
            if(currentTransactionId != null && !currentTransactionId.equals(transactionId)) {
                // the logic in IsisTransaction means that any subsequent transactions within a given command
                // should reuse the xactnId of the first transaction created within that interaction.
                throw new IllegalStateException("Attempting to set a different transactionId on command");
            }
            commandJdo.setTransactionId(transactionId);
        }
    }

    @Programmatic
    @Override
    public void complete(final Command command) {
        CommandJdo commandJdo = asUserInitiatedCommandJdo(command);
        if(commandJdo == null) {
            return;
        }
            
        commandJdo.setCompletedAt(Clock.getTimeAsJavaSqlTimestamp());
        persistIfNotAlready(commandJdo);
    }

    /**
     * Not API, factored out from {@link CommandServiceJdoRepository}.
     */
    CommandJdo asUserInitiatedCommandJdo(final Command command) {
        if(!(command instanceof CommandJdo)) {
            // ought not to be the case, since this service created the object in the #create() method
            return null;
        }
        if(command.getNature() != Command.Nature.USER_INITIATED) {
            return null;
        } 
        final CommandJdo commandJdo = (CommandJdo) command;
        if(!commandJdo.isPersistHint()) {
            return null;
        } 
        return commandJdo;
    }
    
    
    
    
}

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
package org.apache.isis.persistence.jdo.integration.transaction;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.collections._Inbox;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.persistence.jdo.integration.persistence.command.CreateObjectCommand;
import org.apache.isis.persistence.jdo.integration.persistence.command.DeleteObjectCommand;
import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommand;

import lombok.NonNull;
import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
final class _PersistenceCommandQueue {

    private final _Inbox<PersistenceCommand> persistenceCommands = new _Inbox<>();

    /**
     * Add the non-null command to the list of commands to execute at the end of
     * the transaction.
     */
    public void append(final @Nullable PersistenceCommand command) {
        if (command == null) {
            return;
        }

        final ManagedObject entity = command.getEntity();

        // Destroys are ignored when preceded by a create, or another destroy
        if (command instanceof DeleteObjectCommand) {
            if (alreadyHasCreate(entity)) {
                removeCreate(entity);
                if (log.isDebugEnabled()) {
                    log.debug("ignored both create and destroy command {}", command);
                }
                return;
            }

            if (alreadyHasDestroy(entity)) {
                if (log.isDebugEnabled()) {
                    log.debug("ignored command {} as command already recorded", command);
                }
                return;
            }
        }

        log.debug("add command {}", command);
        persistenceCommands.add(command);
    }
    
    public void drain(final @NonNull Consumer<Can<PersistenceCommand>> onMoreCommands) {

        //
        // it's possible that in executing these commands that more will be created.
        // so we keep flushing until no more are available (ISIS-533)
        //
        // this is a do...while rather than a while... just for backward compatibility
        // with previous algorithm that always went through the execute phase at least once.
        //
        do {
            // this algorithm ensures that we never execute the same command twice,
            // and also allow new commands to be added to end
            val pc_snapshot = persistenceCommands.snapshotThenClear();

            if(!pc_snapshot.isEmpty()) {
                try {
                    
                    onMoreCommands.accept(pc_snapshot);
                    
                } catch (RuntimeException ex) {
                    // if there's an exception, we want to make sure that
                    // all commands are cleared and propagate
                    persistenceCommands.clear();
                    throw ex;
                }
            }
        } while(!persistenceCommands.isEmpty());

    }
    
    @Override
    public String toString() {
        return String.format("CommandQueue[%s]", persistenceCommands.snapshot());
    }
    
    // -- HELPER
    
    private boolean alreadyHasCommand(final Class<?> commandClass, final ManagedObject onObject) {
        return getCommand(commandClass, onObject) != null;
    }

    private boolean alreadyHasCreate(final ManagedObject onObject) {
        return alreadyHasCommand(CreateObjectCommand.class, onObject);
    }

    private boolean alreadyHasDestroy(final ManagedObject onObject) {
        return alreadyHasCommand(DeleteObjectCommand.class, onObject);
    }

    private PersistenceCommand getCommand(final Class<?> commandClass, final ManagedObject entity) {
        for (final PersistenceCommand command : persistenceCommands.snapshot()) {
            if (command.getEntity().equals(entity)) {
                if (commandClass.isAssignableFrom(command.getClass())) {
                    return command;
                }
            }
        }
        return null;
    }

    private void removeCommand(final Class<?> commandClass, final ManagedObject onObject) {
        final PersistenceCommand toDelete = getCommand(commandClass, onObject);
        persistenceCommands.remove(toDelete);
    }

    private void removeCreate(final ManagedObject onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
    }
    

    
}

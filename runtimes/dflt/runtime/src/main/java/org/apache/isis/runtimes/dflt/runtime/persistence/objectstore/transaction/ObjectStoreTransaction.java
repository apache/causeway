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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.ObjectStoreTransactionManagement;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionAbstract;
import org.apache.log4j.Logger;

public class ObjectStoreTransaction extends IsisTransactionAbstract {
    private static final Logger LOG = Logger.getLogger(ObjectStoreTransaction.class);

    private final ObjectStoreTransactionManagement objectStore;
    private final List<PersistenceCommand> commands = new ArrayList<PersistenceCommand>();

    public ObjectStoreTransaction(final IsisTransactionManager transactionManager, final MessageBroker messageBroker,
        final UpdateNotifier updateNotifier, final ObjectStoreTransactionManagement objectStore) {
        super(transactionManager, messageBroker, updateNotifier);
        this.objectStore = objectStore;
        if (LOG.isDebugEnabled()) {
            LOG.debug("new transaction " + this);
        }
    }

    // //////////////////////////////////////////////////////////
    // Commands
    // //////////////////////////////////////////////////////////

    /**
     * Add the non-null command to the list of commands to execute at the end of the transaction.
     */
    public void addCommand(final PersistenceCommand command) {
        if (command == null) {
            return;
        }

        final ObjectAdapter onObject = command.onObject();

        // Saves are ignored when preceded by another save, or a delete
        if (command instanceof SaveObjectCommand) {
            if (alreadyHasCreate(onObject) || alreadyHasSave(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command as object already created/saved" + command);
                }
                return;
            }

            if (alreadyHasDestroy(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command " + command + " as object no longer exists");
                }
                return;
            }
        }

        // Destroys are ignored when preceded by a create, or another destroy
        if (command instanceof DestroyObjectCommand) {
            if (alreadyHasCreate(onObject)) {
                removeCreate(onObject);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored both create and destroy command " + command);
                }
                return;
            }

            if (alreadyHasSave(onObject)) {
                removeSave(onObject);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("removed prior save command " + command);
                }
            }

            if (alreadyHasDestroy(onObject)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("ignored command " + command + " as command already recorded");
                }
                return;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("add command " + command);
        }
        commands.add(command);
    }

    // //////////////////////////////////////////////////////////
    // abort
    // //////////////////////////////////////////////////////////

    @Override
    public void doAbort() {
    }

    // //////////////////////////////////////////////////////////
    // commit
    // //////////////////////////////////////////////////////////

    @Override
    public void doFlush() {

        if (commands.size() > 0) {
            objectStore.execute(Collections.unmodifiableList(commands));

            for (final PersistenceCommand command : commands) {
                if (command instanceof DestroyObjectCommand) {
                    final ObjectAdapter adapter = command.onObject();
                    adapter.setOptimisticLock(null);
                    adapter.changeState(ResolveState.DESTROYED);
                }
            }
            commands.clear();
        }
    }

    // //////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////

    private boolean alreadyHasCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        return getCommand(commandClass, onObject) != null;
    }

    private boolean alreadyHasCreate(final ObjectAdapter onObject) {
        return alreadyHasCommand(CreateObjectCommand.class, onObject);
    }

    private boolean alreadyHasDestroy(final ObjectAdapter onObject) {
        return alreadyHasCommand(DestroyObjectCommand.class, onObject);
    }

    private boolean alreadyHasSave(final ObjectAdapter onObject) {
        return alreadyHasCommand(SaveObjectCommand.class, onObject);
    }

    private PersistenceCommand getCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        for (final PersistenceCommand command : commands) {
            if (command.onObject().equals(onObject)) {
                if (commandClass.isAssignableFrom(command.getClass())) {
                    return command;
                }
            }
        }
        return null;
    }

    private void removeCommand(final Class<?> commandClass, final ObjectAdapter onObject) {
        final PersistenceCommand toDelete = getCommand(commandClass, onObject);
        commands.remove(toDelete);
    }

    private void removeCreate(final ObjectAdapter onObject) {
        removeCommand(CreateObjectCommand.class, onObject);
    }

    private void removeSave(final ObjectAdapter onObject) {
        removeCommand(SaveObjectCommand.class, onObject);
    }

    @Override
    protected ToString appendTo(final ToString str) {
        super.appendTo(str);
        str.append("commands", commands.size());
        return str;
    }

}

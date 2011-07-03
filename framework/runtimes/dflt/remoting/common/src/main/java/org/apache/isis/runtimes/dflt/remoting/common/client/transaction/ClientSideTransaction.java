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

package org.apache.isis.runtimes.dflt.remoting.common.client.transaction;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.MessageBroker;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.UpdateNotifier;
import org.apache.isis.runtimes.dflt.runtime.transaction.IsisTransactionAbstract;

/**
 * Encapsulates a transaction occurring on the client, where each of the actions (add, remove and change) are then
 * passed to the server as an atomic unit.
 * 
 * <p>
 * Each action is captured as an {@link ClientTransactionEvent} object.
 */
public class ClientSideTransaction extends IsisTransactionAbstract {

    private final List<ClientTransactionEvent> transactionEvents = new ArrayList<ClientTransactionEvent>();

    // //////////////////////////////////////////////////////////////
    // Constructor
    // //////////////////////////////////////////////////////////////

    public ClientSideTransaction(final IsisTransactionManager transactionManager, final MessageBroker messageBroker,
        final UpdateNotifier updateNotifier) {
        super(transactionManager, messageBroker, updateNotifier);
    }

    // //////////////////////////////////////////////////////////////
    // create (makePersistent)
    // //////////////////////////////////////////////////////////////

    /**
     * Add an event to transaction for the adding of the specified object.
     */
    public void addMakePersistent(final ObjectAdapter object) {
        add(new ClientTransactionEvent(object, ClientTransactionEvent.ADD));
    }

    // //////////////////////////////////////////////////////////////
    // modify (objectChanged)
    // //////////////////////////////////////////////////////////////

    /**
     * Add an event to transaction for the updating of the specified object.
     */
    public void addObjectChanged(final ObjectAdapter object) {
        add(new ClientTransactionEvent(object, ClientTransactionEvent.CHANGE));
    }

    // //////////////////////////////////////////////////////////////
    // delete (destroy)
    // //////////////////////////////////////////////////////////////

    /**
     * Add an event to transaction for the destruction of the specified object.
     */
    public void addDestroyObject(final ObjectAdapter object) {
        add(new ClientTransactionEvent(object, ClientTransactionEvent.DELETE));
    }

    // //////////////////////////////////////////////////////////////
    // Entries
    // //////////////////////////////////////////////////////////////

    /**
     * Return all the events for the transaction.
     */
    public ClientTransactionEvent[] getEntries() {
        return transactionEvents.toArray(new ClientTransactionEvent[] {});
    }

    /**
     * Returns true if there are no entries, hence the transaction is not used.
     */
    public boolean isEmpty() {
        return transactionEvents.size() == 0;
    }

    // //////////////////////////////////////////////////////////////
    // commit, abort
    // //////////////////////////////////////////////////////////////

    @Override
    protected void doFlush() {
        // nothing to do
    }

    /**
     * TODO: need to restore the state of all involved objects
     */
    @Override
    public void doAbort() {

    }

    // //////////////////////////////////////////////////////////////
    // Helpers
    // //////////////////////////////////////////////////////////////

    private void add(final ClientTransactionEvent entry) {
        if (!transactionEvents.contains(entry)) {
            transactionEvents.add(entry);
        }
    }

}

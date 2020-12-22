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

import java.util.List;

import javax.jdo.PersistenceManager;

import org.apache.isis.persistence.jdo.integration.persistence.HasPersistenceManager;
import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommand;

import lombok.NonNull;

interface _TxHelper extends HasPersistenceManager {
    
    public static _TxHelper create(final @NonNull HasPersistenceManager pmProvider) {
        return new _TxHelper() {
            @Override public PersistenceManager getPersistenceManager() {
                return pmProvider.getPersistenceManager();
            }
        };
    }
    
    /**
     * to tell the underlying object store to start a transaction.
     */
    default void startTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            throw new IllegalStateException("Transaction already active");
        }
        transaction.begin();
    }
    
    /**
     * to tell the underlying object store to commit a transaction.
     */
    default void endTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.commit();
        }
    }

    /**
     * to tell the underlying object store to abort a transaction.
     */
    default void abortTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.rollback();
        }
    }
    
    /**
     * to tell the underlying object store to flush a transaction.
     */
    default void flushTransaction() {
        final javax.jdo.Transaction transaction = getPersistenceManager().currentTransaction();
        if (transaction.isActive()) {
            transaction.getPersistenceManager().flush();
        }
    }
    
    default void execute(final List<PersistenceCommand> commands) {

        // previously we used to check that there were some commands, and skip processing otherwise.
        // we no longer do that; it could be (is quite likely) that DataNucleus has some dirty objects anyway that
        // don't have commands wrapped around them...

        for (final PersistenceCommand command : commands) {
            command.execute();
        }
        getPersistenceManager().flush();
    }
    
}

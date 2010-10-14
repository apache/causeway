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


package org.apache.isis.runtime.persistence.objectstore.transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.commons.ensure.Ensure.ensureThatArg;

import org.apache.log4j.Logger;
import org.apache.isis.runtime.persistence.PersistenceSessionTransactionManagement;
import org.apache.isis.runtime.persistence.objectstore.ObjectStoreTransactionManagement;
import org.apache.isis.runtime.transaction.IsisTransactionManagerAbstract;
import org.apache.isis.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.runtime.transaction.updatenotifier.UpdateNotifier;


public class ObjectStoreTransactionManager extends IsisTransactionManagerAbstract<ObjectStoreTransaction> {

    private static final Logger LOG = Logger.getLogger(ObjectStoreTransactionManager.class);

    private final PersistenceSessionTransactionManagement objectPersistor;
    private final ObjectStoreTransactionManagement objectStore;
    
    
    // package level visibility so tests can look at directly
    int transactionLevel;

    public ObjectStoreTransactionManager(
            final PersistenceSessionTransactionManagement objectPersistor, 
            final ObjectStoreTransactionManagement objectStore) {
        this.objectPersistor = objectPersistor;
        this.objectStore = objectStore;
    }


    ////////////////////////////////////////////////////////
    // start, flush, abort, end
    ////////////////////////////////////////////////////////

    public void startTransaction() {
        
    	boolean noneInProgress = false;
        if (getTransaction() == null || getTransaction().getState().isComplete()) {
        	noneInProgress = true;
        	
            createTransaction();
            transactionLevel = 0;
            objectStore.startTransaction();
        }
        
        transactionLevel++;
        
        if (LOG.isInfoEnabled()) {
        	LOG.info(
        			"startTransaction: level " + 
        			(transactionLevel-1) + "->" + (transactionLevel) + 
        			(noneInProgress?" (no transaction in progress or was previously completed; transaction created)":""));
        }
    }

    public boolean flushTransaction() {
    	
        if (LOG.isDebugEnabled()) {
        	LOG.debug("flushTransaction");
        }

        if (getTransaction() != null) {
            objectPersistor.objectChangedAllDirty();
            getTransaction().flush();
        }
        return false;
    }

    public void endTransaction() {
        if (LOG.isInfoEnabled()) {
        	LOG.info("endTransaction: level " + 
        			(transactionLevel) + "->" + (transactionLevel-1));
        }

        transactionLevel--;
        if (transactionLevel == 0) {
        	LOG.info("endTransaction: committing");
            objectPersistor.objectChangedAllDirty();
            getTransaction().commit();
            objectStore.endTransaction();
        } else if (transactionLevel < 0) {
        	LOG.error("endTransaction: transactionLevel=" + transactionLevel);
            transactionLevel = 0;
            throw new IllegalStateException(" no transaction running to end (transactionLevel < 0)");
        }
    }

    public void abortTransaction() {
        if (getTransaction() != null) {
            getTransaction().abort();
            transactionLevel = 0;
            objectStore.abortTransaction();
        }
    }

    
    //////////////////////////////////////////////////////////////////
    // Not public API 
    //////////////////////////////////////////////////////////////////

    public void addCommand(final PersistenceCommand command) {
        getTransaction().addCommand(command);
    }


    
    ////////////////////////////////////////////////////////////////
    // Hooks
    ////////////////////////////////////////////////////////////////

    protected ObjectStoreTransaction createTransaction(
            final MessageBroker messageBroker, 
            final UpdateNotifier updateNotifier) {
        ensureThatArg(messageBroker, is(not(nullValue())));
        ensureThatArg(updateNotifier, is(not(nullValue())));

        return new ObjectStoreTransaction(this, messageBroker, updateNotifier, objectStore);
    }




}


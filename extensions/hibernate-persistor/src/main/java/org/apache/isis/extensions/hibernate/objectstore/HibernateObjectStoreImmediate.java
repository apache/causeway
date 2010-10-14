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


package org.apache.isis.extensions.hibernate.objectstore;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.extensions.hibernate.objectstore.util.HibernateUtil;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.PersistenceSession;
import org.apache.isis.runtime.transaction.IsisTransactionManager;
import org.apache.isis.runtime.transaction.PersistenceCommand;


/**
 * Same as {@link HibernateObjectStore} except doesn't create commands to save/update objects 
 * at the end of the NOF transaction, instead it does the update there and then.
 */
public class HibernateObjectStoreImmediate extends HibernateObjectStore {
    private static final Logger LOG = Logger.getLogger(HibernateObjectStoreImmediate.class);

    public HibernateObjectStoreImmediate() {
        super();
    }

    ///////////////////////////////////////////////////////////
    // Transaction
    ///////////////////////////////////////////////////////////

    @Override
    protected boolean startHibernateTransaction() {
        boolean started = false;
        if (!HibernateUtil.inTransaction()) {
            // If first query in transaction need to start - otherwise use existing
            HibernateUtil.startTransaction();
            started = true;
        }
        boolean flushed = getTransactionManager().flushTransaction();

        // If started a hibernate transaction and didn't flush - will need to commit it 
        // (standalone query) so return true.
        // For all other cases rely on endTransaction or execute to commit.
        return started && !flushed;
    }


    @Override
    public void endTransaction() {
        // Need to catch scenario where commands are flushed by query
        // but then no more commands are created. Execute won't be run as no commands in array
        // so need to commit the outstanding hibernate transaction here.
        if (HibernateUtil.inTransaction()) {
            HibernateUtil.commitTransaction();
        }
        super.endTransaction();
    }


    ///////////////////////////////////////////////////////////
    // Execute
    ///////////////////////////////////////////////////////////

    @Override
    public void execute(final List<PersistenceCommand> commands) {
        LOG.debug("execute " + commands.size() + " commands");
        if (commands.size() > 0) {
            if (!HibernateUtil.inTransaction()) {
                // There will only be a hibernate transaction already if commands were flushed 
                // and then more added
                HibernateUtil.startTransaction();
            }
            executeCommands(commands);
            HibernateUtil.commitTransaction();
        }
    }
    
    ///////////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////////

    private static PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    private static IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }



}

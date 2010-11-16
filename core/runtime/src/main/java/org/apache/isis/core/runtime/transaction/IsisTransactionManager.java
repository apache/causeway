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


package org.apache.isis.core.runtime.transaction;

import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.runtime.session.IsisSession;


public interface IsisTransactionManager extends SessionScopedComponent, Injectable {


    //////////////////////////////////////////////////////////////////////
    // Session
    //////////////////////////////////////////////////////////////////////

    /**
     * The owning {@link IsisSession}.
     *
     * <p>
     * Will be non-<tt>null</tt> when {@link #open() open}ed, but <tt>null</tt> if {@link #close() close}d .
     */
    IsisSession getSession();


    //////////////////////////////////////////////////////////////////////
    // Transaction Management
    //////////////////////////////////////////////////////////////////////

    
    void startTransaction();
    
    boolean flushTransaction();

    void abortTransaction();

    /**
     * Ends the transaction if nesting level is 0. 
     */
    void endTransaction();


    /**
     * The current transaction, if any.
     */
    IsisTransaction getTransaction();



    //////////////////////////////////////////////////////////////////////
    // Transactional Execution
    //////////////////////////////////////////////////////////////////////

    /**
     * Run the supplied {@link Runnable block of code (closure)} in a {@link IsisTransaction transaction}.
     * 
     * <p>
     * If a transaction is {@link IsisContext#inTransaction() in progress}, then
     * uses that.  Otherwise will {@link #startTransaction() start} a transaction before
     * running the block and {@link #endTransaction() commit} it at the end.  If the
     * closure throws an exception, then will {@link #abortTransaction() abort} the transaction. 
     */
    public void executeWithinTransaction(TransactionalClosure closure);


    /**
     * Run the supplied {@link Runnable block of code (closure)} in a {@link IsisTransaction transaction}.
     * 
     * <p>
     * If a transaction is {@link IsisContext#inTransaction() in progress}, then
     * uses that.  Otherwise will {@link #startTransaction() start} a transaction before
     * running the block and {@link #endTransaction() commit} it at the end.  If the
     * closure throws an exception, then will {@link #abortTransaction() abort} the transaction. 
     */
    public <T> T executeWithinTransaction(TransactionalClosureWithReturn<T> closure);

    //////////////////////////////////////////////////////////////////////
    // Debugging
    //////////////////////////////////////////////////////////////////////

    void debugData(DebugString debug);



}


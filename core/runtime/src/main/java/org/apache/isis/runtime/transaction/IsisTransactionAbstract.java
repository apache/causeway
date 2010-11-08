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


package org.apache.isis.runtime.transaction;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.apache.isis.core.commons.ensure.Ensure.ensureThatState;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.runtime.transaction.updatenotifier.UpdateNotifier;

public abstract class IsisTransactionAbstract implements IsisTransaction {

    private static final Logger LOG = Logger.getLogger(IsisTransactionAbstract.class);

    private final IsisTransactionManager transactionManager;
    private final MessageBroker messageBroker;
    private final UpdateNotifier updateNotifier;
    
    private State state;

    private RuntimeException cause;
    
    public IsisTransactionAbstract(
            final IsisTransactionManager transactionManager, 
            final MessageBroker messageBroker, 
            final UpdateNotifier updateNotifier) {
        
        ensureThatArg(transactionManager, is(not(nullValue())), "transaction manager is required");
        ensureThatArg(messageBroker, is(not(nullValue())), "message broker is required");
        ensureThatArg(updateNotifier, is(not(nullValue())), "update notifier is required");
        
        this.transactionManager = transactionManager;
        this.messageBroker = messageBroker;
        this.updateNotifier = updateNotifier;
        
        this.state = State.IN_PROGRESS;
    }



    //////////////////////////////////////////////////////////////////
    // State 
    //////////////////////////////////////////////////////////////////

    public State getState() {
        return state;
    }
    
    private void setState(State state) {
        this.state = state;
    }

    
    //////////////////////////////////////////////////////////////////
    // commit, abort 
    //////////////////////////////////////////////////////////////////

    public final void flush() {
        ensureThatState(getState().canFlush(), is(true), "state is: " + getState());
        if (LOG.isInfoEnabled()) {
            LOG.info("flush transaction " + this);
        }
        
        try {
            doFlush();
        } catch(RuntimeException ex) {
            setState(State.MUST_ABORT);
            setAbortCause(ex);
            throw ex;
        }
    }


    public final void commit() {
        ensureThatState(getState().canCommit(), is(true), "state is: " + getState());

        if (LOG.isInfoEnabled()) {
            LOG.info("commit transaction " + this);
        }

        if (getState() == State.COMMITTED) {
            if (LOG.isInfoEnabled()) {
                LOG.info("already committed; ignoring");
            }
            return;
        }
        try {
            doFlush();
            setState(State.COMMITTED);
        } catch(RuntimeException ex) {
            setAbortCause(ex);
            throw ex;
        }
    }

    public final void abort() {
        ensureThatState(getState().canAbort(), is(true), "state is: " + getState());
        if (LOG.isInfoEnabled()) {
            LOG.info("abort transaction " + this);
        }

        try {
            doAbort();
        } catch(RuntimeException ex) {
            setAbortCause(ex);
            throw ex;
        } finally {
            setState(State.ABORTED);
        }
    }

    /**
     * Mandatory hook method for subclasses to persist all pending changes.
     * 
     * <p>
     * Called by both {@link #commit()} and by {@link #flush()}:
     * <table>
     * <tr>
     * <th>called from</th><th>next {@link #getState() state} if ok</th><th>next {@link #getState() state} if exception</th>
     * </tr>
     * <tr>
     * <td>{@link #commit()}</td><td>{@link State#COMMITTED}</td><td>{@link State#ABORTED}</td>
     * </tr>
     * <tr>
     * <td>{@link #flush()}</td><td>{@link State#IN_PROGRESS}</td><td>{@link State#MUST_ABORT}</td>
     * </tr>
     * </table>
     */
    protected abstract void doFlush();

    /**
     * Mandatory hook method for subclasses to perform additional processing on abort.
     * 
     * <p>
     * After this call the {@link #getState() state} will always be set to 
     * {@link State#ABORTED}, irrespective of whether an exception is thrown or not.
     */
    protected abstract void doAbort();



    //////////////////////////////////////////////////////////////////
    // Abort Cause 
    //////////////////////////////////////////////////////////////////

    protected void setAbortCause(RuntimeException cause) {
        this.cause = cause;
    }
    /**
     * The cause (if any) for the transaction being aborted.
     * 
     * <p>
     * There will be a cause if an exception is thrown either by {@link #doFlush()} or
     * {@link #doAbort()}.
     */
    public RuntimeException getAbortCause() {
        return cause;
    }



    //////////////////////////////////////////////////////////////////
    // toString 
    //////////////////////////////////////////////////////////////////
    
    @Override
    public String toString() {
        return appendTo(new ToString(this)).toString();
    }

    protected ToString appendTo(ToString str) {
        str.append("state", state);
        return str;
    }

    //////////////////////////////////////////////////////////////////
    // Depenendencies  (from constructor) 
    //////////////////////////////////////////////////////////////////

    /**
     * Injected in constructor
     */
    public IsisTransactionManager getTransactionManager() {
        return transactionManager;
    }

    /**
     * Injected in constructor
     */
    public MessageBroker getMessageBroker() {
        return messageBroker;
    }
    
    /**
     * Injected in constructor
     */
    public UpdateNotifier getUpdateNotifier() {
        return updateNotifier;
    }
    

    
}



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

import java.util.function.Supplier;

import javax.enterprise.inject.Vetoed;

import org.apache.isis.applib.services.iactn.Interaction;
import org.apache.isis.applib.services.iactn.InteractionContext;
import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.transaction.integration.IsisTransactionAspectSupport;
import org.apache.isis.core.transaction.integration.IsisTransactionManagerException;
import org.apache.isis.core.transaction.integration.IsisTransactionObject;
import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommand;
import org.apache.isis.persistence.jdo.integration.persistence.command.PersistenceCommandQueue;
import org.apache.isis.persistence.jdo.provider.persistence.HasPersistenceManager;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Vetoed @Log4j2
class _TxManagerInternal 
implements 
    PersistenceCommandQueue {

    // -- constructor, fields

    private final MetaModelContext mmc;
    private final Supplier<InteractionContext> interactionContextProvider;
    private final _TxHelper txHelper;

    _TxManagerInternal(
            MetaModelContext mmc, 
            HasPersistenceManager pmProvider) {

        this.mmc = mmc;
        this.interactionContextProvider = ()->mmc.getServiceRegistry().lookupServiceElseFail(InteractionContext.class);
        this.txHelper = _TxHelper.create(pmProvider);
    }

    public _Tx beginTransaction() {

        val txInProgress = IsisTransactionAspectSupport.isTransactionInProgress();
        if (txInProgress) {

            val txObject = IsisTransactionAspectSupport
                    .currentTransactionObject()
                    .orElseThrow(()->_Exceptions.unrecoverable("no current IsisTransactionObject available"));

            txObject.incTransactionNestingLevel();
            val nestingLevel = txObject.getTransactionNestingLevel();

            if (log.isDebugEnabled()) {
                log.debug("startTransaction: nesting-level {}->{}",
                        nestingLevel - 1,
                        nestingLevel);
            }

            return (_Tx) txObject.getCurrentTransaction();

        } else {

            val interaction = interactionContextProvider.get().currentInteractionElseFail();

            val command = interaction.getCommand();
            val transactionId = command.getUniqueId();

            val currentTransaction = new _Tx(
                    mmc,
                    txHelper,
                    transactionId,
                    interaction.next(Interaction.Sequence.TRANSACTION.id()));

            txHelper.startTransaction();

            if (log.isDebugEnabled()) {
                log.debug("startTransaction: top-level");
            }

            return currentTransaction;
        }

    }


    public void flushTransaction(_Tx transaction) {
        if (transaction != null) {
            log.debug("flushTransaction");
            transaction.flush();
        }
    }

    /**
     * Ends the transaction if nesting level is 0 (but will abort the transaction instead,
     * even if nesting level is not 0, if an {@link _Tx#getAbortCause() abort cause}
     * has been {@link _Tx#setAbortCause(IsisException) set}.
     *
     * <p>
     * If in the process of committing the transaction an exception is thrown, then this will
     * be handled and will abort the transaction instead.
     *
     * <p>
     * If an abort cause has been set (or an exception occurs), then will throw this
     * exception in turn.
     */
    public void commitTransaction(IsisTransactionObject txObject) {

        val transaction = (_Tx) txObject.getCurrentTransaction(); 

        if (transaction == null) {
            // allow this method to be called >1 with no adverse affects

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: no transaction exists");
            }

            return;
        }

        if (transaction.getState().isComplete()) {
            // allow this method to be called >1 with no adverse affects

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: previous transaction completed");
            }

            return;
        }

        val transactionLevel = txObject.getTransactionNestingLevel();
        val isTopLevel = txObject.isTopLevel();

        if (log.isDebugEnabled()) {
            log.debug("endTransaction: level {}->{}", transactionLevel, transactionLevel - 1);
        }

        try {
            endTransactionInternal(txObject);
        } finally {
            val tx = (_Tx) txObject.getCurrentTransaction();
            if(tx==null) {
                log.error("race condition when ending the current transaction object");
            } else {
                val state = tx.getState();
                if(isTopLevel && !state.isComplete()) {
                    log.error("endTransaction: when top-level, "
                            + "transactionState is expected COMMITTED or ABORTED but was: '{}'", state);
                }
            }
        }
    }

    private void endTransactionInternal(IsisTransactionObject txObject) {

        val transaction = (_Tx) txObject.getCurrentTransaction();

        // terminate the transaction early if an abort cause was already set.
        RuntimeException abortCause = transaction.getAbortCause();
        if(transaction.getState().mustAbort()) {

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: aborting instead [EARLY TERMINATION], abort cause '{}' has been set", abortCause.getMessage());
            }
            try {
                abortTransaction(txObject);

                // just in case any different exception was raised...
                val currentTx = this.getCurrentTransaction();
                if(currentTx!=null && currentTx.getAbortCause()!=null) {
                    abortCause = currentTx.getAbortCause();
                }

            } catch(RuntimeException ex) {

                abortCause = ex;

            }

            if(abortCause != null) {

                // re-introduced the throwing of exceptions in 1.15.1 (same as 1.14.x)

                // in 1.15.0 we were not throwing exceptions at this point, resulting in JDOUserException errors
                // (eg malformed SQL) simply being silently ignored

                // the reason that no exceptions were being thrown in 1.15.0 was because it was observed that
                // throwing exceptions always resulted in forwarding to the error page, even if the error had been
                // recognised at the UI layer.  This was the rationale given, at least.
                //
                // Not certain now it is correct; if it was to improve the UI experience.
                //
                // Certainly swallowing severe exceptions is much less acceptable.  Therefore reverting.

                throw abortCause;


            } else {
                // assume that any rendering of the problem has been done lower down the stack.
                return;
            }
        }

        if(!txObject.isTopLevel()) {
            txObject.decTransactionNestingLevel();
            return;
        }

        //
        // TODO: granted, this is some fairly byzantine coding.  but I'm trying to account for different types
        // of object store implementations that could start throwing exceptions at any stage.
        // once the contract/API for the objectstore is better tied down, hopefully can simplify this...
        //

        if(abortCause == null) {

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: committing");
            }

            try {
                
                val currentTx = this.getCurrentTransaction();
                if(currentTx!=null) {
                    currentTx.preCommit();   
                }
                
            } catch(Exception ex) {
                // just in case any new exception was raised...

                // this bizarre code because an InvocationTargetException (which is not a RuntimeException) was
                // being thrown due to a coding error in a domain object
                abortCause = ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);

                val currentTx = this.getCurrentTransaction();
                if(currentTx!=null) {
                    currentTx.setAbortCause(new IsisTransactionManagerException(ex));    
                }
                
            }
        }

        if(abortCause == null) {
            try {
                
                txHelper.endTransaction();
            } catch(Exception ex) {
                // just in case any new exception was raised...
                abortCause = ex instanceof RuntimeException ? (RuntimeException) ex : new RuntimeException(ex);

                // hacky... moving the transaction back to something other than COMMITTED
                val currentTx = this.getCurrentTransaction();
                if(currentTx!=null) {
                    currentTx.setAbortCause(new IsisTransactionManagerException(ex));
                }
            }
        }


        // previously we called __isis_endRequest here on all RequestScopedServices.  This is now
        // done later, in PersistenceSession#close(). If we introduce support for @TransactionScoped
        // services, then this would be the place to finalize them.

        //
        // finally, if an exception was thrown, we rollback the transaction
        //

        if(abortCause != null) {

            if (log.isDebugEnabled()) {
                log.debug("endTransaction: aborting instead, abort cause has been set");
            }
            try {
                abortTransaction(txObject);
            } catch(RuntimeException ex) {
                // ignore; nothing to do:
                // * we want the existing abortCause to be available
                // * the transactionLevel is correctly now at 0.
            }

            throw abortCause;
        } else {

            // keeping things in sync
            val currentTx = this.getCurrentTransaction();
            if(currentTx!=null) {
                currentTx.commit();
            }
        }


    }

    public void abortTransaction(IsisTransactionObject txObject) {
        val transaction = (_Tx) txObject.getCurrentTransaction();
        if (transaction != null) {
            transaction.markAsAborted();
            txHelper.abortTransaction();
            txObject.clear();
        }
    }

    @Override
    public void addCommand(PersistenceCommand command) {
        val transaction = getCurrentTransaction();
        if (transaction != null && command != null) {
            transaction.addCommand(command);
        }
    }

    // -- HELPER

    private _Tx getCurrentTransaction() {
        return IsisTransactionAspectSupport.currentTransactionObject()
                .map(IsisTransactionObject::getCurrentTransaction)
                .map(_Tx.class::cast)
                .orElse(null);
    }


}

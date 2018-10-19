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

package org.apache.isis.core.integtestsupport.components;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.headless.HeadlessTransactionSupport;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction.State;

@DomainService(
        nature=NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
)
public class HeadlessTransactionSupportDefault implements HeadlessTransactionSupport {

    @Override
    public void beginTransaction() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();

        if(transaction == null) {
            startTransactionForUser(transactionManager);
            return;
        }

        final State state = transaction.getState();
        switch(state) {
        case COMMITTED:
        case ABORTED:
            startTransactionForUser(transactionManager);
            break;
        case IN_PROGRESS:
            // nothing to do
            break;
        case MUST_ABORT:
            throw new AssertionError("Transaction is in state of '" + state + "'");
        default:
            throw new AssertionError("Unknown transaction state '" + state + "'");
        }

    }

    private void startTransactionForUser(IsisTransactionManager transactionManager) {
        transactionManager.startTransaction();

        // specify that this command (if any) is being executed by a 'USER'
        final CommandContext commandContext = getService(CommandContext.class);
        Command command = commandContext.getCommand();
        command.internal().setExecutor(Command.Executor.USER);
    }

    /**
     * Either commits or aborts the transaction, depending on the Transaction's {@link IsisTransaction#getState()}
     *
     */
    @Override
    public void endTransaction() {
        final IsisTransactionManager transactionManager = getTransactionManager();
        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
        if(transaction == null) {
            throw new AssertionError("No transaction exists");
        }

        transactionManager.endTransaction();

        final State state = transaction.getState();
        switch(state) {
        case COMMITTED:
            break;
        case ABORTED:
            break;
        case IN_PROGRESS:
            throw new AssertionError("Transaction is still in state of '" + state + "'");
        case MUST_ABORT:
            throw new AssertionError("Transaction is still in state of '" + state + "'");
        default:
            throw new AssertionError("Unknown transaction state '" + state + "'");
        }
    }

    //    /**
    //     * Commits the transaction.
    //     *
    //     * @deprecated - ought to be using regular domain services rather than reaching into the framework
    //     */
    //    @Deprecated
    //    public void commitTran() {
    //        final IsisTransactionManager transactionManager = getTransactionManager();
    //        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
    //        if(transaction == null) {
    //            throw new AssertionError("No transaction exists");
    //        }
    //        final State state = transaction.getState();
    //        switch(state) {
    //            case COMMITTED:
    //            case ABORTED:
    //            case MUST_ABORT:
    //                throw new AssertionError("Transaction is in state of '" + state + "'");
    //            case IN_PROGRESS:
    //                transactionManager.endTransaction();
    //                break;
    //            default:
    //                throw new AssertionError("Unknown transaction state '" + state + "'");
    //        }
    //    }

    //    /**
    //     * Aborts the transaction.
    //     *
    //     * @deprecated - ought to be using regular domain services rather than reaching into the framework
    //     */
    //    @Deprecated
    //    public void abortTran() {
    //        final IsisTransactionManager transactionManager = getTransactionManager();
    //        final IsisTransaction transaction = transactionManager.getCurrentTransaction();
    //        if(transaction == null) {
    //            throw new AssertionError("No transaction exists");
    //        }
    //        final State state = transaction.getState();
    //        switch(state) {
    //            case ABORTED:
    //                break;
    //            case COMMITTED:
    //                throw new AssertionError("Transaction is in state of '" + state + "'");
    //            case MUST_ABORT:
    //            case IN_PROGRESS:
    //                transactionManager.abortTransaction();
    //                break;
    //            default:
    //                throw new AssertionError("Unknown transaction state '" + state + "'");
    //        }
    //    }

    // -- getService

    private <C> C getService(Class<C> serviceClass) {
        final ServicesInjector servicesInjector = isisSessionFactory().getServicesInjector();
        return servicesInjector.lookupServiceElseFail(serviceClass);
    }

    // -- Dependencies

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private PersistenceSession getPersistenceSession() {
        return isisSessionFactory().getCurrentSession().getPersistenceSession();
    }

    private IsisSessionFactory isisSessionFactory() {
        return IsisContext.getSessionFactory();
    }


}

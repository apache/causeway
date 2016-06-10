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
package org.apache.isis.core.runtime.services.transtate;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.core.metamodel.services.transtate.TransactionStateProviderInternal;
import org.apache.isis.core.metamodel.transactions.TransactionState;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;


@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + (Integer.MAX_VALUE - 1)  // ie before the Noop impl in metamodel
)
public class TransactionStateProviderInternalDefault implements TransactionStateProviderInternal {

    @Override
    public TransactionState getTransactionState() {
        final IsisTransaction transaction = getTransactionManager().getCurrentTransaction();
        if (transaction == null) {
            return TransactionState.NONE;
        }
        IsisTransaction.State state = transaction.getState();
        return state.getRuntimeContextState();
    }

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    private PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

}

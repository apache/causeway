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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.jmock.Expectations;
import org.jmock.Sequence;
import org.junit.Before;
import org.junit.Test;

import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

public class ObjectStoreTransactionManager_EndTransactionTest extends ObjectStoreTransactionManagerAbstractTestCase {

    @Before
    public void setUpTransactionManager() throws Exception {
        transactionManager = new IsisTransactionManager(mockPersistenceSession, mockObjectStore);
    }

    @Test
    public void endTransactionDecrementsTransactionLevel() throws Exception {
        // setup
        ignoreCallsToObjectStore();
        transactionManager.startTransaction();
        transactionManager.startTransaction();

        assertThat(transactionManager.getTransactionLevel(), is(2));
        transactionManager.endTransaction();
        assertThat(transactionManager.getTransactionLevel(), is(1));
    }

    @Test
    public void endTransactionCommitsTransactionWhenLevelDecrementsDownToZero() throws Exception {
        // setup
        ignoreCallsToObjectStore();
        transactionManager.startTransaction();

        context.checking(new Expectations() {
            {
                one(mockPersistenceSession).objectChangedAllDirty();
            }
        });
        assertThat(transactionManager.getTransactionLevel(), is(1));
        transactionManager.endTransaction();
        assertThat(transactionManager.getTransactionLevel(), is(0));
    }

    @Test
    public void startTransactionInteractsWithObjectStore() throws Exception {
        // setup
        ignoreCallsToPersistenceSession();

        context.checking(new Expectations() {
            {
                one(mockObjectStore).startTransaction();
            }
        });
        transactionManager.startTransaction();

    }

    @Test
    public void endTransactionInteractsWithObjectStore() throws Exception {
        // setup
        ignoreCallsToPersistenceSession();

        context.checking(new Expectations() {
            {
                final Sequence transactionOrdering = context.sequence("transactionOrdering");
                one(mockObjectStore).startTransaction();
                inSequence(transactionOrdering);

                one(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(transactionOrdering);

                one(mockObjectStore).endTransaction();
                inSequence(transactionOrdering);
            }
        });

        transactionManager.startTransaction();
        transactionManager.endTransaction();
    }

}

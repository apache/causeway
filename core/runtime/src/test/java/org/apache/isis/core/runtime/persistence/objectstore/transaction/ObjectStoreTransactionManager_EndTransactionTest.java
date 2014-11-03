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

package org.apache.isis.core.runtime.persistence.objectstore.transaction;

import java.util.Collections;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jmock.Expectations;
import org.jmock.Sequence;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.services.ServicesInjectorDefault;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ObjectStoreTransactionManager_EndTransactionTest {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    
    @Mock
    private AuthenticationSession mockAuthenticationSession;
    @Mock
    private PersistenceSession mockPersistenceSession;
    @Mock
    private MessageBroker mockMessageBroker;
    @Mock
    private TransactionalResource mockObjectStore;

    private IsisTransactionManager transactionManager;

    @Before
    public void setUpTransactionManager() throws Exception {
        context.checking(new Expectations(){{
            allowing(mockAuthenticationSession).getUserName();
            will(returnValue("sven"));

            allowing(mockAuthenticationSession).getMessageBroker();
            will(returnValue(mockMessageBroker));
        }});

        transactionManager = new IsisTransactionManager(mockPersistenceSession, mockObjectStore, new ServicesInjectorDefault()) {
            @Override
            public AuthenticationSession getAuthenticationSession() {
                return mockAuthenticationSession;
            }
        };
    }

    protected Matcher<Class<?>> anyClass() {
        return new TypeSafeMatcher<Class<?>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("any class");
            }

            @Override
            protected boolean matchesSafely(Class<?> item) {
                return true;
            }
        };
    }

    @Test
    public void endTransactionDecrementsTransactionLevel() throws Exception {
        // setup
        context.ignoring(mockObjectStore, mockPersistenceSession);
        transactionManager.startTransaction();
        transactionManager.startTransaction();

        assertThat(transactionManager.getTransactionLevel(), is(2));
        transactionManager.endTransaction();
        assertThat(transactionManager.getTransactionLevel(), is(1));
    }

    @Test
    public void endTransactionCommitsTransactionWhenLevelDecrementsDownToZero() throws Exception {
        // setup
        context.ignoring(mockObjectStore);
        transactionManager.startTransaction();

        context.checking(new Expectations() {
            {
                oneOf(mockPersistenceSession).objectChangedAllDirty();
            }
        });
        assertThat(transactionManager.getTransactionLevel(), is(1));
        transactionManager.endTransaction();
        assertThat(transactionManager.getTransactionLevel(), is(0));
    }

    @Test
    public void startTransactionInteractsWithObjectStore() throws Exception {
        // setup
        context.ignoring(mockPersistenceSession);

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
        context.ignoring(mockPersistenceSession);

        context.checking(new Expectations() {
            {
                final Sequence transactionOrdering = context.sequence("transactionOrdering");
                oneOf(mockObjectStore).startTransaction();
                inSequence(transactionOrdering);

                // flushed twice, once before publishing, once after
                exactly(3).of(mockObjectStore).execute(with(equalTo(Collections.<PersistenceCommand>emptyList())));
                inSequence(transactionOrdering);

                oneOf(mockObjectStore).endTransaction();
                inSequence(transactionOrdering);
            }
        });

        transactionManager.startTransaction();
        transactionManager.endTransaction();
    }

}

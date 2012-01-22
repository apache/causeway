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

package org.apache.isis.runtimes.dflt.objectstores.dflt;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.isis.runtimes.dflt.objectstores.dflt.internal.ObjectStorePersistedObjects;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tested in style of <i>Working Effectively with Legacy Code</i> (Feathers) and
 * <i>Growing Object-Oriented Software</i> (Freeman &amp; Pryce).
 */
@RunWith(JMock.class)
public class InMemoryObjectStore_openAndClose {

    private InMemoryObjectStore objectStore;

    private final Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    private InMemoryPersistenceSessionFactory mockInMemoryPersistenceSessionFactory;
    private PersistenceSession mockPersistenceSession;

    private ObjectStorePersistedObjects mockObjectStorePersistedObjects;

    private boolean recreatedAdapters = false;

    @Before
    public void setUp() throws Exception {
        mockInMemoryPersistenceSessionFactory = context.mock(InMemoryPersistenceSessionFactory.class);
        mockObjectStorePersistedObjects = context.mock(ObjectStorePersistedObjects.class);
        mockPersistenceSession = context.mock(PersistenceSession.class);
        objectStore = new InMemoryObjectStore() {
            @Override
            protected InMemoryPersistenceSessionFactory getInMemoryPersistenceSessionFactory() {
                return mockInMemoryPersistenceSessionFactory;
            }

            @Override
            protected PersistenceSession getPersistenceSession() {
                return mockPersistenceSession;
            }

            @Override
            protected void recreateAdapters() {
                recreatedAdapters = true;
            }
        };
    }

    private void neverInteractsDirectlyWithPersistenceSession() {
        context.checking(new Expectations() {
            {
                never(mockPersistenceSession);
            }
        });
    }

    @Test
    public void whenOpenForFirstTimeThenCreatesPersistedObjects() throws Exception {
        neverInteractsDirectlyWithPersistenceSession();
        context.checking(new Expectations() {
            {
                one(mockInMemoryPersistenceSessionFactory).getPersistedObjects();
                will(returnValue(null));

                one(mockInMemoryPersistenceSessionFactory).createPersistedObjects();
                will(returnValue(mockObjectStorePersistedObjects));
            }
        });
        objectStore.open();
    }

    @Test
    public void whenOpenSubsequentlyThenObtainsPersistedObjectsFromObjectStoreFactoryAndRecreatesAdapters() throws Exception {
        neverInteractsDirectlyWithPersistenceSession();
        context.checking(new Expectations() {
            {
                one(mockInMemoryPersistenceSessionFactory).getPersistedObjects();
                will(returnValue(mockObjectStorePersistedObjects));
            }
        });

        assertThat(recreatedAdapters, is(false));
        objectStore.open();
        assertThat(recreatedAdapters, is(true));
    }

    @Test
    public void whenCloseThenGivesObjectsBackToObjectStoreFactory() throws Exception {
        neverInteractsDirectlyWithPersistenceSession();

        whenOpenSubsequentlyThenObtainsPersistedObjectsFromObjectStoreFactoryAndRecreatesAdapters();

        context.checking(new Expectations() {
            {
                one(mockInMemoryPersistenceSessionFactory).attach(with(mockPersistenceSession), with(mockObjectStorePersistedObjects));
                never(mockPersistenceSession);
            }
        });
        objectStore.close();
    }

}

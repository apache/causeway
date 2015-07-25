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

package org.apache.isis.core.objectstore;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.apache.isis.core.objectstore.internal.ObjectStorePersistedObjects;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2.Mode;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Tested in style of <i>Working Effectively with Legacy Code</i> (Feathers) and
 * <i>Growing Object-Oriented Software</i> (Freeman &amp; Pryce).
 */
public class InMemoryObjectStoreTest_openAndClose {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    @Mock
    private InMemoryPersistenceSessionFactory mockInMemoryPersistenceSessionFactory;
    @Mock
    private PersistenceSession mockPersistenceSession;
    @Mock
    private ObjectStorePersistedObjects mockObjectStorePersistedObjects;

    private boolean recreatedAdapters = false;

    private InMemoryObjectStore objectStore;
    
    @Before
    public void setUp() throws Exception {
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

    @Test
    public void whenOpenForFirstTimeThenCreatesPersistedObjects() throws Exception {
        context.never(mockPersistenceSession);
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
        context.never(mockPersistenceSession);
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
        context.never(mockPersistenceSession);
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

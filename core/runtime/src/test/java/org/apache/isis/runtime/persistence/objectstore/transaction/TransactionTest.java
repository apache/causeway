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


package org.apache.isis.runtime.persistence.objectstore.transaction;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.transaction.ObjectPersistenceException;
import org.apache.isis.core.runtime.transaction.messagebroker.MessageBroker;
import org.apache.isis.core.runtime.transaction.updatenotifier.UpdateNotifier;
import org.apache.isis.runtime.persistence.objectstore.ObjectStoreSpy;
import org.apache.isis.runtime.testsystem.TestProxySystem;


@RunWith(JMock.class)
public class TransactionTest {

    private Mockery mockery = new JUnit4Mockery();


    private ObjectAdapter object1;
    private ObjectAdapter object2;
    private ObjectStoreSpy os;
    private ObjectStoreTransaction t;


    private IsisTransactionManager mockTransactionManager;
    private MessageBroker mockMessageBroker;
    private UpdateNotifier mockUpdateNotifier;

    private CreateObjectCommand createCreateCommand(final ObjectAdapter object, final String name) {
        return new CreateObjectCommand() {

            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {}

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private DestroyObjectCommand createDestroyCommand(final ObjectAdapter object, final String name) {
        return new DestroyObjectCommand() {

            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {}

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createSaveCommand(final ObjectAdapter object, final String name) {
        return new SaveObjectCommand() {
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {}

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    private SaveObjectCommand createCommandThatAborts(final ObjectAdapter object, final String name) {
        return new SaveObjectCommand() {
            public void execute(final PersistenceCommandContext context) throws ObjectPersistenceException {
                throw new ObjectPersistenceException();
            }

            public ObjectAdapter onObject() {
                return object;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        final TestProxySystem system = new TestProxySystem();
        system.init();


        mockTransactionManager = mockery.mock(IsisTransactionManager.class);
        mockMessageBroker = mockery.mock(MessageBroker.class);
        mockUpdateNotifier = mockery.mock(UpdateNotifier.class);

        os = new ObjectStoreSpy();
        t = new ObjectStoreTransaction(
                mockTransactionManager, mockMessageBroker, mockUpdateNotifier, os);

        object1 = system.createTransientTestObject();
        object2 = system.createTransientTestObject();
    }

    @Test
    public void testAbort() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createSaveCommand(object2, "command 2"));
        t.abort();

        assertEquals(0, os.getActions().size());
    }

    @Test
    public void testAbortBeforeCommand() throws Exception {
        t.abort();

        assertEquals(0, os.getActions().size());
    }

    @Test
    public void testCommandThrowsAnExceptionCausingAbort() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createCommandThatAborts(object2, "command 2"));
        t.addCommand(createSaveCommand(object1, "command 3"));
        try {
            t.commit();
            fail();
        } catch (final ObjectPersistenceException expected) {}
        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute command 1");
        os.assertAction(1, "execute command 2");
    }

    @Test
    public void testAddCommands() throws Exception {
        t.addCommand(createSaveCommand(object1, "command 1"));
        t.addCommand(createSaveCommand(object2, "command 2"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute command 1");
        os.assertAction(1, "execute command 2");
        assertEquals(2, os.getActions().size());
    }

    @Test
    public void testAddCreateCommandsButIgnoreSaveForSameObject() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        /*
         * The next command should be ignored as the above create will have already saved the next object
         */
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.addCommand(createSaveCommand(object2, "save object 2"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute create object 1");
        os.assertAction(1, "execute save object 2");
        assertEquals(2, os.getActions().size());
    }

    @Test
    public void testAddDestoryCommandsButRemovePreviousSaveForSameObject() throws Exception {
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute destroy object 1");
        assertEquals(1, os.getActions().size());
    }

    @Test
    public void testIgnoreBothCreateAndDestroyCommandsWhenForSameObject() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.addCommand(createDestroyCommand(object2, "destroy object 2"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute destroy object 2");
        assertEquals(1, os.getActions().size());
    }

    @Test
    public void testIgnoreSaveAfterDeleteForSameObject() throws Exception {
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.addCommand(createSaveCommand(object1, "save object 1"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        os.assertAction(0, "execute destroy object 1");
        assertEquals(1, os.getActions().size());
    }

    @Test
    public void testNoCommands() throws Exception {
        t.commit();
        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        assertEquals(0, os.getActions().size());
    }

    @Test
    public void createandThenDestroyShouldCancelEachOtherOut() throws Exception {
        t.addCommand(createCreateCommand(object1, "create object 1"));
        t.addCommand(createDestroyCommand(object1, "destroy object 1"));
        t.commit();

        // previously the xactn invoked "endTransaction" on the OS, but this is
        // now done by the xactn mgr.
        // os.assertAction(0, "endTransaction");
        assertEquals(0, os.getActions().size());
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToAbortAnAlreadyAbortedTransaction() throws Exception {
        t.abort();

        t.abort();
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToCommitAnAlreadyAbortedTransaction() throws Exception {
        t.abort();

        t.commit();
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToAbortAnAlreadyCommitedTransaction() throws Exception {
        t.commit();

        t.abort();
    }

    @Test(expected=IllegalStateException.class)
    public void shouldThrowExceptionIfAttemptToCommitAnAlreadyCommitedTransaction() throws Exception {
        t.commit();

        t.commit();
    }


}

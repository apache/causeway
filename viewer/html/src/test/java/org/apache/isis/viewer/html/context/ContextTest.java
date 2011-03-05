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


package org.apache.isis.viewer.html.context;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.runtimes.dflt.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyCollectionAdapter;
import org.apache.isis.runtimes.dflt.runtime.testsystem.TestProxyVersion;
import org.apache.isis.viewer.html.action.ActionException;
import org.apache.isis.viewer.html.context.Context;


public class ContextTest extends ProxyJunit3TestCase {

    private Context context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = new Context(null);
    }

    public void testExceptionThrownWhenNoCollectionForIdentity() {
        context.mapCollection(system.createPersistentTestCollection());
        try {
            assertNull(context.getMappedCollection("112"));
            fail();
        } catch (final ActionException expected) {}
    }

    public void testExceptionThrownWhenNoObjectForIdentity() {
        context.mapAction(new ObjectActionNoop());
        try {
            assertNull(context.getMappedAction("112"));
            fail();
        } catch (final ActionException expected) {}
    }

    public void testExceptionThrownWhenNoActionForIdentity() {
        final ObjectAdapter object = system.createPersistentTestObject();
        context.mapObject(object);
        try {
            assertNull(context.getMappedObject("112"));
            fail();
        } catch (final ActionException expected) {}
    }

    /*
     * REVIEW public void testIdentityUsedToLookupCollection() { DummyCollectionAdapter collection1 = new
     * DummyCollectionAdapter(); String id = context.mapCollection(collection1); assertEquals(collection1,
     * context.getMappedCollection(id)); }
     */
    public void testIdentityUsedToLookupObject() {
        final ObjectAdapter object = system.createPersistentTestObject();
        final String id = context.mapObject(object);
        assertEquals(object, context.getMappedObject(id));
    }

    public void testLookedUpObjectHasDifferentVersion() {
        final ObjectAdapter object = system.createPersistentTestObject();
        final String id = context.mapObject(object);
        // change version on the object being passed back
        object.setOptimisticLock(new TestProxyVersion(5));
        context.getMappedObject(id);
        assertEquals("Reloaded object " + object.titleString(), context.getMessage(1));
    }

    public void testIdentityUsedToLookupAction() {
        final ObjectActionNoop action = new ObjectActionNoop();
        final String id = context.mapAction(action);
        assertEquals(action, context.getMappedAction(id));
    }

    public void testRegisteredCollectionReturnSameIdentityForSameCollection() {
        final TestProxyCollectionAdapter collection = system.createPersistentTestCollection();
        final String id = context.mapCollection(collection);
        final String id2 = context.mapCollection(collection);
        assertEquals(id, id2);
    }

    /*
     * REVIEW public void testRegisteredCollectionReturnDifferentIdentityForDifferentCollection() { replay();
     * String id = context.mapCollection(new DummyCollectionAdapter()); String id2 = context.mapCollection(new
     * DummyCollectionAdapter()); assertNotSame(id, id2); verify(); }
     */

    public void testRegisteredObjectReturnSameIdentityForSameObject() {
        final ObjectAdapter object = system.createPersistentTestObject();
        final String id = context.mapObject(object);
        final String id2 = context.mapObject(object);
        assertEquals(id, id2);
    }

    public void testTransientObjectReturnSameIdentityForSameObject() {
        final ObjectAdapter object = system.createTransientTestObject();
        final String id = context.mapObject(object);
        final String id2 = context.mapObject(object);
        assertEquals(id, id2);
    }

    /*
     * TODO reinstate public void testClearRemovesObject() { replay(); DummyObjectAdapter object = new
     * DummyObjectAdapter(new DummyOid(13)); String id = context.mapObject(object);
     * context.clearMappedObject(object); try { context.getMappedObject(id); fail(); } catch
     * (ObjectAdapterRuntimeException expected) { assertEquals("No object in object map with id " + id,
     * expected.getMessage()); } verify(); }
     */
    public void testRegisteredObjectReturnDifferentIdentityForDifferentObject() {
        final ObjectAdapter dummyObjectAdapter = system.createPersistentTestObject();
        final ObjectAdapter dummyObjectAdapter2 = system.createPersistentTestObject();

        final String id = context.mapObject(dummyObjectAdapter);
        final String id2 = context.mapObject(dummyObjectAdapter2);

        assertNotSame(id, id2);
    }

    public void testRegisteredActionReturnSameIdentityForSameAction() {
        final ObjectActionNoop action = new ObjectActionNoop();
        final String id = context.mapAction(action);
        final String id2 = context.mapAction(action);
        assertEquals(id, id2);
    }

    public void testRegisteredActionReturnDifferentIdentityForDifferentAction() {
        final String id = context.mapAction(new ObjectActionNoop());
        final String id2 = context.mapAction(new ObjectActionNoop());
        assertNotSame(id, id2);
    }

    public void testPersistentObjectsRestoredAsGhostToObjectLoader() {
        final ObjectAdapter object = system.createPersistentTestObject();
        context.mapObject(object);
        final Oid oid = object.getOid();
        system.resetLoader();

        assertNull("loader still has the object", getAdapterManager().getAdapterFor(oid));
        context.restoreAllObjectsToLoader();

        assertNotNull("loaders is missing the object", getAdapterManager().getAdapterFor(oid));
        final ObjectAdapter newAdapter = getAdapterManager().getAdapterFor(oid);
        assertNotSame("expect the loader to have a new adapter", object, newAdapter);
        assertEquals("expect oids to match", object.getOid(), newAdapter.getOid());
        assertNotSame(object.getObject(), newAdapter.getObject());
        assertEquals(object.getObject().getClass(), newAdapter.getObject().getClass());
        assertEquals("expect versions to match", object.getVersion(), newAdapter.getVersion());
        assertEquals(ResolveState.GHOST, newAdapter.getResolveState());
    }

}


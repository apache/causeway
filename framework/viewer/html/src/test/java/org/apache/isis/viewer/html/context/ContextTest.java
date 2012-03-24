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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jmock.auto.Mock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.specloader.ObjectReflector;
import org.apache.isis.core.runtime.userprofile.UserProfileStore;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2;
import org.apache.isis.core.testsupport.jmock.JUnitRuleMockery2.Mode;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSessionFactory;

public class ContextTest {

    @Rule
    public JUnitRuleMockery2 mockContext = JUnitRuleMockery2.createFor(Mode.INTERFACES_AND_CLASSES);

    private Context context;

//    protected TestProxySystem system;
//    private int nextId;
//
//    private TestProxyConfiguration mockConfiguration;
//    private TestProxyReflector mockReflector;
//    private AuthenticationSession mockAuthSession;
//    private TestProxyPersistenceSessionFactory mockPersistenceSessionFactory;
//    private TestProxyPersistenceSession mockPersistenceSession;
//    private UserProfileStoreNoop mockUserProfileStore;

    @Mock
    private IsisConfiguration mockConfiguration;
    @Mock
    private ObjectReflector mockReflector;
    @Mock
    private AuthenticationSession mockAuthSession;
    @Mock
    private PersistenceSessionFactory mockPersistenceSessionFactory;
    @Mock
    private PersistenceSession mockPersistenceSession;
    @Mock
    private UserProfileStore mockUserProfileStore;

    @Before
    public void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
//        system = new TestProxySystem();
//        nextId = 0;
//
//        mockConfiguration = new TestProxyConfiguration();
//        mockReflector = new TestProxyReflector();
//        mockAuthSession = new TestAuthenticationSession();
//        mockPersistenceSessionFactory = new TestProxyPersistenceSessionFactory();
//        mockPersistenceSession = new TestProxyPersistenceSession(mockPersistenceSessionFactory);
//        mockPersistenceSessionFactory.setPersistenceSessionToCreate(mockPersistenceSession);
//        mockUserProfileStore = new UserProfileStoreNoop();
//        
//        system.openSession(mockConfiguration, mockReflector, mockAuthSession, null, null, null, mockUserProfileStore, null, mockPersistenceSessionFactory, null);

        context = new Context(null);
    }

    @Ignore // DKH
    @Test
    public void testExceptionThrownWhenNoCollectionForIdentity() {
        
//        final TestProxyCollectionAdapter collection = new TestProxyCollectionAdapter(new Vector());
//        final TestProxySpecification specification = (TestProxySpecification) mockReflector.loadSpecification(Vector.class);
//        final TestProxySpecification elementSpecification = (TestProxySpecification) mockReflector.loadSpecification(Object.class);
//        specification.addFacet(new TestProxyCollectionFacet());
//        specification.addFacet(new TypeOfFacetDefaultToObject(elementSpecification, mockReflector) {
//        });
//        collection.setupSpecification(specification);
//        final TestProxyCollectionAdapter createPersistentTestCollection = collection;
//        context.mapCollection(createPersistentTestCollection);
//        try {
//            assertNull(context.getMappedCollection("112"));
//            fail();
//        } catch (final ActionException expected) {
//        }
    }

    @Ignore // DKH
    @Test
    public void testExceptionThrownWhenNoObjectForIdentity() {
//        context.mapAction(new ObjectActionNoop());
//        try {
//            assertNull(context.getMappedAction("112"));
//            fail();
//        } catch (final ActionException expected) {
//        }
    }

    @Ignore // DKH
    @Test
    public void testExceptionThrownWhenNoActionForIdentity() {
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter object = adapter;
//        context.mapObject(object);
//        try {
//            assertNull(context.getMappedObject("112"));
//            fail();
//        } catch (final ActionException expected) {
//        }
    }

    /*
     * REVIEW public void testIdentityUsedToLookupCollection() {
     * DummyCollectionAdapter collection1 = new DummyCollectionAdapter(); String
     * id = context.mapCollection(collection1); assertEquals(collection1,
     * context.getMappedCollection(id)); }
     */
    @Ignore // DKH
    @Test
    public void testIdentityUsedToLookupObject() {
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter object = adapter;
//        final String id = context.mapObject(object);
//        assertEquals(object, context.getMappedObject(id));
    }

    @Ignore // DKH
    @Test
    public void testLookedUpObjectHasDifferentVersion() {
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter object = adapter;
//        final String id = context.mapObject(object);
//        // change version on the object being passed back
//        object.setOptimisticLock(new TestProxyVersion(5));
//        context.getMappedObject(id);
//        assertEquals("Reloaded object " + object.titleString(), context.getMessage(1));
    }

    @Test
    public void testIdentityUsedToLookupAction() {
//        final ObjectActionNoop action = new ObjectActionNoop();
//        final String id = context.mapAction(action);
//        assertEquals(action, context.getMappedAction(id));
    }

    @Ignore // DKH
    @Test
    public void testRegisteredCollectionReturnSameIdentityForSameCollection() {
//        final TestProxyCollectionAdapter collection1 = new TestProxyCollectionAdapter(new Vector());
//        final TestProxySpecification specification = (TestProxySpecification) mockReflector.loadSpecification(Vector.class);
//        final TestProxySpecification elementSpecification = (TestProxySpecification) mockReflector.loadSpecification(Object.class);
//        specification.addFacet(new TestProxyCollectionFacet());
//        specification.addFacet(new TypeOfFacetDefaultToObject(elementSpecification, mockReflector) {});
//        collection1.setupSpecification(specification);
//        final TestProxyCollectionAdapter collection = collection1;
//        final String id = context.mapCollection(collection);
//        final String id2 = context.mapCollection(collection);
//        assertEquals(id, id2);
    }

    /*
     * REVIEW public void
     * testRegisteredCollectionReturnDifferentIdentityForDifferentCollection() {
     * replay(); String id = context.mapCollection(new
     * DummyCollectionAdapter()); String id2 = context.mapCollection(new
     * DummyCollectionAdapter()); assertNotSame(id, id2); verify(); }
     */

    @Ignore // DKH
    @Test
    public void testRegisteredObjectReturnSameIdentityForSameObject() {
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter object = adapter;
//        final String id = context.mapObject(object);
//        final String id2 = context.mapObject(object);
//        assertEquals(id, id2);
    }

    @Ignore // DKH
    @Test
    public void testTransientObjectReturnSameIdentityForSameObject() {
//        final TestPojo transientTestPojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(transientTestPojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter object = adapterForTransient;
//        final String id = context.mapObject(object);
//        final String id2 = context.mapObject(object);
//        assertEquals(id, id2);
    }

    /*
     * TODO reinstate public void testClearRemovesObject() { replay();
     * DummyObjectAdapter object = new DummyObjectAdapter(new DummyOid(13));
     * String id = context.mapObject(object); context.clearMappedObject(object);
     * try { context.getMappedObject(id); fail(); } catch
     * (ObjectAdapterRuntimeException expected) {
     * assertEquals("No object in object map with id " + id,
     * expected.getMessage()); } verify(); }
     */
    @Ignore // DKH
    @Test
    public void testRegisteredObjectReturnDifferentIdentityForDifferentObject() {
//        final TestPojo pojo1 = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo1, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter dummyObjectAdapter = adapter;
//        
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid2 = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient2 = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid2);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient2.getResolveState());
//        final ObjectAdapter adapter2 = adapterForTransient2;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter2);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter2.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter dummyObjectAdapter2 = adapter2;
//
//        final String id = context.mapObject(dummyObjectAdapter);
//        final String id2 = context.mapObject(dummyObjectAdapter2);
//
//        assertNotSame(id, id2);
    }

    @Ignore // DKH
    @Test
    public void testRegisteredActionReturnSameIdentityForSameAction() {
//        final ObjectActionNoop action = new ObjectActionNoop();
//        final String id = context.mapAction(action);
//        final String id2 = context.mapAction(action);
//        assertEquals(id, id2);
    }

    @Ignore // DKH
    @Test
    public void testRegisteredActionReturnDifferentIdentityForDifferentAction() {
//        final String id = context.mapAction(new ObjectActionNoop());
//        final String id2 = context.mapAction(new ObjectActionNoop());
//        assertNotSame(id, id2);
    }

    @Ignore // DKH
    @Test
    public void testPersistentObjectsRestoredAsGhostToObjectLoader() {
//        final TestPojo pojo = new TestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(pojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//        final ObjectAdapter adapter = adapterForTransient;
//        
//        // similar to object store implementation
//        ((AdapterManagerPersist) mockPersistenceSession.getAdapterManager()).remapAsPersistent(adapter);
//        
//        // would be done by the object store, we must do ourselves.
//        adapter.setOptimisticLock(new TestProxyVersion(1));
//        final ObjectAdapter object = adapter;
//        context.mapObject(object);
//        final Oid oid = object.getOid();
//        mockPersistenceSession.testReset();
//
//        assertNull("loader still has the object", getAdapterManager().getAdapterFor(oid));
//        context.restoreAllObjectsToLoader();
//
//        assertNotNull("loaders is missing the object", getAdapterManager().getAdapterFor(oid));
//        final ObjectAdapter newAdapter = getAdapterManager().getAdapterFor(oid);
//        assertNotSame("expect the loader to have a new adapter", object, newAdapter);
//        assertEquals("expect oids to match", object.getOid(), newAdapter.getOid());
//        assertNotSame(object.getObject(), newAdapter.getObject());
//        assertEquals(object.getObject().getClass(), newAdapter.getObject().getClass());
//        assertEquals("expect versions to match", object.getVersion(), newAdapter.getVersion());
//        assertEquals(ResolveState.GHOST, newAdapter.getResolveState());
    }

    
//    private PersistenceSession getPersistenceSession() {
//        return IsisContext.getPersistenceSession();
//    }
//
//    private AdapterManager getAdapterManager() {
//        return getPersistenceSession().getAdapterManager();
//    }

}

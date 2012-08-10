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

package org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.dflt;

import org.junit.Before;
import org.junit.Test;

import org.junit.Ignore;

import org.apache.isis.runtimes.dflt.runtime.persistence.objectstore.algorithm.PersistAlgorithmDefault;

public class DefaultPersistAlgorithmTest {

//    private final static class PersistedObjectAdderSpy implements ToPersistObjectSet {
//        private final List<ObjectAdapter> persistedObjects = new ArrayList<ObjectAdapter>();
//
//        public List<ObjectAdapter> getPersistedObjects() {
//            return persistedObjects;
//        }
//
//        @Override
//        public void addPersistedObject(final ObjectAdapter object) {
//            persistedObjects.add(object);
//        }
//
//        @Override
//        public void remapAsPersistent(final ObjectAdapter object) {
//            object.changeState(ResolveState.RESOLVED);
//        }
//    }
//
//    private final String objectType = "CUS";
//
    private PersistAlgorithmDefault algorithm;
    
//    private PersistedObjectAdderSpy adder;
//    private ObjectAdapter object;
//    private TestProxyAdapter fieldsObject;
//
//    protected TestProxySystem system;
//    private int nextId;
//
//    private TestProxyConfiguration mockConfiguration;
//    private TestProxyReflector mockReflector;
//    private AuthenticationSession mockAuthSession;
//    private TestProxyPersistenceSessionFactory mockPersistenceSessionFactory;
//    private TestProxyPersistenceSession mockPersistenceSession;
//    private TestUserProfileStore mockUserProfileStore;
//
//
//    @Override
//    protected void setUp() throws Exception {
//        Logger.getRootLogger().setLevel(Level.OFF);
//        system = new TestProxySystem();
//        nextId = 0;
//        
//        mockConfiguration = new TestProxyConfiguration();
//        mockReflector = new TestProxyReflector();
//        mockAuthSession = new TestProxySession();
//        mockPersistenceSessionFactory = new TestProxyPersistenceSessionFactory();
//        mockPersistenceSession = new TestProxyPersistenceSession(mockPersistenceSessionFactory);
//        mockPersistenceSessionFactory.setPersistenceSessionToCreate(mockPersistenceSession);
//        mockUserProfileStore = new TestUserProfileStore();
//        
//        system.openSession(mockConfiguration, mockReflector, mockAuthSession, null, null, null, mockUserProfileStore, null, mockPersistenceSessionFactory, null);
//
//        
//        algorithm = new DefaultPersistAlgorithm();
//        final RuntimeTestPojo transientTestPojo = new RuntimeTestPojo();
//        final RootOidDefault transientTestOid = RootOidDefault.createTransient("CUS", ""+ (nextId++));
//        final ObjectAdapter adapterForTransient = ((AdapterManagerTestSupport) mockPersistenceSession.getAdapterManager()).testCreateTransient(transientTestPojo, transientTestOid);
//        Assert.assertEquals("", ResolveState.TRANSIENT, adapterForTransient.getResolveState());
//
//        object = adapterForTransient;
//        // object.setupResolveState(ResolveState.TRANSIENT);
//
//        final TestProxySpecification spec = (TestProxySpecification) object.getSpecification();
//        final List<ObjectAssociation> fields = Arrays.asList((ObjectAssociation) new OneToOneAssociationTest() {
//
//            @Override
//            public void initAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
//            }
//
//            @Override
//            public Consent isAssociationValid(final ObjectAdapter inObject, final ObjectAdapter associate) {
//                return null;
//            }
//
//            @Override
//            public void setAssociation(final ObjectAdapter inObject, final ObjectAdapter associate) {
//            }
//
//            @Override
//            public void set(final ObjectAdapter owner, final ObjectAdapter newValue) {
//            }
//
//            @Override
//            public ObjectAdapter get(final ObjectAdapter target) {
//                return null;
//            }
//
//            @Override
//            public ObjectSpecification getSpecification() {
//                return null;
//            }
//
//            @Override
//            public String debugData() {
//                return null;
//            }
//
//            @Override
//            public String getId() {
//                return null;
//            }
//
//            @Override
//            public String getName() {
//                return null;
//            }
//
//            @Override
//            public FeatureType getFeatureType() {
//                return FeatureType.PROPERTY;
//            }
//
//        });
//        spec.setupFields(fields);
//
//        fieldsObject = new TestProxyAdapter();
//        fieldsObject.setupResolveState(ResolveState.TRANSIENT);
//        fieldsObject.setupSpecification((TestProxySpecification) mockReflector.loadSpecification(String.class));
//
//        adder = new PersistedObjectAdderSpy();
//    }

    
    @Before
    public void setUp() throws Exception {
        algorithm = new PersistAlgorithmDefault();
    }
    

    @Ignore //DKH
    @Test
    public void testMakePersistent() {
//        algorithm.makePersistent(object, adder);
//        assertEquals(ResolveState.RESOLVED, object.getResolveState());
//        assertTrue(adder.getPersistedObjects().contains(object));
    }

    @Ignore //DKH
    @Test
    public void testMakePersistentRecursesThroughReferenceFields() {
//        /*
//         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(),
//         * true); fieldControl.expectAndReturn(oneToOneAssociation.isValue(),
//         * false); fieldControl.expectAndReturn(oneToOneAssociation.get(object),
//         * fieldsObject);
//         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent
//         * (object);
//         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent
//         * (fieldsObject);
//         * 
//         * adder.addPersistedObject(object);
//         * adder.addPersistedObject(fieldsObject);
//         */
//
//        // replay();
//        algorithm.makePersistent(object, adder);
//        // verify();
    }

    @Ignore //DKH
    @Test
    public void testMakePersistentRecursesThroughReferenceFieldsSkippingNullReferences() {
//        /*
//         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(),
//         * true); fieldControl.expectAndReturn(oneToOneAssociation.isValue(),
//         * false); fieldControl.expectAndReturn(oneToOneAssociation.get(object),
//         * null);
//         * 
//         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object
//         * );
//         * 
//         * adder.addPersistedObject(object);
//         */
//        algorithm.makePersistent(object, adder);
    }

    @Ignore //DKH
    @Test
    public void testMakePersistentRecursesThroughReferenceFieldsSkippingNonPersistentFields() {
//        /*
//         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(),
//         * false);
//         * 
//         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object
//         * );
//         * 
//         * adder.addPersistedObject(object);
//         */
//        algorithm.makePersistent(object, adder);
    }

    @Ignore //DKH
    @Test
    public void testMakePersistentRecursesThroughReferenceFieldsSkippingObjectsThatAreAlreadyPersistent() {
//        /*
//         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(),
//         * true); fieldControl.expectAndReturn(oneToOneAssociation.isValue(),
//         * false); fieldControl.expectAndReturn(oneToOneAssociation.get(object),
//         * fieldsObject); fieldsObject.setupResolveState(ResolveState.RESOLVED);
//         * 
//         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object
//         * );
//         * 
//         * adder.addPersistedObject(object);
//         */
//        algorithm.makePersistent(object, adder);
    }

    @Ignore //DKH
    @Test
    public void testMakePersistentSkipsAggregatedObjects() {
//        class DefaultPersistAlgorithmSubclassForTesting extends DefaultPersistAlgorithm {
//            @Override
//            protected void persist(final ObjectAdapter object, final ToPersistObjectSet persistor) {
//                super.persist(object, persistor);
//            }
//
//            public void sensingPersist(final ObjectAdapter object, final ToPersistObjectSet persistor) {
//                persist(object, persistor);
//            }
//        }
//        final PojoAdapter aggregatedObject = new PojoAdapter(new Object(), RootOidDefault.createTransient(objectType, ""+1));
//        aggregatedObject.changeState(ResolveState.VALUE);
//        new DefaultPersistAlgorithmSubclassForTesting().sensingPersist(aggregatedObject, adder);
//        assertEquals(0, adder.getPersistedObjects().size());
    }

}

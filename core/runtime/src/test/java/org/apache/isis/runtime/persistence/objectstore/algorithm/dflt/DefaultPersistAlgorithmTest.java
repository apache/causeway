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


package org.apache.isis.runtime.persistence.objectstore.algorithm.dflt;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.consent.Consent;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.metamodel.spec.Persistability;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.metamodel.testspec.TestProxySpecification;
import org.apache.isis.runtime.persistence.NotPersistableException;
import org.apache.isis.runtime.persistence.adapterfactory.pojo.PojoAdapter;
import org.apache.isis.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.runtime.testspec.OneToOneAssociationTest;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.runtime.testsystem.TestProxyAdapter;



public class DefaultPersistAlgorithmTest extends ProxyJunit3TestCase {

    private final static class PersistedObjectAdderSpy implements ToPersistObjectSet {
        private final List<ObjectAdapter> persistedObjects = new ArrayList<ObjectAdapter>();

        public List<ObjectAdapter> getPersistedObjects() {
            return persistedObjects;
        }

        public void addPersistedObject(final ObjectAdapter object) {
            persistedObjects.add(object);
        }

        public void remapAsPersistent(final ObjectAdapter object) {
            object.changeState(ResolveState.RESOLVED);
        }
    }

    private DefaultPersistAlgorithm algorithm;
    private PersistedObjectAdderSpy adder;
    private ObjectAdapter object;
    private TestProxyAdapter fieldsObject;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        algorithm = new DefaultPersistAlgorithm();

        object = system.createTransientTestObject();
        // object.setupResolveState(ResolveState.TRANSIENT);

        final TestProxySpecification spec = system.getSpecification(object);
        final ObjectAssociation[] fields = new ObjectAssociation[] { new OneToOneAssociationTest() {

            public void initAssociation(ObjectAdapter inObject, ObjectAdapter associate) {}

            public Consent isAssociationValid(ObjectAdapter inObject, ObjectAdapter associate) {
                return null;
            }

            public void setAssociation(ObjectAdapter inObject, ObjectAdapter associate) {}

            public void set(ObjectAdapter owner, ObjectAdapter newValue) {}

            public ObjectAdapter get(ObjectAdapter target) {
                return null;
            }

            public ObjectSpecification getSpecification() {
                return null;
            }

            public String debugData() {
                return null;
            }

            public String getId() {
                return null;
            }

            public String getName() {
                return null;
            }

        } };
        spec.setupFields(fields);

        fieldsObject = new TestProxyAdapter();
        fieldsObject.setupResolveState(ResolveState.TRANSIENT);
        fieldsObject.setupSpecification(system.getSpecification(String.class));

        adder = new PersistedObjectAdderSpy();
    }

    public void testMakePersistentFailsIfObjectAlreadyPersistent() {
        object.changeState(ResolveState.RESOLVED);
        try {
            algorithm.makePersistent(object, adder);
            fail();
        } catch (final NotPersistableException expected) {}
    }

    public void testMakePersistentFailsIfObjectMustBeTransient() {
        try {
            system.getSpecification(object).setupPersistable(Persistability.TRANSIENT);
            algorithm.makePersistent(object, adder);
        } catch (final NotPersistableException expected) {}
    }

    public void testMakePersistent() {
        algorithm.makePersistent(object, adder);
        assertEquals(ResolveState.RESOLVED, object.getResolveState());
        assertTrue(adder.getPersistedObjects().contains(object));
    }

    public void testMakePersistentRecursesThroughReferenceFields() {
        /*
         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(), true);
         * fieldControl.expectAndReturn(oneToOneAssociation.isValue(), false);
         * fieldControl.expectAndReturn(oneToOneAssociation.get(object), fieldsObject);
         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object);
         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(fieldsObject);
         * 
         * adder.addPersistedObject(object); adder.addPersistedObject(fieldsObject);
         */

        // replay();
        algorithm.makePersistent(object, adder);
        // verify();
    }

    public void testMakePersistentRecursesThroughReferenceFieldsSkippingNullReferences() {
        /*
         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(), true);
         * fieldControl.expectAndReturn(oneToOneAssociation.isValue(), false);
         * fieldControl.expectAndReturn(oneToOneAssociation.get(object), null);
         * 
         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object);
         * 
         * adder.addPersistedObject(object);
         */
        algorithm.makePersistent(object, adder);
    }

    public void testMakePersistentRecursesThroughReferenceFieldsSkippingNonPersistentFields() {
        /*
         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(), false);
         * 
         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object);
         * 
         * adder.addPersistedObject(object);
         */
        algorithm.makePersistent(object, adder);
    }

    public void testMakePersistentRecursesThroughReferenceFieldsSkippingObjectsThatAreAlreadyPersistent() {
        /*
         * fieldControl.expectAndReturn(oneToOneAssociation.isPersisted(), true);
         * fieldControl.expectAndReturn(oneToOneAssociation.isValue(), false);
         * fieldControl.expectAndReturn(oneToOneAssociation.get(object), fieldsObject);
         * fieldsObject.setupResolveState(ResolveState.RESOLVED);
         * 
         * IsisContext.getObjectPersistor().getIdentityMap().madePersistent(object);
         * 
         * adder.addPersistedObject(object);
         */
        algorithm.makePersistent(object, adder);
    }

    public void testMakePersistentSkipsAggregatedObjects() {
        class DefaultPersistAlgorithmSubclassForTesting extends DefaultPersistAlgorithm {
            @Override
            protected void persist(final ObjectAdapter object, final ToPersistObjectSet persistor) {
                super.persist(object, persistor);
            }

            public void sensingPersist(final ObjectAdapter object, final ToPersistObjectSet persistor) {
                persist(object, persistor);
            }
        }
        final PojoAdapter aggregatedObject = new PojoAdapter(new Object(), SerialOid.createTransient(1));
        aggregatedObject.changeState(ResolveState.VALUE);
        new DefaultPersistAlgorithmSubclassForTesting().sensingPersist(aggregatedObject, adder);
        assertEquals(0, adder.getPersistedObjects().size());
    }

}


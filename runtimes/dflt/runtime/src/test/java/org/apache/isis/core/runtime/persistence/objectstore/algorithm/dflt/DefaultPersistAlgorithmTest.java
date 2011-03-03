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


package org.apache.isis.core.runtime.persistence.objectstore.algorithm.dflt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.consent.Consent;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.Persistability;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.testspec.TestProxySpecification;
import org.apache.isis.core.runtime.persistence.NotPersistableException;
import org.apache.isis.core.runtime.persistence.adapterfactory.pojo.PojoAdapter;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.core.runtime.testspec.OneToOneAssociationTest;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;
import org.apache.isis.core.runtime.testsystem.TestProxyAdapter;



public class DefaultPersistAlgorithmTest extends ProxyJunit3TestCase {

    private final static class PersistedObjectAdderSpy implements ToPersistObjectSet {
        private final List<ObjectAdapter> persistedObjects = new ArrayList<ObjectAdapter>();

        public List<ObjectAdapter> getPersistedObjects() {
            return persistedObjects;
        }

        @Override
        public void addPersistedObject(final ObjectAdapter object) {
            persistedObjects.add(object);
        }

        @Override
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
        final List<ObjectAssociation> fields = Arrays.asList( (ObjectAssociation)new OneToOneAssociationTest() {

            @Override
            public void initAssociation(ObjectAdapter inObject, ObjectAdapter associate) {}

            @Override
            public Consent isAssociationValid(ObjectAdapter inObject, ObjectAdapter associate) {
                return null;
            }

            @Override
            public void setAssociation(ObjectAdapter inObject, ObjectAdapter associate) {}

            @Override
            public void set(ObjectAdapter owner, ObjectAdapter newValue) {}

            @Override
            public ObjectAdapter get(ObjectAdapter target) {
                return null;
            }

            @Override
            public ObjectSpecification getSpecification() {
                return null;
            }

            @Override
            public String debugData() {
                return null;
            }

            @Override
            public String getId() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public FeatureType getFeatureType() {
                return FeatureType.PROPERTY;
            }

        }
        );
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


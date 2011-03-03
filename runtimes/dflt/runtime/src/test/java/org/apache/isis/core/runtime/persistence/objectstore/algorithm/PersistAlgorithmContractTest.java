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


package org.apache.isis.core.runtime.persistence.objectstore.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.runtime.persistence.adapterfactory.pojo.PojoAdapter;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.PersistAlgorithm;
import org.apache.isis.core.runtime.persistence.objectstore.algorithm.ToPersistObjectSet;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.core.runtime.testsystem.ProxyJunit3TestCase;


public abstract class PersistAlgorithmContractTest extends ProxyJunit3TestCase {

    protected static final class PersistedObjectAdderSpy implements ToPersistObjectSet {
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

    interface PersistAlgorithmSensing extends PersistAlgorithm {
        void persist(ObjectAdapter object, ToPersistObjectSet adder);
    }

    private PersistedObjectAdderSpy adder;
    private PersistAlgorithm persistAlgorithm;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        adder = new PersistedObjectAdderSpy();
        persistAlgorithm = createPersistAlgorithm();
    }

    /**
     * Hook for any implementation to implement.
     * 
     * @return
     */
    protected abstract PersistAlgorithm createPersistAlgorithm();

    public void testMakePersistentSkipsAggregatedObjects() {
        final PojoAdapter aggregatedObject = new PojoAdapter(new Object(), SerialOid.createTransient(1));
        aggregatedObject.changeState(ResolveState.VALUE);
        persistAlgorithm.makePersistent(aggregatedObject, adder);
        assertEquals(0, adder.getPersistedObjects().size());
    }

}


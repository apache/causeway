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


package org.apache.isis.runtime.system;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.metamodel.runtimecontext.RuntimeContext;
import org.apache.isis.metamodel.runtimecontext.noruntime.RuntimeContextNoRuntime;
import org.apache.isis.metamodel.specloader.internal.OneToManyAssociationImpl;
import org.apache.isis.runtime.system.specpeer.DummyOneToManyPeer;
import org.apache.isis.runtime.testsystem.ProxyJunit3TestCase;



public class OneToManyAssociationImplTest extends ProxyJunit3TestCase {

//    private static final String FIELD_ID = "members";
//    private static final String FIELD_NAME = "Members";
    private ObjectAdapter adapter;
//    private ObjectAdapter associate;
    private OneToManyAssociation association;
//    private TestSpecification type;
    private DummyOneToManyPeer associationDelegate;
	private RuntimeContext runtimeContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        adapter = system.createPersistentTestObject();
//        associate = system.createPersistentTestObject();
        runtimeContext = new RuntimeContextNoRuntime();

        associationDelegate = new DummyOneToManyPeer(system.getSpecification(String.class));
        association = new OneToManyAssociationImpl(associationDelegate, runtimeContext);
    }

//    public void xxxtestType() {
//        assertEquals(type, association.getSpecification());
//    }
//
//    public void xxxtestSet() {
//        association.addElement(adapter, associate);
//        associationDelegate.assertAction(0, "add " + adapter);
//        associationDelegate.assertAction(1, "add " + associate);
//    }
//
//    public void xxxtestClear() {
//        association.removeElement(adapter, associate);
//        associationDelegate.assertAction(0, "remove " + adapter);
//        associationDelegate.assertAction(1, "remove " + associate);
//    }

    public void testClearWithNull() {
        try {
            association.removeElement(adapter, null);
            fail();
        } catch (final IllegalArgumentException expected) {}
        associationDelegate.assertActions(0);
    }

    public void testSetWithNull() {
        try {
            association.addElement(adapter, null);
            fail();
        } catch (final IllegalArgumentException expected) {}
        associationDelegate.assertActions(0);
    }

//    public void xxxtestName() {
//        assertEquals(FIELD_NAME, association.getId());
//    }
//
//    public void xxxtestLabel() {
//        assertEquals(FIELD_NAME, association.getName());
//    }
}

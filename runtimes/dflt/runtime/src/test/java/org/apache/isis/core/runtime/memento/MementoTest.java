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


package org.apache.isis.core.runtime.memento;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.runtime.memento.Memento;
import org.apache.isis.core.runtime.testsystem.ProxyJunit4TestCase;


public class MementoTest extends ProxyJunit4TestCase {

    private ObjectAdapter originalAdapter;
    private ObjectAdapter returnedAdapter;

    @Before
    public void setUp() throws Exception {

        originalAdapter = system.createAdapterForTransient(new TestPojoSimple("fred"));
        final Memento memento = new Memento(originalAdapter);
        system.resetLoader();
        returnedAdapter = memento.recreateObject();
    }

    @Test
    public void testDifferentAdapterReturned() throws Exception {
        assertNotSame(originalAdapter, returnedAdapter);
    }

    @Test
    public void testHaveSameOid() throws Exception {
        assertEquals(originalAdapter.getOid(), returnedAdapter.getOid());
    }

    @Test
    public void testHaveSameSpecification() throws Exception {
        assertEquals(originalAdapter.getSpecification(), returnedAdapter.getSpecification());
    }

//    @Ignore("TODO need to add reflective code to test system for this to work")
//    @Test
//    public void testName() throws Exception {
//        @SuppressWarnings("unused")
//        final TestPojoSimple originalPojo = (TestPojoSimple) originalAdapter.getObject();
//
//        @SuppressWarnings("unused")
//        final TestPojoSimple returnedPojo = (TestPojoSimple) returnedAdapter.getObject();
//
//        // assertEquals(originalPojo.getName(), returnedPojo.getName());
//    }

}


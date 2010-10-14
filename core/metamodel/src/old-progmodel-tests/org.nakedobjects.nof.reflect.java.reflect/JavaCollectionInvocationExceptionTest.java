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


package org.apache.isis.progmodel.java5.reflect;

import java.lang.reflect.Method;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.progmodel.java5.facets.propcoll.read.JavaBeanPropertyAccessorMethod;
import org.apache.isis.progmodel.java5.reflect.collections.JavaCollectionAssociation;
import org.apache.isis.nof.reflect.peer.MemberIdentifier;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.reflect.peer.ReflectionException;
import org.apache.isis.nof.testsystem.TestPojo;
import org.apache.isis.nof.testsystem.TestProxyAdapter;
import org.apache.isis.nof.testsystem.TestProxySystem;


public class JavaCollectionInvocationExceptionTest extends TestCase {

    private TestProxySystem system;
    private JavaCollectionAssociation collectionField;
    private ObjectAdapter testAdapter;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestProxySystem();
        system.init();

        MemberIdentifier identifer = new MemberIdentifierImpl(TestObjectWithCollection.class.getName());

        Class elementType = TestPojo.class;
        collectionField = new JavaCollectionAssociation(identifer, elementType);

        Method getMethod = TestObjectWithCollection.class.getMethod("getList", new Class[0]);
        collectionField.addFacet(new JavaBeanPropertyAccessorMethod(getMethod, collectionField));

        Vector collection = new Vector();
        TestObjectWithCollection testPojo = new TestObjectWithCollection(collection, true);
        testAdapter = system.createPersistentTestObject(testPojo);
    }

    public void testGetAssociations() throws Exception {
        try {
            collectionField.getAssociations(testAdapter);
            fail();
        } catch (ReflectionException e) {}
    }

    public void testAdapterReflectsEmptyCollection() throws Exception {
        try {
            collectionField.isEmpty(testAdapter);
            fail();
        } catch (ReflectionException e) {}
    }

    public void testAddAssociation() {
        try {
            TestPojo elementToAdd = new TestPojo();
            collectionField.addAssociation(testAdapter, system.createPersistentTestObject(elementToAdd));
            fail();
        } catch (ReflectionException e) {}

    }

    public void testRemoveAssociation() {
        try {
            TestPojo elementToAdd = new TestPojo();
            TestProxyAdapter elementAdapter = system.createPersistentTestObject(elementToAdd);
            collectionField.removeAssociation(testAdapter, elementAdapter);
            fail();
        } catch (ReflectionException e) {}

    }

    public void testInitAssociations() {
        try {
            TestPojo elementToAdd1 = new TestPojo();
            TestPojo elementToAdd2 = new TestPojo();
            collectionField.initOneToManyAssociation(testAdapter, new ObjectAdapter[] {
                    system.createPersistentTestObject(elementToAdd1), system.createPersistentTestObject(elementToAdd2) });
            fail();
        } catch (ReflectionException e) {}
    }

    public void testRemoveAllAssociations() {
        try {
            collectionField.removeAllAssociations(testAdapter);
            fail();
        } catch (ReflectionException e) {}

    }

    public void testIsAddValid() throws Exception {
        try {
            TestPojo elementToAdd = new TestPojo();
            collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
            fail();
        } catch (ReflectionException e) {}

    }

    public void testIsRemoveValid() throws Exception {
        try {
            TestPojo elementToAdd = new TestPojo();
            collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
            fail();
        } catch (ReflectionException e) {}
    }

}


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
import java.util.ArrayList;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.nof.core.util.DebugString;
import org.apache.isis.progmodel.java5.facets.collections.validate.AddToCollectionValidMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.validate.RemoveFromCollectionValidMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.AddToCollectionMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.ClearCollectionMethodFacet;
import org.apache.isis.progmodel.java5.facets.collections.write.RemoveFromCollectionMethodFacet;
import org.apache.isis.progmodel.java5.facets.propcoll.read.JavaBeanPropertyAccessorMethod;
import org.apache.isis.progmodel.java5.reflect.collections.JavaCollectionAssociation;
import org.apache.isis.nof.reflect.peer.MemberIdentifier;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.testsystem.TestPojo;
import org.apache.isis.nof.testsystem.TestProxyCollectionAdapter;
import org.apache.isis.nof.testsystem.TestProxyAdapter;
import org.apache.isis.nof.testsystem.TestProxySystem;


public class JavaCollectionAssociationUsingMethodsTest extends TestCase {

    private TestProxySystem system;
    private JavaCollectionAssociation collectionField;
    private TestObjectWithCollection testPojo;
    private TestProxyAdapter testAdapter;
    private Vector collection;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestProxySystem();
        system.init();

        Class<TestObjectWithCollection> targetClass = TestObjectWithCollection.class;
        MemberIdentifier identifer = new MemberIdentifierImpl(targetClass.getName());

        Class elementType = TestPojo.class;
        collectionField = new JavaCollectionAssociation(identifer, elementType);

        Method getMethod = targetClass.getMethod("getList", new Class[0]);
        collectionField.addFacet(new JavaBeanPropertyAccessorMethod(getMethod, collectionField));

        Method addToMethod = targetClass.getMethod("addToList", new Class[] { Object.class });
        collectionField.addFacet(new AddToCollectionMethodFacet(addToMethod, collectionField));

        Method removeFromMethod = targetClass.getMethod("removeFromList", new Class[] { Object.class });
        collectionField.addFacet(new RemoveFromCollectionMethodFacet(removeFromMethod, collectionField));

        Method clearMethod = targetClass.getMethod("clearList", new Class[0]);
        collectionField.addFacet(new ClearCollectionMethodFacet(clearMethod, collectionField));

        Method validateAddToMethod = targetClass.getMethod("validateAddToList", new Class[] { Object.class });
        collectionField.addFacet(new AddToCollectionValidMethodFacet(validateAddToMethod, collectionField));

        Method validateRemoveFromMethod = targetClass.getMethod("validateRemoveFromList", new Class[] { Object.class });
        collectionField.addFacet(new RemoveFromCollectionValidMethodFacet(validateRemoveFromMethod, collectionField));

        collection = new Vector();
        testPojo = new TestObjectWithCollection(collection, false);
        testAdapter = system.createPersistentTestObject(testPojo);
    }

    public void testGetAssociations() {
        ArrayList expectedArrayList = new ArrayList(1);
        expectedArrayList.add("Test element");

        collection.add("Test element");

        TestProxyCollectionAdapter associations = (TestProxyCollectionAdapter) collectionField.getAssociations(testAdapter);
        Vector object = (Vector) associations.getObject();
        assertEquals(1, object.size());
        assertEquals("Test element", object.elementAt(0));
    }

    public void testAdapterReflectsEmptyCollection() throws Exception {
        assertTrue(collectionField.isEmpty(testAdapter));
    }

    public void testAdapterReflectsNotEmptyCollection() throws Exception {
        collection.add("Test element");
        assertFalse(collectionField.isEmpty(testAdapter));
    }

    public void testSizeIsZero() throws Exception {
        assertEquals(0, collection.size());
    }

    public void testAddAssociation() {
        TestPojo elementToAdd = new TestPojo();
        collectionField.addAssociation(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(1, collection.size());
        assertEquals(elementToAdd, collection.get(0));
    }

    public void testRemoveAssociation() {
        TestPojo elementToRemove = new TestPojo();
        TestProxyAdapter elementAdapter = system.createPersistentTestObject(elementToRemove);
        collection.add(elementToRemove);

        collectionField.removeAssociation(testAdapter, elementAdapter);
        assertEquals(0, collection.size());
    }

    public void testRemoveAllAssociations() {
        collection.add(new TestPojo());
        collection.add(new TestPojo());
        assertEquals(2, collection.size());

        collectionField.removeAllAssociations(testAdapter);
        assertEquals(0, collection.size());
    }

    public void testIsAddValid() throws Exception {
        TestPojo elementToAdd = new TestPojo();
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsRemoveValid() throws Exception {
        TestPojo elementToAdd = new TestPojo();
        Consent addValid = collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testDebug() throws Exception {
        DebugString debug = new DebugString();
        collectionField.debugData(debug);
        assertTrue(debug.toString().length() > 0);
    }

    public void testToString() throws Exception {
        assertTrue(collectionField.toString().length() > 0);
    }

    public void testIsAddValidCallsIsValidMethod() throws Exception {
        TestPojo elementToAdd = new TestPojo();
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsAddValidFailsCallsIsValidMethod() throws Exception {
        TestPojo elementToAdd = new TestObjectWithCollection(null, false);
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertTrue(addValid.isVetoed());
    }

    public void testIsRemoveValidCallsIsValidMethod() throws Exception {
        TestPojo elementToAdd = new TestPojo();
        Consent addValid = collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsRemoveValidFailsCallsIsValidMethod() throws Exception {
        TestPojo elementToAdd = new TestObjectWithCollection(null, false);
        Consent removeValid = collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertTrue(removeValid.isVetoed());
    }

    public void testValid() {
        assertFalse(collectionField.isAddValid(testAdapter, null).isAllowed());
    }

}


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
import java.util.Enumeration;
import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.isis.noa.adapter.CollectionAdapter;
import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.reflect.Consent;
import org.apache.isis.nof.core.reflect.Allow;
import org.apache.isis.nof.core.util.DebugString;
import org.apache.isis.progmodel.java5.facets.propcoll.read.JavaBeanPropertyAccessorMethod;
import org.apache.isis.progmodel.java5.reflect.collections.JavaCollectionAssociation;
import org.apache.isis.nof.reflect.peer.MemberIdentifier;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.testsystem.TestPojo;
import org.apache.isis.nof.testsystem.TestProxyCollectionAdapter;
import org.apache.isis.nof.testsystem.TestProxyAdapter;
import org.apache.isis.nof.testsystem.TestProxySystem;


public class JavaCollectionAssociationTest extends TestCase {

    private TestProxySystem system;
    private JavaCollectionAssociation collectionField;
    private TestObjectWithCollection testPojo;
    private TestProxyAdapter testAdapter;
    private Vector collection;
    private TestPojo elementToAdd;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestProxySystem();
        system.init();

        MemberIdentifier identifer = new MemberIdentifierImpl(TestObjectWithCollection.class.getName());

        Class elementType = TestPojo.class;
        collectionField = new JavaCollectionAssociation(identifer, elementType);

        Method getMethod = TestObjectWithCollection.class.getMethod("getList", new Class[0]);
        collectionField.addFacet(new JavaBeanPropertyAccessorMethod(getMethod, collectionField));

        collection = new Vector();
        testPojo = new TestObjectWithCollection(collection, false);
        testAdapter = system.createPersistentTestObject(testPojo);

        elementToAdd = new TestPojo();
    }

    public void testType() throws Exception {
        assertEquals(system.getSpecification(TestPojo.class), collectionField.getSpecification());
    }

    public void testCollectionSizeZero() {
        CollectionAdapter c = collectionField.getAssociations(testAdapter);
        assertEquals(0, c.size());
    }

    public void testAddAssociation() {
        CollectionAdapter c = collectionField.getAssociations(testAdapter);
        TestPojo elementToAdd = new TestPojo();
        collectionField.addAssociation(testAdapter, system.createPersistentTestObject(elementToAdd));

        assertEquals(1, c.size());
        assertEquals(elementToAdd, c.firstElement().getObject());
    }

    public void testRemoveAssociation() {
        CollectionAdapter c = collectionField.getAssociations(testAdapter);
        TestProxyAdapter elementAdapter = system.createPersistentTestObject();
        c.add(elementAdapter);
        assertEquals(1, c.size());

        collectionField.removeAssociation(testAdapter, elementAdapter);
        assertEquals(0, c.size());
    }

    public void testRemoveAllAssociations() {
        CollectionAdapter c = collectionField.getAssociations(testAdapter);
        c.add(system.createPersistentTestObject());
        c.add(system.createPersistentTestObject());
        assertEquals(2, c.size());

        collectionField.removeAllAssociations(testAdapter);
        assertEquals(0, c.size());
    }

    public void testInitAssociations() {
        ObjectAdapter elementToAdd1 = system.createPersistentTestObject();
        ObjectAdapter elementToAdd2 = system.createPersistentTestObject();
        collectionField.initOneToManyAssociation(testAdapter, new ObjectAdapter[] { elementToAdd1, elementToAdd2 });

        CollectionAdapter c = collectionField.getAssociations(testAdapter);
        assertEquals(2, c.size());

        Enumeration elements = c.elements();
        assertEquals(elementToAdd1, elements.nextElement());
        assertEquals(elementToAdd2, elements.nextElement());
        assertFalse(elements.hasMoreElements());
    }

    public void testIsAddValid() throws Exception {
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsRemoveValid() throws Exception {
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

    public void testIsAddValidDefaultsToAllow() throws Exception {
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsAddValidReflectsCollection() throws Exception {
        TestProxyCollectionAdapter c = (TestProxyCollectionAdapter) collectionField.getAssociations(testAdapter);
        c.setupAddValidMessage("veto");
        
        Consent addValid = collectionField.isAddValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertTrue(addValid.isVetoed());
    }

    public void testIsRemoveValidDefaultsToAllow() throws Exception {
        Consent addValid = collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertEquals(Allow.DEFAULT, addValid);
    }

    public void testIsRemoveValidReflectsCollection() throws Exception {
        TestProxyCollectionAdapter c = (TestProxyCollectionAdapter) collectionField.getAssociations(testAdapter);
        c.setupRemoveValidMessage("veto");
        
        Consent addValid = collectionField.isRemoveValid(testAdapter, system.createPersistentTestObject(elementToAdd));
        assertTrue(addValid.isVetoed());
    }

    public void testIsAddValidVetoesNull() {
        Consent consent = collectionField.isAddValid(testAdapter, null);
        assertFalse(consent.isAllowed());
    }

    public void testIsRemoveValidVetoesNull() {
        Consent consent = collectionField.isRemoveValid(testAdapter, null);
        assertFalse(consent.isAllowed());
    }

}


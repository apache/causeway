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

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.isis.noa.adapter.CollectionAdapter;
import org.apache.isis.noa.adapter.ObjectAdapter;
import org.apache.isis.noa.facets.FacetHolder;
import org.apache.isis.progmodel.java5.method.JavaBeanPropertyAccessorMethod;
import org.apache.isis.progmodel.java5.reflect.collections.JavaCollectionAssociation;
import org.apache.isis.nof.reflect.peer.MemberIdentifierImpl;
import org.apache.isis.nof.testsystem.TestProxySystem;
import org.apache.isis.testing.DummyCollectionAdapter;
import org.apache.isis.testing.TestSpecification;
import org.apache.isis.testing.TestSystem;

import test.org.apache.isis.object.ObjectAdapterTestCase;


public class JavaArrayWithAllMethodsTest extends ObjectAdapterTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";

    public static void main(final String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaArrayWithAllMethodsTest.class));
    }

    private JavaCollectionAssociation collectionField;
    private JavaReferencedObject elements[];
    private ObjectAdapter adapter;
    private JavaObjectWithVector objectWithVector;
    private TestProxySystem system;

    public JavaArrayWithAllMethodsTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestProxySystem();
        system.init();

        objectWithVector = new JavaObjectWithVector();
        adapter = system.createAdapterForTransient(objectWithVector);
        elements = new JavaReferencedObject[3];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new JavaReferencedObject();
        }

        Class cls = JavaObjectWithVector.class;
        MemberIdentifierImpl memberIdentifierImpl = new MemberIdentifierImpl("cls", MEMBERS_FIELD_NAME);
        collectionField = new JavaCollectionAssociation(memberIdentifierImpl, Object.class);

        Method get = cls.getDeclaredMethod("getMethod", new Class[0]);
		collectionField.addFacet(new JavaBeanPropertyAccessorMethod(get, collectionField));
        
        Method add = cls.getDeclaredMethod("addToMethod", new Class[] { JavaReferencedObject.class });
        Method remove = cls.getDeclaredMethod("removeFromMethod", new Class[] { JavaReferencedObject.class });

        Method visible = cls.getDeclaredMethod("hideMethod", new Class[] { JavaReferencedObject.class });
        Method available = cls.getDeclaredMethod("availableMethod", new Class[] { JavaReferencedObject.class });
        Method valid = cls.getDeclaredMethod("validMethod", new Class[] { JavaReferencedObject.class });
/*
        FieldMethods fieldMethods = new FieldMethods(get, null, add, remove, null, null);
        GeneralControlMethods controlMethods = new GeneralControlMethods(new MethodHelper(visible), new MethodHelper(available), valid, null);
        DescriptiveMethods descriptiveMethods = new DescriptiveMethods(new StaticHelper("name"), null);
        FieldFlags xxMethods = new FieldFlags(When.NEVER, false, When.NEVER, false, "");
        FieldSessionMethods sessionMethods = new FieldSessionMethods(null, null);
*/
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testAdd() {
        JavaReferencedObject associate = new JavaReferencedObject();
        ObjectAdapter objectAssociation = system.createAdapterForTransient(associate);

//        spec = new TestSpecification();
//        system.addSpecificationToLoader(spec);

        assertNull(objectWithVector.added);
        collectionField.addAssociation(adapter, objectAssociation);
        assertEquals(associate, objectWithVector.added);
    }

    public void testAvailableWhenModifiableAndAvailable() throws Exception {
        objectWithVector.available = true;
        assertTrue(collectionField.isUsable(adapter).isAllowed());
    }

    public void testGet() {
        TestSpecification spec = new TestSpecification();
        system.addSpecificationToLoader(spec);

        spec = new TestSpecification();
        system.addSpecificationToLoader(spec);

        DummyCollectionAdapter collection = new DummyCollectionAdapter();
        system.addCollectionToObjectLoader(collection);

        assertNotNull(collectionField.getAssociations(adapter));
        assertEquals(collection.getObject(), collectionField.getAssociations(adapter).getObject());
    }

    public void testNotAvailableWhenModifiable() throws Exception {
        objectWithVector.available = false;
        assertFalse(collectionField.isUsable(adapter).isAllowed());
        assertEquals("not available", collectionField.isUsable(adapter).getReason());
    }

    public void testRemove() {
        TestSpecification spec = new TestSpecification();
        system.addSpecificationToLoader(spec);

        JavaReferencedObject associate = new JavaReferencedObject();
        ObjectAdapter objectAssociation = system.createAdapterForTransient(associate);

        spec = new TestSpecification();
        system.addSpecificationToLoader(spec);

        assertNull(objectWithVector.removed);
        collectionField.removeAssociation(adapter, objectAssociation);
        assertEquals(associate, objectWithVector.removed);
    }

    public void testType() {
        TestSpecification spec = new TestSpecification(CollectionAdapter.class.getName());
        system.addSpecificationToLoader(spec);
        assertEquals(spec, collectionField.getSpecification());
    }

    public void testValid() {
        assertFalse(collectionField.isAddValid(adapter, null).isAllowed());
    }

    public void testVisible() {
        assertFalse("invisible", collectionField.isVisible(adapter));
        objectWithVector.visible = true;
        assertTrue("visible", collectionField.isVisible(adapter));
    }
}

package org.nakedobjects.xat;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Veto;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.MockControl;

import test.org.nakedobjects.object.DummyNakedValue;
import test.org.nakedobjects.object.TestSystem;


public class CopyOfTestObjectImplFieldTest extends TestCase {

    public static void main(String[] args) {}

    TestSystem system;

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        system = new TestSystem();

        system.init();
    }

    public void testGetField() {
        DummyNakedValue value = new DummyNakedValue();
        
        MockControl factoryControl = MockControl.createControl(TestObjectFactory.class);
        TestObjectFactory factory = (TestObjectFactory) factoryControl.getMock(); 
        factoryControl.expectAndDefaultReturn(factory.createTestValue(value), new TestValueImpl(value));
        factoryControl.replay();
        
        
        MockControl fieldControl = MockControl.createControl(OneToOneAssociation.class);
        OneToOneAssociation field = (OneToOneAssociation) fieldControl.getMock();
        fieldControl.expectAndReturn(field.isAuthorised(), true);
        fieldControl.expectAndReturn(field.getId(), "fieldname", 2);
        fieldControl.expectAndDefaultReturn(field.isValue(), true);
        fieldControl.replay();
        
        MockControl specificationControl = MockControl.createControl(NakedObjectSpecification.class);
        NakedObjectSpecification specification = (NakedObjectSpecification) specificationControl.getMock();
        specificationControl.expectAndReturn(specification.getFields(), new NakedObjectField[] {field}); 
        specificationControl.expectAndReturn(specification.getField("fieldname"), field, 1);
        specificationControl.replay();
 
        
        MockControl nakedObjectControl = MockControl.createControl(NakedObject.class);
        NakedObject nakedObject = (NakedObject) nakedObjectControl.getMock();
        nakedObjectControl.expectAndReturn(nakedObject.getSpecification(), specification, 2);
        nakedObjectControl.expectAndReturn(nakedObject.isVisible(field), Allow.DEFAULT);
        nakedObjectControl.expectAndReturn(nakedObject.getField(field), value, 2);
        nakedObjectControl.replay();
        
        
        
        TestObjectImpl testObject = new TestObjectImpl(nakedObject, factory);
        testObject.getField("fieldname");
        
        factoryControl.verify();
        fieldControl.verify();
        specificationControl.verify();
        nakedObjectControl.verify();
    }

    public void testAssertFieldInvisibleWhereNotAuthorised() {
        DummyNakedValue value = new DummyNakedValue();
        
        MockControl factoryControl = MockControl.createControl(TestObjectFactory.class);
        TestObjectFactory factory = (TestObjectFactory) factoryControl.getMock(); 
        factoryControl.expectAndDefaultReturn(factory.createTestValue(value), new TestValueImpl(value));
        factoryControl.replay();
        
        
        MockControl fieldControl = MockControl.createControl(OneToOneAssociation.class);
        OneToOneAssociation field = (OneToOneAssociation) fieldControl.getMock();
        fieldControl.expectAndReturn(field.isAuthorised(), false);
//        fieldControl.expectAndReturn(field.getId(), "fieldname", 2);
//        fieldControl.expectAndDefaultReturn(field.isValue(), true);
        fieldControl.replay();
        
        MockControl specificationControl = MockControl.createControl(NakedObjectSpecification.class);
        NakedObjectSpecification specification = (NakedObjectSpecification) specificationControl.getMock();
//        specificationControl.expectAndReturn(specification.getFields(), new NakedObjectField[] {field}); 
        specificationControl.expectAndReturn(specification.getField("fieldname"), field, 1);
        specificationControl.replay();
 
        
        MockControl nakedObjectControl = MockControl.createControl(NakedObject.class);
        NakedObject nakedObject = (NakedObject) nakedObjectControl.getMock();
        nakedObjectControl.expectAndReturn(nakedObject.getSpecification(), specification, 1);
//        nakedObjectControl.expectAndReturn(nakedObject.isVisible(field), Allow.DEFAULT);
//        nakedObjectControl.expectAndReturn(nakedObject.getField(field), value, 2);
        nakedObjectControl.replay();
        
        
        
        TestObjectImpl testObject = new TestObjectImpl(nakedObject, factory);
        testObject.assertFieldInvisible("fieldname");
        
        factoryControl.verify();
        fieldControl.verify();
        specificationControl.verify();
        nakedObjectControl.verify();
    }


    public void testAssertFieldInvisibleWhereNotVisible() {
        DummyNakedValue value = new DummyNakedValue();
        
        MockControl factoryControl = MockControl.createControl(TestObjectFactory.class);
        TestObjectFactory factory = (TestObjectFactory) factoryControl.getMock(); 
        factoryControl.expectAndDefaultReturn(factory.createTestValue(value), new TestValueImpl(value));
        factoryControl.replay();
        
        
        MockControl fieldControl = MockControl.createControl(OneToOneAssociation.class);
        OneToOneAssociation field = (OneToOneAssociation) fieldControl.getMock();
        fieldControl.expectAndReturn(field.isAuthorised(), true);
//        fieldControl.expectAndReturn(field.getId(), "fieldname", 2);
//        fieldControl.expectAndDefaultReturn(field.isValue(), true);
        fieldControl.replay();
        
        MockControl specificationControl = MockControl.createControl(NakedObjectSpecification.class);
        NakedObjectSpecification specification = (NakedObjectSpecification) specificationControl.getMock();
//        specificationControl.expectAndReturn(specification.getFields(), new NakedObjectField[] {field}); 
        specificationControl.expectAndReturn(specification.getField("fieldname"), field, 1);
        specificationControl.replay();
 
        
        MockControl nakedObjectControl = MockControl.createControl(NakedObject.class);
        NakedObject nakedObject = (NakedObject) nakedObjectControl.getMock();
        nakedObjectControl.expectAndReturn(nakedObject.getSpecification(), specification, 1);
        nakedObjectControl.expectAndReturn(nakedObject.isVisible(field), Veto.DEFAULT);
//        nakedObjectControl.expectAndReturn(nakedObject.getField(field), value, 2);
        nakedObjectControl.replay();
        
        
        
        TestObjectImpl testObject = new TestObjectImpl(nakedObject, factory);
        testObject.assertFieldInvisible("fieldname");
        
        factoryControl.verify();
        fieldControl.verify();
        specificationControl.verify();
        nakedObjectControl.verify();
        
/*        
        factoryControl.reset();
        fieldControl.reset();
        specificationControl.reset();
        nakedObjectControl.reset();

        factoryControl.replay();
        fieldControl.replay();
        specificationControl.replay();
        nakedObjectControl.replay();

        testObject.assertFieldInvisible("fieldname");
   */     
    }


    public void testAssertFieldModifiable() {
        DummyNakedValue value = new DummyNakedValue();
        
        MockControl factoryControl = MockControl.createControl(TestObjectFactory.class);
        TestObjectFactory factory = (TestObjectFactory) factoryControl.getMock(); 
        factoryControl.expectAndDefaultReturn(factory.createTestValue(value), new TestValueImpl(value));
        factoryControl.replay();
        
        
        MockControl fieldControl = MockControl.createControl(OneToOneAssociation.class);
        OneToOneAssociation field = (OneToOneAssociation) fieldControl.getMock();
        fieldControl.expectAndReturn(field.isAuthorised(), true);
        fieldControl.expectAndReturn(field.getId(), "fieldname", 1);
//        fieldControl.expectAndDefaultReturn(field.isValue(), true);
        fieldControl.replay();
        
        MockControl specificationControl = MockControl.createControl(NakedObjectSpecification.class);
        NakedObjectSpecification specification = (NakedObjectSpecification) specificationControl.getMock();
//        specificationControl.expectAndReturn(specification.getFields(), new NakedObjectField[] {field}); 
        specificationControl.expectAndReturn(specification.getField("fieldname"), field, 1);
        specificationControl.replay();
 
        
        MockControl nakedObjectControl = MockControl.createControl(NakedObject.class);
        NakedObject nakedObject = (NakedObject) nakedObjectControl.getMock();
        nakedObjectControl.expectAndReturn(nakedObject.getSpecification(), specification, 1);
        nakedObjectControl.expectAndReturn(nakedObject.isVisible(field), Allow.DEFAULT);
        nakedObjectControl.expectAndReturn(nakedObject.isUsable(field), Allow.DEFAULT);
//        nakedObjectControl.expectAndReturn(nakedObject.getField(field), value, 2);
        nakedObjectControl.replay();
        
        
        
        TestObjectImpl testObject = new TestObjectImpl(nakedObject, factory);
        testObject.assertFieldModifiable("fieldname");
        
        factoryControl.verify();
        fieldControl.verify();
        specificationControl.verify();
        nakedObjectControl.verify();
    }

    public void testGetFieldInvisible() {
        DummyNakedValue value = new DummyNakedValue();
        
        MockControl factoryControl = MockControl.createControl(TestObjectFactory.class);
        TestObjectFactory factory = (TestObjectFactory) factoryControl.getMock(); 
        factoryControl.expectAndDefaultReturn(factory.createTestValue(value), new TestValueImpl(value));
        factoryControl.replay();
        
        MockControl fieldControl = MockControl.createControl(OneToOneAssociation.class);
        OneToOneAssociation field = (OneToOneAssociation) fieldControl.getMock();
        fieldControl.expectAndReturn(field.isAuthorised(), false);
        fieldControl.expectAndReturn(field.getId(), "fieldname", 2);
        fieldControl.expectAndDefaultReturn(field.isValue(), true);
        fieldControl.replay();
        
        MockControl specificationControl = MockControl.createControl(NakedObjectSpecification.class);
        NakedObjectSpecification specification = (NakedObjectSpecification) specificationControl.getMock();
        specificationControl.expectAndReturn(specification.getFields(), new NakedObjectField[] {field}); 
        specificationControl.expectAndReturn(specification.getField("fieldname"), field, 1);
        specificationControl.replay();
 
        
        MockControl nakedObjectControl = MockControl.createControl(NakedObject.class);
        NakedObject nakedObject = (NakedObject) nakedObjectControl.getMock();
        nakedObjectControl.expectAndReturn(nakedObject.getSpecification(), specification, 2);
        nakedObjectControl.expectAndReturn(nakedObject.isVisible(field), Allow.DEFAULT);
        nakedObjectControl.expectAndReturn(nakedObject.getField(field), value, 2);
        nakedObjectControl.replay();
        
        
        
        TestObjectImpl testObject = new TestObjectImpl(nakedObject, factory);
        try {
            testObject.getField("fieldname");
            fail();
        } catch (NakedAssertionFailedError e) {
        }
        
        factoryControl.verify();
        //fieldControl.verify();
        //specificationControl.verify();
        //nakedObjectControl.verify();
    }

    public void testAssertEmpty() {}

    public void testInvokeAction() {}

    public void testAssociate() {}

    public void testCantAssociate() {}

    public void testCantClearAssociation() {}
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is

 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
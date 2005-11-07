package org.nakedobjects.xat;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.OneToOneAssociation;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.object.reflect.OneToManyAssociationImpl;
import org.nakedobjects.object.reflect.OneToOneAssociationImpl;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.MockNakedObject;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyActionPeer;
import test.org.nakedobjects.object.reflect.DummyNakedCollection;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyOneToManyPeer;
import test.org.nakedobjects.object.reflect.DummyOneToOnePeer;


public class TestObjectImplFieldTest extends TestCase {

    public static void main(String[] args) {}

    private TestObjectImplExt target;
    private MockNakedObject object;
    private MockObjectFactory factory;

    TestSystem system;

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        
        system = new TestSystem();
        
        factory = new MockObjectFactory();

        object = new MockNakedObject();
        target = new TestObjectImplExt(object, factory);
        object.setupIsUsable(Veto.DEFAULT);
        object.setupIsVisible(Allow.DEFAULT);
//        hint.setupCanAccess(new NoOpAllow());
   //     hint.setupCanUse(new NoOpAllow());
      //  object.setupHint(hint);


        //new NakedObjectsClient().setObjectManager(new MockObjectManager());
        
        system.init();
    }

    public void testGetField() {
        DummyNakedObjectSpecification fieldSpec = new DummyNakedObjectSpecification();

        DummyNakedObjectSpecification objectSpec = new DummyNakedObjectSpecification();
        OneToOneAssociation field1 = new OneToOneAssociationImpl("cls", "one", fieldSpec, new DummyOneToOnePeer());
        OneToOneAssociation field2 = new OneToOneAssociationImpl("cls", "two", fieldSpec, new DummyOneToOnePeer());
        objectSpec.setupFields(new NakedObjectField[] { field1, field2 });
        object.setupSpecification(objectSpec);
    
        DummyNakedObject fieldObject1 = new DummyNakedObject();
        fieldObject1.setupResolveState(ResolveState.GHOST);
        object.setupFieldValue("one", fieldObject1);

        DummyNakedObject fieldObject2 = new DummyNakedObject();
        object.setupFieldValue("two", fieldObject2);

        target.expected.addExpectedMethod("fieldAccessorFor");
        target.expected.addExpectedParameter("one");

        target.setupField(field1);

        factory.calls.addExpectedMethod("createTestObject");
        factory.calls.addExpectedParameter(fieldObject1);
        factory.calls.addExpectedParameter("cache");
        
        factory.calls.addExpectedMethod("createTestObject");
        factory.calls.addExpectedParameter(fieldObject2);
        factory.calls.addExpectedParameter("cache");

        MockTestObject fieldTestObject = new MockTestObject();
        factory.setupObject(fieldTestObject);
        
        TestNaked field = target.getField("one");
        assertEquals(fieldTestObject, field);
        
        target.verify();
        factory.verify();
    }
    

    public void testGetCollectionField() {
        DummyNakedObjectSpecification fieldSpec = new DummyNakedObjectSpecification();

        DummyNakedObjectSpecification objectSpec = new DummyNakedObjectSpecification();
        OneToOneAssociation field1 = new OneToOneAssociationImpl("cls", "one", fieldSpec, new DummyOneToOnePeer());
        OneToManyAssociation field2 = new OneToManyAssociationImpl("cls", "two", fieldSpec, new DummyOneToManyPeer());
        objectSpec.setupFields(new NakedObjectField[] { field1, field2 });
        object.setupSpecification(objectSpec);
        
        DummyNakedObject fieldObject = new DummyNakedObject();
        fieldObject.setupResolveState(ResolveState.GHOST);
        object.setupFieldValue("one", fieldObject);

        DummyNakedCollection fieldCollection = new DummyNakedCollection();
        object.setupFieldValue("two", fieldCollection);


        target.expected.addExpectedMethod("fieldAccessorFor");
        target.expected.addExpectedParameter("two");

        target.setupField(field1);

        factory.calls.addExpectedMethod("createTestObject");
        factory.calls.addExpectedParameter(fieldObject);
        factory.calls.addExpectedParameter("cache");
        
        factory.calls.addExpectedMethod("createTestCollection");
        factory.calls.addExpectedParameter(fieldCollection);
 
        MockTestObject fieldTestObject = new MockTestObject();
        factory.setupObject(fieldTestObject);

        MockTestCollection fieldTestCollection = new MockTestCollection();
        factory.setupCollection(fieldTestCollection);

        TestNaked field = target.getField("two");
        assertEquals(fieldTestCollection, field);
        
        target.verify();
        factory.verify();
    }
    

    public void testAssertEmpty() {
        DummyNakedObjectSpecification fieldSpec = new DummyNakedObjectSpecification();

        DummyNakedObjectSpecification objectSpec = new DummyNakedObjectSpecification();
        OneToOneAssociation field1 = new OneToOneAssociationImpl("cls", "one", fieldSpec, new DummyOneToOnePeer());
        OneToOneAssociation field2 = new OneToOneAssociationImpl("cls", "two", fieldSpec, new DummyOneToOnePeer());
        objectSpec.setupFields(new NakedObjectField[] { field1, field2 });
        object.setupSpecification(objectSpec);
    
        DummyNakedObject fieldObject1 = new DummyNakedObject();
        fieldObject1.setupResolveState(ResolveState.GHOST);
        object.setupFieldValue("one", fieldObject1);

        DummyNakedObject fieldObject2 = new DummyNakedObject();
        object.setupFieldValue("two", fieldObject2);

        target.expected.addExpectedMethod("fieldAccessorFor");
        target.expected.addExpectedParameter("one");

        target.setupField(field1);

        factory.calls.addExpectedMethod("createTestObject");
        factory.calls.addExpectedParameter(fieldObject1);
        factory.calls.addExpectedParameter("cache");
        
        factory.calls.addExpectedMethod("createTestObject");
        factory.calls.addExpectedParameter(fieldObject2);
        factory.calls.addExpectedParameter("cache");

        MockTestObject fieldTestObject = new MockTestObject();
        factory.setupObject(fieldTestObject);
        
        
        target.assertEmpty("one");
        
        target.verify();
        factory.verify();
    }
    
    
    public void testInvokeAction() {
        target.expected.addExpectedMethod("getAction");
        target.expected.addExpectedParameter("testaction");

        Action action = new ActionImpl("cls", "method", new DummyActionPeer());
        target.setupAction(action);
 
        object.calls.addExpectedMethod("execute");
        object.calls.addExpectedParameter(action);
        
        target.invokeAction("Test Action", new TestNaked[0]);
        
        
        target.verify();
    }
    
    /*

    public void testAssociate() {
        target.associate("Four Default", singleParameter);
        assertEquals(singleParameter.toString(), targetObject.result());
        
        try {
	        target.associate("Seven Unusable", singleParameter);
	        fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testCantAssociate() {
        try {
            target.associate("Five Unmodifiable", singleParameter);
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.associate("Six Invisible", singleParameter);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testCantClearAssociation() {
        try {
            target.clearAssociation("Five Unmodifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.clearAssociation("Six Invisible");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testCantInvokeAction() {

        try {
            target.invokeAction("Four Unusable");
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            target.invokeAction("Six Unusable", singleParameter);
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            target.invokeAction("ten Unusable", multipleParameters);
            fail();
        } catch (NakedAssertionFailedError e) {}
    }

    public void testCantSeeAction() {
        try {
            target.invokeAction("Three Invisible");
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            target.invokeAction("Five Invisible", singleParameter);
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            target.invokeAction("EIGHT Invisible", multipleParameters);
            fail();
        } catch (NakedAssertionFailedError e) {}
    }

    public void testCantSetValueFields() {
        try {
            target.fieldEntry("Two Unmodifiable", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.fieldEntry("Three Invisible", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testClearAssociation() {
        target.clearAssociation("Four Default");
    }

    public void testGetFieldElement() {
        TestObject fld = target.getField("collection", "three");

        assertEquals(elementThree, fld);
    }

    public void testInvalidFieldEntry() {
        try {
            target.fieldEntry("amount", "7.0");
            target.fieldEntry("amount", "-3.0");
            fail();
        } catch (IllegalActionError expected) {}
        Money amount = ((Money) target.getField("amount").getForObject());
        assertEquals(7.0, amount.doubleValue(), 0.0);
    }

    public void testInvokeAction() {
        target.invokeAction("One Default");
        assertEquals("one", targetObject.result());

        target.invokeAction("Two Default", singleParameter);
        assertEquals("two", targetObject.result());

        target.invokeAction("Seven", multipleParameters);
        assertEquals("a value", targetObject.result());
    }

    public void testSetValueFields() {
        target.fieldEntry("One Modifiable", "text entry");
    }

    public void testTestAssociate() {
        target.testField("Four Default", singleParameter);
    }

    public void testTestAssociateButCantBeChanged() {
        try {
            target.testField("Five Unmodifiable", singleParameter);
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.testField("Six Invisible", singleParameter);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testValueTestFields() {
        target.testField("One Modifiable", "text entry");
        target.testField("One Modifiable", "text entry", "text entry");
    }

    public void testValueTestFieldsThatCantBeChanged() {
        try {
            target.testField("Two Unmodifiable", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.testField("Two Unmodifiable", "text entry", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.testField("Three Invisible", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            target.testField("Three Invisible", "text entry", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }
    
    */
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
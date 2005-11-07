package org.nakedobjects.xat;

import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Veto;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.object.reflect.OneToOneAssociationImpl;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyAction;
import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.MockNakedObject;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.control.NoOpAllow;
import test.org.nakedobjects.object.control.NoOpVeto;
import test.org.nakedobjects.object.reflect.DummyActionPeer;
import test.org.nakedobjects.object.reflect.DummyField;
import test.org.nakedobjects.object.reflect.DummyOneToOnePeer;


public class TestObjectImplFieldsTest extends TestCase {

    public static void main(String[] args) {}

    private TestObjectImplExt target;
    private MockNakedObject object;

    DummyTestNaked parameter1 = new DummyTestNaked();
    DummyTestNaked parameter2 = new DummyTestNaked();
    DummyTestNaked parameter3 = new DummyTestNaked();
            

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        
        object = new MockNakedObject();
        TestObjectFactory factory = new MockObjectFactory();
        target = new TestObjectImplExt(object, factory);
   //     target.setupAction(new DummyAction());
    }
    
    public void testZeroParameterAssertActionExistsFails() {
        try {
            expectCallToGetAction(false, 0);

            target.assertActionExists("Action Name");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }


    private void expectCallToGetAction(boolean returnAction, int parameters) {
        if(returnAction) {
            target.setupAction(new ActionImpl("cls", "method", new DummyActionPeer()));
        }
        
        target.expected.addExpectedMethod("getAction");
        target.expected.addExpectedParameter("Action Name");
        if(parameters >= 1) {
            target.expected.addExpectedParameter(parameter1);
        }
        if(parameters >= 2) {
            target.expected.addExpectedParameter(parameter2);
        }
        if(parameters >= 3) {
            target.expected.addExpectedParameter(parameter3);
        }
        if(parameters >= 4) {
           	throw new IllegalArgumentException();
        }

    }
    

    private void expectCallToFieldFor() {
        target.setupField(new OneToOneAssociationImpl("", "", new DummyNakedObjectSpecification(), new DummyOneToOnePeer()));
        
        target.expected.addExpectedMethod("fieldAccessorFor");
        target.expected.addExpectedParameter("Field Name");
    }


    public void testZeroParameterAssertActionExists() {
        expectCallToGetAction(true, 0);
        target.assertActionExists("Action Name");
        target.verify();
    }
    
    public void testOneParameterAssertActionExistsFails() {
        try {
            expectCallToGetAction(false, 1);
                        
            target.assertActionExists("Action Name", parameter1);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }
    

    public void testOneParameterAssertActionExists() {
        expectCallToGetAction(true, 1);
        target.assertActionExists("Action Name", parameter1);
        target.verify();
    }
    

    public void testTwoParameterAssertActionExistsFails() {
        try {
            expectCallToGetAction(false, 2);
            target.assertActionExists("Action Name", new TestNaked[] {parameter1, parameter2});
            fail();
        } catch (NakedAssertionFailedError expected) {}
        target.verify();
    }
    

    public void testTwoParameterAssertActionExists() {
        expectCallToGetAction(true, 2);

        target.assertActionExists("Action Name", new TestNaked[] {parameter1, parameter2});

        target.verify();
    }
    

    public void testAssertActionInvisible() {
        target.setupAction(new DummyAction());        
        object.setupIsVisible(Veto.DEFAULT);
        
        
        expectCallToGetAction(false, 0);
        target.assertActionInvisible("Action Name");

        target.verify();
    }

    public void testAssertActionInvisibleFails() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Allow.DEFAULT);

        expectCallToGetAction(false, 0);
        
        try {
	        target.assertActionInvisible("Action Name");
	        fail();
        } catch (NakedAssertionFailedError expected) {
        }

        target.verify();
    }
    

    public void testAssertActionVisible() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Allow.DEFAULT);
                
        expectCallToGetAction(false, 0);
        target.assertActionVisible("Action Name");

        target.verify();
    }

    public void testAssertActionVisibleFails() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Veto.DEFAULT);

        expectCallToGetAction(false, 0);
        
        try {
	        target.assertActionVisible("Action Name");
	        fail();
        } catch (NakedAssertionFailedError expected) {
        }

        target.verify();
    }

    public void testAssertActionUsable() {
        target.setupAction(new DummyAction());
        object.setupIsUsable(Allow.DEFAULT);
        object.setupIsValid(Allow.DEFAULT);
        
        expectCallToGetAction(false, 0);
        target.assertActionUsable("Action Name");

        target.verify();
    }

    public void testAssertActionUsableFails() {
        target.setupAction(new DummyAction());
        object.setupIsUsable(Veto.DEFAULT);
        
        expectCallToGetAction(false, 0);
        
        try {
	        target.assertActionUsable("Action Name");
	        fail();
        } catch (NakedAssertionFailedError expected) {
        }

        target.verify();
    }

    public void testAssertActionUnusable() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Allow.DEFAULT);
        object.setupIsUsable(Veto.DEFAULT);
//        object.setupIsValid(Allow.DEFAULT);        
        
        expectCallToGetAction(false, 0);
        target.assertActionUnusable("Action Name");

        target.verify();
    }

    public void testAssertActionUnusableAsNotValuie() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Allow.DEFAULT);
        object.setupIsUsable(Allow.DEFAULT);
        object.setupIsValid(Veto.DEFAULT);        
        
        expectCallToGetAction(false, 0);
        target.assertActionUnusable("Action Name");

        target.verify();
    }

    public void testAssertActionUnusableFails() {
        target.setupAction(new DummyAction());
        object.setupIsVisible(Allow.DEFAULT);
        object.setupIsUsable(Allow.DEFAULT);
        object.setupIsValid(Allow.DEFAULT);
        
        expectCallToGetAction(false, 0);
        
        try {
	        target.assertActionUnusable("Action Name");
	        fail();
        } catch (NakedAssertionFailedError expected) {
        }

        target.verify();
    }

    public void testAssertFieldInvisible() {
        new TestSystem().init();
        
        expectCallToFieldFor();
        target.assertFieldInvisible("Field Name");

        target.verify();
    }
 

/*


    public void testAssertFieldContainsElement() {
        target.assertFieldContains("collection", "one");
        try {
            target.assertFieldContains("collection", "four");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }
    
    public void testAssertFieldDoesNotContain() {
        target.assertFieldDoesNotContain("", object);
    }

    public void testAssertFieldEntryInvalid() {
        target.assertFieldEntryInvalid("amount", "-3.0");
        try {
            target.assertFieldEntryInvalid("amount", "7.0");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldEntryNotParseable() {
        target.assertFieldEntryCantParse("amount", "xxx");
        try {
            target.assertFieldEntryCantParse("amount", "-3.0");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldExists() {
        target.assertFieldExists("One Modifiable");
        target.assertFieldExists("Three Invisible");
        try {
            target.assertFieldExists("Nonexistant");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldInvisible() {
        target.assertFieldInvisible("Three Invisible");
        try {
            target.assertFieldInvisible("One Modifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldModifiable() {
        target.assertFieldModifiable("One Modifiable");
        try {
            target.assertFieldModifiable("Two Unmodifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldUnmodifiable() {
        target.assertFieldUnmodifiable("Two Unmodifiable");
        try {
            target.assertFieldUnmodifiable("One Modifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldVisible() {
        target.assertFieldVisible("One Modifiable");
        try {
            target.assertFieldVisible("Three Invisible");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFirstElementInField() {
        target.assertFirstElementInField("collection", elementOne);
        try {
            target.assertFirstElementInField("collection", elementThree);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFirstElementInFieldByTitle() {
        target.assertFirstElementInField("collection", "one");
        try {
            target.assertFirstElementInField("collection", "thtree");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertLastElementInField() {
        target.assertLastElementInField("collection", elementThree);
        try {
            target.assertLastElementInField("collection", elementOne);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertLasttElementInFieldByTitle() {
        target.assertLastElementInField("collection", "three");
        try {
            target.assertLastElementInField("collection", "one");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

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
package org.nakedobjects.xat;

import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.security.SecurityContext;
import org.nakedobjects.security.Session;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TestObjectImplTest extends TestCase {

    private TestObjectImpl testObject;
    private TestObjectExample object;
    private MockObjectManager om;

    public static void main(String[] args) {}

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        om = MockObjectManager.setup();
        om.setupAddClass(NakedObject.class);
        om.setupAddClass(TestObjectExample.class);

        Session.initSession();
        SecurityContext context = Session.getSession().getSecurityContext();
        object = new TestObjectExample();

        testObject = new TestObjectImpl(context, object);
    }
    
    protected void tearDown() throws Exception {
        Session.getSession().shutdown();
        om.shutdown();
        super.tearDown();
    }

    public void testAssertActionVisible() {
        testObject.assertActionVisible("One Default");
        testObject.assertActionVisible("Two Default", testObject);
    }

    public void testAssertActionExists() {
        testObject.assertActionExists("One Default");
        testObject.assertActionExists("Two Default", testObject);
        testObject.assertActionExists("Three Invisible");

    }

    public void testAssertActionInvisible() {
        testObject.assertActionInvisible("Three Invisible");

        try {
            testObject.assertActionInvisible("One Default");
        } catch (AssertionFailedError expected) {
            return;
        }
        fail();
    }

    public void testAssertActionUsable() {
        testObject.assertActionUsable("One Default");
    }

    public void testAssertActionUnusable() {
        testObject.assertActionUnusable("Four Unusable");
    }

    public void testAssertFieldModifiable() {
        testObject.assertFieldModifiable("One Modifiable");
        try {
            testObject.assertFieldModifiable("Two Unmodifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldUnmodifiable() {
        testObject.assertFieldUnmodifiable("Two Unmodifiable");
        try {
            testObject.assertFieldUnmodifiable("One Modifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldVisible() {
        testObject.assertFieldVisible("One Modifiable");
        try {
            testObject.assertFieldVisible("Three Invisible");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldInvisible() {
        testObject.assertFieldInvisible("Three Invisible");
        try {
            testObject.assertFieldInvisible("One Modifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssertFieldExists() {
        testObject.assertFieldExists("One Modifiable");
        testObject.assertFieldExists("Three Invisible");
        try {
            testObject.assertFieldExists("Nonexistant");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testInvokeAction() {
        testObject.invokeAction("One Default");
        assertEquals("one", object.result());

        testObject.invokeAction("Two Default", testObject);
        assertEquals("two", object.result());

        try {
            testObject.invokeAction("Three Invisible");
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            testObject.invokeAction("Five Invisible", testObject);
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            testObject.invokeAction("Four Unusable");
            fail();
        } catch (NakedAssertionFailedError e) {}

        try {
            testObject.invokeAction("Six Unusable", testObject);
            fail();
        } catch (NakedAssertionFailedError e) {}
    }

    public void testSetValueFields() {
        testObject.fieldEntry("One Modifiable", "text entry");

        try {
            testObject.fieldEntry("Two Unmodifiable", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.fieldEntry("Three Invisible", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testValueTestFields() {
        testObject.testField("One Modifiable", "text entry");
        testObject.testField("One Modifiable", "text entry", "text entry");

        try {
            testObject.testField("Two Unmodifiable", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.testField("Two Unmodifiable", "text entry", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.testField("Three Invisible", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.testField("Three Invisible", "text entry", "text entry");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

    public void testAssociate() {
        testObject.associate("Four Default", testObject);
        assertEquals(testObject.toString(), object.result());

        try {
            testObject.associate("Five Unmodifiable", testObject);
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.associate("Six Invisible", testObject);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }
    
    public void testTestAssociate() {
        testObject.testField("Four Default", testObject);

        try {
            testObject.testField("Five Unmodifiable", testObject);
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.testField("Six Invisible", testObject);
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }
    

    public void testClearAssociation() {
        testObject.clearAssociation("Four Default");

        try {
            testObject.clearAssociation("Five Unmodifiable");
            fail();
        } catch (NakedAssertionFailedError expected) {}

        try {
            testObject.clearAssociation("Six Invisible");
            fail();
        } catch (NakedAssertionFailedError expected) {}
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
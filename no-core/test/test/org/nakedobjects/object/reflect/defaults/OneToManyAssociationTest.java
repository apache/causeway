package test.org.nakedobjects.object.reflect.defaults;


import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToManyAssociationImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.NakedObjectTestCase;
import test.org.nakedobjects.object.control.MockHint;
import test.org.nakedobjects.object.defaults.MockObjectPersistenceManager;
import test.org.nakedobjects.object.reflect.DummyNakedCollection;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.object.reflect.DummyOneToManyPeer;


public class OneToManyAssociationTest extends NakedObjectTestCase {
   // private static final String FIELD_LABEL = "Members";
    private static final String FIELD_NAME = "members";
	private NakedObject nakedObject;
	private NakedObject associate;
	private OneToManyAssociation association;
    private DummyNakedObjectSpecification type;
    private DummyOneToManyPeer associationDelegate;
	
    public OneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(OneToManyAssociationTest.class));
    }

    public void setUp()  throws ObjectPerstsistenceException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);

    	new NakedObjectsClient().setPersistenceManager(new MockObjectPersistenceManager());
    	
       	nakedObject = new DummyNakedObject();
        associate = new DummyNakedObject();
        
        type = new DummyNakedObjectSpecification();
        associationDelegate = new DummyOneToManyPeer();
        association = new OneToManyAssociationImpl("", FIELD_NAME, type, associationDelegate);
    }
    
    public void testType() {
    	assertEquals(type, association.getSpecification());
    }
    	
    public void testSet() {
        association.setAssociation(nakedObject, associate);
        associationDelegate.assertAction(0, "add " + nakedObject);
        associationDelegate.assertAction(1, "add " + associate);
    }     	
    
    public void testClear() {
        association.clearAssociation(nakedObject, associate);
        associationDelegate.assertAction(0, "remove " + nakedObject);
        associationDelegate.assertAction(1, "remove " + associate);
 }     	
    
    public void testClearWithNull() {
        try {
        association.clearAssociation(nakedObject, null);
        fail();
        } catch (IllegalArgumentException expected) {
        }
        associationDelegate.assertActions(0);
 }     	

    
    public void testSetWithNull() {
        try {
        association.setAssociation(nakedObject, null);
        fail();
        } catch (IllegalArgumentException expected) {
        }
        associationDelegate.assertActions(0);
 }     	


    public void testGet() {
        NakedCollection collection = new DummyNakedCollection();
        associationDelegate.getCollection = collection;
        Naked returnedObject = association.get(nakedObject);
        assertEquals(collection, returnedObject);
    }     	
    
    public void testName() {
    	assertEquals(FIELD_NAME, association.getName());
    }
    
    public void testLabel() {
        associationDelegate.setupHint(null);
        
        assertEquals(FIELD_NAME, association.getLabel(nakedObject));

        MockHint mockHint = new MockHint();
        mockHint.setupName("label from hint");
        associationDelegate.hint = mockHint;
        
        associationDelegate.expect("getHint " + "#members()" + " " + nakedObject + " null true");

        assertEquals("label from hint", association.getLabel(nakedObject));
   }
    
    public void testAboutForSet() {
        assertFalse(association.hasHint());

        Hint about = association.getHint(nakedObject, associate, true);
       assertNull(associationDelegate.hint);
       assertTrue(about instanceof DefaultHint);
       associationDelegate.assertActions(0);

       MockHint mockHint = new MockHint();
       associationDelegate.hint = mockHint;
       
       associationDelegate.expect("getHint " + "#members()" + " " + nakedObject + " " + associate + " true");

       assertTrue(association.hasHint());

       about = association.getHint(nakedObject, associate, true);
       assertEquals(associationDelegate.hint, about);
       associationDelegate.assertAction(0, "about " + nakedObject);
       associationDelegate.assertAction(1, "about " + associate);
       associationDelegate.assertAction(2, "about " + true);
    }
    
    
    public void testAboutForClear() {
        assertFalse(association.hasHint());

        Hint about = association.getHint(nakedObject, associate, false);
       assertNull(associationDelegate.hint);
       assertTrue(about instanceof DefaultHint);
       associationDelegate.assertActions(0);

       MockHint mockHint = new MockHint();
       associationDelegate.hint = mockHint;
       
       associationDelegate.expect("getHint " + "#members()" + " " + nakedObject  + " " + associate + " false");

       assertTrue(association.hasHint());

       about = association.getHint(nakedObject, associate, false);
       assertEquals(associationDelegate.hint, about);
       associationDelegate.assertAction(0, "about " + nakedObject);
       associationDelegate.assertAction(1, "about " + associate);
       associationDelegate.assertAction(2, "about " + false);
    }

    public void testFullAbout() {
        assertFalse(association.hasHint());

        Hint about = association.getHint(nakedObject);
       assertNull(associationDelegate.hint);
       assertTrue(about instanceof DefaultHint);

       MockHint mockHint = new MockHint();
       associationDelegate.hint = mockHint;
       
       associationDelegate.expect("getHint " + "#members()" + " " + nakedObject  + " null true");

      assertTrue(association.hasHint());

       about = association.getHint(nakedObject);
       assertEquals(associationDelegate.hint, about);
    }

}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/

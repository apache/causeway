package test.org.nakedobjects.object.reflect.defaults;


import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.OneToManyAssociation;
import org.nakedobjects.object.control.Allow;
import org.nakedobjects.object.control.Consent;
import org.nakedobjects.object.reflect.OneToManyAssociationImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.NakedObjectTestCase;
import test.org.nakedobjects.object.defaults.MockObjectPersistor;
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

    	new NakedObjectsClient().setObjectPersistor(new MockObjectPersistor());
    	
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
        association.addElement(nakedObject, associate);
        associationDelegate.assertAction(0, "add " + nakedObject);
        associationDelegate.assertAction(1, "add " + associate);
    }     	
    
    public void testClear() {
        association.removeElement(nakedObject, associate);
        associationDelegate.assertAction(0, "remove " + nakedObject);
        associationDelegate.assertAction(1, "remove " + associate);
 }     	
    
    public void testClearWithNull() {
        try {
        association.removeElement(nakedObject, null);
        fail();
        } catch (IllegalArgumentException expected) {
        }
        associationDelegate.assertActions(0);
 }     	

    
    public void testSetWithNull() {
        try {
        association.addElement(nakedObject, null);
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
    	assertEquals(FIELD_NAME, association.getId());
    }
    
    public void testLabel() {
        assertEquals(FIELD_NAME, association.getName());
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

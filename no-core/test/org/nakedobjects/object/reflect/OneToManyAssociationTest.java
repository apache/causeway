package org.nakedobjects.object.reflect;


import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.collection.InternalCollectionVector;
import org.nakedobjects.object.defaults.value.TestClock;
import org.nakedobjects.object.security.Session;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class OneToManyAssociationTest extends NakedObjectTestCase {
    private static final String MEMBERS_FIELD_LABEL = "Members";
    private static final String MEMBERS_FIELD_NAME = "members";
	private Team object;
	private OneToManyAssociationSpecification collectionField;
	private Person elements[];
    private MockObjectManager manager;
    private Session session;
	
    public OneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(OneToManyAssociationTest.class));
    }

    public void setUp()  throws ObjectStoreException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);

    	manager = MockObjectManager.setup();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
    	new TestClock();
    	
       	session = new Session();

		object = new Team();
		object.setNakedClass(NakedObjectSpecification.getSpecification(object.getClass()));
		object.setContext(manager.getContext());
        elements = new Person[3];
        for (int i = 0; i < elements.length; i++) {
			elements[i] = new Person();
		}
        NakedObjectSpecification c = object.getSpecification();
        
        collectionField = (OneToManyAssociationSpecification) c.getField(MEMBERS_FIELD_NAME);
    }
    
    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    public void testType() {
    	assertEquals(Person.class.getName(), collectionField.getType().getFullName());
    }
    	
    public void testSet() {
    	for (int i = 0; i < elements.length; i++) {
    		collectionField.setAssociation(object, elements[i]);
    	}
 
    	InternalCollection collection = object.getMembers();
    	assertEquals(elements.length, collection.size());
    	for (int i = 0; i < elements.length; i++) {
    		assertEquals(elements[i], collection.elementAt(i));
    	}
    }     	
    
    public void testRemove() {
    }     	
    
    public void testGet() {
    	assertTrue( collectionField.get(object).isSameAs(new InternalCollectionVector(Person.class, object)));
    }     	
    
    public void testName() {
    	assertEquals(MEMBERS_FIELD_NAME, collectionField.getName());
    }
    
    public void testLabel() {
    	assertEquals(MEMBERS_FIELD_LABEL, collectionField.getLabel(session, object));
    }
    
    public void testAbout() {
    	assertTrue(collectionField.hasAbout());

    	assertNotNull(collectionField.getAbout(session, object));
    }
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.reflect.defaults;


import org.nakedobjects.object.DummyInternalCollection;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.value.TestClock;

import java.lang.reflect.Method;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class JavaOneToManyAssociationTest extends NakedObjectTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";
 	private MockTeam object;
	private JavaOneToManyAssociation collectionField;
	private MockPerson elements[];
    private MockObjectManager manager;

    public JavaOneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToManyAssociationTest.class));
    }

    public void setUp()  throws Exception {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);

    	manager = MockObjectManager.setup();
    	new TestClock();
    	
		object = new MockTeam();
		object.setContext(manager.getContext());
        elements = new MockPerson[3];
        for (int i = 0; i < elements.length; i++) {
			elements[i] = new MockPerson();
		}

        Class cls = MockTeam.class;
        Method get = cls.getDeclaredMethod("getMethod", new Class[0]);
        Method add = cls.getDeclaredMethod("addToMethod", new Class[] {MockPerson.class});
        Method remove = cls.getDeclaredMethod("removeFromMethod", new Class[] {MockPerson.class});

        collectionField = new JavaOneToManyAssociation(MEMBERS_FIELD_NAME, InternalCollection.class, get, add, remove, null);
    }
    
    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    public void testType() {
    	assertEquals(InternalCollection.class.getName(), collectionField.getType().getFullName());
    }
    	
    public void testAdd() {
        MockPerson associate = new MockPerson();
        collectionField.addAssociation(object, associate);
        
        assertEquals(associate, object.added);
    }     	
    
    public void testRemove() {
        MockPerson associate = new MockPerson();
        collectionField.removeAssociation(object, associate);
        
        assertEquals(associate, object.removed);
    }     	
    
    public void testGet() {
    	assertNull( collectionField.get(object));
    	
    	object.collection = new DummyInternalCollection();
    	assertNotNull( collectionField.get(object));
    	assertEquals(object.collection, collectionField.get(object));
    }     	
    
    public void testName() {
    	assertEquals(MEMBERS_FIELD_NAME, collectionField.getName());
    }
    
    public void testAbout() {
    	assertFalse(collectionField.hasAbout());
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

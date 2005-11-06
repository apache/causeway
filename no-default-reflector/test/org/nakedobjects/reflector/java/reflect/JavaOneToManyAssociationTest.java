package org.nakedobjects.reflector.java.reflect;



import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;

import java.lang.reflect.Method;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.NakedObjectTestCase;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyNakedCollection;


public class JavaOneToManyAssociationTest extends NakedObjectTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";
 	private JavaObjectWithVector objectWithVector;
	private JavaOneToManyAssociation collectionField;
	private JavaReferencedObject elements[];
//    private MockNakedObjectSpecificationLoader loader;
    private NakedObject nakedObject;
  //  private ObjectLoaderImpl objectLoader;
    private TestSystem system;

    public JavaOneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToManyAssociationTest.class));
    }

    protected void setUp()  throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.addSpecification(new DummyNakedObjectSpecification(JavaObjectWithVector.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(JavaReferencedObject.class.getName()));
        system.addSpecification(new DummyNakedObjectSpecification(InternalCollection.class.getName()));
        system.init();

        objectWithVector = new JavaObjectWithVector();
		nakedObject = system.createAdapterForTransient(objectWithVector);
        elements = new JavaReferencedObject[3];
        for (int i = 0; i < elements.length; i++) {
			elements[i] = new JavaReferencedObject();
		}

        Class cls = JavaObjectWithVector.class;
        Method get = cls.getDeclaredMethod("getMethod", new Class[0]);
        Method add = cls.getDeclaredMethod("addToMethod", new Class[] {JavaReferencedObject.class});
        Method remove = cls.getDeclaredMethod("removeFromMethod", new Class[] {JavaReferencedObject.class});
        collectionField = new JavaOneToManyAssociation(MEMBERS_FIELD_NAME, InternalCollection.class, get, add, remove, null);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testType() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification(InternalCollection.class.getName());
        system.addSpecification(spec);
    	assertEquals(spec, collectionField.getType());
    }
    	
    public void testAdd() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        JavaReferencedObject associate = new JavaReferencedObject();
        NakedObject nakedObjectAssoicate = system.createAdapterForTransient(associate);
        
        spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        assertNull(objectWithVector.added);
        collectionField.addAssociation(nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.added);
    }     	
    
    public void testRemove() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        JavaReferencedObject associate = new JavaReferencedObject();
        NakedObject nakedObjectAssoicate =system.createAdapterForTransient(associate);
        
        spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        assertNull(objectWithVector.removed);
        collectionField.removeAssociation(nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.removed);
    }     	
    
    public void testGet() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        DummyNakedCollection collection = new DummyNakedCollection();
        system.addNakedCollectionAdapter(collection);
        
    	assertNotNull(collectionField.getAssociations(nakedObject));
    	assertEquals(collection.getObject(), collectionField.getAssociations(nakedObject).getObject());
    }     	
    
    public void testName() {
    	assertEquals(MEMBERS_FIELD_NAME, collectionField.getIdentifier());
    }
    
    public void testAbout() {
    	assertFalse(collectionField.hasHint());
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

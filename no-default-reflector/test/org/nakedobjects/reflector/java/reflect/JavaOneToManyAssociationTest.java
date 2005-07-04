package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.reflect.internal.DummyIdentifier;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;

import java.lang.reflect.Method;
import java.util.Vector;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaOneToManyAssociationTest extends NakedObjectTestCase {
    private static final String MEMBERS_FIELD_NAME = "members";
 	private JavaObjectWithVector objectWithVector;
	private JavaOneToManyAssociation collectionField;
	private JavaReferencedObject elements[];
    private MockNakedObjectSpecificationLoader loader;
    private NakedObject nakedObject;
    private ObjectLoaderImpl objectLoader;

    public JavaOneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToManyAssociationTest.class));
    }

    protected void setUp()  throws Exception {
        
        Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
    	NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setSpecificationLoader(loader);
    	nakedObjects.setConfiguration(new TestConfiguration());
        
		objectWithVector = new JavaObjectWithVector();
    	objectLoader = new ObjectLoaderImpl();
		objectLoader.setPojoAdapterHash(new PojoAdapterHashImpl());
		objectLoader.setReflectorFactory(new NullReflectorFactory());
		nakedObject = objectLoader.createAdapterForTransient(objectWithVector);
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
    
    public void testType() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
    	assertEquals(spec, collectionField.getType());
    }
    	
    public void testAdd() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        JavaReferencedObject associate = new JavaReferencedObject();
        NakedObject nakedObjectAssoicate =objectLoader.createAdapterForTransient(associate);
        
        spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        assertNull(objectWithVector.added);
        collectionField.addAssociation(new DummyIdentifier(), nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.added);
    }     	
    
    public void testRemove() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        JavaReferencedObject associate = new JavaReferencedObject();
        NakedObject nakedObjectAssoicate =objectLoader.createAdapterForTransient(associate);
        
        spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        assertNull(objectWithVector.removed);
        collectionField.removeAssociation(new DummyIdentifier(), nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.removed);
    }     	
    
    public void testGet() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
    	//objectWithVector.collection = new DummyInternalCollection();
    	assertNotNull(collectionField.getAssociations(new DummyIdentifier(), nakedObject));
    	assertEquals(new Vector(), collectionField.getAssociations(new DummyIdentifier(), nakedObject).getObject());
    }     	
    
    public void testName() {
    	assertEquals(MEMBERS_FIELD_NAME, collectionField.getName());
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

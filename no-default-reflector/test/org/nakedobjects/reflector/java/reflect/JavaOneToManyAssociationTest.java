package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.MockNakedObjectContext;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.reflect.PojoAdapter;

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
    private PojoAdapter nakedObject;

    public JavaOneToManyAssociationTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToManyAssociationTest.class));
    }

    public void setUp()  throws Exception {
        
        Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
    	
    	ConfigurationFactory.setConfiguration(new TestConfiguration());
        
		objectWithVector = new JavaObjectWithVector();
		nakedObject = PojoAdapter.createAdapter(objectWithVector);
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
        NakedObject nakedObjectAssoicate =PojoAdapter.createAdapter(associate);
        
        NakedObjectContext context = new MockNakedObjectContext(MockObjectManager.setup());
        nakedObject.setContext(context);

        assertNull(objectWithVector.added);
        collectionField.addAssociation(nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.added);
    }     	
    
    public void testRemove() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        JavaReferencedObject associate = new JavaReferencedObject();
        NakedObject nakedObjectAssoicate =PojoAdapter.createAdapter(associate);
        
        assertNull(objectWithVector.removed);
        collectionField.removeAssociation(nakedObject, nakedObjectAssoicate);
        assertEquals(associate, objectWithVector.removed);
    }     	
    
    public void testGet() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
    	//objectWithVector.collection = new DummyInternalCollection();
    	assertNotNull(collectionField.getAssociations(nakedObject));
    	assertEquals(new Vector(), collectionField.getAssociations(nakedObject).getObject());
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

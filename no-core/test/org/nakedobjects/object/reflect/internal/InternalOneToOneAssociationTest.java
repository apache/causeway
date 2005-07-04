package org.nakedobjects.object.reflect.internal;


import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.DummyNakedObject;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class InternalOneToOneAssociationTest extends TestCase {
    private static final String PERSON_FIELD_NAME = "person";
	private InternalObjectWithOneToOneAssociations objectWithOneToOneAssoications;
	private MockNakedObject nakedObject;
	private InternalOneToOneAssociation personField;
	private InternalObjectForReferencing referencedObject;
	private MockNakedObject associate;
    private MockNakedObjectSpecificationLoader loader;
    private DummyNakedObjectSpecification spec;
    private NakedObjectsClient nakedObjects;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(InternalOneToOneAssociationTest.class));
    }

    protected void setUp() throws Exception {
        super.setUp();

        Logger.getRootLogger().setLevel(Level.OFF);
        loader = new MockNakedObjectSpecificationLoader();

        spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        nakedObjects = new NakedObjectsClient();
        nakedObjects.setSpecificationLoader(loader);

        objectWithOneToOneAssoications = new InternalObjectWithOneToOneAssociations();
        nakedObject = new MockNakedObject();
        nakedObject.setupObject(objectWithOneToOneAssoications);

        Class cls = InternalObjectWithOneToOneAssociations.class;
        Method get = cls.getDeclaredMethod("getReferencedObject", new Class[0]);
        Method set = cls.getDeclaredMethod("setReferencedObject", new Class[] { InternalObjectForReferencing.class });
        Method about = cls.getDeclaredMethod("aboutReferencedObject", new Class[] { InternalAbout.class,
                InternalObjectForReferencing.class });

        personField = new InternalOneToOneAssociation(PERSON_FIELD_NAME, InternalObjectForReferencing.class, get, set, null,
                null, about);

        referencedObject = new InternalObjectForReferencing();
        associate = new MockNakedObject();
        associate.setupObject(referencedObject);
    }

    public void testType() {
      //  DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
    	assertEquals(spec, personField.getType());
    }
    	
    public void testSet() {
        loader.addSpec(spec);
       
     	personField.setAssociation(new DummyIdentifier(), nakedObject, associate);
     	
     	assertEquals(associate.getObject(), objectWithOneToOneAssoications.getReferencedObject());
    }     	
    
    public void testRemove() {
        loader.addSpec(spec);
        
    	objectWithOneToOneAssoications.setReferencedObject(referencedObject);
    	assertNotNull(objectWithOneToOneAssoications.getReferencedObject());
    	
    	personField.clearAssociation(new DummyIdentifier(), nakedObject, associate);
    	
    	assertNull(objectWithOneToOneAssoications.getReferencedObject());
    }     	
    
    public void testGet() {
        MockDummyObjectLoader objectLoader = new MockDummyObjectLoader();
        DummyNakedObject pojoFromLoader = new DummyNakedObject();
        objectLoader.setupAdapter(pojoFromLoader);
        nakedObjects.setObjectLoader(objectLoader);
        
    	objectWithOneToOneAssoications.setReferencedObject(referencedObject);
    	
    	Naked association = personField.getAssociation(new DummyIdentifier(), nakedObject);
        assertEquals(pojoFromLoader, association);
    }     	
    
    public void testInitGet() {
    	personField.initValue(new DummyIdentifier(), nakedObject, referencedObject);
    
    	assertEquals(associate.getObject(), objectWithOneToOneAssoications.getReferencedObject());
    }
    
    public void testName() {
    	assertEquals(PERSON_FIELD_NAME, personField.getName());
    }
     
    public void testAboutAssignment() {
    	assertTrue(personField.hasHint());

    	assertNotNull(personField.getHint(null, nakedObject, associate));
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
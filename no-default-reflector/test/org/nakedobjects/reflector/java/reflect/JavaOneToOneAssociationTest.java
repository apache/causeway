package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObjectContext;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.security.Session;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaOneToOneAssociationTest extends TestCase {
    private static final String PERSON_FIELD_NAME = "person";
	private JavaObjectWithOneToOneAssociations objectWithOneToOneAssoications;
	private JavaOneToOneAssociation personField;
	private JavaReferencedObject referencedObject;
    private MockNakedObjectSpecificationLoader loader;
    private PojoAdapter nakedObject;
    private PojoAdapter associatedNakedObject;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToOneAssociationTest.class));
    }

    protected void setUp()  throws Exception {
    	super.setUp();

    	
    	Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
    	loader.addSpec(new DummyNakedObjectSpecification());  // for String
        loader.addSpec(new DummyNakedObjectSpecification()); // for Date
        loader.addSpec(new DummyNakedObjectSpecification()); // for float
            	
    	ConfigurationFactory.setConfiguration(new TestConfiguration());
    	
        objectWithOneToOneAssoications = new JavaObjectWithOneToOneAssociations();
        nakedObject = PojoAdapter.createAdapter(objectWithOneToOneAssoications);
        
        Class cls = JavaObjectWithOneToOneAssociations.class;
        Method get = cls.getDeclaredMethod("getReferencedObject", new Class[0]);
        Method set = cls.getDeclaredMethod("setReferencedObject", new Class[] {JavaReferencedObject.class});
        Method about = cls.getDeclaredMethod("aboutReferencedObject", new Class[] {FieldAbout.class, JavaReferencedObject.class});
        
        personField = new JavaOneToOneAssociation(PERSON_FIELD_NAME, JavaReferencedObject.class, get, set, null, null, about);
        
        referencedObject = new JavaReferencedObject();
        associatedNakedObject = PojoAdapter.createAdapter(referencedObject);
    }

    public void testType() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
    	assertEquals(spec, personField.getType());
    }
    	
    public void testSet() {
        loader.addSpec(new DummyNakedObjectSpecification());  //  for String
        loader.addSpec(new DummyNakedObjectSpecification()); // for Object
        
        NakedObjectContext context = new MockNakedObjectContext(MockObjectManager.setup());
        nakedObject.setContext(context);
       
        assertNull(objectWithOneToOneAssoications.getReferencedObject());
     	personField.setAssociation(nakedObject, associatedNakedObject);
     	assertEquals(referencedObject, objectWithOneToOneAssoications.getReferencedObject());
    }     	
    
    public void testRemove() {
        loader.addSpec(new DummyNakedObjectSpecification());  //  for String
        loader.addSpec(new DummyNakedObjectSpecification()); // for Object
             
    	objectWithOneToOneAssoications.setReferencedObject(referencedObject);
    	
    	assertNotNull(objectWithOneToOneAssoications.getReferencedObject());
    	personField.clearAssociation(nakedObject, associatedNakedObject);
    	assertNull(objectWithOneToOneAssoications.getReferencedObject());
    }     	
    
    public void testGet() {
        assertNull(objectWithOneToOneAssoications.getReferencedObject());
    	objectWithOneToOneAssoications.setReferencedObject(referencedObject);    	
    	assertEquals(referencedObject, personField.getAssociation(nakedObject).getObject());
    }     	
    
    public void testInitGet() {
      loader.addSpec(new DummyNakedObjectSpecification());  //  for Object
       loader.addSpec(new DummyNakedObjectSpecification()); // for Object
      
        assertNull(objectWithOneToOneAssoications.getReferencedObject());
    	personField.initAssociation(nakedObject, associatedNakedObject);
    	assertEquals(referencedObject, objectWithOneToOneAssoications.getReferencedObject());
    }
    
    public void testName() {
    	assertEquals(PERSON_FIELD_NAME, personField.getName());
    }
     
    public void testAboutAssignment() {
    	assertTrue(personField.hasHint());
    	assertNotNull(personField.getHint(new Session(), nakedObject, associatedNakedObject));
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
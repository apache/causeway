package org.nakedobjects.reflector.java.reflect;



import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.object.NakedObject;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.TestSystem;


public class JavaOneToOneAssociationTest extends TestCase {
    private static final String PERSON_FIELD_NAME = "person";
	private JavaObjectWithOneToOneAssociations objectWithOneToOneAssoications;
	private JavaOneToOneAssociation personField;
	private JavaReferencedObject referencedObject;
    private TestSystem system;
    private NakedObject nakedObject;
    private NakedObject associatedNakedObject;
    private DummyNakedObjectSpecification spec;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToOneAssociationTest.class));
    }

    protected void setUp()  throws Exception {
    	super.setUp();
    	Logger.getRootLogger().setLevel(Level.OFF);

        system = new TestSystem();
        system.init();
    	system.addSpecification(new DummyNakedObjectSpecification());  // for String
        system.addSpecification(new DummyNakedObjectSpecification()); // for Date
        system.addSpecification(new DummyNakedObjectSpecification()); // for float
    	
        system.addSpecification(new DummyNakedObjectSpecification(JavaObjectWithOneToOneAssociations.class.getName()));
        spec = new DummyNakedObjectSpecification(JavaReferencedObject.class.getName());
        system.addSpecification(spec);

        objectWithOneToOneAssoications = new JavaObjectWithOneToOneAssociations();

        nakedObject = system.createAdapterForTransient(objectWithOneToOneAssoications);
        
        Class cls = JavaObjectWithOneToOneAssociations.class;
        Method get = cls.getDeclaredMethod("getReferencedObject", new Class[0]);
        Method set = cls.getDeclaredMethod("setReferencedObject", new Class[] {JavaReferencedObject.class});
        Method about = cls.getDeclaredMethod("aboutReferencedObject", new Class[] {FieldAbout.class, JavaReferencedObject.class});
        
        personField = new JavaOneToOneAssociation(true, PERSON_FIELD_NAME, JavaReferencedObject.class, get, set, null, null, about, false);
        
        referencedObject = new JavaReferencedObject();
        associatedNakedObject = system.createAdapterForTransient(referencedObject);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testType() {
    	assertEquals(spec, personField.getType());
    }
    	
    public void testSet() {
        system.addSpecification(new DummyNakedObjectSpecification());  //  for String
        system.addSpecification(new DummyNakedObjectSpecification()); // for Object
        
        assertNull(objectWithOneToOneAssoications.getReferencedObject());
     	personField.setAssociation(nakedObject, associatedNakedObject);
     	assertEquals(referencedObject, objectWithOneToOneAssoications.getReferencedObject());
    }     	
    
    public void testRemove() {
        system.addSpecification(new DummyNakedObjectSpecification());  //  for String
        system.addSpecification(new DummyNakedObjectSpecification()); // for Object
             
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
        system.addSpecification(new DummyNakedObjectSpecification());  //  for Object
        system.addSpecification(new DummyNakedObjectSpecification()); // for Object
      
        assertNull(objectWithOneToOneAssoications.getReferencedObject());
    	personField.initAssociation(nakedObject, associatedNakedObject);
    	assertEquals(referencedObject, objectWithOneToOneAssoications.getReferencedObject());
    }
    
    public void testName() {
    	assertEquals(PERSON_FIELD_NAME, personField.getIdentifier());
    }
     
    public void testAboutAssignment() {
    	assertTrue(personField.hasHint());
    	assertNotNull(personField.getHint(null, nakedObject, associatedNakedObject));
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */
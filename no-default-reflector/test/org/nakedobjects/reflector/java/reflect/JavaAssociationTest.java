package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.ObjectLoaderImpl;
import org.nakedobjects.object.defaults.PojoAdapterHashImpl;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.reflect.internal.DummyIdentifier;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaAssociationTest extends TestCase {
    private static final String PERSON_FIELD_NAME = "person";
	private JavaObjectWithOneToOneAssociations javaObjectWithOneToOneAssociations;
	private NakedObject nakedObjectHoldingObjectWithAssociations;
	private JavaOneToOneAssociation personField;
	private JavaReferencedObject javaObjectForReferencing;
	private NakedObject associate;
    private MockNakedObjectSpecificationLoader loader;
    private DummyNakedObjectSpecification spec;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaAssociationTest.class));
    }

    protected void setUp()  throws Exception {
    	Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
    	loader.addSpec(spec = new DummyNakedObjectSpecification()); // for String
    	loader.addSpec(new DummyNakedObjectSpecification()); // for Date	
    	loader.addSpec(new DummyNakedObjectSpecification()); // for float
    	NakedObjectsClient nakedObjectsClient = new NakedObjectsClient();
        nakedObjectsClient.setSpecificationLoader(loader);
        nakedObjectsClient.setConfiguration(new TestConfiguration());
        
        javaObjectWithOneToOneAssociations = new JavaObjectWithOneToOneAssociations();
    	ObjectLoaderImpl objectLoader = new ObjectLoaderImpl(){
            public NakedObject recreateAdapter(Oid oid, NakedObjectSpecification spec) {
                return null;
            }};
       objectLoader.setPojoAdapterHash(new PojoAdapterHashImpl());
       objectLoader.setReflectorFactory(new NullReflectorFactory());
       nakedObjectsClient.setObjectLoader(objectLoader);

        nakedObjectHoldingObjectWithAssociations = objectLoader.createAdapterForTransient(javaObjectWithOneToOneAssociations);        
        
        Class cls = JavaObjectWithOneToOneAssociations.class;
        Method get = cls.getDeclaredMethod("getReferencedObject", new Class[0]);
        Method set = cls.getDeclaredMethod("setReferencedObject", new Class[] {JavaReferencedObject.class});
        Method about = cls.getDeclaredMethod("aboutReferencedObject", new Class[] {FieldAbout.class, JavaReferencedObject.class});
        
        personField = new JavaOneToOneAssociation(PERSON_FIELD_NAME, JavaReferencedObject.class, get, set, null, null, about);
        
        javaObjectForReferencing = new JavaReferencedObject();
        associate = objectLoader.createAdapterForTransient(javaObjectForReferencing);
    }

    

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testType() {
    	assertEquals(spec, personField.getType());
    }
    	
    public void testSet() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for one-to-one
        loader.addSpec(new DummyNakedObjectSpecification()); // for object
        
     	assertNull(javaObjectWithOneToOneAssociations.getReferencedObject());
     	personField.setAssociation(new DummyIdentifier(), nakedObjectHoldingObjectWithAssociations, associate);
     	assertEquals(javaObjectForReferencing, javaObjectWithOneToOneAssociations.getReferencedObject());
    }     	
    
    public void testRemove() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for one-to-one
        loader.addSpec(new DummyNakedObjectSpecification()); // for object
        
        javaObjectWithOneToOneAssociations.setReferencedObject(javaObjectForReferencing);
    	
    	assertNotNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    	personField.clearAssociation(new DummyIdentifier(), nakedObjectHoldingObjectWithAssociations, associate);
    	assertNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    }     	
    
    public void testGet() {
       	assertNull(personField.getAssociation(new DummyIdentifier(), nakedObjectHoldingObjectWithAssociations));
       	javaObjectWithOneToOneAssociations.setReferencedObject(javaObjectForReferencing);
    	assertEquals(associate, personField.getAssociation(new DummyIdentifier(), nakedObjectHoldingObjectWithAssociations));
    }     	
    
    public void testInitGet() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for object
        loader.addSpec(new DummyNakedObjectSpecification()); // for object

        assertNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    	personField.initAssociation(new DummyIdentifier(), nakedObjectHoldingObjectWithAssociations, associate);
    	assertEquals(javaObjectForReferencing, javaObjectWithOneToOneAssociations.getReferencedObject());
    }
    
    public void testName() {
    	assertEquals(PERSON_FIELD_NAME, personField.getName());
    }
     
    public void testAboutAssignment() {
    	assertTrue(personField.hasHint());
    	assertNotNull(personField.getHint(null, nakedObjectHoldingObjectWithAssociations, associate));
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
package org.nakedobjects.reflector.java.reflect;


import org.nakedobjects.NakedObjects;
import org.nakedobjects.application.control.FieldAbout;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.PojoAdapter;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
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
    	NakedObjects.setSpecificationLoader(loader);

    	ConfigurationFactory.setConfiguration(new TestConfiguration());
        
        javaObjectWithOneToOneAssociations = new JavaObjectWithOneToOneAssociations();
    	PojoAdapter.setPojoAdapterHash(new PojoAdapterHashImpl());
    	PojoAdapter.setReflectorFactory(new NullReflectorFactory());
        nakedObjectHoldingObjectWithAssociations = PojoAdapter.createNOAdapter(javaObjectWithOneToOneAssociations);        
        
        Class cls = JavaObjectWithOneToOneAssociations.class;
        Method get = cls.getDeclaredMethod("getReferencedObject", new Class[0]);
        Method set = cls.getDeclaredMethod("setReferencedObject", new Class[] {JavaReferencedObject.class});
        Method about = cls.getDeclaredMethod("aboutReferencedObject", new Class[] {FieldAbout.class, JavaReferencedObject.class});
        
        personField = new JavaOneToOneAssociation(PERSON_FIELD_NAME, JavaReferencedObject.class, get, set, null, null, about);
        
        javaObjectForReferencing = new JavaReferencedObject();
        associate = PojoAdapter.createNOAdapter(javaObjectForReferencing);
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
     	personField.setAssociation(nakedObjectHoldingObjectWithAssociations, associate);
     	assertEquals(javaObjectForReferencing, javaObjectWithOneToOneAssociations.getReferencedObject());
    }     	
    
    public void testRemove() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for one-to-one
        loader.addSpec(new DummyNakedObjectSpecification()); // for object
        
        javaObjectWithOneToOneAssociations.setReferencedObject(javaObjectForReferencing);
    	
    	assertNotNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    	personField.clearAssociation(nakedObjectHoldingObjectWithAssociations, associate);
    	assertNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    }     	
    
    public void testGet() {
       	assertNull(personField.getAssociation(nakedObjectHoldingObjectWithAssociations));
       	javaObjectWithOneToOneAssociations.setReferencedObject(javaObjectForReferencing);
    	assertEquals(associate, personField.getAssociation(nakedObjectHoldingObjectWithAssociations));
    }     	
    
    public void testInitGet() {
        loader.addSpec(new DummyNakedObjectSpecification()); // for object
        loader.addSpec(new DummyNakedObjectSpecification()); // for object

        assertNull(javaObjectWithOneToOneAssociations.getReferencedObject());
    	personField.initAssociation(nakedObjectHoldingObjectWithAssociations, associate);
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
package org.nakedobjects.object.reflect.defaults;


import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObjectContext;
import org.nakedobjects.object.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.object.system.TestClock;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaOneToOneAssociationTest extends TestCase {
    private static final String PERSON_FIELD_NAME = "person";
	private MockRole object;
	private JavaOneToOneAssociation personField;
	private MockPerson associate;
    private Session session;
    private MockNakedObjectSpecificationLoader loader;
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaOneToOneAssociationTest.class));
    }

    protected void setUp()  throws Exception {
    	super.setUp();

    	
    	Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
		new TestClock();
        
		NakedObjectContext context = null;
		
    	session = new Session();
        object = new MockRole();
        object.setContext(context);
        
        Class cls = MockRole.class;
        Method get = cls.getDeclaredMethod("getPerson", new Class[0]);
        Method set = cls.getDeclaredMethod("setPerson", new Class[] {MockPerson.class});
        Method about = cls.getDeclaredMethod("aboutPerson", new Class[] {FieldAbout.class, MockPerson.class});
        
        personField = new JavaOneToOneAssociation(PERSON_FIELD_NAME, MockPerson.class, get, set, null, null, about);
        
        associate = new MockPerson();
    }

    public void testType() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.setupSpecification(spec);
    	assertEquals(spec, personField.getType());
    }
    	
    public void testSet() {
        NakedObjectContext context = new MockNakedObjectContext(new MockObjectManager());
        object.setContext(context);
       
     	personField.setAssociation(object, associate);
     	
     	assertEquals(associate, object.getPerson());
    }     	
    
    public void testRemove() {
    	object.setPerson(associate);
    	
    	personField.clearAssociation(object, associate);
    	
    	assertNull(object.getPerson());
    }     	
    
    public void testGet() {
    	object.setPerson(associate);
    	
    	assertEquals(associate, personField.get(object));
    }     	
    
    public void testInitGet() {
    	personField.initData(object, associate);
    
    	assertEquals(associate, object.getPerson());
    }
    
    public void testName() {
    	assertEquals(PERSON_FIELD_NAME, personField.getName());
    }
     
    public void testAboutAssignment() {
    	assertTrue(personField.hasAbout());

    	assertNotNull(personField.getAbout(session, object, associate));
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
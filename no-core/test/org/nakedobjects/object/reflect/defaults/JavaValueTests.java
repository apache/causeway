package org.nakedobjects.object.reflect.defaults;


import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.MockNakedObjectContext;
import org.nakedobjects.object.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectDefinitionException;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.control.FieldAbout;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.object.system.TestClock;

import java.lang.reflect.Method;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaValueTests extends NakedObjectTestCase {
    private static final String FIELD_NAME = "value";
    
    private JavaValueField field;
    private MockValueTestObject object;
    private Session session;

    private MockNakedObjectSpecificationLoader loader;
    
    public JavaValueTests(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaValueTests.class));
    }

    protected void setUp()  throws Exception {
       	Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
		new TestClock();
        
        object = new MockValueTestObject();
        
        Class cls = MockValueTestObject.class;
        Method get = cls.getDeclaredMethod("getValue", new Class[0]);
        Method about = cls.getDeclaredMethod("aboutValue", new Class[] {FieldAbout.class});

        field = new JavaValueField(FIELD_NAME, MockValue.class, get, about, null, false);
     }
    
    protected void tearDown() throws Exception {
         super.tearDown();
    }

    public void testType() {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.setupSpecification(spec);
    	assertEquals(spec, field.getType());
    }
    	
    public void testGetValue() {
        try {
            field.get(object);
            fail("Expection expected when null value");
        } catch (NakedObjectDefinitionException expected) {
        }
        
        object.mockValue = new MockValue();
        Naked value = field.get(object);
        assertEquals(object.mockValue, value);
    }
    
    public void testParse() throws Exception {
        MockValue value = new MockValue();
        field.parseValue(value, "test");
        assertEquals("test", value.parseValue);
    }
    
    public void testSave() throws InvalidEntryException {
        MockValue value = new MockValue();
        object.mockValue = value;
        
        NakedObjectContext context = new MockNakedObjectContext(new MockObjectManager());
        object.setContext(context);
        
     	field.saveValue(object, "Fred");
     	
     	assertEquals("Fred", value.saveValue);
    }     	
    
    public void testRestore() {
        MockValue value = new MockValue();
        object.mockValue = value;

       field.restoreValue(object, "xxxxx");
    }
    
      
    public void testName() {
         assertEquals(FIELD_NAME, field.getName());
    }
  
    public void testAbout() {
    	assertTrue(field.hasAbout());
    	field.getAbout(session, object);
    	assertNotNull(object.about);
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


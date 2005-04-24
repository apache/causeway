package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.FieldPeer;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class JavaReflectorTest extends TestCase {

    private MockJavaReflector reflector;
    private MockObjectFactory objectFactory;
    private MockNakedObjectSpecificationLoader loader;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorTest.class));
    }
 
    protected void setUp() throws ClassNotFoundException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
       	ConfigurationFactory.setConfiguration(new TestConfiguration());

    	loader = new MockNakedObjectSpecificationLoader();     	
    	loader.addSpec(new DummyNakedObjectSpecification());
    	
    	PojoAdapterFactoryImpl pojoAdapterFactory = new PojoAdapterFactoryImpl();
    	pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
    	pojoAdapterFactory.setReflectorFactory(new NullReflectorFactory());
		NakedObjects.setPojoAdapterFactory(pojoAdapterFactory);
		
    	objectFactory = new MockObjectFactory();
        reflector = new MockJavaReflector(JavaObjectForReflector.class.getName(), objectFactory);
    }

    public void testObjectActions() throws NakedObjectSpecificationException {
         ActionPeer[] actions = reflector.actions(false);
         assertEquals(1, actions.length);
         
         MockAction member = (MockAction) actions[0];
         assertEquals("MethodOne", member.getName());
         assertEquals("actionMethodOne", member.getMethod().getName());
         assertEquals("aboutActionMethodOne", member.getAboutMethod().getName());
     }
    
    public void testFieldSortOrder() throws NakedObjectSpecificationException {
        String[] fields = reflector.fieldSortOrder();
        assertEquals(3, fields.length);
        assertEquals("one", fields[0]);
        assertEquals("two", fields[1]);
        assertEquals("three", fields[2]);
        
    }
    
    public void testActionSortOrder() throws NakedObjectSpecificationException {
        String[] names = reflector.actionSortOrder();
        assertEquals(2, names.length);
        assertEquals("start", names[0]);
        assertEquals("stop", names[1]);
    }
    
    public void testClassActionSortOrder() throws NakedObjectSpecificationException {
        String[] names = reflector.classActionSortOrder();
        assertEquals(2, names.length);
        assertEquals("top", names[0]);
        assertEquals("bottom", names[1]);
    }
    

    public void testAcquire() {
        DummyObject newInstance = new DummyObject();
        objectFactory.setupNewInstance(newInstance);
        Naked acquiredInstance = reflector.acquireInstance();
        assertNotNull(acquiredInstance);
        assertEquals(newInstance, acquiredInstance.getObject());
    }
    
   public void testShortName() {
        assertEquals("JavaObjectForReflector", reflector.shortName());
    }

    public void testPluralName() {
        assertEquals("Plural", reflector.pluralName());
    }

    public void testClassAbout() {
        Hint about = reflector.classHint();
        assertEquals(about, JavaObjectForReflector.about);
    }


    public void testSingularName() {
        assertEquals("Singular", reflector.singularName());
    }

    public void testFields() throws Exception {
        loader.addSpec(new DummyNakedObjectSpecification()); // for Date
        loader.addSpec(new DummyNakedObjectSpecification()); // for float
        
        FieldPeer[] fields = reflector.fields();
        
        assertEquals(1, fields.length);
        
       JavaField member = (JavaField) fields[0];
        assertEquals("Value", member.getName());
        assertEquals("aboutValue", member.getAboutMethod().getName());
    }

    public void testNameManipulations() {
        assertEquals("CarRegistration", JavaReflector.javaBaseName("getCarRegistration"));
        assertEquals("Driver", JavaReflector.javaBaseName("Driver"));
        assertEquals("Register", JavaReflector.javaBaseName("actionRegister"));
        assertEquals("", JavaReflector.javaBaseName("action"));
    }
    
    public void testSuperclass() {
        assertEquals(Object.class.getName(), reflector.getSuperclass());
    }
    
    
    public void testInterfaces() {
        String[] interfaces = reflector.getInterfaces();
        assertEquals(2, interfaces.length);
        assertEquals(Interface1.class.getName(), interfaces[0]);
        assertEquals(Interface2.class.getName(), interfaces[1]);
    }
    
    public void testLookup() {
        assertFalse(reflector.isLookup());
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

package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class JavaActionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JavaActionTest.class);
    }

    private JavaAction javaAction;
    private NakedObject nakedObject;
    private JavaActionTestObject javaObject;
    private MockNakedObjectSpecificationLoader loader;

    protected void setUp() throws Exception {
        super.setUp();
        
        ConfigurationFactory.setConfiguration(new TestConfiguration());
        
        new NakedObjectsClient(); 
        
       	Logger.getRootLogger().setLevel(Level.OFF);
    	loader = new MockNakedObjectSpecificationLoader();
        
		javaObject = new JavaActionTestObject();
    	PojoAdapterFactoryImpl pojoAdapterFactory = new PojoAdapterFactoryImpl();
		pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
		pojoAdapterFactory.setReflectorFactory(new NullReflectorFactory());
		nakedObject = pojoAdapterFactory.createNOAdapter(javaObject);

        Class cls = Class.forName(getClass().getName() + "Object");
        Method action = cls.getDeclaredMethod("actionMethod", new Class[0]);
        Method about = cls.getDeclaredMethod("aboutMethod", new Class[] { ActionAbout.class });
        javaAction = new JavaAction("methodName", Action.EXPLORATION, action, about);
        assertNotNull(javaAction);
    }

    public void testAbout() {
        Hint about = javaAction.getHint(null, nakedObject, new Naked[0]);
        assertNotNull(about);
        assertEquals("about for test", about.getName());
    }

    public void testAction() throws Exception {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        loader.addSpec(spec);
        
        javaAction.execute(null, nakedObject, new Naked[0]);

  //      manager.assertAction(0, "start transaction");
   //     manager.assertAction(1, "end transaction");
    }

    public void testHasAbout() {
        assertTrue(javaAction.hasHint());
    }

    public void testMethodName() throws Exception {
        assertEquals("methodName", javaAction.getName());
    }

    public void testReturnType() {
        assertNull(javaAction.returnType());
    }

    public void testType() {
        assertEquals(Action.EXPLORATION, javaAction.getType());
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */
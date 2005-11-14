package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.application.control.ActionAbout;
import org.nakedobjects.object.Action;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.reflect.MemberIdentifierImpl;

import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.TestSystem;


public class JavaActionTest extends TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(JavaActionTest.class);
    }

    private TestSystem system;
    private JavaAction javaAction;
    private NakedObject nakedObject;
    private JavaActionTestObject javaObject;
 
    protected void setUp() throws Exception {
        super.setUp();
       	Logger.getRootLogger().setLevel(Level.OFF);
       	
        system = new TestSystem();
        system.init();

        system.addSpecification(new DummyNakedObjectSpecification());

		javaObject = new JavaActionTestObject();
		nakedObject = NakedObjects.getObjectLoader().createAdapterForTransient(javaObject);
		 		
        Class cls = Class.forName(getClass().getName() + "Object");
        Method action = cls.getDeclaredMethod("actionMethod", new Class[0]);
        Method about = cls.getDeclaredMethod("aboutMethod", new Class[] { ActionAbout.class });
        javaAction = new JavaAction(new MemberIdentifierImpl("cls", "methodName", null), Action.EXPLORATION, new NakedObjectSpecification[0], Action.LOCAL, action, about);
        assertNotNull(javaAction);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testAction() throws Exception {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        system.addSpecification(spec);
        
        javaAction.execute(nakedObject, new Naked[0]);

  //      manager.assertAction(0, "start transaction");
   //     manager.assertAction(1, "end transaction");
    }

    public void testMethodName() throws Exception {
        assertEquals(new MemberIdentifierImpl("cls", "methodName"), javaAction.getIdentifier());
    }

    public void testReturnType() {
        assertNull(javaAction.getReturnType());
    }

    public void testType() {
        assertEquals(Action.EXPLORATION, javaAction.getType());
    }
    
    public void testTarget() {
        assertEquals(Action.LOCAL, javaAction.getTarget());
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
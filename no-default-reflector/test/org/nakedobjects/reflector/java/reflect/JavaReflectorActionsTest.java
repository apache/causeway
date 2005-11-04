package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObjectSpecificationException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.TestSystem;


public class JavaReflectorActionsTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorActionsTest.class));
    }

    private Action[] actions;
    private JavaIntrospector reflector;
    private TestSystem system;

    protected void setUp() throws ClassNotFoundException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        system = new TestSystem();
        system.init();
        system.addSpecification(new DummyNakedObjectSpecification());

        reflector = new JavaIntrospector(BusinessObjectWithActions.class, new DummyBuilder());
        reflector.introspect();
        actions = reflector.getObjectActions();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testAbout() {
        Action action = actions[3];
        assertEquals("WithAbout", action.getName());
        
        assertTrue(action.hasHint());
    }

    public void testNumberOfActions() throws Exception {
        assertEquals(5, actions.length);
    }

    public void testObjectDefaultActionsWithOneParameter() throws Exception {
        Action action = actions[4];
        assertEquals("Two", action.getName());
        assertFalse(action.hasHint());
        assertEquals(Action.DEFAULT, action.getActionTarget());
        assertEquals(Action.DEBUG, action.getActionType());
        assertEquals(0, action.getParameterCount());
        //   assertEquals(String.class.getName(),
        // action.returnType().getFullName());
    }

    public void testObjectDefaultActionsWithZeroParameters() throws Exception {
        Action action = actions[1];
        assertEquals("One", action.getName());
        assertFalse(action.hasHint());
        assertEquals(Action.DEFAULT, action.getActionTarget());
        assertEquals(Action.USER, action.getActionType());
        assertEquals(null, action.getReturnType());
        assertEquals(0, action.getParameterCount());
    }

    public void testObjectLocalAction() throws NakedObjectSpecificationException {
        Action action = actions[0];
        assertEquals("RunOnClient", action.getName());
        assertEquals(Action.LOCAL, action.getActionTarget());
    }

    public void testObjectRemoteAction() throws NakedObjectSpecificationException {
        Action action = actions[2];
        assertEquals("RunOnServer", action.getName());
        assertEquals(Action.REMOTE, action.getActionTarget());
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

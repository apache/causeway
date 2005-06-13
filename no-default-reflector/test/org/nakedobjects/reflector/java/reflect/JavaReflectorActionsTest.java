package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.container.configuration.ConfigurationFactory;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.ActionPeer;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;
import org.nakedobjects.reflector.java.JavaObjectFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class JavaReflectorActionsTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorActionsTest.class));
    }

    private ActionPeer[] actions;
    private MockNakedObjectSpecificationLoader loader;
    private JavaObjectFactory objectFactory;
    private JavaReflector reflector;

    protected void setUp() throws ClassNotFoundException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        NakedObjectsClient nakedObjects = new NakedObjectsClient();

        nakedObjects.setConfiguration(new TestConfiguration());

        loader = new MockNakedObjectSpecificationLoader();
        loader.addSpec(new DummyNakedObjectSpecification());

        PojoAdapterFactoryImpl pojoAdapterFactory = new PojoAdapterFactoryImpl();
        pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
        pojoAdapterFactory.setReflectorFactory(new NullReflectorFactory());
        nakedObjects.setPojoAdapterFactory(pojoAdapterFactory);

        objectFactory = new JavaObjectFactory();
        reflector = new JavaReflector(BusinessObjectWithActions.class.getName(), objectFactory);
        actions = reflector.actionPeers(false);
    }

    public void testAbout() {
        ActionPeer action = actions[3];
        assertEquals("WithAbout", action.getName());
        
        assertTrue(action.hasHint());
    }

    public void testNumberOfActions() throws Exception {
        assertEquals(5, actions.length);
    }

    public void testObjectDefaultActionsWithOneParameter() throws Exception {
        ActionPeer action = actions[4];
        assertEquals("Two", action.getName());
        assertFalse(action.hasHint());
        assertEquals(Action.DEFAULT, action.getTarget());
        assertEquals(Action.DEBUG, action.getType());
        assertEquals(0, action.parameterTypes().length);
        //   assertEquals(String.class.getName(),
        // action.returnType().getFullName());
    }

    public void testObjectDefaultActionsWithZeroParameters() throws Exception {
        ActionPeer action = actions[1];
        assertEquals("One", action.getName());
        assertFalse(action.hasHint());
        assertEquals(Action.DEFAULT, action.getTarget());
        assertEquals(Action.USER, action.getType());
        assertEquals(null, action.returnType());
        assertEquals(0, action.parameterTypes().length);
    }

    public void testObjectLocalAction() throws NakedObjectSpecificationException {
        ActionPeer action = actions[0];
        assertEquals("RunOnClient", action.getName());
        assertEquals(Action.LOCAL, action.getTarget());
    }

    public void testObjectRemoteAction() throws NakedObjectSpecificationException {
        ActionPeer action = actions[2];
        assertEquals("RunOnServer", action.getName());
        assertEquals(Action.REMOTE, action.getTarget());
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

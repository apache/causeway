package org.nakedobjects.object.reflect;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.DummyNakedObjectSpecificationLoader;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.control.DefaultHint;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.persistence.ObjectStoreException;
import org.nakedobjects.object.security.Session;

import java.util.Vector;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class ActionTest extends NakedObjectTestCase {
    private static final String ACTION_LABEL = "Reduce Headcount";
    private static final String ACTION_NAME = "reduceheadcount";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ActionTest.class));
    }

    private Action action;

    private MockAction actionDelegate;
    private NakedObject nakedObject;

    public ActionTest(String name) {
        super(name);
    }

    protected void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        NakedObjects.setObjectManager(new MockObjectManager());
        NakedObjects.setSpecificationLoader(new DummyNakedObjectSpecificationLoader());
        
        actionDelegate = new MockAction();
        action = new Action("", ACTION_NAME, actionDelegate);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAbout() {
        assertFalse(action.hasHint());

        Hint about = action.getHint(new Session(), nakedObject, null);
        assertNull(actionDelegate.about);
        assertTrue(about instanceof DefaultHint);

        actionDelegate.hasAbout = true;
        assertTrue(action.hasHint());

        about = action.getHint(new Session(), nakedObject, null);
        assertEquals(actionDelegate.about, about);
    }

    public void testAction() {
        actionDelegate.returnObject = new DummyNakedObject();

        NakedObject nakedObject = new DummyNakedObject();
        Naked[] parameters = new Naked[2];
        Naked returnObject = action.execute(nakedObject, parameters);

        actionDelegate.assertAction(0, "execute " + nakedObject);
        actionDelegate.assertAction(1, "execute " + parameters);
        assertEquals(actionDelegate.returnObject, returnObject);
    }

    public void testHasReturn() {
        assertFalse(action.hasReturn());

        actionDelegate.returnType = new DummyNakedObjectSpecification();
        assertTrue(action.hasReturn());
    }

    public void testLabel() {
        assertEquals(ACTION_NAME, action.getLabel(new Session(), nakedObject));

        actionDelegate.label = ACTION_LABEL;
        assertEquals(ACTION_NAME, action.getLabel(new Session(), nakedObject));

        actionDelegate.hasAbout = true;
        assertEquals(ACTION_LABEL, action.getLabel(new Session(), nakedObject));
    }

    public void testName() {
        assertEquals(ACTION_NAME, action.getName());
    }

    public void testReturnType() {
        assertFalse(action.hasReturn());
        assertNull(action.getReturnType());

        actionDelegate.returnType = new DummyNakedObjectSpecification();
        assertTrue(action.hasReturn());
        assertEquals(actionDelegate.returnType, action.getReturnType());
    }

    public void testGetParameters() {
        NakedObject nakedObject = new DummyNakedObject();
        ActionParameterSet parameters = action.getParameters(null, nakedObject);

        String[] parameterLabels = parameters.getParameterLabels();
        assertEquals("one", parameterLabels[0]);
        assertEquals("two", parameterLabels[1]);
        assertEquals("three", parameterLabels[2]);

        Object[] defaultParameterValues = parameters.getDefaultParameterValues();
        assertEquals(new String(), defaultParameterValues[0]);
        assertEquals(new Integer(123), defaultParameterValues[1]);
        assertEquals(new Vector(), defaultParameterValues[2]);
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

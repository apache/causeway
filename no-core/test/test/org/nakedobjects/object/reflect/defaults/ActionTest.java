package test.org.nakedobjects.object.reflect.defaults;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.object.repository.NakedObjectsClient;

import java.util.Vector;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyNakedObjectSpecificationLoader;
import test.org.nakedobjects.object.NakedObjectTestCase;
import test.org.nakedobjects.object.defaults.MockObjectPersistor;
import test.org.nakedobjects.object.reflect.DummyActionPeer;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class ActionTest extends NakedObjectTestCase {
    private static final String ACTION_LABEL = "Headcount Down";
    private static final String ACTION_NAME = "reduceheadcount";

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ActionTest.class));
    }

    private Action action;

    private DummyActionPeer actionPeer;
    private NakedObject nakedObject;

    public ActionTest(String name) {
        super(name);
    }

    protected void setUp() throws ObjectPerstsistenceException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        NakedObjectsClient nakedObjects = new NakedObjectsClient();
        nakedObjects.setObjectPersistor(new MockObjectPersistor());
        nakedObjects.setSpecificationLoader(new DummyNakedObjectSpecificationLoader());
        
        actionPeer = new DummyActionPeer();
        actionPeer.setupName("reduceheadcount()");
        action = new ActionImpl("", ACTION_NAME, actionPeer);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAction() {
        DummyNakedObject expectedObject = new DummyNakedObject();
        actionPeer.setupReturnObject(expectedObject);

        NakedObject target = new DummyNakedObject();
        Naked[] parameters = new Naked[2];
        actionPeer.expect("execute #reduceheadcount() " + target);
        Naked returnObject = action.execute(target, parameters);

        actionPeer.verify();
        assertEquals(expectedObject, returnObject);
        
    }

    public void testHasReturn() {
        assertFalse(action.hasReturn());

        actionPeer.setupReturnType(new DummyNakedObjectSpecification());
        assertTrue(action.hasReturn());
    }

    public void testLabel() {
        assertEquals(ACTION_NAME, action.getLabel());
        
        actionPeer.setupLabel(ACTION_LABEL);
//        MockHint hint = new MockHint();
//        hint.setupName(ACTION_LABEL);
//        actionPeer.setupHint(hint);
        
        assertEquals(ACTION_LABEL, action.getLabel());
    }

    public void testName() {
        assertEquals(ACTION_NAME, action.getId());
    }

    public void testReturnType() {
        assertFalse(action.hasReturn());
        assertNull(action.getReturnType());

        DummyNakedObjectSpecification returnType = new DummyNakedObjectSpecification();
        actionPeer.setupReturnType(returnType);

        assertTrue(action.hasReturn());
        assertEquals(returnType, action.getReturnType());
    }

    public void testGetParameters() {
        NakedObject nakedObject = new DummyNakedObject();
        ActionParameterSet parameters = action.getParameters(nakedObject);

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

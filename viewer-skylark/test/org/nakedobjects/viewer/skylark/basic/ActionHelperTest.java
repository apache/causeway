package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.ActionParameterSet;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.viewer.skylark.ParameterContent;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.easymock.MockControl;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.TestSystem;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class ActionHelperTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionHelperTest.class);
    }

    private ActionHelper actionHelper;
    private NakedObject target;
    private DummyNakedObject adapter1;
    private TestSystem system;
    private DummyNakedObject adapter3;
    private DummyNakedObject adapter2;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        String pojo1 = "object#1";
        
        adapter1 = new DummyNakedObject();
        adapter2 = new DummyNakedObject();
        adapter3 = new DummyNakedObject();
        Naked[] values = new Naked[] { adapter1, null };
        
        system = new TestSystem();
        system.addAdapter(pojo1, adapter1);
        system.addAdapter("object#2", adapter2);
        system.addAdapter("object#3", adapter3);
        system.init();

        MockControl actionc = MockControl.createControl(Action.class);
        Action action = (Action) actionc.getMock();

        MockControl targetc = MockControl.createControl(NakedObject.class);
        targetc.setDefaultMatcher(MockControl.ARRAY_MATCHER);
        target = (NakedObject) targetc.getMock();
        targetc.expectAndReturn(target.execute(action, values), null);

        actionc.expectAndDefaultReturn(action.getParameterTypes(), new DummyNakedObjectSpecification[] {
                new DummyNakedObjectSpecification(), new DummyNakedObjectSpecification() });

        MockControl parameterSetc = MockControl.createNiceControl(ActionParameterSet.class);
        ActionParameterSet parameterSet = (ActionParameterSet) parameterSetc.getMock();
        parameterSetc.expectAndDefaultReturn(parameterSet.getParameterLabels(), new String[] {"one", "two"});
        parameterSetc.expectAndDefaultReturn(parameterSet.getDefaultParameterValues(), new Object[] {pojo1, null});
        parameterSetc.expectAndDefaultReturn(parameterSet.getOptions(), new Object[][] {new Object[] {pojo1, "object#2", "object#3"}, null});
        parameterSetc.expectAndDefaultReturn(parameterSet.getRequiredParameters(), new boolean[] {false, true});
                    
        
        targetc.expectAndDefaultReturn(target.getParameters(action), parameterSet);

        actionc.replay();
        targetc.replay();
        parameterSetc.replay();
        
        actionHelper = ActionHelper.createInstance(target, action);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }
    
    public void testInvokeAction() {
        actionHelper.invoke();
    }

    public void testNumberOfParametersInSet() {
        actionHelper.getParameter(0);
        actionHelper.getParameter(1);

        try {
            actionHelper.getParameter(2);
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {}

    }

    public void testNumberOfParametersCreated() {
        ParameterContent[] p = actionHelper.createParameters();
        assertEquals(2, p.length);
    }

    public void testIds() {
        ParameterContent[] p = actionHelper.createParameters();
        assertEquals("one", p[0].getParameterName());
        assertEquals("two", p[1].getParameterName());
    }

    public void testRequired() {
        ParameterContent[] p = actionHelper.createParameters();
        assertEquals(false, p[0].isRequired());
        assertEquals(true, p[1].isRequired());
    }
    
    public void testNames() {
        ParameterContent[] p = actionHelper.createParameters();
        assertEquals("one", p[0].getParameterName());
        assertEquals("two", p[1].getParameterName());
    }

    public void testDefaults() {
        assertEquals(adapter1, actionHelper.getParameter(0));
        assertEquals(null, actionHelper.getParameter(1));
    }

    public void testTarget() {
        assertEquals(target, actionHelper.getTarget());
    }
    
    public void testOptions() {
        ParameterContent[] p = actionHelper.createParameters();
        
        ObjectParameter c = (ObjectParameter) p[1];
        assertNull(c.getOptions());
        
        c = (ObjectParameter) p[0];
        assertNotNull(c.getOptions());
        
        assertEquals(adapter1, c.getOptions()[0]);
        assertEquals(adapter2, c.getOptions()[1]);
        assertEquals(adapter3, c.getOptions()[2]);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
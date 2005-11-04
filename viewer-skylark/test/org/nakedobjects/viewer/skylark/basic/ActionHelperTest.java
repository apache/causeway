package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.ActionImpl;
import org.nakedobjects.viewer.skylark.ParameterContent;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.reflect.DummyActionPeer;
import test.org.nakedobjects.object.reflect.DummyNakedObject;


public class ActionHelperTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionHelperTest.class);
    }

    private ActionHelper actionHelper;
    private DummyNakedObject target;
    private DummyNakedObject param1;
    private DummyNakedObject param2;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        DummyActionPeer mockActionPeer = new DummyActionPeer();
        mockActionPeer.setUpParamterTypes(new NakedObjectSpecification[] {});

        Action action = new ActionImpl("cls name", "method name", mockActionPeer);

        target = new DummyNakedObject();

        param1 = new DummyNakedObject();
        param2 = new DummyNakedObject();
        Naked[] values = new Naked[] { param1, param2 };
        NakedObjectSpecification[] types = new DummyNakedObjectSpecification[] { new DummyNakedObjectSpecification(),
                new DummyNakedObjectSpecification() };
        String[] labels = {"one", "two"};
        actionHelper = new ActionHelper(target, action, labels, values, types, new boolean[2]);
    }

    public void testInvokeAction() {
        actionHelper.invoke();
    }

    public void testNumberOfParameters() {
        actionHelper.getParameter(0);
        actionHelper.getParameter(1);

        try {
            actionHelper.getParameter(2);
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {}

    }

    public void testParam() {
        ParameterContent[] p = actionHelper.createParameters();
        assertEquals(2, p.length);
    }

    public void testParameters() {
        assertEquals(param1, actionHelper.getParameter(0));
        assertEquals(param2, actionHelper.getParameter(1));
    }

    public void testTarget() {
        assertEquals(target, actionHelper.getTarget());
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
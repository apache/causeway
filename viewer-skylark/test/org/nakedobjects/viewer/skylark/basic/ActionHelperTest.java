package org.nakedobjects.viewer.skylark.basic;

import org.nakedobjects.object.Naked;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.security.ClientSession;
import org.nakedobjects.object.security.Session;
import org.nakedobjects.viewer.skylark.ParameterContent;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class ActionHelperTest extends TestCase {
    private ActionHelper actionInvocation;
    private DummyNakedObject target;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(ActionHelperTest.class);
    }

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);

        ClientSession.setSession(new Session());

        MockActionPeer mockActionPeer = new MockActionPeer();
        mockActionPeer.setUpParamterTypes(new NakedObjectSpecification[] {});
        
  //      mockActionPeer.setUpParamterTypes(new NakedObjectSpecification[] { new DummyNakedObjectSpecification(),
  //              new DummyNakedObjectSpecification() });
        Action action = new Action("cls name", "method name", mockActionPeer);

        target = new DummyNakedObject();

        actionInvocation = new ActionHelper(target, action);
    }

    public void testTarget() {
        assertEquals(target, actionInvocation.getTarget());
    }


	public void testParam() {
	   ParameterContent[] p = actionInvocation.createParameters();
	   assertEquals(0, p.length);
    }
	
	public void testInvokeAction() {
	    actionInvocation.invoke();
    }
	
    public void xxxtestNumberOfParameters() {
        actionInvocation.getParameter(0);
        actionInvocation.getParameter(1);

        try {
            actionInvocation.getParameter(2);
            fail();
        } catch (ArrayIndexOutOfBoundsException expected) {}

    }

    public void xxtestParameters() {
        Naked param1 = actionInvocation.getParameter(0);
        Naked param2 = actionInvocation.getParameter(1);

        assertEquals(null, param1);
        assertEquals(null, param2);
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
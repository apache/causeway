package org.nakedobjects.object.reflect;


import org.nakedobjects.object.LocalReflectionFactory;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.control.About;
import org.nakedobjects.object.value.TestClock;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class ActionTest extends NakedObjectTestCase {
    private static final String DISOLVE_ACTION_NAME = "reduceheadcount";
    private static final String DISOLVE_ACTION_LABEL = "Reduce Headcount";
	private Team object;
	private ActionSpecification action;
    private MockObjectManager manager;
    
    public ActionTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ActionTest.class));
    }

    protected void setUp()  throws ObjectStoreException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);

    	manager = MockObjectManager.setup();
    	NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
    	new TestClock();
    	
        object = new Team();
        object.setContext(manager.getContext());
        NakedObjectSpecification c = object.getSpecification();
        
        action = (ActionSpecification) c.getObjectAction(ActionSpecification.USER, DISOLVE_ACTION_NAME);
    }
    
    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    public void testReturnType() {
    	assertTrue(action.hasReturn());
    	assertEquals(Person.class.getName(), action.getReturnType().getFullName());
    }
    	
    public void testName() {
    	assertEquals(DISOLVE_ACTION_NAME, action.getName());
    }
    
    public void testLabel() {
    	assertEquals(DISOLVE_ACTION_LABEL, action.getLabel(new NakedObjectContext(manager), object));
    }
    
    public void testAction() {
    	assertFalse(object.reduced);
    	
    	action.execute(object);
    	
    	manager.assertAction(0, "start transaction");
    	manager.assertAction(1, "end transaction");
    	
    	assertTrue(object.reduced);
    }
    
    public void testIsVisible() {
    	action.canAccess(new NakedObjectContext(manager), object);
    }
    
    public void test() {
    	action.hasReturn();
    }
    
    public void testAboutAssignment() {
    	assertTrue(action.hasAbout());

    	About about = action.getAbout(new NakedObjectContext(manager), object);
    	assertNotNull(about);
    }
}
/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

package org.nakedobjects.object.reflect;


import org.nakedobjects.object.InvalidEntryException;
import org.nakedobjects.object.LocalReflectionFactory;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectTestCase;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.value.Money;
import org.nakedobjects.object.value.TestClock;
import org.nakedobjects.object.value.TextString;

import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class ValueTests extends NakedObjectTestCase {
    private static final String SALARY_FIELD_LABEL = "Salary";
    private static final String SALARY_FIELD_NAME = "salary";
    private static final String NAME_FIELD_LABEL = "Name";
    private static final String NAME_FIELD_NAME = "name";
    private ValueTestObject object;
    
    private ValueFieldSpecification nameField, salaryField;
    private MockObjectManager manager;
    
    public ValueTests(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(ValueTests.class));
    }

    protected void setUp()  throws ObjectStoreException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);

    	manager = MockObjectManager.setup();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
    	new TestClock();
    	
        object = new ValueTestObject();
        object.setContext(manager.getContext());
        
        NakedObjectSpecification c = NakedObjectSpecification.getNakedClass(ValueTestObject.class.getName());
        
        nameField = (ValueFieldSpecification) c.getField(NAME_FIELD_NAME);
        salaryField = (ValueFieldSpecification) c.getField(SALARY_FIELD_NAME);
    }
    
    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    public void testType() {
    	assertEquals(TextString.class.getName(), nameField.getType().getFullName());
    	assertEquals(Money.class.getName(), salaryField.getType().getFullName());
    }
    	
    public void testSetGet() throws InvalidEntryException {
     	nameField.parseAndSave(object, "Fred");
     	salaryField.parseAndSave(object, "20.41");
     	
     	assertEquals("Fred", object.getName());
     	assertEquals(20.41, object.getSalary().doubleValue(), 0.001);
    }     	
    
    public void testSetInvalidValue() throws Exception {
        salaryField.parseAndSave(object, "12.0");
   	try{
	        salaryField.parseAndSave(object, "-1.0");
	        fail();
    	} catch(InvalidEntryException expected) {}
    	assertEquals(12.0, ((Money) salaryField.get(object)).doubleValue(), 0.0);
    }
    
    public void testInitGet() {
    	nameField.restoreValue(object, "Joe");
    	salaryField.restoreValue(object, "18.83");
    
    	assertEquals("Joe", object.getName());
    	assertEquals(18.83, object.getSalary().doubleValue(), 0.001);
    }
    
    public void testName() {
    	assertEquals(NAME_FIELD_NAME, nameField.getName());
    	assertEquals(SALARY_FIELD_NAME, salaryField.getName());
    }
    
    public void testLabel() {
    	NakedObjectContext context = new NakedObjectContext(manager);
    	assertEquals(NAME_FIELD_LABEL, nameField.getLabel(context, object));
    	assertEquals(SALARY_FIELD_LABEL, salaryField.getLabel(context, object));
    }
    
    public void testAbout() {
    	assertFalse(nameField.hasAbout());
    	assertTrue(salaryField.hasAbout());

    	assertNotNull(salaryField.getAbout(new NakedObjectContext(manager), object));
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


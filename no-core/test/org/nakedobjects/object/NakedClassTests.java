package org.nakedobjects.object;

import org.nakedobjects.object.control.ClassAbout;
import org.nakedobjects.object.reflect.Action;
import org.nakedobjects.object.reflect.Field;
import org.nakedobjects.object.value.TextString;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.mockobjects.ExpectationSet;

// TODO most of these tests are testing the JavaReflector; move them to its test
public class NakedClassTests extends TestCase {

    private MockReflector reflector;
    private NakedClass nakedClass;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(NakedClassTests.class));
    }

    public NakedClassTests(String name) {
        super(name);
    }

    private NakedClass nakedClass(Class cls) {
        return NakedClassManager.getInstance().getNakedClass(cls.getName());
    }

    private NakedClass reflect(Class clazz) {
        NakedClass c = nakedClass(clazz);
        assertEquals(clazz.getName(), c.fullName());
        return c;
    }

    protected void setUp() {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        MockObjectManager manager = MockObjectManager.setup();
        manager.setupAddClass(Role.class);
        manager.setupAddClass(GenericTestObject.class);
        manager.setupAddClass(ContactTestObject.class);
        manager.setupAddClass(ProductTestObject.class);
        manager.setupAddClass(ConcreteEmployee.class);
        manager.setupAddClass(AssociationExample.class);
        manager.setupAddClass(Employee.class);
        
        manager.setupAddClass(NakedObject.class);
        
        reflector = new MockReflector();
        nakedClass = new NakedClass();
        Field[] fields = new Field[] {
            new MockField("One"),
            new MockField("Two"),
            new MockField("Three")
        };
        nakedClass.init(reflector, NakedObject.class.getName(), fields, null, null);
    }

    public void testAcquireInstance() {
        NakedObject acquire = new Person();
        reflector.setupAcquireInstance(acquire);
        assertEquals(acquire, nakedClass.acquireInstance());
    }
    
    public void testClassAbout() {
        ClassAbout about = new ClassAbout("", true);
        reflector.setupClassAbout(about);
        assertEquals(about, nakedClass.getClassAbout());
       }

    public void testEquals() {
        MockReflector reflector2 = new MockReflector();
        NakedClass nakedClass2 = new NakedClass();
        nakedClass2.init(reflector, NakedObject.class.getName(), null, null, null);
        
        assertFalse(reflector.equals(reflector2));
    }
     
    public void testGetFieldsNew() {
        Field[] actions = nakedClass.getFields();

        assertEquals(3, actions.length); // 1 NakedObject

       ExpectationSet exp = new ExpectationSet("field names");

        exp.addExpected("One");
        exp.addExpected("Two");
        exp.addExpected("Three");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    // TODO the rest of the test need to use mocks - as the above three do
    public void testGetFields() {
        NakedClass c = nakedClass(ContactTestObject.class);
        Field[] actions = c.getFields();

        assertEquals(5, actions.length); // 1 NakedObject

        assertNotNull(actions[0]);
        ExpectationSet exp = new ExpectationSet("action names");

        exp.addExpected("Is Contact");
        exp.addExpected("Address");
        exp.addExpected("Favourite");
        exp.addExpected("Name");
        exp.addExpected("Worth");

        com.mockobjects.ExpectationList exp2 = new com.mockobjects.ExpectationList("ordered action names");

        exp2.addExpected("Name");
        exp2.addExpected("Address");
        exp2.addExpected("Worth");
        exp2.addExpected("Is Contact");
        exp2.addExpected("Favourite");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
            exp2.addActual(actions[i].getName());
        }
        exp.verify();
        exp2.verify();

    }
    
    public void testGetClassActionByParam() {
        NakedClass targetClass = nakedClass(GenericTestObject.class);
        NakedClass[] params = new NakedClass[] { nakedClass(ContactTestObject.class) };
        Action action = targetClass.getClassAction(Action.USER, params);
        assertNotNull(action);
        assertEquals("Accept Contact", action.getName());
        assertEquals(Action.USER, action.getType());
        assertEquals(null, action.returns());
        
        params = new NakedClass[] { nakedClass(ProductTestObject.class) };
        assertNull(targetClass.getClassAction(Action.USER, params));
    }

    public void testGetClassActions() {
        NakedClass c = nakedClass(ContactTestObject.class);
        Action[] actions = c.getClassActions(Action.USER);
        ExpectationSet exp = new ExpectationSet("action names");
        exp.addExpected("Class Op");
        exp.addExpected("Do Whatever");
        exp.addExpected("Example");

        for (int i = 0; i < actions.length; i++) {
            assertNotNull(actions[i]);
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testGetExplorationActions() {
        NakedClass c = nakedClass(ContactTestObject.class);
        Action[] actions = c.getObjectActions(Action.EXPLORATION);
        ExpectationSet exp = new ExpectationSet("action names");
        exp.addExpected("Clone");
        exp.addExpected("Class");
        exp.addExpected("Make Persistent");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testGetObjectsActions() {
        NakedClass c = nakedClass(ContactTestObject.class);
        Action[] actions = c.getObjectActions(Action.USER);
        //assertEquals(6, actions.length);

        ExpectationSet exp = new ExpectationSet("action names");
        // 1 param actions
        exp.addExpected("Add Contact");
        exp.addExpected("Renew");

        // 0 param actions
        exp.addExpected("Create Invoice");
        exp.addExpected("Duplicate");
        exp.addExpected("Set Up");
        exp.addExpected("Reset Worth");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testHashCodes() {
        NakedClass c1 = nakedClass(Role.class);
        NakedClass c2 = nakedClass(AssociationExample.class);

        assertTrue(c1.hashCode() != c2.hashCode());

    }

    public void testMemberOrdering() {
        NakedClass c = nakedClass(GenericTestObject.class);
        com.mockobjects.ExpectationList exp2 = new com.mockobjects.ExpectationList("ordered names");

        exp2.addExpected("Customer");
        exp2.addExpected("Products");
        Field[] attributes = c.getFields();

        for (int i = 0; i < attributes.length; i++) {
            exp2.addActual(attributes[i].getName());
        }
        exp2.verify();
    }

    public void testNameAttribute() {
        NakedClass c = nakedClass(ContactTestObject.class);
        Field[] actions = c.getFields();

        for (int i = 0; i < actions.length; i++) {
            if (actions[i].getName().equals("Name")) {
                Field att = actions[i];

                assertEquals(true, att.isValue());
                assertEquals(TextString.class, att.getType());
                assertEquals("Name", att.getName());
                return;
            }
        }
        fail("Didn't find  Name attribute");
    }

    public void testNames() {
        NakedClass c = reflect(ContactTestObject.class);

        assertEquals("org.nakedobjects.object.ContactTestObject", c.fullName());
        assertEquals("ContactTestObject", c.getShortName());
        assertEquals("ContactTestObject", c.getIconName());
        assertEquals("Contact", c.getSingularName());
        assertEquals("Contacts", c.getPluralName());
    }

    public void testObject() {
        NakedClass c = reflect(GenericTestObject.class);

        assertEquals("Generic Objects", c.getPluralName());
    }

    public void testProduct() {
        reflect(ProductTestObject.class);
    }

    public void testRepeatability() {
        NakedClass a = nakedClass(ContactTestObject.class);
        NakedClass b = nakedClass(ProductTestObject.class);

        // repeated calls gets the same objects
        assertEquals(a, nakedClass(ContactTestObject.class));
        assertEquals(b, nakedClass(ProductTestObject.class));
    }

    public void testTypes() {
        // concrete classes maintain their type
        NakedClass c1 = nakedClass(ConcreteEmployee.class);

        //      assertEquals("org.nakedobjects.object.ConcreteEmployee",
        // c1.getJavaType().getNameAsString());
        Object obj1 = c1.acquireInstance();

        assertEquals("org.nakedobjects.object.ConcreteEmployee", obj1.getClass().getName());

        // abstract classes become ...$Proxy
        //
        //       NakedClass c2 = nakedClass(Employee.class);

        //       assertEquals("org.nakedobjects.object.Employee",
        // c2.getJavaType().getNameAsString());
        //        Object obj2 = c2.acquireInstance();

        //       assertEquals("org.nakedobjects.unittesting.testobjects.Employee$Proxy",
        // obj2.getClass().getName());
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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

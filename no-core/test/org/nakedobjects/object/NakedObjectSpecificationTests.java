package org.nakedobjects.object;

import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.reflect.ActionSpecification;
import org.nakedobjects.object.reflect.FieldSpecification;
import org.nakedobjects.object.reflect.NakedObjectSpecificationException;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;
import org.nakedobjects.object.system.TestClock;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.mockobjects.ExpectationSet;


// TODO most of these tests are testing the JavaReflector; move them to its test
public class NakedObjectSpecificationTests extends TestCase {
    private NakedObjectSpecification nakedClass;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(NakedObjectSpecificationTests.class));
    }

    public NakedObjectSpecificationTests(String name) {
        super(name);
    }

    private NakedObjectSpecification nakedClass(Class cls) {
        return NakedObjectSpecificationLoader.getInstance().loadSpecification(cls.getName());
    }

    protected void setUp() {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        new TestClock();

        new NakedObjectSpecificationLoaderImpl();
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
        NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());

        new MockReflector();

        nakedClass = nakedClass(NakedClassTestObject.class);
    }

    public void testInvalidObjectType() {
        try {
            nakedClass(InvalidNakedClassTestObject.class);
            fail();
        } catch (NakedObjectSpecificationException expected) {}
    }

    protected void tearDown() throws Exception {
        // manager.shutdown();
        super.tearDown();
    }

    public void testAcquireInstance() {
        Naked acquire = nakedClass.acquireInstance();
        assertTrue(acquire instanceof NakedClassTestObject);
    }

    /*
     * TODO reimplement public void testClassAbout() { ClassAbout about = new
     * ClassAbout("", true); reflector.setupClassAbout(about);
     * assertEquals(about, nakedClass.getClassAbout()); }
     */

    public void testGetFieldsNew() {
        FieldSpecification[] actions = nakedClass.getFields();

        assertEquals(3, actions.length);

        ExpectationSet exp = new ExpectationSet("field names");
        exp.addExpected("one");
        exp.addExpected("two");
        exp.addExpected("three");
        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    // TODO the rest of the test need to use mocks - as the above three do

    public void testGetClassActionByParam() {
        NakedObjectSpecification[] params = new NakedObjectSpecification[] { nakedClass(NakedClassTestParameter.class) };
        ActionSpecification action = nakedClass.getClassAction(ActionSpecification.USER, "example method", params);
        assertNotNull(action);
        assertEquals("examplemethod", action.getName());
        assertEquals(ActionSpecification.USER, action.getActionType());
        assertEquals(null, action.getReturnType());

        params = new NakedObjectSpecification[] { nakedClass(ProductTestObject.class) };
        assertNull(nakedClass.getClassAction(ActionSpecification.USER, "no method", params));
    }

    public void testGetClassActions() {
        NakedObjectSpecification c = nakedClass(ContactTestObject.class);
        ActionSpecification[] actions = c.getClassActions(ActionSpecification.USER);
        ExpectationSet exp = new ExpectationSet("action names");
        exp.addExpected("classop");
        exp.addExpected("dowhatever");
        exp.addExpected("example");

        for (int i = 0; i < actions.length; i++) {
            assertNotNull(actions[i]);
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testGetExplorationActions() {
        ActionSpecification[] actions = nakedClass.getObjectActions(ActionSpecification.EXPLORATION);
        ExpectationSet exp = new ExpectationSet("action names");
        exp.addExpected("explore");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testGetObjectsActions() {
        NakedObjectSpecification c = nakedClass(ContactTestObject.class);
        ActionSpecification[] actions = c.getObjectActions(ActionSpecification.USER);
        //assertEquals(6, actions.length);

        ExpectationSet exp = new ExpectationSet("action names");
        // 1 param actions
        exp.addExpected("addcontact");
        exp.addExpected("renew");

        // 0 param actions
        exp.addExpected("createinvoice");
        exp.addExpected("duplicate");
        exp.addExpected("setup");
        exp.addExpected("resetworth");
        exp.addExpected("persist");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testHashCodes() {
        NakedObjectSpecification c1 = nakedClass(Role.class);
        NakedObjectSpecification c2 = nakedClass(AssociationExample.class);

        assertTrue(c1.hashCode() != c2.hashCode());

    }

    public void testMemberOrdering() {
        NakedObjectSpecification c = nakedClass(GenericTestObject.class);
        com.mockobjects.ExpectationList exp2 = new com.mockobjects.ExpectationList("ordered names");

        exp2.addExpected("customer");
        exp2.addExpected("products");
        exp2.addExpected("datecreated");
        exp2.addExpected("lastactivity");
        FieldSpecification[] attributes = c.getFields();

        for (int i = 0; i < attributes.length; i++) {
            exp2.addActual(attributes[i].getName());
        }
        exp2.verify();
    }

    public void testNameAttribute() {
        NakedObjectSpecification c = nakedClass(ContactTestObject.class);
        FieldSpecification[] actions = c.getFields();

        for (int i = 0; i < actions.length; i++) {
            if (actions[i].getName().equals("name")) {
                FieldSpecification att = actions[i];

                assertEquals(true, att.isValue());
                assertEquals(TextString.class.getName(), att.getType().getFullName());
                assertEquals("name", att.getName());
                return;
            }
        }
        fail("Didn't find  Name attribute");
    }

    public void testNames() {
        NakedObjectSpecification c = nakedClass(ContactTestObject.class);

        assertEquals("org.nakedobjects.object.ContactTestObject", c.getFullName());
        assertEquals("ContactTestObject", c.getShortName());
        assertEquals("Contact", c.getSingularName());
        assertEquals("Contacts", c.getPluralName());
    }

    public void testObject() {
        NakedObjectSpecification c = nakedClass(GenericTestObject.class);

        assertEquals("Generic Objects", c.getPluralName());
    }

    public void testProduct() {
        nakedClass(ProductTestObject.class);
    }

    public void testRepeatability() {
        NakedObjectSpecification a = nakedClass(ContactTestObject.class);
        NakedObjectSpecification b = nakedClass(ProductTestObject.class);

        // repeated calls gets the same objects
        assertEquals(a, nakedClass(ContactTestObject.class));
        assertEquals(b, nakedClass(ProductTestObject.class));
    }

    public void testTypes() {
        // concrete classes maintain their type
        NakedObjectSpecification c1 = nakedClass(ConcreteEmployee.class);

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

    public void testSuperclass() {
        NakedObjectSpecification cls = nakedClass(Person.class);
        NakedObjectSpecification subclass1 = nakedClass(LittlePerson.class);

        assertEquals(cls, subclass1.superclass());
    }

    public void testSubclasses() {
        NakedObjectSpecification cls = nakedClass(Person.class);
        NakedObjectSpecification subclass1 = nakedClass(LittlePerson.class);
        NakedObjectSpecification subclass2 = nakedClass(BigPerson.class);
        NakedObjectSpecification subclass3 = nakedClass(SmallPerson.class);

        ExpectationSet set = new ExpectationSet("");
        set.addExpected(subclass1);
        set.addExpected(subclass2);

        NakedObjectSpecification[] subclasses = cls.subclasses();
        set.addActualMany(subclasses);
        set.verify();

        assertTrue(cls.isOfType(cls));
        assertTrue(subclass1.isOfType(cls));
        assertTrue(subclass2.isOfType(cls));
        assertTrue(subclass3.isOfType(cls));

        assertFalse(cls.isOfType(subclass1));
        assertFalse(cls.isOfType(subclass2));
        assertFalse(cls.isOfType(subclass3));

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

package org.nakedobjects.reflector.java.reflect;

import org.nakedobjects.object.Action;
import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.NakedObjectSpecificationException;
import org.nakedobjects.object.Persistable;
import org.nakedobjects.object.control.Hint;
import org.nakedobjects.object.reflect.OneToOnePeer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyOneToOneAssociation;
import test.org.nakedobjects.object.TestSystem;


public class JavaReflectorTest extends TestCase {

    private JavaIntrospector reflector;
    private TestSystem system;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(JavaReflectorTest.class));
    }
 
    protected void setUp() throws ClassNotFoundException {
    	LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
    	system = new TestSystem();
        system.init();
        system.addSpecification(new DummyNakedObjectSpecification());
    	
        reflector = new JavaIntrospector(JavaObjectForReflector.class, new DummyBuilder());
        reflector.introspect();
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testObjectActions() throws NakedObjectSpecificationException {
         Action[] actions = reflector.getObjectActions();
         assertEquals(2, actions.length);
         
         Action member = actions[0];
         assertEquals("Stop", member.getId());
     }
    
    public void testFieldSortOrder() throws NakedObjectSpecificationException {


        NakedObjectField[] fields = reflector.getFields();
        
        assertEquals(3, fields.length);
        assertEquals("One", fields[0].getId());
        assertEquals("Two", fields[1].getId());
        assertEquals("Three", fields[2].getId());
        
    }
    
    public void testActionSortOrder() throws NakedObjectSpecificationException {
        Action[] actions = reflector.getObjectActions();
        
        assertEquals(2, actions.length);
        assertEquals("Stop", actions[0].getId());
        assertEquals("Start", actions[1].getId());
    }
    
    public void testClassActionSortOrder() throws NakedObjectSpecificationException {
        Action[] actions = reflector.getClassActions();

        assertEquals(2, actions.length);
        assertEquals("Top", actions[0].getId());
        assertEquals("Bottom", actions[1].getId());
    }
    
   public void testShortName() {
        assertEquals("JavaObjectForReflector", reflector.shortName());
    }

    public void testPluralName() {
        assertEquals("Plural", reflector.pluralName());
    }
    
    public void testPersistable() {
        assertEquals(Persistable.USER_PERSISTABLE, reflector.persistable());
        
        
        reflector = new JavaIntrospector(JavaObjectForReflectorTransient.class, new DummyBuilder());
        reflector.introspect();
        assertEquals(Persistable.TRANSIENT, reflector.persistable());

    }

    public void testClassAbout() {
        Hint about = reflector.classHint();
        assertEquals(about, JavaObjectForReflector.about);
    }

    public void testSingularName() {
        assertEquals("Singular", reflector.singularName());
    }

    public void testFields() throws Exception {
//        system.addSpecification(new DummyNakedObjectSpecification()); // for Date
//        system.addSpecification(new DummyNakedObjectSpecification()); // for float
        
        NakedObjectField[] fields = reflector.getFields();
        
        assertEquals(3, fields.length);
        
       OneToOnePeer member = ((DummyOneToOneAssociation) fields[0]).getPeer();
       assertEquals("One", member.getIdentifier());
       assertEquals(false, member.isObject());
       
       member = ((DummyOneToOneAssociation) fields[2]).getPeer();
       assertEquals("Three", member.getIdentifier());
       assertEquals(true, member.isObject());
    }

    public void testNameManipulations() {
        assertEquals("CarRegistration", JavaIntrospector.javaBaseName("getCarRegistration"));
        assertEquals("Driver", JavaIntrospector.javaBaseName("Driver"));
        assertEquals("Register", JavaIntrospector.javaBaseName("actionRegister"));
        assertEquals("", JavaIntrospector.javaBaseName("action"));
    }
    
    public void testSuperclass() {
        assertEquals(Object.class.getName(), reflector.getSuperclass());
    }
    
    
    public void testInterfaces() {
        String[] interfaces = reflector.getInterfaces();
        assertEquals(2, interfaces.length);
        assertEquals(Interface1.class.getName(), interfaces[0]);
        assertEquals(Interface2.class.getIdentifier(), interfaces[1]);
    }
    
    public void testLookup() {
        assertFalse(reflector.isLookup());
    }
    
    
    
    
    
    
   /* 

    public void testGetClassActionByParam() {
        NakedObjectSpecification[] params = new NakedObjectSpecification[] { nakedObjectSpecification(NakedClassTestParameter.class) };
        Action action = nakedObjectSpecification.getClassAction(Action.USER, "example method", params);
        assertNotNull(action);
        assertEquals("examplemethod", action.getName());
        assertEquals(Action.USER, action.getActionType());
        assertEquals(null, action.getReturnType());

        params = new NakedObjectSpecification[] { nakedObjectSpecification(ProductTestObject.class) };
        assertNull(nakedObjectSpecification.getClassAction(Action.USER, "no method", params));
    }

    public void testGetClassActions() {
        NakedObjectSpecification c = nakedObjectSpecification(ContactTestObject.class);
        Action[] actions = c.getClassActions(Action.USER);
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
        Action[] actions = nakedObjectSpecification.getObjectActions(Action.EXPLORATION);
        ExpectationSet exp = new ExpectationSet("action names");
        exp.addExpected("explore");

        for (int i = 0; i < actions.length; i++) {
            exp.addActual(actions[i].getName());
        }
        exp.verify();
    }

    public void testGetObjectsActions() {
        NakedObjectSpecification c = nakedObjectSpecification(ContactTestObject.class);
        Action[] actions = c.getObjectActions(Action.USER);
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
        NakedObjectSpecification c1 = nakedObjectSpecification(Role.class);
        NakedObjectSpecification c2 = nakedObjectSpecification(AssociationExample.class);

        assertTrue(c1.hashCode() != c2.hashCode());

    }

    public void testMemberOrdering() {
        NakedObjectSpecification c = nakedObjectSpecification(GenericTestObject.class);
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
        NakedObjectSpecification c = nakedObjectSpecification(ContactTestObject.class);
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
        NakedObjectSpecification c = nakedObjectSpecification(ContactTestObject.class);

        assertEquals("org.nakedobjects.object.ContactTestObject", c.getFullName());
        assertEquals("ContactTestObject", c.getShortName());
        assertEquals("Contact", c.getSingularName());
        assertEquals("Contacts", c.getPluralName());
    }

    public void testObject() {
        NakedObjectSpecification c = nakedObjectSpecification(GenericTestObject.class);

        assertEquals("Generic Objects", c.getPluralName());
    }

    public void testProduct() {
        nakedObjectSpecification(ProductTestObject.class);
    }

    public void testRepeatability() {
        NakedObjectSpecification a = nakedObjectSpecification(ContactTestObject.class);
        NakedObjectSpecification b = nakedObjectSpecification(ProductTestObject.class);

        // repeated calls gets the same objects
        assertEquals(a, nakedObjectSpecification(ContactTestObject.class));
        assertEquals(b, nakedObjectSpecification(ProductTestObject.class));
    }

    public void testTypes() {
        // concrete classes maintain their type
        NakedObjectSpecification c1 = nakedObjectSpecification(ConcreteEmployee.class);

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


    public void testSubclasses() {
        NakedObjectSpecification cls = nakedObjectSpecification(Person.class);
        NakedObjectSpecification subclass1 = nakedObjectSpecification(LittlePerson.class);
        NakedObjectSpecification subclass2 = nakedObjectSpecification(BigPerson.class);
        NakedObjectSpecification subclass3 = nakedObjectSpecification(SmallPerson.class);

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
    private NakedObjectSpecification nakedObjectSpecification(Class cls) {
        return NakedObjects.getSpecificationLoader().loadSpecification(cls.getName());
    }
    */
}

/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

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

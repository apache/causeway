package org.nakedobjects.object;

import org.nakedobjects.object.defaults.value.Date;
import org.nakedobjects.object.defaults.value.FloatingPointNumber;
import org.nakedobjects.object.defaults.value.Label;
import org.nakedobjects.object.defaults.value.Logical;
import org.nakedobjects.object.defaults.value.Money;
import org.nakedobjects.object.defaults.value.Option;
import org.nakedobjects.object.defaults.value.Percentage;
import org.nakedobjects.object.defaults.value.TextString;
import org.nakedobjects.object.defaults.value.Time;
import org.nakedobjects.object.defaults.value.TimeStamp;
import org.nakedobjects.object.defaults.value.URLString;
import org.nakedobjects.object.defaults.value.WholeNumber;


public abstract class NakedObjectStoreFieldsTestCase extends NakedObjectStoreTestCase {
    private Person people[];
    private NakedObjectSpecification personClass;
    private NakedObjectSpecification roleClass;
    private Role[] roles;

    public NakedObjectStoreFieldsTestCase(String name) {
        super(name);
    }

    private void cleanPersonInstances() throws ObjectStoreException, UnsupportedFindException {
        NakedObject[] initialInstances;

        do {
            initialInstances = objectStore.getInstances(personClass, false);

            for (int i = 0; i < initialInstances.length; i++) {
                NakedObject object = (NakedObject) initialInstances[i];
                objectStore.destroyObject(object);
            }
        } while (initialInstances.length > 0);

        assertTrue("no person objects", !objectStore.hasInstances(personClass, false));
    }

    private void cleanRoleInstances() throws ObjectStoreException, UnsupportedFindException {
        NakedObject[] initialInstances;

        do {
            initialInstances = objectStore.getInstances(roleClass, false);

            for (int i = 0; i < initialInstances.length; i++) {
                NakedObject object = (NakedObject) initialInstances[i];
                objectStore.destroyObject(object);
            }
        } while (initialInstances.length > 0);

        assertTrue("no role objects", !objectStore.hasInstances(roleClass, false));
    }
    
    protected void initialiseObjects() throws Exception {
        // classes
        personClass = NakedObjectSpecification.getSpecification(Person.class.getName());
        roleClass = NakedObjectSpecification.getSpecification(Role.class.getName());

        // objects
        String names[] = { "Freddy", "John", "Sam", "Zax", "fdfdklj" };
        people = new Person[names.length];
        for (int i = 0; i < names.length; i++) {
            people[i] = new Person();
            people[i].setContext(context);
            people[i].setOid(nextOid());
            people[i].name.setValue(names[i]);
            objectStore.createObject(people[i]);

            assertNotNull(people[i].getOid());
        }

        String roleNames[] = { "Leader", "Specialist", "Tester" };
        roles = new Role[roleNames.length];
        for (int i = 0; i < roleNames.length; i++) {
            roles[i] = new Role();
            roles[i].setContext(context);
            roles[i].setOid(nextOid());
            roles[i].name.setValue(roleNames[i]);
            roles[i].person = people[i];
            objectStore.createObject(roles[i]);
        }
    }
 
    public void testGetObject() throws Exception {
        restartObjectStore();
        
        for (int i = 0; i < people.length; i++) {
            Person person = (Person) objectStore.getObject(people[i].getOid(), personClass);
            assertEquals(people[i].name, person.name);
        }
    }

    public void testGetSameInstance() throws Exception {
        restartObjectStore();
        NakedObject p1 = objectStore.getObject(people[2].getOid(), personClass);
        
        Role r = (Role) objectStore.getObject(roles[2].getOid(), roleClass);
        Person p3 = r.getPerson();
        assertSame(p1, p3);
     }
    
    public void testInstancesWithAssociationPattern() throws Exception {
        restartObjectStore();

        try {
            Role rolePattern; 
            rolePattern = new Role();
            rolePattern.setContext(context);
            rolePattern.makeFinder();
            rolePattern.person = people[1];

            assertTrue(objectStore.hasInstances(roleClass, false));

            NakedObject[] v = objectStore.getInstances(rolePattern, false);
            assertEquals(1, v.length);
            assertEquals(roles[1], v[0]);

            rolePattern.person = people[3];

            v = objectStore.getInstances(rolePattern, false);
            assertEquals(0, v.length);
        } catch (UnsupportedFindException e) {}

    }

    public void testInstancesWithString() throws Exception {
        restartObjectStore();

        try {
            NakedObject[] v = objectStore.getInstances(personClass, "JOHN", false);
            assertEquals(1, v.length);
            assertEquals(people[1], v[0]);

            v = objectStore.getInstances(personClass, "red", false);
            assertEquals(1, v.length);
            assertEquals(people[0], v[0]);
        } catch (UnsupportedFindException e) {}

    }

    public void testInstancesWithValuePattern() throws Exception {
        restartObjectStore();

        try {
            Person personPattern;
            personPattern = new Person();
            personPattern.makeFinder();
            personPattern.name.setValue(people[3].name.title().toString());

            NakedObject[] v = objectStore.getInstances(personPattern, false);
            assertEquals(1, v.length);
            assertEquals(people[3], v[0]);

            personPattern.name.setValue("no match personPattern");

            v = objectStore.getInstances(personPattern, false);
            assertEquals(0, v.length);
        } catch (UnsupportedFindException e) {}

    }

    public void testPatternedInstances() throws Exception {
        restartObjectStore();

        try {
            cleanPersonInstances();
            cleanRoleInstances();

            Role[] obj = new Role[4];

            Person v = null;

            for (int i = 0; i < obj.length; i++) {
                obj[i] = new Role();
                obj[i].setContext(context);
                obj[i].setOid(nextOid());
                obj[i].getName().setValue(((i % 2) == 0) ? "even" : "odd");

                v = new Person();
                v.setContext(context);
                v.setOid(nextOid());
                obj[i].setPerson(v);
                objectStore.createObject(obj[i]);
            }

            // with an empty pattern, expect all
            Role examplePattern = new Role();
            examplePattern.setContext(context);
            examplePattern.makeFinder();

            NakedObject[] instances = objectStore.getInstances(examplePattern, false);
            assertEquals("instances size", 4, instances.length);

            // with a pattern that doesn't match, expect 0
            examplePattern.getName().setValue("neither");
            instances = objectStore.getInstances(examplePattern, false);
            assertEquals("instances size", 0, instances.length);

            // with a pattern that matches half, expect half
            examplePattern.getName().setValue("even");
            instances = objectStore.getInstances(examplePattern, false);
            assertEquals("instances size", 2, instances.length);

            // with a pattern that matches the other half, expect half
            examplePattern.getName().setValue("odd");
            instances = objectStore.getInstances(examplePattern, false);
            assertEquals("instances size", 2, instances.length);

            // with a pattern on the association, expect one
            examplePattern.getName().clear();
            examplePattern.setPerson(v);
            instances = objectStore.getInstances(examplePattern, false);
            assertEquals("instances size", 1, instances.length);
        } catch (UnsupportedFindException e) {
            // don't test OSes that don't support this kind of find
        }
    }
    

    public void testSave() throws Exception {
        // extend to test associations
        Person person = people[2];
        person.getName().setValue("Samuel");
        person.getSalary().setValue(10.0);
        
        objectStore.save(person);

        restartObjectStore();
 
        assertEquals("Samuel", ((Person) objectStore.getObject(person.getOid(), personClass)).name.stringValue());
        assertEquals(10.0f, ((Person) objectStore.getObject(person.getOid(), personClass)).salary.floatValue(), 0.0f);
 
        assertEquals("Freddy", ((Person) objectStore.getObject(people[0].getOid(), personClass)).name.stringValue());
    }


    public void testValues() throws Exception {
        ValueObjectExample e1 = new ValueObjectExample();
        e1.setOid(nextOid());

        // get each value object
        Date date = e1.getDate();
        FloatingPointNumber floatingPoint = e1.getFloatingPoint();
        Label label = e1.getLabel();

        //		label.setValue("Abc");
        Logical logical = e1.getLogical();
        Money money = e1.getMoney();
        Option option = e1.getOption();
        Percentage percentage = e1.getPercentage();
        TextString textString = e1.getTextString();
        textString.setValue("Abcd");

        Time time = e1.getTime();
        TimeStamp timestamp = e1.getTimeStamp();
        URLString urlString = e1.getUrlString();
        WholeNumber wholeNumber = e1.getWholeNumber();
        wholeNumber.setValue(198218);

        // make persistent
        objectStore.createObject(e1);

        restartObjectStore();

        ValueObjectExample e2 = (ValueObjectExample) objectStore.getObject(e1.getOid(), e1.getSpecification());

        // check each value
        assertTrue("Dates differ", date.isSameAs(e2.getDate()));
        assertTrue("Floating points differ", floatingPoint.isSameAs(e2.getFloatingPoint()));
        assertTrue("Labels differ '" + label.stringValue() + "' '" + e2.getLabel().stringValue() + "'", label.isSameAs(e2
                .getLabel()));
        assertTrue("Logicals differ", logical.isSameAs(e2.getLogical()));
        assertTrue("Moneys differ", money.isSameAs(e2.getMoney()));
        assertTrue("Options differ", option.isSameAs(e2.getOption()));
        assertTrue("Percentages differ", percentage.isSameAs(e2.getPercentage()));
        assertTrue("TextStrings differ", textString.isSameAs(e2.getTextString()));
        assertTrue("Times differ " + time + " " + e2.getTime(), time.isSameAs(e2.getTime()));
        assertTrue("Timestamps differ " + timestamp + " " + e2.getTimeStamp(), timestamp.isSameAs(e2.getTimeStamp()));
        assertTrue("URLStrings differ", urlString.isSameAs(e2.getUrlString()));
        assertTrue("WholeNumbers differ", wholeNumber.isSameAs(e2.getWholeNumber()));
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
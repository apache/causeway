package org.nakedobjects.object;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.defaults.SimpleNakedClass;


public abstract class NakedObjectStoreInstancesTestCase extends NakedObjectStoreTestCase {
    private Person people[];
    private NakedObjectSpecification personClass;
    private String personClassName;
    private Person personPattern;
    private NakedObjectSpecification roleClass;

    private Role[] roles;

    public NakedObjectStoreInstancesTestCase(String name) {
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
        personClassName = Person.class.getName();
        personClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class);
        roleClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Role.class);

        // patterns
        personPattern = new Person();
        personPattern.makeFinder();

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

    protected abstract NakedObjectStore installObjectStore() throws Exception;

    public void testClass() throws Exception {
        NakedClass nc = new SimpleNakedClass(personClassName);
        nc.setContext(context);
        nc.setOid(nextOid());
        objectStore.createNakedClass(nc);

        NakedClass nc2 = objectStore.getNakedClass(personClassName);
        assertEquals(nc, nc2);
        assertEquals(personClassName, nc2.getName());
    }

    public void testClassAsObjects() throws Exception {
        NakedClass nc = new SimpleNakedClass(personClassName);
        nc.setOid(nextOid());
        objectStore.createNakedClass(nc);

        NakedClass nc2 = (NakedClass) objectStore.getObject(nc.getOid(), nc.getSpecification());
        assertEquals(nc, nc2);
        assertEquals(personClassName, nc2.getName());
    }

    public void testClassNotFound() throws Exception {
        try {
            String name = "org.company.Test";
            objectStore.getNakedClass(name);
            fail();
        } catch (ObjectNotFoundException expected) {}
    }

    public void testDestroyObject() throws Exception {
        objectStore.destroyObject(people[2]);

        restartObjectStore();

        NakedObject[] v = objectStore.getInstances(personClass, false);
        assertEquals(people.length - 1, v.length);
        // all instances should not be the one we destroyed
        for (int i = 0; i < v.length; i++) {
            assertFalse(people[2].equals(v[i]));
        }
    }

    public void testGetInstancesForClass() throws Exception {
        restartObjectStore();

        try {
            assertTrue(objectStore.hasInstances(personClass, false));

            assertEquals(people.length, objectStore.numberOfInstances(personClass, false));

            NakedObject[] v = objectStore.getInstances(personPattern, false);
            assertEquals(people.length, v.length);
            for (int i = 0; i < v.length; i++) {
                NakedObject element = (NakedObject) v[i];
                assertEquals(people[i], element);
            }
        } catch (UnsupportedFindException e) {}
    }

    public void testGetObject() throws Exception {
        restartObjectStore();

        for (int i = 0; i < people.length; i++) {
            Person person = (Person) objectStore.getObject(people[i].getOid(), personClass);
            assertEquals(people[i], person);
        }
    }

    public void testGetSameInstance() throws Exception {
        restartObjectStore();
        NakedObject p1 = objectStore.getObject(people[2].getOid(), personClass);
        NakedObject p2 = objectStore.getObject(people[2].getOid(), personClass);
        assertEquals(p1, p2);
    }

    public void testInstancesRepeatability() throws ConfigurationException, ComponentException, ObjectStoreException, Exception {
        restartObjectStore();

        try {
            NakedObject[] v1 = objectStore.getInstances(personPattern, false);
            NakedObject[] v2 = objectStore.getInstances(personPattern, false);

            for (int i = 0; i < v1.length; i++) {
                NakedObject element1 = (NakedObject) v1[i];
                NakedObject element2 = (NakedObject) v2[i];

                assertEquals(element1, element2);
            }
        } catch (UnsupportedFindException e) {}
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
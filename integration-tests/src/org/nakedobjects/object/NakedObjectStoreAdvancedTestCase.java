package org.nakedobjects.object;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ConfigurationException;



public abstract class NakedObjectStoreAdvancedTestCase extends NakedObjectStoreTestCase {
    private Oid membersOid;
    private Person people[];
    private NakedObjectSpecification personClass;
    private Person personPattern;
    private NakedObjectSpecification roleClass;
    private Role rolePattern;
    private Role[] roles;
    private Team team;
    private Person[] teamMembers;
    private Oid teamOid;

    public NakedObjectStoreAdvancedTestCase(String name) {
        super(name);
    }

    protected void initialiseObjects() throws Exception {
        // classes
        personClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Person.class.getName());
        roleClass = NakedObjectSpecificationLoader.getInstance().loadSpecification(Role.class.getName());

        // patterns
        personPattern = new Person();
        personPattern.makeFinder();
        rolePattern = new Role();
        rolePattern.makeFinder();

        // objects
        String names[] = { "Freddy", "John", "Sam", "Zax" };
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
            roles[i].referencedObject = people[i];
            objectStore.createObject(roles[i]);
        }

        team = new Team();
        team.setContext(context);
        team.setOid(teamOid = nextOid());
        team.getMembers().setOid(membersOid = nextOid());
        
        teamMembers = new Person[names.length - 1];
        for (int i = 0; i < names.length - 1; i++) {
            teamMembers[i] = people[i];
            team.getMembers().add(teamMembers[i]);
        }

        objectStore.createObject(team);
        objectStore.createObject(team.getMembers());
    }

    public void testHangingReference() throws Exception {
        restartObjectStore();

        Role role = new Role();
        role.setContext(context);
        role.setOid(nextOid());
        role.name.setValue("Leader");

        Person person = new Person();
        person.setContext(context);
        person.setOid(nextOid());
        person.getName().setValue("Fred");
        role.referencedObject = person;

        objectStore.createObject(role);
        objectStore.createObject(person);

        Oid roleOid = role.getOid();
        Oid persoOid = role.referencedObject.getOid();

        assertTrue(role.isPersistent());
        assertTrue(person.isPersistent());
        assertFalse("but the object should not be empty", person.getName().isEmpty());

        // remove and object, leaving a reference hanging
        objectStore.destroyObject(person);
        person = null;
        role = null;

        // read the object back in
        role = (Role) objectStore.getObject(roleOid, roleClass);
        person = role.getReferencedObject();

        assertNotNull("an empty object of the allowable type should be accessed", person);
        assertEquals("the oid should be the same", persoOid, person.getOid());
    }

    public void testMakeEmptyInternalCollectionPersistent() throws Exception {
        restartObjectStore();

        Team restoredTeam = (Team) objectStore.getObject(teamOid, team.getSpecification());
        assertEquals(teamOid, restoredTeam.getOid());
        assertEquals(membersOid, restoredTeam.getMembers().getOid());
    }

    public void testMakeInternalCollectionsObjectsPersistent() throws Exception {
        restartObjectStore();

        assertNotNull(team.getOid());
        InternalCollection collection = team.getMembers();
        assertNotNull(collection.getOid());
        assertEquals(teamMembers.length, collection.size());
        for (int i = 0; i < teamMembers.length; i++) {
            assertNotNull(collection.elementAt(i).getOid());
        }

        for (int i = 0; i < teamMembers.length; i++) {
            NakedObject obj = objectStore.getObject(teamMembers[i].getOid(), personClass);
            assertEquals(teamMembers[i], obj);
        }
    }

    public void testResolve() throws ConfigurationException, ComponentException, ObjectStoreException, Exception {
        restartObjectStore();

        Role role = (Role) objectStore.getObject(roles[1].getOid(), roleClass);
        objectStore.resolve(role);
        Person person = role.getReferencedObject();

        // at this point the person could just be a skeleton object
        objectStore.resolve(person);
        // after the resolve it must be be complete

        assertEquals(people[1].getName(), person.getName());
        assertEquals(people[1].getSalary(), person.getSalary());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
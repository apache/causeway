package org.nakedobjects.object;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class NakedObjectMementoTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(NakedObjectMementoTest.class);
    }

    private MockObjectManager manager;
    private Person person1;
    private Person person2;
    private Role role;
    private Team team;
    private LoadedObjects loadedObjects;

    protected void setUp() {
        Logger.getRootLogger().setLevel(Level.OFF);
        manager = MockObjectManager.setup();
        manager.setupAddClass(NakedObject.class);
        manager.setupAddClass(Team.class);
        manager.setupAddClass(Person.class);
        manager.setupAddClass(Role.class);
        
        loadedObjects = new LoadedObjects();
        
        team = new Team();
        team.setOid(new Integer(11));
        team.getMembers().setOid(new Integer(13));

        person1 = new Person();
        person1.setOid(new Integer(9));
        person1.getName().setValue("Fred");
        team.getMembers().added(person1);

        person2 = new Person();
        person2.setOid(new Integer(17));
        person2.getName().setValue("John");
        team.getMembers().added(person2);

        role = new Role();
        role.setOid(new Integer(19));
        role.getName().setValue("One");
        role.setPerson(person2);
    }

    public void testManyReferencesInRecreated() {
        NakedObjectMemento mem = new NakedObjectMemento(team);

        Person expected = new Person();
        expected.setOid(new Integer(17));
        loadedObjects.loaded(expected);

        Team t2 = (Team) mem.recreateObject(loadedObjects);
        
        Person recreated = (Person) t2.getMembers().elementAt(0);
        assertEquals(person1.getOid(), recreated.getOid());
        assertFalse(person1 == recreated);
 
        recreated = (Person) t2.getMembers().elementAt(1);
        assertEquals(person2.getOid(), recreated.getOid());
        assertSame(expected, recreated);
    }

    public void testManyReferencesInRecreatedWithNewAssociatedInstances() {
        NakedObjectMemento mem = new NakedObjectMemento(team);
        Team t2 = (Team) mem.recreateObject(loadedObjects);

        assertFalse(team == t2);
        assertEquals(team.getOid(), t2.getOid());

        Person recreated = (Person) t2.getMembers().elementAt(0);
        assertEquals(recreated, person1);
        assertFalse(recreated == person1);
        assertEquals(person1.getOid(), recreated.getOid());

        recreated = (Person) t2.getMembers().elementAt(1);
        assertEquals(recreated, person2);
        assertFalse(recreated == person2);
        assertEquals(person2.getOid(), recreated.getOid());
    }

    public void testReferenceInRecreated() {
        NakedObjectMemento mem = new NakedObjectMemento(role);

        Person expected = new Person();
        expected.setOid(new Integer(17));
        loadedObjects.loaded(expected);
        
        Role r2 = (Role) mem.recreateObject(loadedObjects);
        Person p2 = (Person) r2.getPerson();
        assertFalse(p2 == person2);
        assertEquals(person2.getOid(), p2.getOid());
        
        assertSame(expected, p2);
    }

    public void testReferenceInRecreatedWithNewAssociatedInstances() {
        NakedObjectMemento mem = new NakedObjectMemento(role);

        Role r2 = (Role) mem.recreateObject(loadedObjects);
        Person recreated = (Person) r2.getPerson();
        assertFalse(recreated == person2);
        assertEquals(person2.getOid(), recreated.getOid());
    }

    public void testValuesInRecreatedObject() {
        NakedObjectMemento mem = new NakedObjectMemento(person1);

        Person p2 = (Person) mem.recreateObject(loadedObjects);
        assertFalse(person1 == p2);
        assertEquals(person1.getName(), p2.getName());
        assertEquals(person1.getOid(), p2.getOid());
    }
    
    public void testReferencesInRecreated2() {
        NakedObjectMemento mem = new NakedObjectMemento(role);

        Role r2 = (Role) mem.recreateObject(loadedObjects);
        assertFalse(role == r2);
        assertEquals(role.getName(), r2.getName());
        assertEquals(role.getOid(), r2.getOid());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2004 Naked Objects Group Ltd
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
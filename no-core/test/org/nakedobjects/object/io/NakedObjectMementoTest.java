package org.nakedobjects.object.io;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecificationImpl;
import org.nakedobjects.object.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.SerialOid;
import org.nakedobjects.object.defaults.value.TestClock;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class NakedObjectMementoTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(NakedObjectMementoTest.class);
    }

    private MockObjectManager originatingManager;
   // private MockObjectManager receivingManager;
    private Person person;
    private Person person2;
    private Role role;
    private Team team;
    private LoadedObjects loadedObjects;
    private NakedObjectContext context;

    protected void setUp() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        
        new TestClock();
        
    	new NakedObjectSpecificationLoaderImpl();
    	NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
    	NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());
    	
        originatingManager = MockObjectManager.setup();
 
        loadedObjects = new LoadedObjectsHashtable();
        
       context = new NakedObjectContext(originatingManager);

       team = new Team();
        team.setContext(context);
        team.setOid(new SerialOid(11));
        team.getMembers().setOid(new SerialOid(13));

        person = new Person();
        person.setContext(context);
        person.setOid(new SerialOid(9));
        person.getName().setValue("Fred");
        team.getMembers().added(person);

        person2 = new Person();
        person2.setContext(context);
        person2.setOid(new SerialOid(17));
        person2.getName().setValue("John");
        team.getMembers().added(person2);

        role = new Role();
        role.setContext(context);
        role.setOid(new SerialOid(19));
        role.getName().setValue("One");
        role.setPerson(person2);
        
    }

    protected void tearDown() throws Exception {
        originatingManager.shutdown();
        super.tearDown();
    }
    
    public void testManyReferencesInRecreated() {
        Memento mem = mementoTransfer(team);

        Person expected = new Person();
        expected.setOid(new SerialOid(17));
        loadedObjects.loaded(expected);

        Team t2 = (Team) mem.recreateObject(loadedObjects, context);
        
        Person recreated = (Person) t2.getMembers().elementAt(0);
        assertEquals(person.getOid(), recreated.getOid());
        assertFalse(person == recreated);
 
        recreated = (Person) t2.getMembers().elementAt(1);
        assertEquals(person2.getOid(), recreated.getOid());
        assertSame(expected, recreated);
    }

    public void testManyReferencesInRecreatedWithNewAssociatedInstances() {
        Memento mem = mementoTransfer(team);
        Team t2 = (Team) mem.recreateObject(loadedObjects, context);

        assertFalse(team == t2);
        assertEquals(team.getOid(), t2.getOid());

        Person recreated = (Person) t2.getMembers().elementAt(0);
        assertEquals(recreated, person);
        assertFalse(recreated == person);
        assertEquals(person.getOid(), recreated.getOid());

        recreated = (Person) t2.getMembers().elementAt(1);
        assertEquals(recreated, person2);
        assertFalse(recreated == person2);
        assertEquals(person2.getOid(), recreated.getOid());
    }

    public void testReferenceInRecreated() {
        Memento mem = mementoTransfer(role);

        Person expected = new Person();
        expected.setContext(context);
        expected.setOid(new SerialOid(17));
        loadedObjects.loaded(expected);
        
        Role r2 = (Role) mem.recreateObject(loadedObjects, context);
        Person p2 = (Person) r2.getPerson();
        assertFalse(p2 == person2);
        assertEquals(person2.getOid(), p2.getOid());
        
        assertSame(expected, p2);
    }

    public void testReferenceInRecreatedWithNewAssociatedInstances() {
        Memento mem = mementoTransfer(role);

        Role r2 = (Role) mem.recreateObject(loadedObjects, context);
        Person recreated = (Person) r2.getPerson();
        assertFalse(recreated == person2);
        assertEquals(person2.getOid(), recreated.getOid());
    }

    public void testValuesInRecreatedObject() {
        Memento mem = mementoTransfer(person);
        
        Person p2 = (Person) mem.recreateObject(loadedObjects, context);
        assertFalse(person == p2);
        assertEquals(person.getName(), p2.getName());
        assertEquals(person.getOid(), p2.getOid());
    }
    
    private Memento mementoTransfer(NakedObject object) {
        Memento mem = new Memento(object);
        BinaryTransferableWriter data = new BinaryTransferableWriter();
        mem.writeData(data);
        TransferableReader data2 = new BinaryTransferableReader(data.getBinaryData());
        Memento mem2 = new Memento();
        mem2.restore(data2);
        return mem2;
    }

    public void testReferencesInRecreated2() {
        Memento mem = mementoTransfer(role);

        Role r2 = (Role) mem.recreateObject(loadedObjects, context);
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
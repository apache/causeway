package org.nakedobjects.io;

import org.nakedobjects.BasicSystem;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.io.Memento;
import org.nakedobjects.object.persistence.SerialOid;
import org.nakedobjects.object.system.TestClock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class MementoTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(MementoTest.class);
    }

    private NakedObjectLoader objectLoader;
    private Person person;
    private Person person2;
    private NakedObject personAdapter;
    private NakedObject personAdapter2;
    private Role role;
    private NakedObject roleAdapter;
    private BasicSystem system;
    private Team team;

    private Memento mementoTransfer(Object pojo) throws Exception {
        NakedObject object = objectLoader.getAdapterFor(pojo);
        assertNotNull(object);
        Memento mem = new Memento(object);
        
        ByteArrayOutputStream baos;
        ObjectOutputStream oos = new ObjectOutputStream(baos = new ByteArrayOutputStream());
        oos.writeObject(mem);
        oos.close();
        byte[] data = baos.toByteArray();
        oos.close();
        
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        return (Memento) ois.readObject();
    }
    
    protected void setUp() {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);
        
        new TestClock();
        
        system = new BasicSystem();
        system.init();
        
        objectLoader = NakedObjects.getObjectLoader();

        team = new Team();
        NakedObject teamAdapter = objectLoader.createAdapterForTransient(team);
        objectLoader.madePersistent(teamAdapter, new SerialOid(11));
        
        
        person = new Person();
        personAdapter = objectLoader.createAdapterForTransient(person);
        objectLoader.madePersistent(personAdapter, new SerialOid(9));
        person.getName().setValue("Fred");
        team.getMembers().addElement(person);

        person2 = new Person();
        personAdapter2 = objectLoader.createAdapterForTransient(person2);
        objectLoader.madePersistent(personAdapter2, new SerialOid(17));
        person2.getName().setValue("John");
        team.getMembers().addElement(person2);

        role = new Role();
        roleAdapter = objectLoader.createAdapterForTransient(role);
        objectLoader.madePersistent(roleAdapter, new SerialOid(19));
        role.getName().setValue("One");
        role.setPerson(person2);
    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testManyReferencesInRecreated() throws Exception {
        // TODO memento is not recursing into the objects - specifically values in person are missing
        Memento mem = mementoTransfer(team);

        objectLoader.unloaded(objectLoader.getAdapterFor(team));
        objectLoader.unloaded(objectLoader.getAdapterFor(person));
        objectLoader.unloaded(objectLoader.getAdapterFor(person2));
                      
        
        NakedObject teamAdapter = mem.recreateObject();
        assertEquals(new SerialOid(11), teamAdapter.getOid());
        assertEquals(ResolveState.RESOLVED, teamAdapter.getResolveState());
        
        Team rt = (Team) teamAdapter.getObject();
        assertNotSame(team, rt);
        
        Person rp1 = (Person) rt.getMembers().elementAt(0);
        assertNotSame(person, rp1);
        assertEquals("Fred", rp1.getName().stringValue());
 
        Person rp2 = (Person) rt.getMembers().elementAt(1);
        assertNotSame(person2, rp1);
        assertEquals("John", rp2.getName().stringValue());
    }

    public void testManyReferencesInRecreatedWithNewAssociatedInstances() throws Exception {
        Memento mem = mementoTransfer(team);
        NakedObject teamAdapter = mem.recreateObject();
        Team t2 = (Team) teamAdapter.getObject();

        assertFalse(team == t2);
   //     assertEquals(team.getOid(), t2.getOid());

        Person recreated = (Person) t2.getMembers().elementAt(0);
        assertEquals(recreated, person);
        assertFalse(recreated == person);
    //    assertEquals(person.getOid(), recreated.getOid());

        recreated = (Person) t2.getMembers().elementAt(1);
        assertEquals(recreated, person2);
        assertFalse(recreated == person2);
    //    assertEquals(person2.getOid(), recreated.getOid());
    }

    public void testReferenceInRecreated() throws Exception {
        Memento mem = mementoTransfer(role);

        Person expected = new Person();
    //    expected.setOid(new SerialOid(17));
    //    objectLoader.loaded(expected);
        
        Role r2 = (Role) mem.recreateObject();
        Person p2 = (Person) r2.getPerson();
        assertFalse(p2 == person2);
      //  assertEquals(person2.getOid(), p2.getOid());
        
        assertSame(expected, p2);
    }

    public void testReferenceInRecreatedWithNewAssociatedInstances() throws Exception {
        Memento mem = mementoTransfer(role);

        Role r2 = (Role) mem.recreateObject();
        Person recreated = (Person) r2.getPerson();
        assertFalse(recreated == person2);
     //   assertEquals(person2.getOid(), recreated.getOid());
    }
    
    public void testReferencesInRecreated2() throws Exception {
        Memento mem = mementoTransfer(role);

        NakedObject adapter = mem.recreateObject();
        Role r2 = (Role) adapter.getObject();
        assertFalse(role == r2);
        assertEquals(role.getName(), r2.getName());
        assertEquals(roleAdapter.getOid(), adapter.getOid());
    }

    public void testValuesInRecreatedObject() throws Exception {
        Memento mem = mementoTransfer(person);
        
        NakedObject adapter = mem.recreateObject();
        Person p2 = (Person) adapter.getObject(); 
        assertFalse(person == p2);
        assertEquals(person.getName(), p2.getName());
        assertEquals(personAdapter.getOid(), adapter.getOid());
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
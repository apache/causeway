package org.nakedobjects.object.transaction;

import org.nakedobjects.object.MockLoadedObjects;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.MockObjectStore;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.collection.InternalCollection;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class TransactionTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(TransactionTest.class);
    }

    private InternalCollection members;
    private Oid membersOid;
    private Person person;
    private Oid personOid;
    private Oid teamOid;

    private Transaction t;
    private Team team;
    private MockTransactionManager tm;
    private MockObjectStore objectStore;
    private MockLoadedObjects loadedObjects;
    private Oid roleOid;
    private Role role;

    public TransactionTest(String arg0) {
        super(arg0);
    }

    protected void setUp() throws Exception {
        super.setUp();

        LogManager.getRootLogger().setLevel(Level.OFF);

        MockObjectManager manager = MockObjectManager.setup();
       
        tm = new MockTransactionManager();
        t = new Transaction(tm);

        objectStore = new MockObjectStore();
        loadedObjects = (MockLoadedObjects) objectStore.getLoadedObjects();

        personOid = new MockOid(1);
        person = new Person();
        person.setOid(personOid);

        roleOid = new MockOid(2);
        role = new Role();
        role.setOid(roleOid);

        teamOid = new MockOid(3);
        membersOid = new MockOid(5);
        team = new Team();
        team.setOid(teamOid);
        members = team.getMembers();
        members.setOid(membersOid);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testComplete() {
        assertFalse(t.isComplete());

        t.start();
        t.end();
        assertFalse(t.isComplete());

        t.end();
        assertTrue(t.isComplete());
    }

    public void testCreate() {
        t.prepareCreate(person);
        t.prepareCreate(team);

        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals(4, actions.size());
        assertEquals("createObject " + person, actions.elementAt(1));
        assertEquals("createObject " + team, actions.elementAt(2));
    }

    public void testCreateAndDelete() {
        t.prepareCreate(person);
        t.prepareDestroy(person);

        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals("the create and destroy cancel each other out", 0, actions.size());
    }

    public void testCreateAndSave() {
        t.prepareCreate(person);
        t.prepareSave(person);

        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals("both the create and saves become one OS action", 3, actions.size());
        assertEquals("createObject " + person, actions.elementAt(1));
    }

    public void testCreateSaveAndDestory() {
        t.prepareCreate(person);
        t.prepareSave(person);
        t.prepareDestroy(person);

        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals("first two actions are cancelled by last", 0, actions.size());
    }

    public void testDelete() {
        t.prepareDestroy(person);
        t.prepareDestroy(team);

        t.commit(objectStore);

        loadedObjects.assertAction(0, "unloaded " + person);
        
        Vector actions = objectStore.getActions();
        assertEquals(4, actions.size());
        assertEquals("destroyObject " + personOid, actions.elementAt(1));
        assertEquals("destroyObject " + team.getOid(), actions.elementAt(2));
    }

    public void testIsolatedObjectWithAssociation() {
        Integer roleOid = new Integer(3);
        String personName = "Fred";
        String roleName = "Orator";

        person.getName().setValue(personName);

        role.getName().setValue(roleName);
        role.setPerson(person);

        Role transactionRole = (Role) t.getObject(roleOid, role);
        assertTrue(transactionRole.isResolved());
        Person transactionPerson = transactionRole.getPerson();
        assertFalse(transactionPerson.isResolved());

        assertEquals(person, transactionPerson);
        assertEquals("same oid", person.getOid(), transactionPerson.getOid());
        assertFalse("but different instance", person == transactionPerson);
        assertTrue("value in associated object is empty as it not resolved yet", transactionPerson.getName().isEmpty());

        t.resolve(transactionPerson, person);
        assertEquals("Fred", transactionPerson.getName().stringValue());
        assertTrue(transactionPerson.isResolved());
    }

    public void testIsolatedObjectWithEmptyOneToManyAssociation() {
        Team transactionTeam = (Team) t.getObject(teamOid, team);
        assertTrue(transactionTeam.isResolved());
        InternalCollection transactionMembers = transactionTeam.getMembers();
        
        assertEquals(membersOid, transactionMembers.getOid());
        assertFalse(members ==transactionMembers);

        t.resolve(transactionMembers, team.getMembers());
        assertEquals(0, transactionMembers.size());
        assertTrue(transactionMembers.isResolved());
    }

    public void testIsolatedObjectWithNullReferences() {
        Oid roleOid = new MockOid(3);
        String roleName = "Orator";

        Role role = new Role();
        role.getName().setValue(roleName);
        role.setOid(roleOid);

        Role transactionRole = (Role) t.getObject(roleOid, role);
        assertTrue(transactionRole.isResolved());

        assertEquals("same oid", role.getOid(), transactionRole.getOid());
        assertFalse("but different instance", role == transactionRole);
        assertNull(transactionRole.getPerson());
    }

    public void testIsolatedObjectWithOneToManyAssociation() {
        Oid roleOid = new MockOid(3);
        Oid membersOid = new MockOid(5);
        String personName = "Fred";

        person.getName().setValue(personName);

        Team team = new Team();
        team.setOid(roleOid);
        InternalCollection members = team.getMembers();
        members.setOid(membersOid);
        team.getMembers().add(person);

        Team transactionTeam = (Team) t.getObject(roleOid, team);
        assertTrue(transactionTeam.isResolved());

        InternalCollection transactionMembers = transactionTeam.getMembers();
  //      assertFalse(transactionMembers.isResolved());

        assertEquals(membersOid, transactionMembers.getOid());
        assertFalse(members == transactionMembers);

        t.resolve(transactionMembers, team.getMembers());
        assertTrue(transactionMembers.isResolved());
        assertEquals(1, transactionMembers.size());

        Person transactionPerson = (Person) transactionMembers.elementAt(0);
        assertEquals(person, transactionPerson);
        assertEquals("same oid", person.getOid(), transactionPerson.getOid());
        assertFalse("but different instance", person == transactionPerson);
 
        t.resolve(transactionPerson, person);
        assertEquals("Fred", transactionPerson.getName().stringValue());
    }

    /*
     * getObject returns an isolated copy of the specified object
     */
    public void testIsolatedObjectWithValues() {
        person.getName().setValue("Fred");

        Person transactionPerson = (Person) t.getObject(personOid, person);
        assertTrue(transactionPerson.isResolved());

        assertEquals(person, transactionPerson);
        assertEquals("same oid", person.getOid(), transactionPerson.getOid());
        assertFalse("but different instance", person == transactionPerson);
        assertEquals("Fred", transactionPerson.getName().stringValue());
    }

    
    public void testSave() {
        NakedObject transactionPerson = t.getObject(personOid, person);
        NakedObject transactionalTeam = t.getObject(teamOid, team);
        t.prepareSave(transactionPerson);
        t.prepareSave(transactionalTeam);
        t.prepareSave(transactionPerson);
        t.prepareSave(transactionPerson);
        t.prepareSave(transactionalTeam);

       // tm.addLoaded(person);
    //   tm.addLoaded(team);
        objectStore.setupLoaded(new NakedObject[] {person, team});
        
        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals(4, actions.size());
        assertEquals("saveObject " + transactionPerson, actions.elementAt(1));
        assertEquals("saveObject " + transactionalTeam, actions.elementAt(2));
    }

    public void testSaveAndDestroy() {
        t.prepareSave(person);
        t.prepareDestroy(person);

        t.commit(objectStore);

        Vector actions = objectStore.getActions();
        assertEquals("only the destory is needed", 3, actions.size());
        assertEquals("destroyObject " + personOid, actions.elementAt(1));
    }

    
    public void testTransactionIsolation() {
        Person transactionalPerson = (Person) t.getObject(personOid, person);
        transactionalPerson.getName().setValue("Fred");
        t.prepareSave(transactionalPerson);
        
        assertFalse(person.getName() == transactionalPerson.getName());
        
        loadedObjects.setupLoadedObjects(new NakedObject[] {person});
        
        t.commit(objectStore);
        
        assertEquals(transactionalPerson.getName(), person.getName());
    }
    
    public void testTransactionIsolation2() {
        Role transactionalRole  = (Role) t.getObject(roleOid, role);
        Person transactionalPerson = (Person) t.getObject(personOid, person);
        
        transactionalRole.setPerson(transactionalPerson);
        
        t.prepareSave(transactionalRole);
        
        loadedObjects.setupLoadedObjects(new NakedObject[] {role, person});
               
        t.commit(objectStore);
        
        assertEquals(transactionalRole.getPerson(), role.getPerson());
    }
    
    public void testTransactionIsolation3() {
        Team transactionalTeam  = (Team) t.getObject(teamOid, team);
        Person transactionalPerson = (Person) t.getObject(personOid, person);
        
        transactionalTeam.getMembers().added(transactionalPerson);
        
        t.prepareSave(transactionalTeam);
        
        loadedObjects.setupLoadedObjects(new NakedObject[] {team, person});
        
        t.commit(objectStore);
        
        assertEquals(transactionalTeam.getMembers().firstElement(), team.getMembers().firstElement());
    }
    
   public void test() {     
        Role r = new Role(); // transient object
        Oid roleOid = new MockOid(11);
        r.setOid(roleOid);
        
        Person transactionPerson = (Person) t.getObject(personOid, person);
        r.setPerson(transactionPerson);	// new object being setup with ref to proxy

        t.prepareCreate(r);
        
        objectStore.setupLoaded(new NakedObject[] {person});
        
        t.commit(objectStore);
        
        assertSame("reference should be to persistent object and not the proxy", person, r.getPerson());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
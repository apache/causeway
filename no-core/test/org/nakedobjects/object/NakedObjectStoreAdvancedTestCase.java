package org.nakedobjects.object;

import org.nakedobjects.configuration.ComponentException;
import org.nakedobjects.configuration.ConfigurationException;
import org.nakedobjects.object.collection.InternalCollection;


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
        personClass = NakedObjectSpecification.getNakedClass(Person.class.getName());
        roleClass = NakedObjectSpecification.getNakedClass(Role.class.getName());

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
            roles[i].person = people[i];
            objectStore.createObject(roles[i]);
        }

        team = new Team();
        team.setContext(context);
        team.setOid(teamOid = nextOid());
        team.getMembers().setOid(membersOid = nextOid());
        
        teamMembers = new Person[names.length - 1];
        for (int i = 0; i < names.length - 1; i++) {
            teamMembers[i] = people[i];
            team.getMembers().added(teamMembers[i]);
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
        role.person = person;

        objectStore.createObject(role);
        objectStore.createObject(person);

        Oid roleOid = role.getOid();
        Oid persoOid = role.person.getOid();

        assertTrue(role.isPersistent());
        assertTrue(person.isPersistent());
        assertFalse("but the object should not be empty", person.getName().isEmpty());

        // remove and object, leaving a reference hanging
        objectStore.destroyObject(person);
        person = null;
        role = null;

        // read the object back in
        role = (Role) objectStore.getObject(roleOid, roleClass);
        person = role.getPerson();

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
        Person person = role.getPerson();

        // at this point the person could just be a skeleton object
        objectStore.resolve(person);
        // after the resolve it must be be complete

        assertEquals(people[1].getName(), person.getName());
        assertEquals(people[1].getSalary(), person.getSalary());
    }


	/*
	public void testMakeAssociatedObjectsPersistent() throws Exception{
		Role role = new Role();
		role.name.setValue("worker");
		role.person = new Person();
		
		objectManager.makePersistent(role);
		
		assertNotNull(role.getOid());
		assertNotNull(role.person.getOid());
		
		assertEquals(people.length + 1, objectStore.numberOfInstances(personPattern));
		
		Vector v = objectStore.getInstances(personPattern);
		assertEquals(people.length + 1, v.size());
		assertEquals(role.person, v.lastElement());	
	}


	public void testMakePersistentPersistsAssociation() throws Exception {
		Role role = new Role();
		role.person = new Person();
		objectManager.makePersistent(role);
		
		Role role2 = (Role) objectManager.getObject(role.getOid());
		assertEquals(role.person, role2.person);
	}

	public void testMakePersistentSetsAssociatesOid() throws Exception {
		Role role = new Role();
		role.person = new Person();
		objectManager.makePersistent(role);
		assertNotNull(role.person.getOid());
	}

	public void testMakePersistentSetsOid() throws Exception {
		Role role = new Role();
		objectManager.makePersistent(role);
		assertNotNull(role.getOid());
	}

	public void testObjectLifecyle() throws ObjectStoreException {
		Role role = new Role();
		role.getName().setValue("Leader");
		assertFalse("Object is not persistent", role.isPersistent());
		assertFalse("Object has no oid", role.getOid() != null);
		assertFalse("Object is not resolved", role.isResolved());
	
		Person person = new Person();
		role.person = person;
		person.getName().setValue("Billy");
	
		objectStore.createObject(role);
	
		Object oid1 = role.getOid();
		//Object oid2 = e1.getMulitpleAssociations().getOid();
	
		assertTrue("Object is now persistent", role.isPersistent());
		assertTrue("Object now has an oid", role.getOid() != null);
		assertTrue("Object now is resolved", role.isResolved());
		assertTrue("Associated object is also persistent", person.isPersistent());
		assertTrue("Associated object also has an oid", person.getOid() != null);
		assertTrue("Associated object is also resolved", person.isResolved());
	
		assertTrue("cached object must be same object wherever used", role == objectStore.getObject(oid1));
	
		// retrieve from storage
		Role read1 = (Role) objectStore.getObject(oid1);
	
		assertEquals("read object is same", role, read1);
		assertEquals("value fields are the same", role.name, read1.name);
		assertEquals("object fields are the same", role.person, read1.person);
	
		assertTrue("after get, field should be resolved", read1.getPerson().isResolved());
		assertEquals("referenced objects can now be used", role.person.getName(), read1.person.getName());
	
		// change object
		read1.person = null;
		updateNotifier.clearExpected();
		updateNotifier.addExpectedBroadcastObject(role);
		objectStore.save(read1);
		updateNotifier.verify();
		assertNull(read1.getPerson());
	
		Role read2 = (Role) objectStore.getObject(oid1);
		assertNull(read2.getPerson());
	
		read2 = (Role) objectStore.getObject(oid1);
	
		objectStore.destroyObject(oid1);
	
		try {
			objectStore.getObject(oid1);
			fail("Object should not be available after destroy");
		} catch (ObjectNotFoundException e) {
			;
		}
	
		updateNotifier.verify();
	}


	public void testObjectLifecyle2() throws ObjectStoreException {
		Role e1 = new Role();
		assertFalse("Object is not persistent", e1.isPersistent());
		assertFalse("Object has no oid", e1.getOid() != null);
		assertFalse("Object is not resolved", e1.isResolved());
	
		objectStore.createObject(e1);
	
		Object oid1 = e1.getOid();
		//Object oid2 = e1.getMulitpleAssociations().getOid();
	
		assertTrue("Object is now persistent", e1.isPersistent());
		assertTrue("Object now has an oid", e1.getOid() != null);
		assertTrue("Object now is resolved", e1.isResolved());
		
		
		Person ae1 = new Person();
		ae1.getName().setValue("Billy");
	
		objectStore.createObject(ae1);
		
		assertTrue("Associated object is also persistent", ae1.isPersistent());
		assertTrue("Associated object also has an oid", ae1.getOid() != null);
		assertTrue("Associated object is also resolved", ae1.isResolved());
	
		assertTrue("cached object must be same object wherever used", e1 == objectStore.getObject(oid1));
	
		e1.getName().setValue("Bart");
		e1.person = ae1;
		
		objectStore.save(e1);
		
		// retrieve from storage
		Role read1 = (Role) objectStore.getObject(oid1);
	
		assertEquals("read object is same", e1, read1);
		assertEquals("value fields are the same", e1.name, read1.name);
		assertEquals("object fields are the same", e1.person, read1.person);
	
		assertTrue("after get, field should be resolved", read1.getPerson().isResolved());
		assertEquals("referenced objects can now be used", e1.person.getName(), read1.person.getName());
	
		// change object
		read1.person = null;
		updateNotifier.clearExpected();
		updateNotifier.addExpectedBroadcastObject(e1);
		objectStore.save(read1);
		updateNotifier.verify();
		assertNull(read1.getPerson());
	
		Role read2 = (Role) objectStore.getObject(oid1);
		assertNull(read2.getPerson());
	
		read2 = (Role) objectStore.getObject(oid1);
	
		objectStore.destroyObject(oid1);
	
		try {
			objectStore.getObject(oid1);
			fail("Object should not be available after destroy");
		} catch (ObjectNotFoundException e) {
			;
		}
	
		updateNotifier.verify();
	}
	
	
	public void testRecursion() throws Exception {
		Node base = new Node();
		Node level1 = new Node();
		Node level2 = new Node();
		Node level3 = new Node();

		base.getParentNodes().add(level1);
		level1.getParentNodes().add(level2);
		level2.getParentNodes().add(level3);

		objectStore.makePersistent(base);

		assertTrue("base object should be persistent", base.isPersistent());
		assertTrue("base object's parents collection should be persistent", base.parents.isPersistent());
		assertTrue("base object's children collection should be persistent", base.children.isPersistent());
		assertTrue("contained object #1 should be persistent", level1.isPersistent());
		assertTrue("contained object #2 should be persistent", level2.isPersistent());
		assertTrue("contained object #3 should be persistent", level3.isPersistent());
		
		objectStore.restart();

		InstanceCollection ic = new InstanceCollection(Node.class);
		ic.first();

		// read the object back in
		//		BasicExample2 readObject = (BasicExample2) objectStore.getObject(headOid);
		//		MultipleAssociationExample readObject = (MultipleAssociationExample)
		// objectStore.getObject(headOid);
		//    	readObject.resolve();
	}

*/
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
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
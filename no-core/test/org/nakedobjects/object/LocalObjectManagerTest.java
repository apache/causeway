package org.nakedobjects.object;

import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public final class LocalObjectManagerTest extends NakedObjectTestCase {
	private static NakedObjectManager objectManager;
	private static MockObjectStore objectStore;
	
	public LocalObjectManagerTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
	    LogManager.getLoggerRepository().setThreshold(Level.OFF);

		try {
            objectManager = NakedObjectManager.getInstance();
        } catch (IllegalStateException alreadySetUp) {
			MockUpdateNotifier updateNotifier = new MockUpdateNotifier();
			objectStore = new MockObjectStore();
			objectManager = new LocalObjectManager(objectStore, updateNotifier);
            objectManager.init();
        }
		super.setUp();
	}

	public void testGetInstances() throws Exception {
	    NakedClass nc = new NakedClass();
	    Vector v = new Vector();
        objectStore.setupInstances(v, nc);
		assertSame(v, objectManager.getInstances(nc));
	}

	public void testGetObjectRepeatability() throws ObjectStoreException {
	    NakedClass nc = new NakedClass();
	    Object oid = new Integer(1);
		
	    objectStore.setupIsLoaded(false);
	    Person person = new Person();
	    person.setOid(oid);
        objectStore.setupGetObject(person);
	    assertSame(person, objectManager.getObject(oid, nc));
	    
	    objectStore.setupIsLoaded(true);
	    objectStore.setupGetObject(null);
	    objectStore.setupLoaded(new NakedObject[] {person});
	    objectStore.setupGetObject(person);
	}

	public void testHasInstances() throws Exception {
	    NakedClass nc = new NakedClass();
	    
	    objectStore.setupHasInstances(false);
		assertFalse(objectManager.hasInstances(nc));
	    objectStore.setupHasInstances(true);
		assertTrue(objectManager.hasInstances(nc));

	}
	
	public void testInstancesCount() throws Exception {
	    NakedClass nc = new NakedClass();
		  		
		objectStore.setupInstancesCount(0);
		assertEquals(0, objectManager.numberOfInstances(nc));
		
		objectStore.setupInstancesCount(5);
		assertEquals(5, objectManager.numberOfInstances(nc));
	}

	/*
	  
	 public void testMakePersistentPersistsValue() throws Exception {
		Role role = new Role();

		objectManager.makePersistent(role);
		
		Vector actions = objectStore.getActions();
		assertEquals(5, actions.size());
		assertEquals("createObject " + role, actions.elementAt(4));
	
		assertNotNull(role.getOid());
		assertTrue(role.isPersistent());
		assertFalse(role.isFinder());
		assertTrue(role.isResolved());
	}

	/**
	 * To test the serial number we need to ensure that for each call the serial number is
	 * different to the previous one and that it comes after the previous one.
	 */
/*	public void testSerialNumbers() throws ObjectStoreException {
	    Vector v = new Vector();
	    v.addElement(new Sequence());
	    objectStore.setupInstances(v);
	    objectStore.setupNakedClass(Sequence.class);
	    assertEquals(0, objectManager.serialNumber("new"));
	    
	    Sequence serialNumber = new Sequence();
	    serialNumber.getName().setValue("persisted");
		serialNumber.getSerialNumber().setValue(26);
//	    objectStore.addInstance(serialNumber);
	   
		for (int i = 27; i < 31; i++) {
			long current = objectManager.serialNumber("persisted");

			assertTrue("repeated request should give another number " + i, current == i);
		}
	}
	
	  public void testSequences() throws Exception {
	        MockObjectStore store = new MockObjectStore();
	        LocalObjectManager manager = new LocalObjectManager(store,  null);
	        store.setupNakedClass(NakedClass.createNakedClass(Sequence.class.getName(), JavaReflector.class.getName()));
	        
	        assertEquals(0, manager.serialNumber("one"));
	        assertEquals(1, manager.serialNumber("one"));
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
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedObjectSpecificationImpl;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.collection.ArbitraryCollectionVector;
import org.nakedobjects.object.defaults.value.TestClock;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class NakedCollectionTests extends TestCase {
	private final int SIZE = 79;
	private Role elements[];
    private MockObjectManager manager;
    private CollectionIterator ac;
    
	public NakedCollectionTests(String name) {
		super(name);
	}

	protected void setUp() {
		LogManager.getLoggerRepository().setThreshold(Level.ERROR);
		PropertyConfigurator.configure("log4j.testing.properties");
	
		new TestClock();
        NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());

        manager = MockObjectManager.setup();
    
        ArbitraryCollectionVector collection;
        collection = new ArbitraryCollectionVector();
        collection.setContext(manager.getContext());
		
		elements = new Role[SIZE];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new Role();
			elements[i].getName().setValue("Element " + i);
			assertTrue(!collection.contains(elements[i]));
			collection.add(elements[i]);
			assertTrue(collection.contains(elements[i]));
		}
		
	    ac = new CollectionIterator(collection);
	}
	
    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

	public static void main(String[] args) {
		junit.textui.TestRunner.run(NakedCollectionTests.class);
	}

	public void testGetWindowSize() throws ObjectStoreException {
		assertEquals(12, ac.getDisplaySize());
		ac.setDisplaySize(10);
		assertEquals(10, ac.getDisplaySize());
		assertEquals(10, ac.getDisplaySize());
	}

	public void testDisplayElements() throws ObjectStoreException {
		displayElements();
	}
	
	public void displayElements() {
		Enumeration data = ac.displayElements();
		int i = 0;
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals("element " + i, elements[i++], element);
		}
		assertEquals(ac.getDisplaySize(), i);
	}
		
	public void testFirst() throws ObjectStoreException {
		first();
	}
	
	public void first()  {
		ac.first();
		assertEquals(0, ac.getStartWindowAt());
		Enumeration data = ac.displayElements();
		int i = 0;
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		assertEquals(ac.getDisplaySize(), i);
	}

	public void testHasNext() throws ObjectStoreException {
		ac.first();
		assertTrue(ac.hasNext());
		// using persistence
		ac.first();
		assertTrue(ac.hasNext());
		assertTrue(!ac.hasPrevious());
	}

	public void testHasPrevious() throws ObjectStoreException {
		ac.last();
		assertTrue(ac.hasPrevious());
		// using persistence
		ac.last();
		assertTrue(ac.hasPrevious());
		assertTrue(!ac.hasNext());
	}

	public void testLast() throws ObjectStoreException {
		last();
		// using persistence
		last();
	}

	private void last() {
		ac.last();
		Enumeration data = ac.displayElements();
		int i = SIZE - ac.getDisplaySize();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		assertEquals(SIZE, i);
	}
	
	public void testNext() throws ObjectStoreException {
		next();
		// using persistence
		next();
	}
	
	private void next() {
		ac.first();
		ac.next();
		Enumeration data = ac.displayElements();
		int i = ac.getDisplaySize();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		ac.next();
		data = ac.displayElements();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		ac.next();
		data = ac.displayElements();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
	}

	public void testPrevious() throws ObjectStoreException {
		previous();
		previous();	
	}
	
	private void previous() {
		ac.last();
		ac.previous();
		Enumeration data = ac.displayElements();
		int i = SIZE - 2 * ac.getDisplaySize();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals("element " + i, elements[i++], element);
		}
		ac.previous();
		data = ac.displayElements();
		i  = i - 2 * ac.getDisplaySize();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		ac.previous();
		data = ac.displayElements();
		i  = i - 2 * ac.getDisplaySize();
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2003  Naked Objects Group Ltd

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

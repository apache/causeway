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

package org.nakedobjects.object.collection;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;

public class NakedCollectionTests extends TestCase {
	private ArbitraryCollection ac;
	private final int SIZE = 79;
	private Role elements[];
	
	public NakedCollectionTests(String name) {
		super(name);
	}

	public void setUp() {
		LogManager.getLoggerRepository().setThreshold(Level.ERROR);
		PropertyConfigurator.configure("log4j.testing.properties");
	
		MockObjectManager.setup();
		
		ac = new ArbitraryCollection();
		
		elements = new Role[SIZE];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new Role();
			elements[i].getName().setValue("Element " + i);
			assertTrue(!ac.contains(elements[i]));
			ac.add(elements[i]);
			assertTrue(ac.contains(elements[i]));
		}
	}

	public void testAddTwice() {
		ac.add(elements[1]);
		assertEquals(SIZE, ac.size());
	}
	
	public static void main(String[] args) {
		junit.textui.TestRunner.run(NakedCollectionTests.class);
	}

	public void testTitle() {
		assertEquals("", ac.title().toString());
	}

	public void testGetWindowSize() throws ObjectStoreException {
		assertEquals(12, ac.getDisplaySize());
		ac.setDisplaySize(10);
		assertEquals(10, ac.getDisplaySize());
		ac.makePersistent();
		assertEquals(10, ac.getDisplaySize());
	}

	public void testDisplayElements() throws ObjectStoreException {
		displayElements();
		ac.makePersistent();
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

	public void testElements() throws ObjectStoreException {
		elements();
		ac.makePersistent();
		elements();
	}
		
	public void elements() {
		Enumeration data = ac.elements();
		int i = 0;
		while (data.hasMoreElements()) {
			Role element = (Role) data.nextElement();
			assertEquals(elements[i++], element);
		}
		assertEquals("all elements retrieved", SIZE, i);
	}

	public void testFirst() throws ObjectStoreException {
		first();
		ac.makePersistent();
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
		ac.makePersistent();
		ac.first();
		assertTrue(ac.hasNext());
		assertTrue(!ac.hasPrevious());
	}

	public void testHasPrevious() throws ObjectStoreException {
		ac.last();
		assertTrue(ac.hasPrevious());
		// using persistence
		ac.makePersistent();
		ac.last();
		assertTrue(ac.hasPrevious());
		assertTrue(!ac.hasNext());
	}

	public void testLast() throws ObjectStoreException {
		last();
		// using persistence
		ac.makePersistent();
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
		ac.makePersistent();
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
		ac.makePersistent();
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

	public void testRemove() {
		for (int i = 0; i < SIZE; i++) {
			ac.remove(elements[i]);
		}
		assertEquals(0, ac.size());
	}

	public void testPersistentRemove() throws ObjectStoreException {
		ac.makePersistent();
		for (int i = 0; i < SIZE; i++) {
			ac.remove(elements[i]);
		}
		assertEquals(0, ac.size());
	}

	public void testReset() {
	}

	public void testSize() throws ObjectStoreException {
		assertEquals(SIZE, ac.size())	;
		ac.makePersistent();
		assertEquals("persistent", SIZE, ac.size())	;
	}
	
	public void testIsEmpty() {
		for (int i = 0; i < SIZE; i++) {
			ac.remove(elements[i]);
		}
		assertTrue(ac.isEmpty());
	}

	public void testPersistentIsEmpty() throws ObjectStoreException {
		ac.makePersistent();
		for (int i = 0; i < SIZE; i++) {
			ac.remove(elements[i]);
		}
		assertTrue(ac.isEmpty());
	}
}

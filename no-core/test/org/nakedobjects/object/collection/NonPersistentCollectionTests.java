package org.nakedobjects.object.collection;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.nakedobjects.object.EmptyExample;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;



public class NonPersistentCollectionTests extends TestCase {
 private MockObjectManager manager;

//   private static NakedObjectStore objectStore;

    public NonPersistentCollectionTests(String name) {
        super(name);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(NonPersistentCollectionTests.class));
    }

    public void setUp() {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
        
        manager = MockObjectManager.setup();
    }

    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }


    /**
     *
     */
    public void testElements() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();
        Role[] e = setupCollection(collection, 200);
        Enumeration enum = collection.elements();

        int i = 0;

        while (enum.hasMoreElements()) {
            assertEquals(e[i], enum.nextElement());
            i++;
        }

        assertEquals(200, i);
    }

    public void testElements2() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();
        Role[] e = setupCollection(collection, 2);
        Enumeration enum = collection.elements();

        int i = 0;

        while (enum.hasMoreElements()) {
            assertEquals(e[i], enum.nextElement());
            i++;
        }

        assertEquals(2, i);
    }

    public void testHasNext() throws ObjectStoreException {
        AbstractNakedCollection ac = new ArbitraryCollection();

        setupCollection(ac, 26);
        ac.first();
        assertTrue(ac.hasNext());
        assertTrue(!ac.hasPrevious());
    }

    public void testHasPrevious() throws ObjectStoreException {
        AbstractNakedCollection ac = new ArbitraryCollection();

        setupCollection(ac, 26);
        ac.last();
        assertTrue(ac.hasPrevious());
        assertTrue(!ac.hasNext());
    }

    public void testDisplayOfInternalCollection() throws ObjectStoreException {
        Team m = new Team();

        AbstractNakedCollection collection = m.getMembers();

        Person[] v = new Person[4];

        for (int i = 0; i < v.length; i++) {
            collection.add(v[i] = new Person());
        }

        assertEquals(4, collection.size());

        // now check the cache - moving forward through it
        collection.first();
        checkDisplayElements(collection, v, 0);
        collection.next();
        checkDisplayElements(collection, v, 0);

        // try and add another type
        collection.add(new Role());
        collection.add(new EmptyExample());

        assertEquals("Size should not grow", 4, collection.size());
    }

    /**
     *
     */
    public void testDisplayOfLargeCollection() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 33);

        // now check the cache - moving forward through it
        collection.setDisplaySize(5);
        collection.first();
        checkDisplayElements(collection, e, 0);
        assertTrue("..5", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 5);
        assertTrue("..10", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 10);
        assertTrue("..15", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 15);
        assertTrue("..20", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 20);
        assertTrue("..25", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 25);
        assertTrue("..last", collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 28);
        assertTrue("..last again", !collection.hasNext());
        collection.next();
        checkDisplayElements(collection, e, 28);

        // moving backward
        collection.previous();
        assertTrue("..23", collection.hasPrevious());
        checkDisplayElements(collection, e, 23);
        assertTrue("..18", collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 18);
        assertTrue("..13", collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 13);
        assertTrue("..8", collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 8);
        assertTrue("..3", collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 3);
        assertTrue("..first", collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 0);
        assertTrue("..first again", !collection.hasPrevious());
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testDisplayOfLargeCollection2() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 26);

        // now check the cache - moving forward through it
        collection.setDisplaySize(7);
        collection.first();
        checkDisplayElements(collection, e, 0);
        collection.next();
        checkDisplayElements(collection, e, 7);
        collection.next();
        checkDisplayElements(collection, e, 14);
        collection.next();
        checkDisplayElements(collection, e, 19); // 14 + 7 > 26 -> 26 -7 = 19
        collection.next();
        checkDisplayElements(collection, e, 19);

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 12); // 19 -7 = 12
        collection.previous();
        checkDisplayElements(collection, e, 5);
        collection.previous();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testDisplayOfLargeCollection3() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 256);

        // now check the cache - moving forward through it
        collection.setDisplaySize(7);
        collection.first();
        checkDisplayElements(collection, e, 0);
        collection.next();
        checkDisplayElements(collection, e, 7);
        collection.next();
        checkDisplayElements(collection, e, 14);
        collection.last();
        checkDisplayElements(collection, e, 256 - 7);

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 256 - 7 - 7);
        collection.previous();
        checkDisplayElements(collection, e, 256 - 7 - 7 - 7);
        collection.first();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testDisplayOfLargeCollection4() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 79);

        // now check the cache - moving forward through it
        collection.first();
        checkDisplayElements(collection, e, 0);
        collection.next();
        checkDisplayElements(collection, e, 12);
        collection.next();
        checkDisplayElements(collection, e, 24);
        collection.last();
        checkDisplayElements(collection, e, 79 - 12);
        collection.next();
        checkDisplayElements(collection, e, 79 - 12);

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 79 - 12 - 12);
        collection.previous();
        checkDisplayElements(collection, e, 79 - 12 - 12 - 12);
        collection.first();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testLast() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 26);
        collection.setDisplaySize(5);
        collection.last();
        checkDisplayElements(collection, e, 21);
    }

    public void testRemove() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        collection.setDisplaySize(5);
        Role[] e = setupCollection(collection, 26);

        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }
    }

    public void testReset() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        setupCollection(collection, 33);

        assertEquals(33, collection.size());
        collection.reset();
        assertEquals(0, collection.size());
    }

    public void testGetWindowSize() throws ObjectStoreException {
        AbstractNakedCollection ac = new ArbitraryCollection();
        assertEquals(12, ac.getDisplaySize());
        ac.setDisplaySize(10);
        assertEquals(10, ac.getDisplaySize());
        ac.makePersistent();
        assertEquals(10, ac.getDisplaySize());
    }

    public void testSize() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();
        setupCollection(collection, 26);
        assertEquals("26 elements added", 26, collection.size());
    }

    public void testTitle() {
        AbstractNakedCollection ac = new ArbitraryCollection();

        assertEquals("", ac.title().toString());
    }

    public void testDisplayOfSmallCollection() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 8);

        // now check the cache - moving forward through it
        collection.setDisplaySize(5);
        collection.first();
        checkDisplayElements(collection, e, 0);
        collection.next();
        checkDisplayElements(collection, e, 3);
        collection.next();
        checkDisplayElements(collection, e, 3);

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    private void checkDisplayElements(AbstractNakedCollection collection, Object[] e, int empStart) {
        Enumeration elements = collection.displayElements();

        assertTrue("No next display elements (start=" + empStart + ")", elements.hasMoreElements());

        for (int i = 0; i < collection.getDisplaySize(); i++) {
            if (!elements.hasMoreElements()) {
                return;
            }

            int empNo = i + empStart;

            assertEquals("Next BasicExample " + empNo, e[empNo], elements.nextElement());
        }

        assertTrue("Next : Too many elements (start=" + empStart + ")", !elements.hasMoreElements());
    }

    /**
     *
     */
    private Role[] setupCollection(AbstractNakedCollection collection, int size)
        throws ObjectStoreException {
        Role[] e = new Role[size];

        for (int i = 0; i < size; i++) {
            e[i] = new Role();
            e[i].created();
            e[i].getName().setValue("A" + i);
            collection.add(e[i]);
            assertTrue("Find added object " + e[i], collection.contains(e[i]));
        }

        assertEquals(size, collection.size());

        return e;
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

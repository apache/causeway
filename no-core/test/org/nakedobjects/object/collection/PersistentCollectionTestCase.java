package org.nakedobjects.object.collection;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.nakedobjects.object.AssociationExample;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;

import com.mockobjects.ExpectationSet;




public final class PersistentCollectionTestCase extends TestCase {
    private MockObjectManager objectManager;

//    private static NakedObjectStore objectStore;

    public PersistentCollectionTestCase(String name) {
        super(name);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(PersistentCollectionTestCase.class));
    }

    public void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
         objectManager = MockObjectManager.setup();
    }

    protected void tearDown() throws Exception {
        objectManager.shutdown();
        super.tearDown();
    }
    
    public void testHasNext() throws ObjectStoreException {
        AbstractNakedCollection ac = new ArbitraryCollection();

        setupCollection(ac, 26);
        ac.first();
        assertTrue(ac.hasNext());
        ac.first();
        assertTrue(ac.hasNext());
        assertTrue(!ac.hasPrevious());
    }

    public void testHasPrevious() throws ObjectStoreException {
        AbstractNakedCollection ac = new ArbitraryCollection();

        setupCollection(ac, 26);
        ac.last();
        assertTrue(ac.hasPrevious());
        ac.last();
        assertTrue(ac.hasPrevious());
        assertTrue(!ac.hasNext());
    }

    public void testCachingWithDefaultSizes() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 26);

        // now check the cache - moving forward through it
        collection.first();
        checkDisplayElements(collection, e, 0); // first
        collection.next();
        checkDisplayElements(collection, e, 12); // + 12
        collection.next();
        checkDisplayElements(collection, e, 14); // + 12 > 26 -> 26 - 12 = 14
        collection.next();
        checkDisplayElements(collection, e, 14); // 14...25

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 2); // 2..13
        collection.previous();
        checkDisplayElements(collection, e, 0); // 0..11
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testCachingWithDefaultSizes2() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 11);

        // now check the cache - moving forward through it
        collection.first();
        checkDisplayElements(collection, e, 0); // first
        collection.next();
        checkDisplayElements(collection, e, 0); // + 12 > 26

        // moving backward
        collection.previous();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
        collection.previous();
        checkDisplayElements(collection, e, 0);
    }

    /**
     *
     */
    public void testElementsWithCaching() throws ObjectStoreException {
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

    public void testElementsWithCaching2() throws ObjectStoreException {
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

    /**
     *
     */
    public void testInternalCollection() throws ObjectStoreException {
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
        collection.add(new AssociationExample());
        collection.add(new Role());

        assertEquals("Size should be the same as before", 4, collection.size());
    }

    /**
     *
     */
    public void testCachingWithLargeCollection() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 33);

        collection.setDisplaySize(5);

        // now check the cache - moving forward through it
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
    public void testCachingWithLargeCollection2() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        collection.setDisplaySize(7);
        Role[] e = setupCollection(collection, 26);

        // now check the cache - moving forward through it
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
    public void testCachingWithLargeCollection3() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 256);

        collection.setDisplaySize(7);

        // now check the cache - moving forward through it
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
    public void testCachingWithLargeCollection4() throws ObjectStoreException {
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

        // now check the cache - moving forward through it
        collection.last();
        checkDisplayElements(collection, e, 21);
    }

    public void testRemove() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        collection.setDisplaySize(5);
        //      collection.setMaxCacheSize(14);
        Role[] e = setupCollection(collection, 26);

        //
        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }
    }

    public void testRemovePersistent() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        collection.setDisplaySize(5);

        Role[] e = setupCollection(collection, 26);

        ExpectationSet set = new ExpectationSet("remove");

        for (int i = 0; i < 5; i++) {
            set.addExpected(e[i]);
        }

        for (int i = 11; i < e.length; i++) {
            set.addExpected(e[i]);
        }

        //
        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }

        set.addActualMany(collection.elements());
        set.verify();
    }

    /**
     *
     */
    public void testSize() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        setupCollection(collection, 26);

        collection.setDisplaySize(5);
 
        assertEquals("26 elements added", 26, collection.size());
    }

    /**
     *
     */
    public void testSmallCollection() throws ObjectStoreException {
        AbstractNakedCollection collection = new ArbitraryCollection();

        Role[] e = setupCollection(collection, 8);

        collection.setDisplaySize(5);

        // now check the cache - moving forward through it
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

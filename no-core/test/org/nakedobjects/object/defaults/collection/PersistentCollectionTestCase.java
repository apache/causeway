package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.ArbitraryNakedCollection;
import org.nakedobjects.object.AssociationExample;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Person;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.Team;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.collection.ArbitraryCollectionVector;
import org.nakedobjects.object.defaults.value.TestClock;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;

import com.mockobjects.ExpectationSet;




public final class PersistentCollectionTestCase extends TestCase {
    private MockObjectManager objectManager;
    private NakedObjectContext context;

    public PersistentCollectionTestCase(String name) {
        super(name);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(PersistentCollectionTestCase.class));
    }

    protected void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
         objectManager = MockObjectManager.setup();
         context = objectManager.getContext();
         new TestClock();
         NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
    }

    protected void tearDown() throws Exception {
        objectManager.shutdown();
        super.tearDown();
    }
    
    /**
     *
     */
    public void testElementsWithCaching() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();
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
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();
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
        m.setContext(context);

        InternalCollection collection = m.getMembers();

        Person[] v = new Person[4];

        for (int i = 0; i < v.length; i++) {
            collection.add(v[i] = new Person());
        }

        assertEquals(4, collection.size());

        // try and add another type
        collection.add(new AssociationExample());
        collection.add(new Role());

        assertEquals("Size should be the same as before", 4, collection.size());
    }

    public void testRemove() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        Role[] e = setupCollection(collection, 26);

        //
        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }
    }

    public void testRemovePersistent() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

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
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        setupCollection(collection, 26);

        assertEquals("26 elements added", 26, collection.size());
    }

    /**
     *
     */
    public void testSmallCollection() throws ObjectStoreException {
        ArbitraryNakedCollection collection = new ArbitraryCollectionVector();

        Role[] e = setupCollection(collection, 8);
    }

    /**
     *
     */
    private Role[] setupCollection(ArbitraryNakedCollection collection, int size)
        throws ObjectStoreException {
        collection.setContext(context);
        
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

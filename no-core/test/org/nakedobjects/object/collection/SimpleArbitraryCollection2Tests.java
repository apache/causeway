package org.nakedobjects.object.collection;

import org.nakedobjects.object.LocalReflectionFactory;
import org.nakedobjects.object.MockObjectManager;
import org.nakedobjects.object.NakedObjectContext;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.value.TestClock;

import java.util.Enumeration;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class SimpleArbitraryCollection2Tests extends TestCase {

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(SimpleArbitraryCollection2Tests.class));
    }

    private NakedObjectContext context;
    private MockObjectManager manager;

    public SimpleArbitraryCollection2Tests(String name) {
        super(name);
    }

    protected void setUp() {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        manager = MockObjectManager.setup();
        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());
        context = manager.getContext();

        new TestClock();
    }

    /**
     *  
     */
    private Role[] setupCollection(AbstractVectorCollection collection, int size) throws ObjectStoreException {
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

    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    /**
     *  
     */
    public void testElements() throws ObjectStoreException {
        AbstractVectorCollection collection = new SimpleArbitraryCollection();
        collection.setContext(context);
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
        AbstractVectorCollection collection = new SimpleArbitraryCollection();
        collection.setContext(context);
        Role[] e = setupCollection(collection, 2);
        Enumeration enum = collection.elements();

        int i = 0;

        while (enum.hasMoreElements()) {
            assertEquals(e[i], enum.nextElement());
            i++;
        }

        assertEquals(2, i);
    }

    public void testRemove() throws ObjectStoreException {
        AbstractVectorCollection collection = new SimpleArbitraryCollection();

        Role[] e = setupCollection(collection, 26);

        for (int i = 5; i < 11; i++) {
            collection.remove(e[i]);
            assertTrue("removed BasicExample " + i, !collection.contains(e[i]));
        }
    }

    public void testSize() throws ObjectStoreException {
        AbstractVectorCollection collection = new SimpleArbitraryCollection();
        setupCollection(collection, 26);
        assertEquals("26 elements added", 26, collection.size());
    }

    public void testTitle() {
        AbstractVectorCollection ac = new SimpleArbitraryCollection("Objects");

        assertEquals("Objects", ac.titleString());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2003 Naked Objects Group
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

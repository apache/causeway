package org.nakedobjects.object.defaults.collection;

import org.nakedobjects.object.NakedObjectSpecificationImpl;
import org.nakedobjects.object.NakedObjectSpecificationLoaderImpl;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.value.TestClock;
import org.nakedobjects.object.reflect.defaults.JavaReflectorFactory;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;


public class SimpleArbitraryCollectionTests extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(SimpleArbitraryCollectionTests.class);
    }

    private ArbitraryCollectionVector ac;
    private Role elements[];
    private MockObjectManager manager;
    private final int SIZE = 79;

    public SimpleArbitraryCollectionTests(String name) {
        super(name);
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

    protected void setUp() {
        LogManager.getLoggerRepository().setThreshold(Level.ERROR);
        PropertyConfigurator.configure("log4j.testing.properties");

        new TestClock();
    	new NakedObjectSpecificationLoaderImpl();
    	NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
    	NakedObjectSpecificationImpl.setReflectorFactory(new JavaReflectorFactory());

        manager = MockObjectManager.setup();

        ac = new ArbitraryCollectionVector("Objects");
        ac.setContext(manager.getContext());

        elements = new Role[SIZE];
        for (int i = 0; i < elements.length; i++) {
            elements[i] = new Role();
            elements[i].getName().setValue("Element " + i);
            assertTrue(!ac.contains(elements[i]));
            ac.add(elements[i]);
            assertTrue(ac.contains(elements[i]));
        }
    }

    protected void tearDown() throws Exception {
        manager.shutdown();
        super.tearDown();
    }

    public void testAddTwice() {
        ac.add(elements[1]);
        assertEquals(SIZE, ac.size());
    }

    public void testElements() throws ObjectStoreException {
        elements();
        ac.makePersistent();
        elements();
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

    public void testPersistentRemove() throws ObjectStoreException {
        ac.makePersistent();
        for (int i = 0; i < SIZE; i++) {
            ac.remove(elements[i]);
        }
        assertEquals(0, ac.size());
    }

    public void testRemove() {
        for (int i = 0; i < SIZE; i++) {
            ac.remove(elements[i]);
        }
        assertEquals(0, ac.size());
    }

    public void testReset() {}

    public void testSize() throws ObjectStoreException {
        assertEquals(SIZE, ac.size());
        ac.makePersistent();
        assertEquals("persistent", SIZE, ac.size());
    }

    public void testTitle() {
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

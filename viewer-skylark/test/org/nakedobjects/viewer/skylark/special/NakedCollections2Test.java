package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.Role;
import org.nakedobjects.object.defaults.LocalReflectionFactory;
import org.nakedobjects.object.defaults.MockObjectManager;
import org.nakedobjects.object.defaults.NakedObjectSpecificationImpl;
import org.nakedobjects.object.defaults.collection.ArbitraryCollectionVector;
import org.nakedobjects.object.system.TestClock;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;



public final class NakedCollections2Test extends TestCase {
    private MockObjectManager objectManager;
    private CollectionIterator ac;

    public NakedCollections2Test(String name) {
        super(name);
    }

    public static void main(java.lang.String[] args) {
        TestRunner.run(new TestSuite(NakedCollections2Test.class));
    }

    protected void setUp() throws ObjectStoreException {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);
         objectManager = MockObjectManager.setup();
         objectManager.getContext();
         new TestClock();
         NakedObjectSpecificationImpl.setReflectionFactory(new LocalReflectionFactory());
         
         ArbitraryCollectionVector collection = new ArbitraryCollectionVector();
         int size = 26;
         Role[] e = new Role[size];
         for (int i = 0; i < size; i++) {
             e[i] = new Role();
             e[i].created();
             e[i].getName().setValue("A" + i);
             collection.add(e[i]);
             assertTrue("Find added object " + e[i], collection.contains(e[i]));
         }
         ac = new CollectionIterator(collection);
    }

    protected void tearDown() throws Exception {
        objectManager.shutdown();
        super.tearDown();
    }
    
    public void testHasNext() throws ObjectStoreException {
        ac.first();
        assertTrue(ac.hasNext());
        ac.first();
        assertTrue(ac.hasNext());
        assertFalse(ac.hasPrevious());
    }

    public void testHasPrevious() throws ObjectStoreException {
        ac.last();
        assertTrue(ac.hasPrevious());
        ac.last();
        assertTrue(ac.hasPrevious());
        assertFalse(ac.hasNext());
    }

    /**
     *
     */
    public void testLast() throws ObjectStoreException {
        ac.setDisplaySize(5);

        // now check the cache - moving forward through it
        ac.last();
        assertTrue(ac.hasPrevious());
        assertFalse(ac.hasNext());
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

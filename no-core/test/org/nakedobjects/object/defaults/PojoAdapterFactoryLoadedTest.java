package org.nakedobjects.object.defaults;

import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.defaults.DummyOid;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.reflect.PojoAdapterFactoryImpl;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public class PojoAdapterFactoryLoadedTest extends TestCase {
    private Cache loaded;
    private DummyOid oid1;
    private DummyNakedObject object1;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(PojoAdapterFactoryLoadedTest.class);
    }

    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        loaded = new Cache();

        loaded.setup(new DummyOid(), new DummyNakedObject());

        oid1 = new DummyOid();
        object1 = new DummyNakedObject();
        object1.setOid(oid1);

        loaded.setup(oid1, object1);

        loaded.setup(new DummyOid(), new DummyNakedObject());
        loaded.setup(new DummyOid(), new DummyNakedObject());
    }

    public void testGetObjectByOid() {
        assertEquals(object1, loaded.getLoadedObject(oid1));
    }

    public void testObjectIsLoadedForOid() {
        assertTrue(loaded.isIdentityKnown(oid1));
    }

    public void testOidHasNoAssociatedLoadedObject() {
        assertFalse(loaded.isIdentityKnown(new DummyOid()));
    }

    public void testObjectIsAddedToCache() {
        MockNakedObject mockNakedObject = new MockNakedObject();
        DummyOid oid = new DummyOid();
        mockNakedObject.setOid(oid);
        loaded.loaded(mockNakedObject);

        assertEquals(mockNakedObject, loaded.get(oid));
        assertEquals(5, loaded.size());
    }

    public void testObjectCannotBeAddedToCacheTwice() {
        MockNakedObject mockNakedObject = new MockNakedObject();
        mockNakedObject.setOid(new DummyOid());

        loaded.loaded(mockNakedObject);
        try {
            loaded.loaded(mockNakedObject);
            fail();
        } catch (NakedObjectRuntimeException expected) {}

    }

    public void testObjectCannotBeLoadedToCacheWithDifferentOid() {
        MockNakedObject mockNakedObject = new MockNakedObject();
        mockNakedObject.setOid(new DummyOid());

        loaded.loaded(mockNakedObject);

        mockNakedObject.setOid(new DummyOid());
        try {
            loaded.loaded(mockNakedObject);
            fail();
        } catch (NakedObjectRuntimeException expected) {}
    }

    public void testObjectUnloadedFromCache() {
        loaded.unloaded(object1);

        assertNull(loaded.get(oid1));
        assertEquals(3, loaded.size());
    }

    public void testResetClearsCache() {
        loaded.setPojoAdapterHash(new PojoAdapterHashImpl());
        assertEquals(4, loaded.size());

        loaded.reset();
        assertEquals(0, loaded.size());
    }
}

class Cache extends PojoAdapterFactoryImpl {

    void setup(Oid oid, NakedObject object) {
        identityMap.put(oid, object);
    }

    Object get(Object key) {
        return identityMap.get(key);
    }

    int size() {
        return identityMap.size();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
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
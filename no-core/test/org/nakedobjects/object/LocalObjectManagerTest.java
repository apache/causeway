package org.nakedobjects.object;


import org.apache.log4j.Level;
import org.apache.log4j.LogManager;


public final class LocalObjectManagerTest extends NakedObjectTestCase {
    private NakedObjectContext context;
    private MockLoadedObjects loadedObjects;
    private NakedObjectSpecification nc;
    private LocalObjectManager objectManager;
    private MockObjectStore objectStore;
    NakedObject[] v;

    public LocalObjectManagerTest(String name) {
        super(name);
    }

    private void assertInstances(NakedObject[] instances) {
        assertEquals(v.length, instances.length);

        assertEquals(v[0], instances[0]);
        assertEquals(v[1], instances[1]);
        assertEquals(v[2], instances[2]);

        assertEquals(context, instances[0].getContext());
        assertEquals(context, instances[1].getContext());
        assertEquals(context, instances[2].getContext());
    }

    protected void setUp() throws Exception {
        LogManager.getLoggerRepository().setThreshold(Level.OFF);

        MockUpdateNotifier updateNotifier = new MockUpdateNotifier();
        objectStore = new MockObjectStore();
        loadedObjects = (MockLoadedObjects) objectStore.getLoadedObjects();
        objectManager = new LocalObjectManager(objectStore, updateNotifier, new SimpleOidGenerator());
        objectManager.init();

        NakedObjectSpecification.setReflectionFactory(new LocalReflectionFactory());

        context = objectManager.getContext();

        nc = NakedObjectSpecification.getNakedClass(MockNakedObject.class);
        v = new NakedObject[] { new MockNakedObject(), new MockNakedObject(), new MockNakedObject() };
        objectStore.setupInstances(v, nc);

        super.setUp();
    }

    protected void tearDown() throws Exception {
        objectManager.shutdown();
        objectStore.shutdown();
        super.tearDown();
    }

    public void testAlreadyExistingSerialNumbers() throws ObjectStoreException {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(Sequence.class);
        Sequence seq;
        v = new NakedObject[] { seq = new Sequence() };
        seq.getName().setValue("test");
        objectStore.setupInstances(v, nc);

        long i = objectManager.serialNumber("test");
        assertEquals(1, i);

        objectStore.assertAction(0, "getInstances " + nc);
        objectStore.assertAction(1, "saveObject " + seq);

        i = objectManager.serialNumber("test");
        assertEquals(2, i);
    }

    public void testFirstSerialNumbers() throws ObjectStoreException {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(Sequence.class);
        NakedObject[] v = new NakedObject[] {};
        objectStore.setupInstances(v, nc);

        long i = objectManager.serialNumber("test");
        assertEquals(0, i);

        objectStore.assertAction(0, "getInstances " + nc);
        objectStore.assertAction(1, "createObject Sequence");
        loadedObjects.assertAction(0, "loaded Sequence");
    }

    public void testGetInstancesForClass() throws Exception {
        NakedObject[] instances = objectManager.getInstances(nc);
        assertInstances(instances);
    }

    public void testGetInstancesForPattern() throws Exception {
        NakedObject[] instances = objectManager.getInstances(new MockNakedObject());

        assertInstances(instances);
    }

    public void testGetInstancesForTerm() throws Exception {
        NakedObject[] instances = objectManager.getInstances(nc, "term");

        assertInstances(instances);
    }

    public void testGetObjectRepeatability() throws ObjectStoreException {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(AbstractNakedObject.class);
        Oid oid = new MockOid(1);

        objectStore.setupIsLoaded(false);
        Person person = new Person();
        person.setOid(oid);
        objectStore.setupGetObject(person);
        assertSame(person, objectManager.getObject(oid, nc));

        objectStore.setupIsLoaded(true);
        objectStore.setupGetObject(null);
        objectStore.setupLoaded(new NakedObject[] { person });
        assertSame(person, objectManager.getObject(oid, nc));
    }

    public void testHasInstances() throws Exception {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(AbstractNakedObject.class);

        objectStore.setupHasInstances(false);
        assertFalse(objectManager.hasInstances(nc));
        objectStore.setupHasInstances(true);
        assertTrue(objectManager.hasInstances(nc));

    }

    public void testInstancesCount() throws Exception {
        NakedObjectSpecification nc = NakedObjectSpecification.getNakedClass(AbstractNakedObject.class);

        objectStore.setupInstancesCount(0);
        assertEquals(0, objectManager.numberOfInstances(nc));

        objectStore.setupInstancesCount(5);
        assertEquals(5, objectManager.numberOfInstances(nc));
    }

    public void testMakePersistentPersistsValue() throws Exception {
        NakedObject object = new Role();
        assertNull(object.getOid());
        objectManager.makePersistent(object);
        assertNotNull(object.getOid());

        objectStore.assertAction(0, "createObject " + object);
        loadedObjects.assertAction(0, "loaded " + object);
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
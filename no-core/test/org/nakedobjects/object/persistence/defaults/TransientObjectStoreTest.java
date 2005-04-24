package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.MockOid;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;

import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


class MockTransientObjectStoreInstances2 extends TransientObjectStoreInstances {
    private Vector actions = new Vector();
    private NakedObject[] instances = new NakedObject[0];
    private NakedObject object;
    
    public void assertAction(int index, String expected) {
        if (index >= actions.size()) {
            throw new AssertionError("No such action: " + index);
        }
        Assert.assertEquals(expected, actions.elementAt(index).toString());
    }

    public Enumeration elements() {
        actions.addElement("elements");
        return null;
    }

    public NakedObject getObject(Oid oid) {
        actions.addElement("get object for " + oid);
        return object;
    }

    public Oid getOidFor(Object object) {
        return null;
    }

    public boolean hasInstances() {
        actions.addElement("has instances");
        return false;
    }

    public NakedObject instanceMatching(String title) {
        return null;
    }

    public NakedObject[] instances() {
        actions.addElement("get instances");
        return instances;
    }

    public int numberOfInstances() {
        return 0;
    }

    public void remove(Oid oid) {
        actions.addElement("remove " + oid);
    }

    public void save(NakedObject object) {
        actions.addElement("save " + object);
    }

    public void shutdown() {
        actions.addElement("shutdown");
    }

    public void setupObject(NakedObject object) {
        this.object = object;
    }
}

public class TransientObjectStoreTest extends TestCase {
    private DummyNakedObjectSpecification objectSpec;
    private NakedObjectStore objectStore;
    private MockTransientObjectStoreInstances2 transientObjectStoreInstances;

    private void assertEquals(NakedObject object, NakedObject v) {
        assertEquals(object.getObject(), v.getObject());
        assertEquals(object.getOid(), v.getOid());
    }

    private NakedObject createTestObject() {
        //     TestObject object = new TestObject();
        //       NakedObject nakedObject =
        // NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
        MockNakedObject nakedObject = new MockNakedObject();
        nakedObject.setupSpecification(objectSpec);
        //    nakedObject.setupObject(new TestObject());
        //      nakedObject.setupTitleString("object");
        //       nakedObject.setOid(new DummyOid());

        return nakedObject;
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        objectSpec = new DummyNakedObjectSpecification();

        transientObjectStoreInstances = new MockTransientObjectStoreInstances2();
        objectStore = new TransientObjectStore() {
            protected TransientObjectStoreInstances createInstances() {
                return transientObjectStoreInstances;
            }
        };

        objectStore.init();
    }

    protected void tearDown() throws Exception {
        objectStore.shutdown();
    }

    public void testCreateInstances() throws Exception {
        NakedObject object1 = createTestObject();
        NakedObject object2 = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(object1),
                objectStore.createCreateObjectCommand(object2) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstances.assertAction(0, "save " + object1);
        transientObjectStoreInstances.assertAction(1, "save " + object2);
    }

    public void testDestroyObject() throws Exception {
        NakedObject objectToDelete = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createDestroyObjectCommand(objectToDelete) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstances.assertAction(0, "remove " + objectToDelete.getOid());
    }

    public void testGetInstances() throws Exception {
        objectStore.getInstances(objectSpec, false);
        transientObjectStoreInstances.assertAction(0, "get instances");
    }

    public void testGetObject() throws Exception {
        Oid oid = new MockOid(0);
        MockNakedObject object = new MockNakedObject();
        transientObjectStoreInstances.setupObject(object);
        
        NakedObject result = objectStore.getObject(oid, objectSpec);
        transientObjectStoreInstances.assertAction(0, "get object for " + oid);
        assertEquals(object, result);
    }
    
    
    public void testHasInstances() throws Exception {
        objectStore.hasInstances(objectSpec, false);
        transientObjectStoreInstances.assertAction(0, "has instances");
    }

    public void testHasNoInstances() throws Exception {
        assertFalse(objectStore.hasInstances(objectSpec, false));

        assertEquals(0, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testSaveInstances() throws Exception {
        NakedObject object1 = createTestObject();
        NakedObject object2 = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createSaveObjectCommand(object1),
                objectStore.createSaveObjectCommand(object2) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstances.assertAction(0, "save " + object1);
        transientObjectStoreInstances.assertAction(1, "save " + object2);
    }

    public void testShutdown() throws Exception {
        objectStore.hasInstances(objectSpec, false);

        objectStore.shutdown();
        transientObjectStoreInstances.assertAction(1, "shutdown");
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
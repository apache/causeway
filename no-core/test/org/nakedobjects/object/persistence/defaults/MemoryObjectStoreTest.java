package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.TestSystem;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.DummyOid;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectLoader;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.reflect.DummyNakedObject;
import org.nakedobjects.object.transaction.PersistenceCommand;

import java.util.Enumeration;
import java.util.Vector;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


class MockMemoryObjectStoreInstances2 extends MemoryObjectStoreInstances {
    public MockMemoryObjectStoreInstances2(NakedObjectLoader objectLoader) {
        super(objectLoader);
    }

    private Vector actions = new Vector();
    private boolean hasInstances;
    private NakedObject[] instances = new NakedObject[0];
    private NakedObject object;
    private int numberOfInstances;

    public void assertAction(int index, String expected) {
        if (index >= actions.size()) {
            throw new AssertionError("No such action: " + index);
        }
        Assert.assertEquals(expected, actions.elementAt(index).toString());
    }

    public void assertNoActions() {
        Assert.assertEquals("actions", 0, actions.size());
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
        return hasInstances;
    }

    public NakedObject instanceMatching(String title) {
        return null;
    }

    public void instances(Vector instanceVector) {
        actions.addElement("get instances");
        for(int i = 0; i < instances.length; i++) {
            	instanceVector.addElement(instances[i]);
        }
    }
    
    public void instances(InstancesCriteria criteria, Vector instanceVector) {
        instances(instanceVector);
    }

    public int numberOfInstances() {
        return numberOfInstances;
    }

    public void setupNumberOfInstances(int numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    public void remove(Oid oid) {
        actions.addElement("remove " + oid);
    }

    public void save(NakedObject object) {
        actions.addElement("save " + object);
    }

    public void setupHasInstances(boolean hasInstances) {
        this.hasInstances = hasInstances;
    }

    public void setupInstances(NakedObject[] instances) {
        this.instances = instances;
    }
    
    public void setupObject(NakedObject object) {
        this.object = object;
    }

    public void shutdown() {
        actions.addElement("shutdown");
    }
}

public class MemoryObjectStoreTest extends TestCase {
    private DummyNakedObjectSpecification objectSpec;
    private MemoryObjectStore objectStore;
    private DummyNakedObjectSpecification superClassObjectSpec;
    private MockMemoryObjectStoreInstances2 transientObjectStoreInstancesForClass;
    private MockMemoryObjectStoreInstances2 transientObjectStoreInstancesForSuperClass;
    private TestSystem system;

    private void assertEquals(NakedObject object, NakedObject v) {
        assertEquals(object.getObject(), v.getObject());
        assertEquals(object.getOid(), v.getOid());
    }

    private NakedObject createTestObject() {
        MockNakedObject nakedObject = new MockNakedObject();
        nakedObject.setupSpecification(objectSpec);
        return nakedObject;
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);
        
        system = new TestSystem();
        system.init();
        
        superClassObjectSpec = new DummyNakedObjectSpecification();
        objectSpec = new DummyNakedObjectSpecification();
        superClassObjectSpec.setupSubclasses(new NakedObjectSpecification[] { objectSpec });

        transientObjectStoreInstancesForSuperClass = new MockMemoryObjectStoreInstances2(NakedObjects.getObjectLoader());
        transientObjectStoreInstancesForClass = new MockMemoryObjectStoreInstances2(NakedObjects.getObjectLoader());
        objectStore = new MemoryObjectStore();
        objectStore.instances.put(superClassObjectSpec, transientObjectStoreInstancesForSuperClass);
        objectStore.instances.put(objectSpec, transientObjectStoreInstancesForClass);

        objectStore.init();
    }

    protected void tearDown() throws Exception {
        objectStore.shutdown();
        
        system.shutdown();
    }

    public void testCreateInstances() throws Exception {
        NakedObject object1 = createTestObject();
        NakedObject object2 = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(object1),
                objectStore.createCreateObjectCommand(object2) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstancesForClass.assertAction(0, "save " + object1);
        transientObjectStoreInstancesForClass.assertAction(1, "save " + object2);
    }

    public void testDestroyObject() throws Exception {
        NakedObject objectToDelete = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createDestroyObjectCommand(objectToDelete) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstancesForClass.assertAction(0, "remove " + objectToDelete.getOid());
    }

    public void xxxtestGetInstancesBySpecification() throws Exception {
        DummyNakedObject object = new DummyNakedObject();
        transientObjectStoreInstancesForClass.setupInstances(new NakedObject[] {object});
        NakedObject[] instances = objectStore.getInstances(objectSpec, false);
        transientObjectStoreInstancesForClass.assertAction(0, "get instances");
        transientObjectStoreInstancesForSuperClass.assertNoActions();
        assertEquals(1, instances.length);
        assertEquals(object, instances[0]);
    }

    public void xxxtestGetInstancesBySpecificationIncludingSubclasses() throws Exception {
        DummyNakedObject object1 = new DummyNakedObject();
        transientObjectStoreInstancesForClass.setupInstances(new NakedObject[] {object1});
        DummyNakedObject object2 = new DummyNakedObject();
        transientObjectStoreInstancesForSuperClass.setupInstances(new NakedObject[] {object2});
        NakedObject[] instances = objectStore.getInstances(superClassObjectSpec, true);
        transientObjectStoreInstancesForClass.assertAction(0, "get instances");
        transientObjectStoreInstancesForSuperClass.assertAction(0, "get instances");
        assertEquals(2, instances.length);
        assertEquals(object1, instances[0]);
        assertEquals(object2, instances[1]);
    }


    public void xxxtestGetInstancesByCriteria() throws Exception {
        DummyNakedObject object = new DummyNakedObject();
        transientObjectStoreInstancesForClass.setupInstances(new NakedObject[] {object});
        NakedObject[] instances = objectStore.getInstances(new TitleCriteria(objectSpec, "test", false));
        transientObjectStoreInstancesForClass.assertAction(0, "get instances");
        transientObjectStoreInstancesForSuperClass.assertNoActions();
        assertEquals(1, instances.length);
        assertEquals(object, instances[0]);
    }

    public void xxxtestGetInstancesByCriteriaIncludingSubclasses() throws Exception {
        DummyNakedObject object = new DummyNakedObject();
        transientObjectStoreInstancesForClass.setupInstances(new NakedObject[] {object});
        DummyNakedObject object2 = new DummyNakedObject();
        transientObjectStoreInstancesForSuperClass.setupInstances(new NakedObject[] {object2});
        NakedObject[] instances = objectStore.getInstances(new TitleCriteria(superClassObjectSpec, "test", true));
        transientObjectStoreInstancesForClass.assertAction(0, "get instances");
        transientObjectStoreInstancesForSuperClass.assertAction(0, "get instances");
        assertEquals(2, instances.length);
        assertEquals(object, instances[0]);
        assertEquals(object2, instances[1]);
    }


    public void xxxxtestGetObject() throws Exception {
        Oid oid = new DummyOid(0);
        MockNakedObject object = new MockNakedObject();
        transientObjectStoreInstancesForClass.setupObject(object);

        NakedObject result = objectStore.getObject(oid, objectSpec);
        transientObjectStoreInstancesForClass.assertAction(0, "get object for " + oid);
        assertEquals(object, result);
    }

    public void testHasInstances() throws Exception {
        objectStore.hasInstances(objectSpec, false);
        transientObjectStoreInstancesForClass.assertAction(0, "has instances");
    }

    public void testHasInstancesIncludingSubclasses() throws Exception {
        assertEquals(false, objectStore.hasInstances(superClassObjectSpec, false));
        transientObjectStoreInstancesForClass.assertNoActions();

        transientObjectStoreInstancesForClass.setupHasInstances(true);
        objectStore.hasInstances(superClassObjectSpec, true);
        transientObjectStoreInstancesForClass.assertAction(0, "has instances");
    }

    public void testHasNoInstances() throws Exception {
        assertFalse(objectStore.hasInstances(objectSpec, false));
    }

    public void testHasNoInstancesIncludingSubclasses() throws Exception {
        assertFalse(objectStore.hasInstances(superClassObjectSpec, false));
    }    

    public void testNumberOfInstances() {
        transientObjectStoreInstancesForClass.setupNumberOfInstances(9);
        assertEquals(9, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testNumberOfInstancesIncludingSubclasses() throws Exception {
        transientObjectStoreInstancesForSuperClass.setupNumberOfInstances(3);
        assertEquals(3, objectStore.numberOfInstances(superClassObjectSpec, false));
        
        transientObjectStoreInstancesForClass.setupNumberOfInstances(6);
        assertEquals(9, objectStore.numberOfInstances(superClassObjectSpec, true));
    }

    public void testSaveInstances() throws Exception {
        NakedObject object1 = createTestObject();
        NakedObject object2 = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createSaveObjectCommand(object1),
                objectStore.createSaveObjectCommand(object2) };
        objectStore.runTransaction(commands);

        transientObjectStoreInstancesForClass.assertAction(0, "save " + object1);
        transientObjectStoreInstancesForClass.assertAction(1, "save " + object2);
    }

    public void testShutdown() throws Exception {
        objectStore.hasInstances(objectSpec, false);

        objectStore.shutdown();
        transientObjectStoreInstancesForClass.assertAction(1, "shutdown");
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
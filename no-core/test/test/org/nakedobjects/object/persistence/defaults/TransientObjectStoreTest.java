package test.org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Oid;
import org.nakedobjects.object.persistence.objectstore.inmemory.MockTransientObjectStoreInstances;
import org.nakedobjects.object.persistence.objectstore.inmemory.TransientObjectStore;
import org.nakedobjects.object.transaction.PersistenceCommand;

import java.util.Vector;

import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.DummyOid;
import test.org.nakedobjects.object.MockNakedObject;
import test.org.nakedobjects.object.TestSystem;


class TestCriteria implements InstancesCriteria {
    private final NakedObjectSpecification spec;
    private final Vector matches = new Vector();
    private boolean includeSubclasses;

    public void addMatch(NakedObject match) {
        matches.addElement(match);
    }

    public TestCriteria(NakedObjectSpecification spec, boolean includeSubclasses) {
        this.spec = spec;
        this.includeSubclasses = includeSubclasses;
    }

    public boolean matches(NakedObject object) {
        return matches.contains(object);
    }

    public NakedObjectSpecification getSpecification() {
        return spec;
    }

    public boolean includeSubclasses() {
        return includeSubclasses;
    }

}


class MockTransientObjectStore extends TransientObjectStore {

    public void put(DummyNakedObjectSpecification superClassObjectSpec, 
            MockTransientObjectStoreInstances transientObjectStoreInstancesForSuperClass) {
        
        instances.put(superClassObjectSpec, transientObjectStoreInstancesForSuperClass);
        
    }
}

public class TransientObjectStoreTest extends TestCase {
    private DummyNakedObjectSpecification objectSpec;
    private MockTransientObjectStore objectStore;
    private DummyNakedObjectSpecification superClassObjectSpec;
    private MockTransientObjectStoreInstances transientObjectStoreInstancesForClass;
    private MockTransientObjectStoreInstances transientObjectStoreInstancesForSuperClass;
    private int nextId;
    private TestSystem system;

    private void assertEquals(NakedObject object, NakedObject v) {
        assertEquals(object.getObject(), v.getObject());
        assertEquals(object.getOid(), v.getOid());
    }

    private NakedObject createTestObject() {
        MockNakedObject nakedObject = new MockNakedObject();
        DummyOid oid = new DummyOid(nextId++);
        nakedObject.setupOid(oid);
        nakedObject.setupSpecification(objectSpec);
        return nakedObject;
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        superClassObjectSpec = new DummyNakedObjectSpecification();
        objectSpec = new DummyNakedObjectSpecification();
        superClassObjectSpec.setupSubclasses(new NakedObjectSpecification[] { objectSpec });

        transientObjectStoreInstancesForSuperClass = new MockTransientObjectStoreInstances();
        transientObjectStoreInstancesForClass = new MockTransientObjectStoreInstances();
        objectStore = new MockTransientObjectStore();
        objectStore.put(superClassObjectSpec, transientObjectStoreInstancesForSuperClass);
        objectStore.put(objectSpec, transientObjectStoreInstancesForClass);

        system = new TestSystem();
        system.init();

    }

    protected void tearDown() throws Exception {
        system.shutdown();
    }

    public void testCreateInstances() throws Exception {
        NakedObject object1 = createTestObject();
        //       object1.setOid(new MockOid(1));

        NakedObject object2 = createTestObject();
        //       object2.setOid(new MockOid(2));

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(object1),
                objectStore.createCreateObjectCommand(object2) };
        objectStore.runTransaction(commands);

        assertEquals(2, objectStore.objects.size());
        assertEquals(object1, objectStore.objects.get(object1.getOid()));
        assertEquals(object2, objectStore.objects.get(object2.getOid()));
    }

    public void testDestroyObject() throws Exception {
        NakedObject objectToDelete = addObject(transientObjectStoreInstancesForClass);

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createDestroyObjectCommand(objectToDelete) };
        objectStore.runTransaction(commands);

        assertEquals(0, objectStore.objects.size());
    }

    public void testGetInstancesBySpecification() throws Exception {
        NakedObject object = addObject(transientObjectStoreInstancesForClass);

        NakedObject[] instances = objectStore.getInstances(objectSpec, false);
        assertEquals(1, instances.length);
        assertEquals(object, instances[0]);
    }

    public void testGetInstancesBySpecificationIncludingSubclasses() throws Exception {
        NakedObject object1 = addObject(transientObjectStoreInstancesForClass);
        NakedObject object2 = addObject(transientObjectStoreInstancesForSuperClass);
        NakedObject object3 = addObject(transientObjectStoreInstancesForClass);

        NakedObject[] instances = objectStore.getInstances(superClassObjectSpec, true);
        assertEquals(3, instances.length);
        assertEquals(object2, instances[0]);
        assertEquals(object1, instances[1]);
        assertEquals(object3, instances[2]);
    }

    public void testGetInstancesByCriteria() throws Exception {
        addObject(transientObjectStoreInstancesForClass);
        NakedObject object = addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);

        TestCriteria criteria = new TestCriteria(objectSpec, false);
        criteria.addMatch(object);

        NakedObject[] instances = objectStore.getInstances(criteria);
        assertEquals(1, instances.length);
        assertEquals(object, instances[0]);
    }

    private NakedObject addObject(MockTransientObjectStoreInstances instances) {
        NakedObject object = createTestObject();
        Oid oid = object.getOid();
        objectStore.objects.put(oid, object);
        instances.objectInstances.addElement(oid);

        return object;
    }

    public void testGetInstancesByCriteriaIncludingSubclasses() throws Exception {
        addObject(transientObjectStoreInstancesForClass);
        NakedObject object1 = addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);

        addObject(transientObjectStoreInstancesForSuperClass);
        NakedObject object2 = addObject(transientObjectStoreInstancesForSuperClass);
        addObject(transientObjectStoreInstancesForSuperClass);

        TestCriteria criteria = new TestCriteria(superClassObjectSpec, true);
        criteria.addMatch(object1);
        criteria.addMatch(object2);
        NakedObject[] instances = objectStore.getInstances(criteria);

        assertEquals(2, instances.length);
        assertEquals(object2, instances[0]);
        assertEquals(object1, instances[1]);
    }

    public void testGetObject() throws Exception {
        Oid oid = new DummyOid(0);
        MockNakedObject object = new MockNakedObject();
        objectStore.objects.put(oid, object);

        NakedObject result = objectStore.getObject(oid, objectSpec);
        assertEquals(object, result);
    }

    public void testGetObjectCantFindObject() throws Exception {
        Oid oid = new DummyOid(0);
        MockNakedObject object = new MockNakedObject();
        objectStore.objects.put(oid, object);

        try {
            objectStore.getObject(new DummyOid(1), objectSpec);
            fail();
        } catch (ObjectNotFoundException expected) {}
    }

    public void testHasInstances() throws Exception {
        addObject(transientObjectStoreInstancesForClass);
        assertTrue(objectStore.hasInstances(objectSpec, false));
    }

    public void testHasInstancesIncludingSubclasses() throws Exception {
        assertEquals(false, objectStore.hasInstances(superClassObjectSpec, false));

        addObject(transientObjectStoreInstancesForClass);

        assertTrue(objectStore.hasInstances(superClassObjectSpec, true));
    }

    public void testHasNoInstances() throws Exception {
        assertFalse(objectStore.hasInstances(objectSpec, false));
    }

    public void testHasNoInstancesIncludingSubclasses() throws Exception {
        assertFalse(objectStore.hasInstances(superClassObjectSpec, false));
    }

    public void testNumberOfInstances() {
        addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);

        assertEquals(3, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testNumberOfInstancesIncludingSubclasses() throws Exception {
        addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForClass);

        assertEquals(3, objectStore.numberOfInstances(superClassObjectSpec, true));

        addObject(transientObjectStoreInstancesForSuperClass);
        addObject(transientObjectStoreInstancesForSuperClass);

        assertEquals(5, objectStore.numberOfInstances(superClassObjectSpec, true));
    }

    public void testShutdown() throws Exception {
        addObject(transientObjectStoreInstancesForClass);
        addObject(transientObjectStoreInstancesForSuperClass);
        objectStore.hasInstances(objectSpec, false);

        objectStore.shutdown();
        assertEquals(0, objectStore.instances.size());
        assertEquals(0, objectStore.objects.size());
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user. Copyright (C) 2000 -
 * 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is Kingsway House, 123
 * Goldworth Road, Woking GU21 1NR, UK).
 */
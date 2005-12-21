package test.org.nakedobjects.object.persistence.objectstore.inmemory;

import org.nakedobjects.object.InstancesCriteria;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.Version;
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
import test.org.nakedobjects.object.reflect.DummyNakedObject;


class TestCriteria implements InstancesCriteria {
    private boolean includeSubclasses;
    private final Vector matches = new Vector();
    private final NakedObjectSpecification spec;

    public TestCriteria(NakedObjectSpecification spec, boolean includeSubclasses) {
        this.spec = spec;
        this.includeSubclasses = includeSubclasses;
    }

    public void addMatch(NakedObject match) {
        matches.addElement(match);
    }

    public NakedObjectSpecification getSpecification() {
        return spec;
    }

    public boolean includeSubclasses() {
        return includeSubclasses;
    }

    public boolean matches(NakedObject object) {
        return matches.contains(object);
    }

}

public class TransientObjectStoreTest extends TestCase {
    private int nextId;
    private DummyNakedObjectSpecification objectSpec;
    private TransientObjectStore objectStore;
    private DummyNakedObjectSpecification superClassObjectSpec;
    private NakedObject object1;
    private NakedObject object2;
    private NakedObject object3;
    private NakedObject object4;
    private NakedObject object5;
    private DummyNakedObjectSpecification superSuperClassObjectSpec;

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

    private NakedObject createTestSuperClassObject() {
        MockNakedObject nakedObject = new MockNakedObject();
        DummyOid oid = new DummyOid(nextId++);
        nakedObject.setupOid(oid);
        nakedObject.setupSpecification(superClassObjectSpec);
        return nakedObject;
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        superSuperClassObjectSpec = new DummyNakedObjectSpecification();
        superClassObjectSpec = new DummyNakedObjectSpecification();
        objectSpec = new DummyNakedObjectSpecification();
        superClassObjectSpec.setupSubclasses(new NakedObjectSpecification[] { objectSpec });
        superSuperClassObjectSpec.setupSubclasses(new NakedObjectSpecification[] { superClassObjectSpec });

        objectStore = new TransientObjectStore();
        objectStore.init();

        assertEquals(0, objectStore.numberOfInstances(objectSpec, false));

        object1 = createTestObject();
        object2 = createTestObject();
        object3 = createTestSuperClassObject();
        object4 = createTestSuperClassObject();
        object5 = createTestObject();
        PersistenceCommand[] commands = new PersistenceCommand[] { 
                objectStore.createCreateObjectCommand(object1),
                objectStore.createCreateObjectCommand(object2),
                objectStore.createCreateObjectCommand(object3),
                objectStore.createCreateObjectCommand(object4),
                objectStore.createCreateObjectCommand(object5) };
        objectStore.startTransaction();
        objectStore.execute(commands);
        objectStore.endTransaction();
    }

    protected void tearDown() throws Exception {
        objectStore.shutdown();
    }

    public void testCreateInstances() throws Exception {
         assertEquals(3, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testDestroyObject() throws Exception {
        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createDestroyObjectCommand(object1) };
        objectStore.execute(commands);

        assertEquals(2, objectStore.numberOfInstances(objectSpec, false));
        assertEquals(object2, objectStore.getObject(object2.getOid(), objectSpec));
        assertEquals(object5, objectStore.getObject(object5.getOid(), objectSpec));
    }

    public void testGetInstancesByCriteria() throws Exception {
        TestCriteria criteria = new TestCriteria(superClassObjectSpec, false);
        criteria.addMatch(object1);
        criteria.addMatch(object3);

        NakedObject[] instances = objectStore.getInstances(criteria);
        assertEquals(1, instances.length);
        assertEquals(object3, instances[0]);
    }

    public void testGetInstancesByCriteriaIncludingSubclasses() throws Exception {
        TestCriteria criteria = new TestCriteria(superClassObjectSpec, true);
        criteria.addMatch(object1);
        criteria.addMatch(object3);
        NakedObject[] instances = objectStore.getInstances(criteria);

        assertEquals(2, instances.length);
        assertEquals(object3, instances[0]);
        assertEquals(object1, instances[1]);
    }

    public void testGetInstancesBySpecification() throws Exception {
        NakedObject[] instances = objectStore.getInstances(superClassObjectSpec, false);
        assertEquals(2, instances.length);
        assertEquals(object3, instances[0]);
        assertEquals(object4, instances[1]);
    }

    public void testGetInstancesBySpecificationIncludingSubclasses() throws Exception {
        NakedObject[] instances = objectStore.getInstances(superClassObjectSpec, true);
        assertEquals(5, instances.length);
        assertEquals(object3, instances[0]);
        assertEquals(object4, instances[1]);
        assertEquals(object1, instances[2]);
        assertEquals(object2, instances[3]);
        assertEquals(object5, instances[4]);
    }

    public void testGetObject() throws Exception {
        assertEquals(object1, objectStore.getObject(object1.getOid(), objectSpec));
        assertEquals(object3, objectStore.getObject(object3.getOid(), superClassObjectSpec));
    }

    public void testGetObjectCantFindObject() throws Exception {
        try {
            objectStore.getObject(new DummyOid(-3), objectSpec);
            fail();
        } catch (ObjectNotFoundException expected) {}
    }

    public void testHasInstances() throws Exception {
        assertTrue(objectStore.hasInstances(superClassObjectSpec, false));
    }

    public void testHasInstancesIncludingSubclasses() throws Exception {
        assertTrue(objectStore.hasInstances(superSuperClassObjectSpec, true));
    }

    public void testHasNoInstances() throws Exception {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        assertFalse(objectStore.hasInstances(spec, false));
    }

    public void testHasNoInstancesIncludingSubclasses() throws Exception {
        DummyNakedObjectSpecification spec = new DummyNakedObjectSpecification();
        DummyNakedObjectSpecification superSpec = new DummyNakedObjectSpecification();
        superSpec.setupSubclasses(new NakedObjectSpecification[] {spec});
        assertFalse(objectStore.hasInstances(superSpec, true));
    }

    public void testNumberOfInstances() {
        assertEquals(2, objectStore.numberOfInstances(superClassObjectSpec, false));
    }

    public void testNumberOfInstancesIncludingSubclasses() throws Exception {
        assertEquals(5, objectStore.numberOfInstances(superClassObjectSpec, true));
    }

    public void testShutdown() throws Exception {
        objectStore.shutdown();
        assertEquals(0, objectStore.numberOfInstances(superClassObjectSpec, false));
        assertEquals(0, objectStore.numberOfInstances(objectSpec, false));
    }
    
    public void testDebug() {
        assertTrue(objectStore.getDebugData().length() > 0);
    }
    
    public void testSave() {
        ((DummyNakedObject) object3).setupTitleString("title");
        
        Version version = object3.getVersion();
        PersistenceCommand[] commands = new PersistenceCommand[] {objectStore.createSaveObjectCommand(object3)};
        objectStore.startTransaction();
        objectStore.execute(commands);
        objectStore.endTransaction();
        
        assertNotSame(version, object3.getVersion());
        
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
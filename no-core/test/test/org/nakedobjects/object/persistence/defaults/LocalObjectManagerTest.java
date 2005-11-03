package test.org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.object.NakedObjectField;
import org.nakedobjects.object.ResolveState;
import org.nakedobjects.object.persistence.objectore.LocalObjectManager;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import test.org.nakedobjects.object.DummyNakedObjectSpecification;
import test.org.nakedobjects.object.MockNakedObject;
import test.org.nakedobjects.object.MockObjectStore;
import test.org.nakedobjects.object.TestSystem;


public class LocalObjectManagerTest extends TestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LocalObjectManagerTest.class);
    }

    private LocalObjectManager objectManager;
    private DummyNakedObjectSpecification objectSpecification;
    private MockObjectStore objectStore;
    private MockNakedObject testNakedObject;
    private TestSystem system;

    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        
        system = new TestSystem();

        objectManager = new LocalObjectManager();
        objectStore = new MockObjectStore();
        objectManager.setObjectStore(objectStore);
        objectManager.setPersistAlgorithm(new DummyPersistAlgorithm());

        system.setObjectManager(objectManager);
        
        system.init();
        
        testNakedObject = new MockNakedObject();
        objectSpecification = new DummyNakedObjectSpecification();
        testNakedObject.setupSpecification(objectSpecification);
    }
    
    protected void tearDown() throws Exception {
      	system.shutdown();
    }

    public void testAbort() {
        objectManager.startTransaction();
        objectManager.destroyObject(testNakedObject);
        objectManager.abortTransaction();

        objectStore.assertAction(0, "destroyObject " + testNakedObject);
        objectStore.assertLastAction(0);
    }

    public void testEndTransaction() {
        objectManager.startTransaction();
        objectManager.destroyObject(testNakedObject);
        objectManager.endTransaction();

        objectStore.assertAction(0, "destroyObject " + testNakedObject);
        objectStore.assertAction(1, "startTransaction");
        objectStore.assertAction(2, "run DestroyObjectCommand " + testNakedObject);
        objectStore.assertAction(3, "endTransaction");
    }

    public void testDestroy() {
        objectSpecification.fields = new NakedObjectField[0];

        objectManager.startTransaction();
        objectManager.destroyObject(testNakedObject);
        objectManager.endTransaction();

        objectStore.assertAction(0, "destroyObject " + testNakedObject);
        objectStore.assertAction(1, "startTransaction");
        objectStore.assertAction(2, "run DestroyObjectCommand " + testNakedObject);
        objectStore.assertAction(3, "endTransaction");

        assertEquals(4, objectStore.getActions().size());
    }

    public void testMakePersistent() {
        objectSpecification.fields = new NakedObjectField[0];
        
        testNakedObject.setupResolveState(ResolveState.TRANSIENT);

        objectManager.startTransaction();
        objectManager.makePersistent(testNakedObject);
        objectManager.endTransaction();
        
        objectStore.assertAction(0, "createObject " + testNakedObject);
        objectStore.assertAction(1, "startTransaction");
        objectStore.assertAction(2, "run CreateObjectCommand " + testNakedObject);
        objectStore.assertAction(3, "endTransaction");
        
        assertEquals(4, objectStore.getActions().size());
   }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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
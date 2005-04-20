package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.NakedObjects;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.defaults.LoadedObjectsHashtable;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoaderNew;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.PojoAdapterFactory;
import org.nakedobjects.object.reflect.PojoAdapterHashImpl;
import org.nakedobjects.object.reflect.internal.NullReflectorFactory;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class TransientObjectStoreInstancesTest extends TestCase {
    private LoadedObjects loadedObjects;
    private DummyNakedObjectSpecification objectSpec;
    private NakedObjectStore objectStore;

    private void assertEquals(NakedObject object, NakedObject v) {
        assertEquals(object.getObject(), v.getObject());
        assertEquals(object.getOid(), v.getOid());
    }

    private NakedObject createTestObject() {
        TestObject object = new TestObject();
        NakedObject nakedObject = NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
        nakedObject.setOid(new DummyOid());
        return nakedObject;
    }

    private NakedObject createReferencedObject() {
        ReferencedObject object = new ReferencedObject();
        NakedObject nakedObject = NakedObjects.getPojoAdapterFactory().createNOAdapter(object);
        nakedObject.setOid(new DummyOid());
        return nakedObject;
    }

    protected void restartObjectStore() throws Exception {
        loadedObjects.reset();
        NakedObjects.getPojoAdapterFactory().reset();
    }

    protected void setUp() throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.OFF);

        PojoAdapterFactory pojoAdapterFactory = new PojoAdapterFactory();
        pojoAdapterFactory.setPojoAdapterHash(new PojoAdapterHashImpl());
        pojoAdapterFactory.setReflectorFactory(new NullReflectorFactory());
        NakedObjects.setPojoAdapterFactory(pojoAdapterFactory);

        MockNakedObjectSpecificationLoaderNew specificationLoader = new MockNakedObjectSpecificationLoaderNew();
        specificationLoader.addSpec(ReferencedObject.class.getName());
        objectSpec =  specificationLoader.addSpec(TestObject.class.getName());
        NakedObjects.setSpecificationLoader(specificationLoader);

        setupObjectStore();
    }

    private void setupObjectStore() throws Exception {
        TransientObjectStore transientObjectStore = new TransientObjectStore();
        loadedObjects = new LoadedObjectsHashtable();
        transientObjectStore.setLoadedObjects(loadedObjects);

        objectStore = transientObjectStore;
        objectStore.init();
    }

    protected void tearDown() throws Exception {
        objectStore.shutdown();
    }
    
    public void testDestroyObject() throws Exception {
        NakedObject object = createTestObject();
        NakedObject objectToDelete = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(objectToDelete),
                objectStore.createCreateObjectCommand(object) };
        objectStore.runTransaction(commands);

        loadedObjects.loaded(objectToDelete);
        commands = new PersistenceCommand[] { objectStore.createDestroyObjectCommand(objectToDelete) };
        objectStore.runTransaction(commands);

        restartObjectStore();

        NakedObject[] v = objectStore.getInstances(objectSpec, false);
        assertEquals(1, v.length);
        assertEquals(object, v[0]);
    }

    public void testGetInstances() throws Exception {
        NakedObject object = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(object) };
        objectStore.runTransaction(commands);

        restartObjectStore();

        NakedObject[] v = objectStore.getInstances(objectSpec, false);
        assertEquals(1, v.length);
        assertEquals(object, v[0]);
    }

    public void testHasInstances() throws Exception {
        NakedObject object = createTestObject();
        NakedObject objectToDelete = createTestObject();

        PersistenceCommand[] commands = new PersistenceCommand[] { objectStore.createCreateObjectCommand(objectToDelete),
                objectStore.createCreateObjectCommand(object) };

        objectStore.runTransaction(commands);

        assertTrue(objectStore.hasInstances(objectSpec, false));
        assertEquals(2, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testHasNoInstances() throws Exception {
        assertFalse(objectStore.hasInstances(objectSpec, false));

        assertEquals(0, objectStore.numberOfInstances(objectSpec, false));
    }

    public void testGetObjectWithGraph() throws Exception {
        NakedObject object = createTestObject();
        NakedObject referenced = createReferencedObject();
       ((TestObject) object.getObject()).setReferencedObject((ReferencedObject) referenced.getObject());
    
        PersistenceCommand[] commands = new PersistenceCommand[] { 
                objectStore.createCreateObjectCommand(object),
                objectStore.createCreateObjectCommand(referenced)
               };

        objectStore.runTransaction(commands);
        
        MockField field = new MockField(objectSpec);
        field.setupFieldContent(referenced);
        objectSpec.fields = new NakedObjectField[] {field};
        
        NakedObjectField fields[] = object.getFields();
        assertEquals(referenced, object.getField(fields[0]));
        
        restartObjectStore();
        
        NakedObject retrievedObject = objectStore.getObject(object.getOid(), objectSpec);
        assertEquals(object, retrievedObject);
        assertNotSame(object, retrievedObject);
         
        assertTrue(retrievedObject.isResolved());
        assertTrue(retrievedObject.isPersistent());
  
        
        NakedObject retrievedReferenced = (NakedObject) retrievedObject.getField(fields[0]);
        Assert.assertEquals(referenced, retrievedReferenced);
              
        assertTrue(retrievedReferenced.isPersistent());
        assertFalse(retrievedReferenced.isResolved());

        assertEquals(referenced, retrievedReferenced);
        
        objectStore.resolveImmediately(object);
        assertFalse(retrievedReferenced.isResolved());

    }
    
    
    


    public void testInstancesWithGraph() throws Exception {
        NakedObject object = createTestObject();
        NakedObject referenced = createReferencedObject();
       ((TestObject) object.getObject()).setReferencedObject((ReferencedObject) referenced.getObject());
    
        PersistenceCommand[] commands = new PersistenceCommand[] { 
                objectStore.createCreateObjectCommand(object),
                objectStore.createCreateObjectCommand(referenced)
               };

        objectStore.runTransaction(commands);
        
        MockField field = new MockField(objectSpec);
        field.setupFieldContent(referenced);
        objectSpec.fields = new NakedObjectField[] {field};
               
        restartObjectStore();
        
        NakedObject instances[] = objectStore.getInstances(objectSpec, false);
        assertEquals(1, instances.length);
        NakedObject retrievedObject = instances[0];
        
        assertEquals(object, retrievedObject);
        assertNotSame(object, retrievedObject);
         
        assertTrue(retrievedObject.isResolved());
        assertTrue(retrievedObject.isPersistent());
  
        
        NakedObjectField fields[] = object.getFields();
        NakedObject retrievedReferenced = (NakedObject) retrievedObject.getField(fields[0]);
        Assert.assertEquals(referenced, retrievedReferenced);
              
        assertTrue(retrievedReferenced.isPersistent());
        assertFalse(retrievedReferenced.isResolved());

        assertEquals(referenced, retrievedReferenced);
        
        objectStore.resolveImmediately(object);
        assertFalse(retrievedReferenced.isResolved());

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
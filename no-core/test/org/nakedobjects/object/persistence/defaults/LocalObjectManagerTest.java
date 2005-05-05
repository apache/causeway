package org.nakedobjects.object.persistence.defaults;

import org.nakedobjects.NakedObjectsClient;
import org.nakedobjects.object.DummyNakedObjectSpecification;
import org.nakedobjects.object.MockNakedObject;
import org.nakedobjects.object.MockObjectStore;
import org.nakedobjects.object.defaults.MockNakedObjectSpecificationLoader;
import org.nakedobjects.object.defaults.MockObjectFactory;
import org.nakedobjects.object.reflect.DummyPojoAdapterFactory;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.object.reflect.PojoAdapterFactory;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LocalObjectManagerTest extends TestCase {

    private LocalObjectManager objectManager;
    private MockObjectStore objectStore;
    private MockNakedObject testNakedObject;
    private DummyNakedObjectSpecification objectSpecification;
    private PojoAdapterFactory pojoAdapterFactory;

    public static void main(String[] args) {
        junit.textui.TestRunner.run(LocalObjectManagerTest.class);
    }

    public void testname() {
        MockNakedObjectSpecificationLoader specLoader = new MockNakedObjectSpecificationLoader();
        specLoader.addSpec(new DummyNakedObjectSpecification());
        
        
        LocalObjectManager om = new LocalObjectManager();
        om.setObjectFactory(new MockObjectFactory());
     }
    

    public void testMakePersistent() {
        objectSpecification.fields = new NakedObjectField[0];

        objectManager.startTransaction();
       objectManager.makePersistent(testNakedObject);
       objectManager.endTransaction();
    }
    

    public void testDestroy() {
        objectSpecification.fields = new NakedObjectField[0];

        pojoAdapterFactory.createAdapter(testNakedObject);
        
        objectManager.startTransaction();
        objectManager.destroyObject(testNakedObject);
        objectManager.endTransaction();
        
        objectStore.assertAction(0, "destroyObject " + testNakedObject);
        objectStore.assertAction(1, "startTransaction");
        objectStore.assertAction(2, "run DestroyObjectCommand " + testNakedObject);
        objectStore.assertAction(3, "endTransaction");

        assertEquals(4, objectStore.getActions().size());
    }
    
    protected void setUp() throws Exception {
        Logger.getRootLogger().setLevel(Level.OFF);
        
        
        objectStore = new MockObjectStore();

        objectManager = new LocalObjectManager();
        objectManager.setObjectStore(objectStore);
        objectManager.setOidGenerator(new MockOidGenerator());
 
        pojoAdapterFactory = new DummyPojoAdapterFactory();
        new NakedObjectsClient().setPojoAdapterFactory(pojoAdapterFactory);
        testNakedObject = new MockNakedObject();
        objectSpecification = new DummyNakedObjectSpecification();
        testNakedObject.setupSpecification(objectSpecification);
    }
}



/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2004  Naked Objects Group Ltd

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
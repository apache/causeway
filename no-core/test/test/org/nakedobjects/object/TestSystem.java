package test.org.nakedobjects.object;

import org.nakedobjects.object.NakedCollection;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectPersistor;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedReference;
import org.nakedobjects.object.NakedValue;
import org.nakedobjects.object.repository.NakedObjectsClient;

import test.org.nakedobjects.object.defaults.MockObjectPersistor;
import test.org.nakedobjects.object.reflect.DummyNakedObject;
import test.org.nakedobjects.utility.configuration.TestConfiguration;


public class TestSystem {
    private final DummyObjectLoader objectLoader;
    private NakedObjectsClient nakedObjects;
    private NakedObjectPersistor objectManager;
    private final DummyNakedObjectSpecificationLoader specificationLoader;
      
    public TestSystem() {
        specificationLoader = new DummyNakedObjectSpecificationLoader();
        objectLoader = new DummyObjectLoader();
        objectManager = new MockObjectPersistor();
    }

    public void init() {
        nakedObjects = new NakedObjectsClient();

        nakedObjects.setConfiguration(new TestConfiguration());
        nakedObjects.setSpecificationLoader(specificationLoader);
        nakedObjects.setObjectLoader(objectLoader);
        nakedObjects.setObjectPersistor(objectManager);

        nakedObjects.init();
    }

    public void addSpecification(NakedObjectSpecification specification) {
        specificationLoader.addSpecification(specification);
    }

    public void addNakedCollectionAdapter(NakedCollection collection) {
        objectLoader.addAdapter(collection.getObject(), collection);
    }

    public NakedObject createAdapterForTransient(Object associate) {
        NakedObject createAdapterForTransient = objectLoader.createAdapterForTransient(associate);
        objectLoader.addAdapter(associate, createAdapterForTransient);
        return createAdapterForTransient;
        
    }

    public void shutdown() {
        nakedObjects.shutdown();
    }

    public void setObjectManager(NakedObjectPersistor objectManager) {
        this.objectManager = objectManager;
    }
    
    public void setupLoadedObject(Object forObject, NakedObject adapter) {
        ((DummyObjectLoader) objectLoader).addAdapter(forObject, adapter);
    }

    public void addLoadedIdentity(DummyOid oid, NakedReference adapter) {
        objectLoader.addIdentity(oid, adapter);
    }

    public void addValue(Object object, NakedValue adapter) {
        objectLoader.addAdapter(object, adapter);
        
    }

    public void addAdapter(Object object, DummyNakedObject adapter) {
        objectLoader.addAdapter(object, adapter);
    }

    public void addRecreated(DummyOid oid, DummyNakedObject adapter) {
        objectLoader.addRecreated(oid, adapter);
        
    }

    public void addRecreatedTransient(DummyNakedObject adapter) {
        objectLoader.addRecreatedTransient(adapter);
        
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */